package com.netease.easyml.local.mllib.tokenizer.transformers;

import com.netease.easyml.local.mllib.tokenizer.Token;
import com.netease.easyml.local.mllib.tokenizer.Tokenizer;

import java.io.Serializable;
import java.text.Normalizer;
import java.util.*;
import java.util.regex.Pattern;

import static com.netease.easyml.common.util.StringUtil.*;
import static com.netease.easyml.local.mllib.tokenizer.transformers.Tokenizers.whitespaceTokenize;

/**
 * Created by linjiuning on 2019/8/22.
 */
public class BasicTokenizer implements Tokenizer, Serializable {
    private boolean doLowerCase;
    private Set<String> neverSplit;
    private boolean tokenizeChineseChars;

    public BasicTokenizer(boolean doLowerCase, Set<String> neverSplit, boolean tokenizeChineseChars) {
        this.doLowerCase = doLowerCase;
        this.neverSplit = neverSplit;
        this.tokenizeChineseChars = tokenizeChineseChars;
    }

    public BasicTokenizer() {
        this(true, Collections.emptySet(), true);
    }

    private List<String> runSplitOnPunc(String text, Set<String> neverSplit) {
        if (neverSplit.contains(text))
            return Collections.singletonList(text);
        char[] chars = text.toCharArray();
        StringBuilder sb = new StringBuilder();
        List<String> outputs = new ArrayList<>();
        for (char ch : chars) {
            if (isPunctuation(ch)) {
                if (sb.length() > 0) {
                    outputs.add(sb.toString());
                    sb = new StringBuilder();
                }
                outputs.add(String.valueOf(ch));
            } else {
                sb.append(ch);
            }
        }
        if (sb.length() > 0) {
            outputs.add(sb.toString());
        }
        return outputs;
    }

    private String tokenizeChineseChars(String text) {
        StringBuilder sb = new StringBuilder();
        for (char ch : text.toCharArray()) {
            int cp = (int) ch;

            if (isChineseChar(cp)) {
                sb.append(" ");
                sb.append(ch);
                sb.append(" ");
            } else
                sb.append(ch);
        }
        return sb.toString();
    }

    private String cleanText(String text) {
        StringBuilder sb = new StringBuilder();

        for (char ch : text.toCharArray()) {
            int cp = (int) ch;
            if (cp == 0 || cp == 0xfffd || isControl(ch))
                continue;
            if (isWhitespace(ch))
                sb.append(" ");
            else
                sb.append(ch);
        }
        return sb.toString();
    }

    public static String stripAccents(String input) {
        if (input == null) {
            return null;
        } else {
            Pattern pattern = Pattern.compile("(\\p{InCombiningDiacriticalMarks}|[ྂ️゙])+");
            String decomposed = Normalizer.normalize(input, Normalizer.Form.NFD);
            return pattern.matcher(decomposed).replaceAll("");
        }
    }

    public List<String> tokenize(String text, Collection<String> neverSplit) {
        Set<String> sneverSplit = new HashSet<>(this.neverSplit);
        sneverSplit.addAll(neverSplit);

        text = cleanText(text);

        if (tokenizeChineseChars) {
            text = tokenizeChineseChars(text);
        }
        List<String> origTokens = whitespaceTokenize(text);
        List<String> splitTokens = new ArrayList<>();
        for (String token : origTokens) {
            if (doLowerCase && !sneverSplit.contains(token)) {
                token = token.toLowerCase();
                token = stripAccents(token);
            }
            splitTokens.addAll(runSplitOnPunc(token, sneverSplit));
        }
        return whitespaceTokenize(String.join(" ", splitTokens));
    }

    @Override
    public List<Token> tokenize(String text) {
        List<String> words = tokenize(text, Collections.emptySet());
        List<Token> tokens = new ArrayList<>();
        for (String word : words) {
            Token token = new Token();
            token.text = word;
            tokens.add(token);
        }
        return tokens;
    }
}
