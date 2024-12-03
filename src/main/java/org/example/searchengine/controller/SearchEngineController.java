package org.example.searchengine.controller;

import lombok.RequiredArgsConstructor;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.TopDocs;
import org.apache.tika.exception.TikaException;
import org.example.searchengine.FiletypeFilter;
import org.example.searchengine.Indexer;
import org.example.searchengine.Searcher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchEngineController {

    private final Indexer indexer;
    private final Searcher searcher;

    @Value("${index.directory.path}")
    private String indexDirPath;

    @Value("${data.directory.path}")
    private String dataDirPath;

    // Indexer and Searcher are already injected via constructor, no need for custom constructor
    // @Autowired will be used implicitly because of @RequiredArgsConstructor

    // Endpoint to index documents
    @PostMapping("/index")
    public String indexDocuments() {
        try {
            long startTime = System.currentTimeMillis();
            int numIndexed = indexer.createIndex(dataDirPath, new FiletypeFilter());
            long endTime = System.currentTimeMillis();
            indexer.close();
            return numIndexed + " files indexed in: " + (endTime - startTime) + " ms";
        } catch (IOException e) {
            return "Error while indexing documents: " + e.getMessage();
        } catch (TikaException e) {
            throw new RuntimeException(e);
        }
    }

    // Endpoint to search for documents
    @GetMapping("/search")
    public String searchDocuments(@RequestParam String query) {
        try {
            long startTime = System.currentTimeMillis();
            TopDocs hits = searcher.search(query);
            long endTime = System.currentTimeMillis();

            StringBuilder response = new StringBuilder();
            response.append(hits.totalHits).append(hits.totalHits == 1 ? " document found. Time: " : " documents found. Time:")
                    .append((endTime - startTime)).append(" ms\n");

            for (var scoreDoc : hits.scoreDocs) {
                var doc = searcher.getDocument(scoreDoc);
                response.append("File: ").append(doc.get("filepath")).append("\n");
            }
            return response.toString();
        } catch (IOException | ParseException e) {
            return "Error while searching documents: " + e.getMessage();
        }
    }
}
