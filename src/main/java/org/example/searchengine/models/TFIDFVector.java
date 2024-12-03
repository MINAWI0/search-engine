package org.example.searchengine.models;


import jakarta.persistence.*;
import lombok.Data;

import java.util.Map;

@Entity
@Data
public class TFIDFVector {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String documentId;

    @ElementCollection(fetch = FetchType.LAZY)
    @MapKeyColumn(name = "term")
    @Column(name = "tfidf_value")
    private Map<String, Double> tfidfVector;
}
