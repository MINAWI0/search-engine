package org.example.searchengine;

import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Paths;

@Service
public class Searcher {

    private final IndexSearcher indexSearcher;
    private final QueryParser queryParser;
    public Searcher(@Value("${index.directory.path}") String indexDirectoryPath) throws IOException {
        Directory indexDirectory = FSDirectory.open(Paths.get(indexDirectoryPath));
        IndexReader reader = DirectoryReader.open(indexDirectory);
        indexSearcher = new IndexSearcher(reader);
        queryParser = new QueryParser("contents", new EnglishAnalyzer());
    }

    public TopDocs search(String searchQuery) throws IOException, ParseException {
        Query query = queryParser.parse(searchQuery);
        return indexSearcher.search(query, 10);
    }

    public Document getDocument(ScoreDoc scoreDoc) throws IOException {
        return indexSearcher.doc(scoreDoc.doc);
    }
}
