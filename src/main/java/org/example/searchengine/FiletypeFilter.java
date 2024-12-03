package org.example.searchengine;


import java.io.File;
import java.io.FileFilter;

public class FiletypeFilter implements FileFilter {

    public boolean accept(File pathname) {
        return pathname.getName().toLowerCase().endsWith(".txt")
                || pathname.getName().toLowerCase().endsWith(".docx")
                || pathname.getName().toLowerCase().endsWith(".doc")
                || pathname.getName().toLowerCase().endsWith(".pdf");
    }
}