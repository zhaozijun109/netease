package com.netease.easyml.common.util;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.corpus.tag.Nature;
import com.hankcs.hanlp.dictionary.CoreDictionary;
import com.hankcs.hanlp.dictionary.CustomDictionary;
import com.hankcs.hanlp.dictionary.py.Pinyin;
import com.hankcs.hanlp.seg.common.Term;
import com.hankcs.hanlp.tokenizer.StandardTokenizer;
import com.vdurmont.emoji.EmojiParser;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by linjiuning on 2020/6/22.
 */
public class StringUtil {
    private static final Logger log = LoggerFactory.getLogger(StringUtil.class);
    private static final String REPEAT_GROUP_NAME = "repeat";

    public static String trim(String text) {
        return strip(text, "　\\s\t\n");
    }

    public static String rstrip(String text, String regex) {
        return text.replaceAll("[" + regex + "]+" + "$", "");
    }

    public static String lstrip(String text, String regex) {
        return text.replaceAll("^" + "[" + regex + "]+", "");
    }

    public static String strip(String text, String regex) {
        String pText = rstrip(text, regex);
        return lstrip(pText, regex);
    }

    public static boolean isEmpty(CharSequence str) {
        return str == null || str.length() == 0;
    }

    public static String strip(String str) {
        if (isEmpty(str)) {
            return str;
        }

        int i = 0;
        while (i < str.length() && (isWhitespace(str.charAt(i)) || isControl(str.charAt(i)))) {
            i++;
        }

        int j = str.length() - 1;
        while (j > i && (isWhitespace(str.charAt(j)) || isControl(str.charAt(j)))) {
            j--;
        }
        return str.substring(i, j + 1);
    }

    public static List<String> split(String text, String sep, int limit) {
        return Arrays.asList(text.split(sep, limit));
    }

    public static List<String> split(String text, String sep) {
        return Arrays.asList(text.split(sep));
    }

    public static List<String> splitField(String text, char sep) {
        List<String> result = new ArrayList<>();
        int stack = 0;
        int jinja = 0;
        List<Integer> sepId = new ArrayList<>();
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (ch == '{')
                jinja += 1;
            else if (ch == '}')
                jinja -= 1;
            else if (ch == '(')
                stack += 1;
            else if (ch == ')')
                stack -= 1;
            else if (ch == sep && stack == 0 && jinja == 0)
                sepId.add(i);
            if (stack < 0)
                throw new IllegalArgumentException(text.substring(i));
        }
        if (sepId.isEmpty())
            result.add(text);
        else {
            for (int i = 0; i < sepId.size(); i++) {
                int idx = sepId.get(i);
                if (i == 0)
                    result.add(text.substring(0, idx));
                else
                    result.add(text.substring(sepId.get(i - 1) + 1, idx));
            }
            result.add(text.substring(sepId.get(sepId.size() - 1) + 1));
        }
        return result.stream().filter(it -> !it.isEmpty()).collect(Collectors.toList());
    }

    public static List<String> splitSentence(String text) {
        return splitSentence(text, false, false);
    }

    public static List<String> splitSentence(String text, boolean keep, boolean fineGrained) {
        String delimiters;
        if (fineGrained)
            delimiters = keep ? Constant.FG_SENTENCE_SEP_KEEP_PATTERN : Constant.FG_SENTENCE_SEP_PATTERN;
        else
            delimiters = keep ? Constant.SENTENCE_SEP_KEEP_PATTERN : Constant.SENTENCE_SEP_PATTERN;
        return split(text, delimiters);
    }

    public static List<String> splitSentenceV2(String text) {
        return splitSentenceV2(text, false, false);
    }

    public static List<String> splitSentenceV2(String text, boolean keep, boolean fineGrained) {
        String delimiters;
        if (fineGrained)
            delimiters = Constant.FG_SENTENCE_SEP_PATTERN_V2;
        else
            delimiters = Constant.SENTENCE_SEP_PATTERN_V2;
        if (keep) {
            text = text.replaceAll(delimiters, "$1" + Constant.SEP);
            delimiters = Constant.SEP;
        }
        return Arrays.asList(text.split(delimiters));
    }

    public static <T> String join(Collection<T> texts, String sep) {
        if (texts == null || texts.isEmpty())
            return "";
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (T text : texts) {
            sb.append(text);
            if (i++ < texts.size() - 1)
                sb.append(sep);
        }
        return sb.toString();
    }

    public static <T> String join(T[] texts, String sep) {
        if (texts == null || texts.length == 0)
            return "";
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (T text : texts) {
            sb.append(text);
            if (i++ < texts.length - 1)
                sb.append(sep);
        }
        return sb.toString();
    }

    public static String join(int[] texts, String sep) {
        if (texts == null || texts.length == 0)
            return "";
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (int text : texts) {
            sb.append(text);
            if (i++ < texts.length - 1)
                sb.append(sep);
        }
        return sb.toString();
    }

    public static String join(long[] texts, String sep) {
        if (texts == null || texts.length == 0)
            return "";
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (long text : texts) {
            sb.append(text);
            if (i++ < texts.length - 1)
                sb.append(sep);
        }
        return sb.toString();
    }

    public static String join(float[] texts, String sep) {
        if (texts == null || texts.length == 0)
            return "";
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (float text : texts) {
            sb.append(text);
            if (i++ < texts.length - 1)
                sb.append(sep);
        }
        return sb.toString();
    }

    public static String join(double[] texts, String sep) {
        if (texts == null || texts.length == 0)
            return "";
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (double text : texts) {
            sb.append(text);
            if (i++ < texts.length - 1)
                sb.append(sep);
        }
        return sb.toString();
    }

    /**
     * check whether a Java String is a legal number (integer or float)
     */
    public static boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    public static int parseInt(String value, int defaultValue) {
        int ret = defaultValue;
        if (value != null && !value.isEmpty()) {
            try {
                ret = Integer.parseInt(value.trim());
            } catch (NumberFormatException ex) {
                ex.printStackTrace();
            }
        }
        return ret;
    }

    public static double parseDouble(String value, double defaultValue) {
        double ret = defaultValue;
        if (value != null && !value.isEmpty()) {
            try {
                ret = Double.parseDouble(value.trim());
            } catch (NumberFormatException ex) {
                ex.printStackTrace();
            }
        }
        return ret;
    }

    public static boolean parseBoolean(String value, boolean defaultValue) {
        boolean ret = defaultValue;
        if (value != null && !value.isEmpty()) {
            ret = Boolean.parseBoolean(value.trim());
        }
        return ret;
    }

    public static boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    public static String getNormRepeatPattern(String... regexes) {
        List<String> singleChar = new ArrayList<>();
        List<String> multiChar = new ArrayList<>();

        for (String regex : regexes) {
            if (regex.isEmpty())
                continue;
            if (regex.length() == 1)
                singleChar.add(regex);
            else
                multiChar.add(regex);
        }
        if (singleChar.isEmpty() && multiChar.isEmpty())
            return "";

        StringBuilder sb = new StringBuilder();
        if (!singleChar.isEmpty()) {
            if (singleChar.size() > 1)
                sb.append("[");
            singleChar.forEach(sb::append);
            if (singleChar.size() > 1)
                sb.append("]");
        }
        if (!multiChar.isEmpty()) {
            if (sb.length() != 0)
                sb.append("|");
            multiChar.forEach(sb::append);
        }

        return String.format("(?<%s>%s)\\k<%s>+", REPEAT_GROUP_NAME, sb.toString(), REPEAT_GROUP_NAME);
    }

    /**
     * normalize all repeated patterns
     * Eg: abc,,def -> abc,def
     */
    public static String normRepeatAll(String text, String... regexes) {
        String pattern = getNormRepeatPattern(regexes);
        if (pattern.isEmpty())
            return text;
        return text.replaceAll(pattern, String.format("${%s}", REPEAT_GROUP_NAME));
    }

    public static String normRepeatFirst(String text, String... regexes) {
        String pattern = getNormRepeatPattern(regexes);
        if (pattern.isEmpty())
            return text;
        return text.replaceFirst(pattern, String.format("${%s}", REPEAT_GROUP_NAME));
    }

    public static String normRepeatPunct(String text) {
        return normRepeatAll(text, Constant.PUNCT_PATTERN);
    }

    public static String removePunctuation(String text) {
        return text.replaceAll(Constant.PUNCT_PATTERN, " ").replaceAll("\\s+", " ");
    }

    public static String removeHTML(String value) {
        return value.replaceAll("<[^>]*>", " ")
                .replaceAll("&[a-z]+;?", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    public static String choice(String text, String regSep) {
        if (text == null || regSep == null || text.isEmpty() || regSep.isEmpty())
            return text;
        List<String> values = Stream.of(text.split("\\|"))
                .filter(it -> !(it.isEmpty() || it.equals(".*"))).collect(Collectors.toList());
        if (values.size() <= 1)
            return text;

        int idx = ThreadLocalRandom.current().nextInt(values.size());
        return values.get(idx);
    }

    public static String unicode2String(String unicode) {
        StringBuilder string = new StringBuilder();
        String[] hex = unicode.split("\\\\u");

        for (int i = 1; i < hex.length; i++) {
            int data = Integer.parseInt(hex[i], 16);
            string.append((char) data);
        }

        return string.toString();
    }

    public static String unicodeStr2String(String unicodeStr) {
        int length = unicodeStr.length();
        int count = 0;
        //正则匹配条件，可匹配“\\u”1到4位，一般是4位可直接使用 String regex = "\\\\u[a-f0-9A-F]{4}";
        String regex = "\\\\u[a-f0-9A-F]{1,4}";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(unicodeStr);
        StringBuilder sb = new StringBuilder();

        while (matcher.find()) {
            String oldChar = matcher.group();
            String newChar = unicode2String(oldChar);
            int index = unicodeStr.indexOf(oldChar, count);

            sb.append(unicodeStr.substring(count, index));//添加前面不是unicode的字符
            sb.append(newChar);//添加转换后的字符
            count = index + oldChar.length();//统计下标移动的位置
        }
        sb.append(unicodeStr.substring(count, length));//添加末尾不是Unicode的字符
        return sb.toString();
    }

    public static String string2Unicode(String string) {
        StringBuilder unicode = new StringBuilder();
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            unicode.append("\\u").append(Integer.toHexString(c));
        }

        return unicode.toString();
    }

    /**
     * String.replaceAll will replace \d -> d
     */
    public static String replaceAll(String text, String regex, String replacement) {
        Matcher m = Pattern.compile(regex).matcher(text);
        boolean result = m.find();
        if (result) {
            StringBuilder sb = new StringBuilder();
            int spos = 0;
            do {
                int epos = m.end();
                sb.append(text.substring(spos, m.start())).append(replacement);
                spos = epos;
                result = m.find();
            } while (result);
            sb.append(text.substring(spos, text.length()));
            return sb.toString();
        }
        return text;
    }

    public static String replaceFirst(String text, String regex, String replacement) {
        if (replacement == null)
            throw new NullPointerException("replacement");
        Matcher m = Pattern.compile(regex).matcher(text);
        if (!m.find())
            return text;
        StringBuilder sb = new StringBuilder();
        sb.append(text.substring(0, m.start()))
                .append(replacement)
                .append(text.substring(m.end(), text.length()));
        return sb.toString();
    }

    @Deprecated
    public static void addVocab(Collection<String> vocab, boolean caseSensitive) {
        if (caseSensitive) {
            for (String wd : vocab) {
                try {
                    if (!(CustomDictionary.contains(wd) || CoreDictionary.contains(wd)))
                        CustomDictionary.add(wd);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println(wd);
                }
            }
        } else {
            Set<String> expVocab = new HashSet<>();
            vocab.forEach(it -> {
                expVocab.add(it);
                expVocab.add(it.toLowerCase());
                expVocab.add(it.toUpperCase());
            });
            addVocab(expVocab);
        }
    }

    @Deprecated
    public static void addVocab(Collection<String> vocab) {
        addVocab(vocab, true);
    }

    public static boolean isRegex(String text) {
        text = text.trim();
        if (isNumeric(text) || text.length() == 1)
            return false;
        Pattern regexChar = Pattern.compile("[*.?+{}()^$=<>]");
        Matcher m = regexChar.matcher(text);
        boolean flag = false;
        if (m.find()) {
            try {
                Pattern.compile(text);
            } catch (PatternSyntaxException e) {
            }
            flag = true;
        }
        return flag;
    }

    public static String escape(String s) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < s.length(); ++i) {
            char c = s.charAt(i);
            if (c == '\\' || c == '+' || c == '-' || c == '!' || c == '(' || c == ')' || c == ':' || c == '^' || c == '[' || c == ']' || c == '"' || c == '{' || c == '}' || c == '~' || c == '*' || c == '?' || c == '|' || c == '&' || c == '/') {
                sb.append('\\');
            }

            sb.append(c);
        }

        return sb.toString();
    }

    public static String cleanConj(String line) {
        StringBuilder choose = new StringBuilder();
        List<Term> termList = HanLP.segment(line);
        boolean startFlag = false;
        List<Nature> natures = termList.stream().map(it -> it.nature).collect(Collectors.toList());
        // handle <S> d c ... <E>
        if (natures.size() > 2 && natures.get(0).startsWith("d") && natures.get(1).startsWith("c"))
            termList = termList.subList(2, termList.size());
        for (Term term : termList) {
            Nature nature = term.nature;
            String wd = term.word;

            if (startFlag && !(nature.startsWith("w") && choose.length() == 0 && !wd.equals("《"))) {
                choose.append(wd);
            } else if (!nature.startsWith("c")) {
                choose.append(wd);
                startFlag = true;
            }
        }
        return choose.toString();
    }

    public static int escapeIndexOf(String text, String str, int fromIndex) {
        while (fromIndex < text.length()) {
            int i = text.indexOf(str, fromIndex);
            if (i < 0) {
                return i;
            }
            if (i > 0 && text.charAt(i - 1) == '\\') {
                fromIndex = i + str.length();
            } else {
                return i;
            }
        }
        return -1;
    }

    public static boolean isClosed(String text, String start, String end, boolean escape) {
        if (text == null || text.isEmpty())
            return true;
        int count = 0;
        int i = 0;
        while (count >= 0 && i >= 0 && i < text.length()) {
            int si = escape ? escapeIndexOf(text, start, i) : text.indexOf(start, i);
            int ei = escape ? escapeIndexOf(text, end, i) : text.indexOf(end, i);
            if (si == -1 && ei == -1) {
                i = -1;
            } else if (si == -1) {
                count--;
                i = ei + end.length();
            } else if (ei == -1) {
                count++;
                i = si + start.length();
            } else {
                if (si == ei) {
                    if (count == 0) {
                        count++;
                        i = si + start.length();
                    } else {
                        count--;
                        i = ei + end.length();
                    }
                } else if (si < ei) {
                    count++;
                    i = si + start.length();
                } else {
                    count--;
                    i = ei + end.length();
                }
            }
        }
        return count == 0;
    }

    /**
     * clean incomplete closed text
     * Example: abc] -> abc
     *
     * @param line raw text
     * @return cleaned text
     */
    public static String cleanClose(String line) {
        if (line == null || line.isEmpty())
            return line;
        List<Integer> toRemove = new ArrayList<>();
        List<Integer> idx = new ArrayList<>();
        for (int i = 0; i < line.length(); i++) {
            String c = line.substring(i, i + 1);
            if (Constant.START_CLOSE.contains(c) || "\"“".contains(c))
                idx.add(i);
            else if (Constant.END_CLOSE.contains(c) || "\"”".contains(c)) {
                if (idx.isEmpty())
                    toRemove.add(i);
                else
                    idx.remove(idx.size() - 1);
            }
        }
        toRemove.addAll(idx);
        toRemove.sort(Integer::compare);
        if (toRemove.isEmpty())
            return line;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < toRemove.size(); i++) {
            if (i == 0)
                sb.append(line, 0, toRemove.get(i));
            else
                sb.append(line, toRemove.get(i - 1) + 1, toRemove.get(i));
            sb.append(" ");
        }
        if (!toRemove.isEmpty())
            sb.append(line.substring(toRemove.get(toRemove.size() - 1) + 1));
        return sb.toString().trim();
    }

    /**
     * abc, -> abc
     * abc! -> abc!
     */
    public static String cleanIllegelSentEnd(String text) {
        String regex = "^" + Constant.MID_SENTENCE_SEP_PATTERN + "+" + "|" + Constant.MID_SENTENCE_SEP_PATTERN + "+" + "$";
        text = text.trim().replaceAll(regex, "").trim();
        return cleanClose(text);
    }

    /**
     * abc, -> abc
     * abc! -> abc!
     */
    public static String ILLEGELSTART = Constant.PUNCT;

    static {
        for (String s : Constant.START_CLOSE.split("")) {
            ILLEGELSTART = ILLEGELSTART.replace(s, "");
        }
    }

    public static String cleanIllegelSentStart(String text) {
        String PUNCT_PATTERN = "[" + Pattern.quote(ILLEGELSTART) + "]";
        String regex = "^" + PUNCT_PATTERN + "+";
        text = text.trim().replaceAll(regex, "");
        return cleanClose(text);
    }

    public static List<Term> cleanStopPos(List<Term> terms) {
        return terms.stream().filter(it -> !Constant.STOP_POS.contains(it.nature.toString())).collect(Collectors.toList());
    }

    /**
     * Following is created by neilzwyang
     * Modified by eddielin on 2018/06/27
     */

    public static List<String> seg(String line) {
        if (line.trim().isEmpty())
            return Collections.singletonList(line);

        List<Term> termList = StandardTokenizer.segment(line);
        return termList.stream().map(it -> it.word).collect(Collectors.toList());
    }

    public static List<Term> segTerm(String line) {
        if (line.isEmpty())
            return Collections.emptyList();

        return StandardTokenizer.segment(line);
    }

    /**
     * TODO: 优化
     * 清除开头的副词部分
     */
    public static String cleanFC(String line) {
        StringBuilder choose = new StringBuilder();
        List<Term> termList = HanLP.segment(line);
        boolean startFlag = false;
        for (Term term : termList) {
            Nature nature = term.nature;
            String wd = term.word;
            //System.out.println(String.format("word:%s, nature:%s", wd, nature));
            if (startFlag && !(nature.startsWith("w") && choose.length() == 0 && !wd.equals("《"))) {
                choose.append(wd);
                continue;
            }
            if (!nature.startsWith("d")) {
                choose.append(wd);
                startFlag = true;
            }
        }
        return choose.toString();
    }

    public static boolean isStartWithFC(String line) {
        List<Term> termList = HanLP.segment(line);
        return !termList.isEmpty() && termList.get(0).nature.startsWith("d");
    }

    public static boolean equals(String str1, String str2) {
        if (str1 == null && str2 == null)
            return true;
        else if (str1 == null || str2 == null)
            return false;
        else
            return str1.equals(str2);
    }

    /**
     * 全角半角字符转换
     * <p>
     * 说明：全角字符的Unicode码点范围为[65281-65374], 对应的半角字符的码点范围为 [33-126],
     * 其中相差固定的距离 65248。除此之外，还存在一个例外情况，空格字符,
     * 空格字符的全角Unicode码点为12288, 对应的半角码点为 32。
     */
    public static class FullHalfConversion {
        private static final int FULL_START = 65281;    // 起始全角字符的Unicode码点
        private static final int FULL_END = 65374;      // 结尾全角字符的Unicode码点
        private static final int HALF_START = 33;       // 起始半角字符的Unicode码点
        private static final int HALF_END = 126;        // 结束半角字符的Unicode码点
        private static final int FULL_HALF_GAP = 65248; // 全角字符与半角字符码点之间的固定差值
        private static final int FULL_SPACE = 12288;    // 全角空格字符的Unicode码点
        private static final int HALF_SPACE = 32;       // 半角空格字符的Unicode码点

        /**
         * 全角字符串转半角字符串.
         *
         * @param s 待转换字符串
         * @return 转换后的半角字符串
         */
        public static String fullToHalf(String s) {
            char[] charList = s.toCharArray();
            for (int i = 0; i < charList.length; ++i) {
                if (charList[i] == FULL_SPACE) {
                    charList[i] = (char) HALF_SPACE;
                } else if (FULL_START <= charList[i] && charList[i] <= FULL_END) {
                    charList[i] = (char) (charList[i] - FULL_HALF_GAP);
                }
            }
            return new String(charList);
        }

        /**
         * 半角字符串转全角字符串.
         *
         * @param s 待转换字符串
         * @return 转换后的全角字符串
         */
        public static String halfToFull(String s) {
            char[] charList = s.toCharArray();
            for (int i = 0; i < charList.length; ++i) {
                if (charList[i] == HALF_SPACE) {
                    charList[i] = (char) FULL_SPACE;
                } else if (HALF_START <= charList[i] && charList[i] <= HALF_END) {
                    charList[i] = (char) (charList[i] + FULL_HALF_GAP);
                }
            }
            return new String(charList);
        }
    }

    public static String fullToHalf(String s) {
        return FullHalfConversion.fullToHalf(s);
    }

    public static String halfToFull(String s) {
        return FullHalfConversion.halfToFull(s);
    }

    public static String uuid() {
        return UUID.randomUUID().toString();
    }

    public static String uuid(int length) {
        String uuid = UUID.randomUUID().toString();
        return uuid.substring(0, Math.min(length, uuid.length()));
    }

    public static int count(String text, String word) {
        if (isEmpty(text) || isEmpty(word))
            return 0;
        Pattern pattern = Pattern.compile(word, Pattern.CASE_INSENSITIVE);
        return count(text, pattern);
    }

    public static int count(String text, Pattern pattern) {
        if (isEmpty(text))
            return 0;
        int cnt = 0;
        Matcher m = pattern.matcher(text);
        while (m.find())
            cnt++;
        return cnt;
    }

    public static Collection<String> count(String text, Pattern pattern, boolean uniq) {
        if (isEmpty(text))
            return Collections.emptyList();
        Collection<String> matches = uniq ? new HashSet<>() : new ArrayList<>();
        Matcher m = pattern.matcher(text);
        while (m.find()) {
            matches.add(m.group());
        }
        return matches;
    }

    public static List<String> matchesAll(String text, String word) {
        if (isEmpty(text) || isEmpty(word))
            return Collections.emptyList();
        Pattern pattern = Pattern.compile(word, Pattern.CASE_INSENSITIVE);
        return matchesAll(text, pattern);
    }

    public static List<String> matchesAll(String text, Pattern pattern) {
        if (isEmpty(text))
            return Collections.emptyList();
        List<String> res = new ArrayList<>();
        Matcher m = pattern.matcher(text);
        while (m.find()) {
            res.add(m.group());
        }
        return res;
    }

    public static int uniqueId(String text) {
        return Hashing.murmur3_32()
                .newHasher()
                .putString(text, Charsets.UTF_8)
                .hash().asInt();
    }

    public static String md5(String text) {
        return DigestUtils.md5Hex(text);
    }

    public static List<String> splitTrimNoEmpty(String text, String regex) {
        return Stream.of(text.split(regex)).map(String::trim).filter(it -> !it.isEmpty()).collect(Collectors.toList());
    }

    public static String reverse(String text) {
        return new StringBuilder(text).reverse().toString();
    }

    public static String pinyin(String text) {
        if (text.replaceAll("[a-zA-Z0-9]", "").isEmpty())
            return text;
        List<Pinyin> pinyins = HanLP.convertToPinyinList(text);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < pinyins.size(); i++) {
            String pin = pinyins.get(i).getPinyinWithoutTone();
            if (pin.equals("none")) {
                if (i >= text.length()) {
                    log.error(String.format("Failed to convert: %s", text));
                    return text;
                }
                pin = text.substring(i, i + 1);
            }
            sb.append(pin);
        }
        return sb.toString();
    }

    public static String removeAllEmojis(String text) {
        return EmojiParser.removeAllEmojis(text);
    }

    public static boolean isWhitespace(char ch) {
        if (ch == ' ' || ch == '\t' || ch == '\n' || ch == '\r')
            return true;
        int cp = (int) ch;
        if (cp == 160 || cp == 8203)
            return true;
        return Character.isWhitespace(ch);
    }

    public static boolean isControl(char ch) {
        if (ch == '\t' || ch == '\n' || ch == '\r')
            return false;
        int cp = (int) ch;
        if (cp == 65279)
            return true;
        return Character.isISOControl(ch);
    }

    public static boolean isPunctuation(char ch) {
        int cp = (int) ch;
        if ((cp >= 33 && cp <= 47) || (cp >= 58 && cp <= 64) ||
                (cp >= 91 && cp <= 96) || (cp >= 123 && cp <= 126))
            return true;
        return Pattern.matches("[\\p{Punct}\\p{IsPunctuation}]", String.valueOf(ch));
    }

    public static boolean isChineseChar(int cp) {
        if ((cp >= 0x4E00 && cp <= 0x9FFF) ||
                (cp >= 0x3400 && cp <= 0x4DBF) ||
                (cp >= 0x20000 && cp <= 0x2A6DF) ||
                (cp >= 0x2A700 && cp <= 0x2B73F) ||
                (cp >= 0x2B740 && cp <= 0x2B81F) ||
                (cp >= 0x2B820 && cp <= 0x2CEAF) ||
                (cp >= 0xF900 && cp <= 0xFAFF) ||
                (cp >= 0x2F800 && cp <= 0x2FA1F))
            return true;
        return false;
    }

    public static List<String> safeSplit(String text, String sep) {
        return safeSplit(text, sep, "\"'", "\"'");
    }

    public static List<String> safeSplit(String text, String sep, String start, String end) {
        List<String> results = new ArrayList<>();
        String m = "";
        for (String sql : text.split(sep)) {
            if (m.isEmpty()) {
                m = sql;
            } else {
                m = m + sep + sql;
            }
            for (int i = 0; i < start.length(); i++) {

            }
            boolean closed = true;
            for (int i = 0; i < start.length(); i++) {
                closed = StringUtil.isClosed(m, start.substring(i, i + 1), end.substring(i, i + 1), true);
                if (!closed) {
                    break;
                }
            }
            if (closed) {
                results.add(m);
                m = "";
            }
        }
        if (!m.isEmpty()) {
            results.add(m);
        }
        return results;
    }
}
