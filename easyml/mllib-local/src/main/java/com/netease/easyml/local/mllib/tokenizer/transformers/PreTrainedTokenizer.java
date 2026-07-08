package com.netease.easyml.local.mllib.tokenizer.transformers;

import com.netease.easyml.common.collection.Params;
import com.netease.easyml.common.util.ArrayUtil;
import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by linjiuning on 2020/2/15.
 */
public abstract class PreTrainedTokenizer implements Serializable {
    private static final Logger log = LoggerFactory.getLogger(PreTrainedTokenizer.class);
    protected Parameters params = new Parameters();

    public String bosToken() {
        String token = params.getBosToken();
        if (token == null) {
            log.error("Using bos_token, but it is not set yet.");
        }
        return token;
    }

    public String eosToken() {
        String token = params.getEosToken();
        if (token == null) {
            log.error("Using eos_token, but it is not set yet.");
        }
        return token;
    }

    public String unkToken() {
        String token = params.getUnkToken();
        if (token == null) {
            log.error("Using unk_token, but it is not set yet.");
        }
        return token;
    }

    public String sepToken() {
        String token = params.getSepToken();
        if (token == null) {
            log.error("Using sep_token, but it is not set yet.");
        }
        return token;
    }

    public String padToken() {
        String token = params.getPadToken();
        if (token == null) {
            log.error("Using pad_token, but it is not set yet.");
        }
        return token;
    }

    public String clsToken() {
        String token = params.getClsToken();
        if (token == null) {
            log.error("Using cls_token, but it is not set yet.");
        }
        return token;
    }

    public String maskToken() {
        String token = params.getMaskToken();
        if (token == null) {
            log.error("Using mask_token, but it is not set yet.");
        }
        return token;
    }

    public Set<String> specialTokens() {
        Set<String> token = params.getSpecialTokens();
        if (token == null) {
            log.error("Using additional_special_tokens, but it is not set yet.");
        }
        return token;
    }

    public int bosTokenId() {
        return convertTokenToId(bosToken());
    }

    public int eosTokenId() {
        return convertTokenToId(eosToken());
    }

    public int unkTokenId() {
        return convertTokenToId(unkToken());
    }

    public int sepTokenId() {
        return convertTokenToId(sepToken());
    }

    public int padTokenId() {
        return convertTokenToId(padToken());
    }

    public int padTokenTypeId() {
        return params.getPadTokenTypeId();
    }

    public int clsTokenId() {
        return convertTokenToId(clsToken());
    }

    public int maskTokenId() {
        return convertTokenToId(maskToken());
    }

    public void addSpecialToken(String token) {
        if (token != null && !token.isEmpty())
            specialTokens().add(token);
    }

    public boolean paddingOnLeft() {
        return Objects.equals(params.getPaddingSide(), PaddingSide.LEFT);
    }

    public abstract Map<String, Integer> vocab();

    public int vocabSize() {
        return vocab().size();
    }

    private List<String> splitOnTokens(Set<String> tokList, String text) {
        if (tokList.isEmpty())
            return _tokenize(text);
        String pattern = tokList.stream().map(Pattern::quote).collect(Collectors.joining("|"));
        String[] split = text.split(pattern);
        List<String> tokens = new ArrayList<>();
        for (String s : split) {
            List<String> toks = _tokenize(s);
            tokens.addAll(toks);
        }
        return tokens;
    }

    public List<String> tokenize(String text) {
        Set<String> specialTokens = params.getSpecialTokens();
        return splitOnTokens(specialTokens, text);
    }

    protected abstract List<String> _tokenize(String text);

    public int[] encode(String text, String pairText) {
        Params params = encodePlus(text, pairText);
        return params.get(TransformersUtil.IndexKeys.INPUT_IDS, int[].class);
    }

    public Params encodePlus(String text, String pairText) {
        int[] firstIds = convertTokensToIds(tokenize(text));
        int[] secondIds = null;
        if (pairText != null) {
            secondIds = convertTokensToIds(tokenize(pairText));
        }
        return prepareForModel(firstIds, secondIds);
    }

    public int numAddedTokens(boolean pair) {
        return buildInputsWithSpecialTokens(new int[0], pair ? new int[0] : null).length;
    }

    public int[] buildInputsWithSpecialTokens(int[] ids, int[] pairIds) {
        if (pairIds == null || pairIds.length == 0) {
            return ids;
        }
        return ArrayUtil.concat(ids, pairIds);
    }

    protected int[] createTokenTypeIdsFromSequences(int[] ids, int[] pairIds) {
        if (pairIds == null) {
            return new int[ids.length];
        }
        int[] typeIds = new int[ids.length + pairIds.length];
        for (int i = ids.length; i < typeIds.length; i++) {
            typeIds[i] = 1;
        }
        return typeIds;
    }

    private Params prepareForModel(int[] ids, int[] pairIds) {
        boolean pair = pairIds != null;
        int lenIds = ids.length;
        int lenPairIds = pair ? pairIds.length : 0;

        Params encodedInputs = new Params();

        boolean addSpecialTokens = params.isAddSpecialTokens();
        boolean returnTokenTypeIds = params.isReturnTokenTypeIds();
        String truncationStrategy = params.getTruncationStrategy();

        // Handle max sequence length
        int totalLen = lenIds + lenPairIds + (addSpecialTokens ? numAddedTokens(pair) : 0);
        int maxLength = params.getMaxLen();
        if (maxLength > 0 && totalLen > maxLength) {
            Pair<int[], int[]> p = TruncateStrategy.truncateSequences(ids, pairIds, totalLen - maxLength, truncationStrategy);
            ids = p.getValue0();
            pairIds = p.getValue1();
        }

        // Handle special_tokens
        int[] sequence;
        int[] tokenTypeIds;
        if (addSpecialTokens) {
            sequence = buildInputsWithSpecialTokens(ids, pairIds);
            tokenTypeIds = createTokenTypeIdsFromSequences(ids, pairIds);
        } else {
            if (pair) {
                int length = ids.length + pairIds.length;
                if (maxLength > 0 && length > maxLength) {
                    length = maxLength;
                }
                sequence = new int[length];
                tokenTypeIds = new int[length];
                int i = 0;
                while (i < ids.length && i < length) {
                    sequence[i] = ids[i];
                    i += 1;
                }
                int j = 0;
                while (j < pairIds.length && i < length) {
                    sequence[i] = pairIds[j];
                    tokenTypeIds[i] = 1;
                    i += 1;
                    j += 1;
                }
            } else {
                if (maxLength > 0 && ids.length > maxLength) {
                    sequence = new int[maxLength];
                    tokenTypeIds = new int[maxLength];
                    System.arraycopy(ids, 0, sequence, 0, sequence.length);
                } else {
                    sequence = ids;
                    tokenTypeIds = new int[ids.length];
                }
            }
        }

        encodedInputs.put(TransformersUtil.IndexKeys.INPUT_IDS, sequence);
        if (returnTokenTypeIds) {
            encodedInputs.put(TransformersUtil.IndexKeys.TOKEN_TYPE_IDS, tokenTypeIds);
        }
        // ignore padding
        return encodedInputs;
    }


    public int[] convertTokensToIds(List<String> tokens) {
        int[] ids = new int[tokens.size()];
        for (int i = 0; i < tokens.size(); i++) {
            ids[i] = convertTokenToId(tokens.get(i));
        }
        return ids;
    }

    public abstract int convertTokenToId(String token);

    public abstract String convertIdToToken(int index);

    private static final List<String> MODEL_TYPES = Arrays.asList(
            "t5", "distilbert", "albert", "camembert", "xlm-roberta", "roberta",
            "bert-base-japanese", "bert", "openai-gpt", "gpt2", "transfo-xl", "xlnet", "xlm", "ctrl"
    );

    public static String modelType(String modelNameOrPath) {
        modelNameOrPath = modelNameOrPath.toLowerCase();
        for (String modelType : MODEL_TYPES) {
            if (modelNameOrPath.contains(modelType)) {
                return modelType;
            }
        }
        throw new IllegalArgumentException(String.format("Unrecognized model identifier in %s. Should contains one of %s.",
                modelNameOrPath, String.join(", ", MODEL_TYPES)));
    }
}
