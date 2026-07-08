package com.netease.easyml.common.util.lucene;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.netease.easyml.common.util.IOUtil;
import com.netease.easyml.common.util.StringUtil;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by linjiuning on 2019/8/28.
 */
public class LuceneIndex {
    private static final Logger log = LoggerFactory.getLogger(LuceneIndex.class);
    private static final String DOC_ID = "doc_id";

    public static void build(String sourcePath, String lucenePath, LuceneConfig config) {
        build(sourcePath, lucenePath, config, 10000);
    }

    public static void build(String sourcePath, String lucenePath, LuceneConfig config, int batchSize) {
        if (IOUtil.exists(lucenePath)) {
            log.warn(String.format("path=%s already exist...", lucenePath));
        } else {
            IOUtil.mkdirs(lucenePath);
        }

        Analyzer analyzer = new WhitespaceAnalyzer();

        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);

        indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);

        long startTime = System.currentTimeMillis();

        try (Directory directory = FSDirectory.open(Paths.get(lucenePath));
             IndexWriter indexWriter = new IndexWriter(directory, indexWriterConfig)) {
            List<Document> docs = new ArrayList<>();
            try (BufferedReader reader = IOUtil.getBufferedReader(sourcePath)) {
                String line = "";
                while ((line = reader.readLine()) != null) {
                    JSONObject jsonObject = JSON.parseObject(line);
                    Map map = jsonObject.toJavaObject(Map.class);

                    Document doc = config.document(map);

                    String key;
                    String docId;
                    if (!StringUtil.isEmpty(config.getId())) {
                        key = config.getId();
                        docId = jsonObject.getString(key);
                    } else {
                        key = DOC_ID;
                        docId = StringUtil.md5(line);
                    }
                    indexWriter.updateDocument(new Term(key, docId), doc);
                    docs.add(doc);
                    if (docs.size() == batchSize) {
                        indexWriter.commit();
                        docs = new ArrayList<>();
                    }
                }
            }

            if (!docs.isEmpty()) {
                indexWriter.commit();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        long endTime = System.currentTimeMillis();
        System.out.println("Cost: " + (endTime - startTime));
    }
}
