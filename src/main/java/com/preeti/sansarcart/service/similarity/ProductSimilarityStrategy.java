package com.preeti.sansarcart.service.similarity;

public interface ProductSimilarityStrategy {
    double computeSimilarity(String text1, String text2);
}
