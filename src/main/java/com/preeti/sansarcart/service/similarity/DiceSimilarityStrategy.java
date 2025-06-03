package com.preeti.sansarcart.service.similarity;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/*
    Dice Similarity Strategy checks the overlapping token present in the given string
    here the given string consist of product name and product variations metadata

    Dice(A, B) = 2 × |A ∩ B| / (|A| + |B|)

    Dice coefficient weighs the commonalities a little bit more than the differences
    This approach gives higher weight to the overlap (commonality) between the
    product names and metadata
 */

@Component
@Primary
public class DiceSimilarityStrategy implements ProductSimilarityStrategy {

    @Override
    public double computeSimilarity(String text1, String text2) {
        Set<String> set1 = new HashSet<>(Arrays.asList(text1.toLowerCase().split("\\s+")));
        Set<String> set2 = new HashSet<>(Arrays.asList(text2.toLowerCase().split("\\s+")));

        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);

        if (set1.isEmpty() && set2.isEmpty()) return 1.0;
        if (set1.isEmpty() || set2.isEmpty()) return 0.0;

        return (2.0 * intersection.size()) / (set1.size() + set2.size());
    }
}
