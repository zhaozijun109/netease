package com.netease.easyml.local.mllib.tokenizer.transformers;

import com.netease.easyml.common.util.SequenceLabeling;
import com.netease.easyml.local.mllib.tokenizer.Token;
import org.javatuples.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.netease.easyml.common.util.CollectionUtil.map;


/**
 * Created by linjiuning on 2020/2/13.
 */
public class TransformersUtil {

    public static class IndexKeys {
        public static final String INPUT_IDS = "input_ids";
        public static final String TOKEN_TYPE_IDS = "token_type_ids";
        public static final String ATTENTION_MASK = "attention_mask";
        public static final String LABELS = "labels";
    }

    public static class TaskType {
        public static final String CLASSIFICATION = "classification";
        public static final String REGRESSION = "regression";
        public static final String TOKEN_CLASSIFICATION = "token_classification";
    }

    static class Variable {
        public static int PAD_TOKEN_ID = 0;
    }


    public static final String PAD_TOKEN_LABEL_STR = "@pad@";
    public static final int PAD_TOKEN_LABEL_ID = -100;


    public static Map<String, Integer> SKIP_INDICES = map(
            "[CLS]", PAD_TOKEN_LABEL_ID,
            "[SEP]", PAD_TOKEN_LABEL_ID,
            PAD_TOKEN_LABEL_STR, PAD_TOKEN_LABEL_ID
    );


    public synchronized static void setPadTokenId(int id) {
        Variable.PAD_TOKEN_ID = id;
    }


    public static int getPadTokenId() {
        return Variable.PAD_TOKEN_ID;
    }

    public static boolean isWordPieceStart(String token) {
        return !token.startsWith("##");
    }

    public static String cleanWordPieceText(String token) {
        return token.substring(2);
    }

    public static List<Pair<String, String>> getEntities(List<Token> tokens, List<String> labels) {
        return getEntities(tokens, labels, false);
    }

    public static List<Pair<String, String>> getEntities(List<Token> tokens, List<String> labels, boolean suffix) {
        assert tokens.size() == labels.size();
        List<String> mTokens = new ArrayList<>();
        List<String> mLabels = new ArrayList<>();
        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);
            String label = labels.get(i);
            if (token.wordPieceStart) {
                mTokens.add(token.text);
                mLabels.add(label);
            } else {
                String lastToken = mTokens.get(mTokens.size() - 1);
                lastToken += cleanWordPieceText(token.text);
                mTokens.set(mTokens.size() - 1, lastToken);
            }
        }
        List<Pair<String, Pair<Integer, Integer>>> entitiesPos = SequenceLabeling.getEntities(mLabels, suffix);
        List<Pair<String, String>> entities = new ArrayList<>();
        for (Pair<String, Pair<Integer, Integer>> pos : entitiesPos) {
            String tag = pos.getValue0();
            Integer b = pos.getValue1().getValue0();
            Integer e = pos.getValue1().getValue1();
            StringBuilder sb = new StringBuilder();
            for (int i = b; i < e + 1; i++) {
                sb.append(mTokens.get(i));
            }
            String text = sb.toString();
            entities.add(new Pair<>(text, tag));
        }
        return entities;
    }
}
