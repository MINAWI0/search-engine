package org.example.searchengine;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.example.searchengine.models.TFIDFVector;
import org.example.searchengine.repo.TFIDFVectorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Service
@Data
public class Indexer {
    private final IndexWriter writer;
    private Map<String, Integer> termFrequencyMap; // Map to track TF for each document
    private Map<String, Integer> documentFrequencyMap; // Map to track DF for each term
    @Autowired
    private TFIDFVectorRepository tfidfVectorRepository;

    public Indexer(@Value("${index.directory.path}") String indexDirectoryPath) throws IOException {
        Directory indexDirectory = FSDirectory.open(Paths.get(indexDirectoryPath));
        IndexWriterConfig iwc = new IndexWriterConfig(new EnglishAnalyzer());
        iwc.setSimilarity(new ClassicSimilarity());  // Set similarity to ClassicSimilarity (TF-IDF)
        writer = new IndexWriter(indexDirectory, iwc);
        termFrequencyMap = new HashMap<>();
        documentFrequencyMap = new HashMap<>();
    }

    public void close() throws IOException {
        writer.close();
    }

    private Document getDocument(File file) throws IOException, TikaException {
        Tika tika = new Tika();
        String filetype = tika.detect(file);
        System.out.println("Indexing: " + filetype);

        Document document = new Document();
        String content = tika.parseToString(file);
        TextField contentField = new TextField("contents", tika.parseToString(file), TextField.Store.YES);
        TextField fileNameField = new TextField("filename", file.getName(), TextField.Store.YES);
        TextField filePathField = new TextField("filepath", file.getCanonicalPath(), TextField.Store.YES);

        document.add(contentField);
        document.add(fileNameField);
        document.add(filePathField);
        updateTermFrequencies(content);
        return document;
    }

    private void indexFile(File file, int totalDocuments) throws IOException, TikaException {
        System.out.println("Indexing file: " + file.getCanonicalPath());
        Document document = getDocument(file);
        String content = document.get("contents"); // Get content from the document
        updateTermFrequencies(content);  // Update term frequencies for the content
        Map<String, Double> tfidfVector = getTFIDFVector(content, totalDocuments); // Get the TF-IDF vector

        // Store the vector in the database
        storeVectorInDatabase(file.getName(), tfidfVector);

        // Add document to the index
        writer.addDocument(document);
    }

    public int createIndex(String dataDirPath, FileFilter filter) throws IOException, TikaException {
        File[] files = new File(dataDirPath).listFiles();
        int totalDocuments = 0;

        if (files != null) {
            for (File file : files) {
                if (!file.isDirectory() && !file.isHidden() && file.exists() && file.canRead() && filter.accept(file)) {
                    totalDocuments++;
                }
            }
        }

        // Now, pass totalDocuments when indexing
        for (File file : files) {
            if (!file.isDirectory() && !file.isHidden() && file.exists() && file.canRead() && filter.accept(file)) {
                indexFile(file, totalDocuments);
            }
        }
        return writer.numDocs();
    }

    private void updateTermFrequencies(String content) {
        String[] tokens = content.split("\\s+"); // Simple tokenization
        for (String token : tokens) {
            token = token.toLowerCase().replaceAll("[^a-zA-Z]", ""); // Clean the token
            if (!token.isEmpty()) {
                // Update term frequency map for the current document
                termFrequencyMap.put(token, termFrequencyMap.getOrDefault(token, 0) + 1);
                // Update document frequency map (track number of documents each term appears in)
                documentFrequencyMap.put(token, documentFrequencyMap.getOrDefault(token, 0) + 1);
            }
        }
    }
    private double calculateIDF(String term, int totalDocuments) {
        int docFrequency = documentFrequencyMap.getOrDefault(term, 0);
        if (docFrequency == 0) return 0.0;
        return Math.log((double) totalDocuments / (docFrequency + 1)); // IDF formula
    }

    private Map<String, Double> getTFIDFVector(String content, int totalDocuments) {
        Map<String, Double> tfidfVector = new HashMap<>();
        String[] tokens = content.split("\\s+");

        for (String token : tokens) {
            token = token.toLowerCase().replaceAll("[^a-zA-Z]", ""); // Clean the token
            if (!token.isEmpty()) {
                double tf = termFrequencyMap.getOrDefault(token, 0);
                double idf = calculateIDF(token, totalDocuments);
                tfidfVector.put(token, tf * idf); // Store TF-IDF for the term
            }
        }
        return tfidfVector;
    }
    private void storeVectorAsJSON(String documentID, Map<String, Double> tfidfVector) throws IOException {
        // Use Jackson ObjectMapper to convert Map to JSON
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonString = objectMapper.writeValueAsString(tfidfVector);

        // For now, print the result to the console (You can modify this to store in a file)
        System.out.println("Document ID: " + documentID);
        System.out.println("TF-IDF Vector (JSON): " + jsonString);
    }
    private void storeVectorInDatabase(String documentID, Map<String, Double> tfidfVector) {
        // Create a new TFIDFVector entity
        TFIDFVector tfidfEntity = new TFIDFVector();
        tfidfEntity.setDocumentId(documentID);
        tfidfEntity.setTfidfVector(tfidfVector);

        // Save the entity to the database
        tfidfVectorRepository.save(tfidfEntity);

        System.out.println("Document ID: " + documentID + " TF-IDF vector stored in the database.");
    }


}
