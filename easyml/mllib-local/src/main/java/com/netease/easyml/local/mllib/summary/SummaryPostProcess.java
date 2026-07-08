package com.netease.easyml.local.mllib.summary;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.common.Term;
import com.netease.easyml.common.util.StringUtil;
import com.netease.easyml.local.mllib.bean.Document;
import com.netease.easyml.local.mllib.bean.Sentence;
import com.netease.easyml.local.mllib.processor.process.RuleMergeDocumentProcess;
import com.netease.easyml.local.mllib.processor.process.SentenceSplitDocumentProcess;
import com.netease.easyml.local.mllib.processor.cleaner.CleanerFactory;
import com.netease.easyml.local.mllib.processor.cleaner.ICleaner;
import com.netease.easyml.local.mllib.processor.cleaner.impl.RegexCleaner;
import com.netease.easyml.local.mllib.scorer.impl.charLevel.JaccardScorer;
import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.netease.easyml.common.util.Constant.CHINESE_NUMBER;
import static com.netease.easyml.common.util.Constant.SPECIAL_NUMBER;
import static com.netease.easyml.local.mllib.bean.Constant.CLEAN_START_PATTERN;

/**
 * Created by eddielin on 2019/4/1.
 */
@Slf4j
@AllArgsConstructor
public class SummaryPostProcess {
    public static final Pattern LABEL_PATTERN = Pattern.compile(String.format("^((理由|方法|方式|危害|优点|缺点|秘技|穿法)[\\d%s]{1,2}[.，,､、:：;；](?!\\d)|第?[\\d%s]{1,2}[步种点]?[.，,､、:：;；](?!\\d)|NO.\\d{1,2}|第?[%s][步种]?([.，,､、:：;；])?|第?([(（\\[【])[\\d%s]{1,2}([)）\\]】])[步种]?([.，,､、:：;；])?)", CHINESE_NUMBER, CHINESE_NUMBER, SPECIAL_NUMBER, CHINESE_NUMBER));
    private RegexCleaner cleaner;
    private RegexCleaner fineGrainedCleaner;
    private RuleMergeDocumentProcess mergeProc;
    private int exceedLen;

    private static final String[] _NON_INDEPENDENT_FIRST_WORDS = {
            "但是", "所以", "不过", "然而", "更"
    };

    public static final Set<String> STOP_FLAG = new HashSet<>(Arrays.asList("。", "？", "?", "!", "！", "…", "~", "."));
    public static final Set<String> NON_STOP_FLAGS = new HashSet<>(Arrays.asList(",", "，", "、", ";", "；", ":", "：", "—", "——"));

    public static final Set<String> NON_INDEPENDENT_FIRST_WORDS = new HashSet<>(Arrays.asList(_NON_INDEPENDENT_FIRST_WORDS));

    public String process(Document document, List<Sentence> chunks, int docLen) {
        // 去除开头和标题重复
//        String title = document.getTitle().getOrigin();
//        title = CleanerFactory.getWeakCleaner().replace("", title).trim();

        ICleaner weak = CleanerFactory.getWeakCleaner();
        Set<String> fineGrain = new HashSet<>();
        List<List<String>> units = new ArrayList<>();
        for (int i = 0; i < chunks.size(); i++) {
            Sentence chunk = chunks.get(i);
            String origin = chunk.getOrigin();
//            origin = StringUtils.cleanClose(origin);
//            log.debug(String.format("Origin%d: %s", i, origin));
            List<String> subUnits = SentenceSplitDocumentProcess.splitSentence(origin, true, false);
            if (cleaner != null)
                subUnits = subUnits.stream()
                        .map(StringUtil::cleanClose)
                        .map(it -> {
                            it = cleaner.replace("", it).trim();
                            if (fineGrainedCleaner != null) {
                                List<String> tmp = SentenceSplitDocumentProcess.splitSentence(it, true, true);
                                tmp = tmp.stream().map(it_ -> StringUtil.removeAllEmojis(fineGrainedCleaner.replace("", it_)).trim()).collect(Collectors.toList());
                                it = StringUtil.join(tmp, "");
                            }
                            return it;
                        })
                        .collect(Collectors.toList());


            subUnits = subUnits.stream()
                    .filter(it -> !it.trim().isEmpty())
                    .filter(it -> {
                        String s = weak.replace("", it).trim();
                        if (s.isEmpty())
                            log.debug("Weak filter: " + it);
                        return !s.isEmpty();
                    }).collect(Collectors.toList());

            subUnits = subUnits
                    .stream()
                    .filter(it -> !it.isEmpty())
                    .collect(Collectors.toList());

            List<String> filterDup = new ArrayList<>();
            for (String unit : subUnits) {
                List<String> tmp = SentenceSplitDocumentProcess.splitSentence(unit, false, true);
                if (tmp.stream()
                        .filter(it -> it.length() >= 5)
                        .anyMatch(fineGrain::contains))
                    continue;
                fineGrain.addAll(tmp);
                filterDup.add(unit);
            }
            if (!filterDup.isEmpty())
                units.add(filterDup);
        }

        // 去除问句结尾
        for (int i = units.size() - 1; i >= 0; i--) {
            boolean flag = false;
            List<String> subUnits = units.get(i);
            while (!subUnits.isEmpty()) {
                int j = subUnits.size() - 1;
                String s = subUnits.get(j);
                if (s.endsWith("?") || s.endsWith("？")) {
                    subUnits.remove(j);
                } else {
                    flag = true;
                    break;
                }
            }
            if (flag)
                break;
        }

        units = units.stream()
                .filter(it -> !it.isEmpty()).collect(Collectors.toList());

        if (exceedLen > 0 && mergeProc != null) {
            int curLen = 0;
            for (List<String> subUnits : units) {
                for (String unit : subUnits) {
                    curLen += unit.length();
                }
            }
            for (int i = units.size() - 1; i >= 0; i--) {
                if (curLen <= docLen + exceedLen)
                    break;
                List<String> subUnits = units.get(i);
                boolean flag = subUnits.size() > 1;
                while (subUnits.size() > 1) {
                    String unitBeforeEnd = subUnits.get(subUnits.size() - 2);
                    if (mergeProc.isLegalEnd(unitBeforeEnd)) {
                        curLen -= subUnits.get(subUnits.size() - 1).length();
                        subUnits.remove(subUnits.size() - 1);
                    } else {
                        break;
                    }
                }
                if (flag)
                    break;
            }
        }
        StringBuilder sb = new StringBuilder();
        if (mergeProc != null) {
            String last = "";
            for (List<String> unit : units) {
                if (sb.length() >= docLen - 30 && unit.size() < 2 && (!last.isEmpty() && mergeProc.isLegalEnd(last))) {
                    break;
                }
                if (!unit.isEmpty())
                    last = unit.get(unit.size() - 1);
                for (String s : unit) {
                    sb.append(s);
                }
            }
        }
        String text = sb.toString();

        // 去除超过6个字的连续重复短句
        JaccardScorer scorer = JaccardScorer.getInstance();

        List<String> fineUnits = SentenceSplitDocumentProcess.splitSentence(text, true, true);
        for (int i = 0; i < fineUnits.size() - 1; i++) {
            String pre = fineUnits.get(i);
            String next = fineUnits.get(i + 1);

            pre = fineGrainedCleaner.replace("", pre).trim();
            next = fineGrainedCleaner.replace("", next).trim();
            if (pre.equals(next) || (pre.length() > 6 && next.length() > 6 && scorer.getScores(pre, next) > 0.9)) {
                fineUnits.set(i, "");
            }
        }

        // 去除开头不合法词
        if (!fineUnits.isEmpty()) {
            String trim = fineUnits.get(0).trim();
            List<Term> words = HanLP.segment(trim);
            if (!words.isEmpty() && NON_INDEPENDENT_FIRST_WORDS.contains(words.get(0).word)) {
                String nStr = trim.replaceFirst(words.get(0).word, "");
                log.debug("Remove illegal start: " + trim + " -> " + nStr);
                fineUnits.set(0, nStr);
            }
        }
        text = StringUtil.join(fineUnits, "");
//        text = text.replaceAll(title, "");
//        text = StringUtils.cleanIllegalSentStart(text);
        text = CleanerFactory.getSpaceCleaner().replace(" ", text).trim();
        if (!text.isEmpty()) {
            int i = text.length() - 1;
            while (i >= 0 && NON_STOP_FLAGS.contains(text.substring(i, i + 1)))
                i--;
            if (i + 1 < text.length())
                text = text.substring(0, i + 1);
            String last = text.substring(text.length() - 1);
            if (!STOP_FLAG.contains(last))
                text += "。";
        }

        return text;
    }

    @Accessors(chain = true)
    @Setter
    public static class Builder {
        private RegexCleaner cleaner = new RegexCleaner(Arrays.asList(
                CLEAN_START_PATTERN.pattern(),
                LABEL_PATTERN.pattern()
        ));
        private RegexCleaner fineGrainedCleaner = new RegexCleaner(Arrays.asList(
                LABEL_PATTERN.pattern(),
                "[↑↓←→△☟▼の❀▲☺★◆✔♦■▶]"
        ));
        private RuleMergeDocumentProcess mergeProc = new RuleMergeDocumentProcess.Builder().build();
        private int exceedLen = 50;

        public SummaryPostProcess build() {
            return new SummaryPostProcess(cleaner, fineGrainedCleaner, mergeProc, exceedLen);
        }
    }
}
