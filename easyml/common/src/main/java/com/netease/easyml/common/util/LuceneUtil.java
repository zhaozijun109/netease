package com.netease.easyml.common.util;

import org.apache.lucene.document.*;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.*;
import org.apache.lucene.util.BytesRef;
import org.javatuples.Pair;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Created by linjiuning on 2018/10/16.
 */
public class LuceneUtil {
    private static final Random RANDOM = new Random();

    public static String clean(String text) {
        return text.replaceAll("/", "");
    }

    public static List<String> clean(List<String> text) {
        return text.stream().map(LuceneUtil::clean).filter(it -> !it.isEmpty()).collect(Collectors.toList());
    }

    public static String render(String key, String value, boolean exact) {
        return render(key, value, "", exact);
    }

    public static String render(String key, String value) {
        return render(key, value, "", false);
    }

    public static String render(String key, String value, String ratio, boolean exact) {
        String query = "";
        if (!StringUtil.isEmpty(key) && !StringUtil.isEmpty(value)) {
            value = clean(value);
            if (!value.isEmpty()) {
                if (ratio != null && !ratio.isEmpty())
                    query = exact ? String.format("(%s:\"%s\")^%s", key, value, ratio) :
                            String.format("(%s:%s)^%s", key, value, ratio);
                else
                    query = exact ? String.format("%s:\"%s\"", key, value) :
                            String.format("(%s:%s)", key, value);
            }
        }
        return query;
    }

    public static String render(String key, String value, String ratio) {
        return render(key, value, ratio, false);
    }

    public static String render(String key, List<String> matchValues, List<String> notMatchValues, String ratio, boolean exact) {
        if (matchValues != null)
            matchValues = clean(matchValues);

        if (notMatchValues != null)
            notMatchValues = clean(notMatchValues);

        if (StringUtil.isEmpty(key) && CollectionUtil.isEmpty(matchValues) && CollectionUtil.isEmpty(notMatchValues))
            return "";

        List<String> cond = new ArrayList<>();
        if (!CollectionUtil.isEmpty(matchValues)) {
            for (String val : matchValues) {
                cond.add(exact ? String.format("+%s:\"%s\"", key, val) :
                        String.format("%s:%s", key, val));
            }
        }

        if (!CollectionUtil.isEmpty(notMatchValues)) {
            for (String val : notMatchValues) {
                cond.add(exact ? String.format("-%s:\"%s\"", key, val) :
                        String.format("%s:%s", key, val));
            }
        }

        String query = StringUtil.join(cond, " ");

        if (StringUtil.isEmpty(ratio)) {
            if (cond.size() > 1 || !exact)
                query = "(" + query + ")";
        } else
            query = String.format("(%s)^%s", query, ratio);
        return query;
    }

    public static String render(String key, List<String> matchValues, List<String> notMatchValues, String ratio) {
        return render(key, matchValues, notMatchValues, ratio, false);
    }

    public static String render(String key, List<String> matchValues, List<String> notMatchValues) {
        return render(key, matchValues, notMatchValues, "");
    }

    // low level api https://lucene.apache.org/core/7_4_0
    // http://codepub.cn/2016/05/20/Lucene-6-0-in-action-2-All-kinds-of-Field-and-sort-operations/
    // support term-level boost
    public static TermQuery termQuery(String field, String value) {
        return new TermQuery(new Term(field, QueryParser.escape(value)));
    }

    public static FuzzyQuery fuzzyQuery(String field, String value) {
        return new FuzzyQuery(new Term(field, QueryParser.escape(value)));
    }

    public static WildcardQuery wildcardQuery(String field, String value) {
        return new WildcardQuery(new Term(field, QueryParser.escape(value)));
    }

    public static PhraseQuery phraseQuery(String field, String... values) {
        return new PhraseQuery(field, values);
    }

    public static PhraseQuery phraseQuery(String field, Collection<String> values) {
        return new PhraseQuery(field, values.toArray(new String[0]));
    }

    public static PhraseQuery phraseQuery(int slop, String field, String... values) {
        return new PhraseQuery(slop, field, values);
    }

    public static PhraseQuery phraseQuery(int slop, String field, Collection<String> values) {
        return new PhraseQuery(slop, field, values.toArray(new String[0]));
    }

    public static Query boostQuery(Query query, float boost) {
        if (boost == 1.0)
            return query;
        return new BoostQuery(query, boost);
    }

    public static Query boostQuery(String field, String value, float boost) {
        if (boost == 1.0)
            return termQuery(field, value);
        return new BoostQuery(termQuery(field, value), boost);
    }

    public static PrefixQuery prefixQuery(String field, String value) {
        return new PrefixQuery(new Term(field, value));
    }

    // numericRangeQuery 使用IntPoint, FloatPoint, DoublePoint的newXXX工厂方法创建

    // constant score query
    // BooleanClause.Occur.FILTER 可使语句不参与打分
    public static ConstantScoreQuery constantQuery(Query query) {
        return new ConstantScoreQuery(query);
    }

    public static ConstantScoreQuery constantQuery(String field, String value) {
        return new ConstantScoreQuery(termQuery(field, value));
    }

    public static BoostQuery constantQuery(String field, String value, float boost) {
        return new BoostQuery(constantQuery(field, value), boost);
    }

    public static BooleanQuery filterQuery(Query query) {
        return booleanQuery(BooleanClause.Occur.FILTER, constantQuery(query));
    }

    public static BooleanQuery filterQuery(String field, String value) {
        return booleanQuery(BooleanClause.Occur.FILTER, constantQuery(field, value));
    }

    public static BooleanQuery booleanQuery(BooleanClause.Occur occur, Collection<Query> queries) {
        BooleanQuery.Builder builder = new BooleanQuery.Builder();
        for (Query query : queries)
            builder.add(query, occur);
        return builder.build();
    }

    public static BooleanQuery booleanQuery(BooleanClause.Occur occur, Query... queries) {
        BooleanQuery.Builder builder = new BooleanQuery.Builder();
        for (Query query : queries)
            builder.add(query, occur);
        return builder.build();
    }

    public static BooleanQuery booleanQuery(Pair<Query, BooleanClause.Occur>... queries) {
        BooleanQuery.Builder builder = new BooleanQuery.Builder();
        for (Pair<Query, BooleanClause.Occur> query : queries) {
            if (query == null || query.getValue0() == null || query.getValue1() == null)
                continue;
            builder.add(query.getValue0(), query.getValue1());
        }
        return builder.build();
    }

    public static BooleanQuery booleanQuery(Collection<Pair<Query, BooleanClause.Occur>> queries) {
        BooleanQuery.Builder builder = new BooleanQuery.Builder();
        for (Pair<Query, BooleanClause.Occur> query : queries) {
            if (query == null || query.getValue0() == null || query.getValue1() == null)
                continue;
            builder.add(query.getValue0(), query.getValue1());
        }
        return builder.build();
    }

    public static BooleanQuery booleanQuery(BooleanClause... clauses) {
        BooleanQuery.Builder builder = new BooleanQuery.Builder();
        for (BooleanClause clause : clauses) {
            builder.add(clause);
        }
        return builder.build();
    }

    public static BooleanQuery booleanQuery(List<BooleanClause> clauses) {
        BooleanQuery.Builder builder = new BooleanQuery.Builder();
        for (BooleanClause clause : clauses) {
            builder.add(clause);
        }
        return builder.build();
    }

    public static List<Field> intPoint(String name, int value) {
        return intPoint(name, value, false, true);
    }

    public static List<Field> intPoint(String name, int value, boolean sort, boolean store) {
        List<Field> fields = new ArrayList<>();
        Field field = new IntPoint(name, value);
        fields.add(field);
        if (sort) {
            //要排序，必须添加一个同名的NumericDocValuesField
            field = new NumericDocValuesField(name, value);
            fields.add(field);
        }
        if (store) {
            //要存储值，必须添加一个同名的StoredField
            field = new StoredField(name, value);
            fields.add(field);
        }
        return fields;
    }

    public static List<Field> longPoint(String name, long value) {
        return longPoint(name, value, false, true);
    }

    public static List<Field> longPoint(String name, long value, boolean sort, boolean store) {
        List<Field> fields = new ArrayList<>();
        Field field = new LongPoint(name, value);
        fields.add(field);
        if (sort) {
            //要排序，必须添加一个同名的NumericDocValuesField
            field = new NumericDocValuesField(name, value);
            fields.add(field);
        }
        if (store) {
            //要存储值，必须添加一个同名的StoredField
            field = new StoredField(name, value);
            fields.add(field);
        }
        return fields;
    }

    public static List<Field> floatPoint(String name, float value) {
        return floatPoint(name, value, false, true);
    }

    public static List<Field> floatPoint(String name, float value, boolean sort, boolean store) {
        List<Field> fields = new ArrayList<>();
        Field field = new FloatPoint(name, value);
        fields.add(field);
        if (sort) {
            //要排序，必须添加一个同名的NumericDocValuesField
            field = new FloatDocValuesField(name, value);
            fields.add(field);
        }
        if (store) {
            //要存储值，必须添加一个同名的StoredField
            field = new StoredField(name, value);
            fields.add(field);
        }
        return fields;
    }

    public static List<Field> doublePoint(String name, double value) {
        return doublePoint(name, value, false, true);
    }

    public static List<Field> doublePoint(String name, double value, boolean sort, boolean store) {
        List<Field> fields = new ArrayList<>();
        Field field = new DoublePoint(name, value);
        fields.add(field);
        if (sort) {
            //要排序，必须添加一个同名的NumericDocValuesField
            field = new DoubleDocValuesField(name, value);
            fields.add(field);
        }
        if (store) {
            //要存储值，必须添加一个同名的StoredField
            field = new StoredField(name, value);
            fields.add(field);
        }
        return fields;
    }

    public static SortField getRandomSortField() {
        return new SortField(""
                , new FieldComparatorSource() {
            @Override
            public FieldComparator<?> newComparator(String fieldname, int numHits, int sortPos, boolean reversed) {
                return new FieldComparator.TermValComparator(numHits, fieldname, reversed) {
                    @Override
                    public int compareValues(BytesRef val1, BytesRef val2) {
                        return RANDOM.nextBoolean() ? 1 : -1;
                    }
                };
            }
        });
    }

    public enum DIRECTORY_TYPE {
        FS,
        MMAP,
        RAM
    }

    public static DirectoryReader newReader(String lucenePath) throws IOException {
        return newReader(lucenePath, DIRECTORY_TYPE.FS);
    }

    public static DirectoryReader newReader(String lucenePath, DIRECTORY_TYPE type) throws IOException {
        FSDirectory fsDirectory = type.equals(DIRECTORY_TYPE.MMAP) ?
                MMapDirectory.open(Paths.get(lucenePath))
                : FSDirectory.open(Paths.get(lucenePath));
        Directory directory = fsDirectory;
        if (type.equals(DIRECTORY_TYPE.RAM)) {
            directory = new RAMDirectory(fsDirectory, IOContext.READONCE);
            fsDirectory.close();
        }
        return DirectoryReader.open(directory);
    }

    public static IndexSearcher newSearcher(DirectoryReader reader) {
        return newSearcher(reader, true);
    }

    public static IndexSearcher newSearcher(DirectoryReader reader, boolean bm25) {
        IndexSearcher searcher = new IndexSearcher(reader);
        if (bm25) {
            searcher.setSimilarity(new BM25Similarity());
        }
        return searcher;
    }
}
