//package org.example.searchengine;
//
//import org.apache.lucene.document.Document;
//import org.apache.lucene.queryparser.classic.ParseException;
//import org.apache.lucene.search.ScoreDoc;
//import org.apache.lucene.search.TopDocs;
//
//import java.io.BufferedReader;
//import java.io.File;
//import java.io.IOException;
//import java.io.InputStreamReader;
//
//
//
//public class LuceneTester {
//
//    private String indexDir = "/home/minaoui/Documents/web/index";
//    private String dataDir = "/home/minaoui/Documents/web";
//    private Indexer indexer;
//    private Searcher searcher;
//
//    public static void main(String[] args) {
//        LuceneTester tester;
//        try {
//            tester = new LuceneTester();
//            tester.createIndex();
//
//
//            while (true){
//                System.out.println("Ce termen cautam?");
//                BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
//                String cautare = reader.readLine();
//
//                if (cautare.length()>1){
//                    tester.search(cautare.toLowerCase().trim());
//                } else {
//                    System.out.println("Cel putin 2 caractere");
//                }
//
//            }
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void createIndex() throws IOException {
//
//        //stergem indecsii deja creati
//        File file = new File(indexDir);
//        String[] myFiles;
//        if(file.isDirectory()){
//            myFiles = file.list();
//            for (String s : myFiles) {
//                File myFile = new File(file, s);
//                myFile.delete();
//            }
//        }
//
//        indexer = new Indexer(indexDir);
//        int numIndexed;
//        long startTime = System.currentTimeMillis();
//        numIndexed = indexer.createIndex(dataDir, new FiletypeFilter());
//        long endTime = System.currentTimeMillis();
//        indexer.close();
//        System.out.println(numIndexed+" fisiere indexate in: "
//                +(endTime-startTime)+" ms");
//    }
//
//    private void search(String searchQuery) throws IOException, ParseException {
//        searcher = new Searcher(indexDir);
//        long startTime = System.currentTimeMillis();
//        TopDocs hits = searcher.search(searchQuery);
//        long endTime = System.currentTimeMillis();
//
//        System.out.println(hits.totalHits +
//                (hits.totalHits == 1 ? " document gasit. Timp :" : " documente gasite. Timp:")
//                + (endTime - startTime));
//        for(ScoreDoc scoreDoc : hits.scoreDocs) {
//            Document doc = searcher.getDocument(scoreDoc);
//            System.out.println("Fisier: " + doc.get("filepath"));
//        }
//    }
//}
