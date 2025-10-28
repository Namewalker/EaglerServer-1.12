package com.example.bookportal.util;

import java.util.ArrayList;
import java.util.List;

public class SpellChecker {

    private static final List<String> VALID_NAMES = List.of("Adventure", "Mystery", "Fantasy", "Sci-Fi", "Horror");

    public static List<String> checkSpelling(String input) {
        List<String> suggestions = new ArrayList<>();
        for (String validName : VALID_NAMES) {
            if (isSimilar(input, validName)) {
                suggestions.add(validName);
            }
        }
        return suggestions;
    }

    private static boolean isSimilar(String input, String validName) {
        // Simple similarity check based on length and character matching
        int maxLength = Math.max(input.length(), validName.length());
        int distance = levenshteinDistance(input.toLowerCase(), validName.toLowerCase());
        return distance <= maxLength / 2; // Allow a certain distance for similarity
    }

    private static int levenshteinDistance(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];
        for (int i = 0; i <= a.length(); i++) {
            for (int j = 0; j <= b.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j; // Deletion
                } else if (j == 0) {
                    dp[i][j] = i; // Insertion
                } else {
                    dp[i][j] = Math.min(dp[i - 1][j] + 1, Math.min(dp[i][j - 1] + 1,
                            dp[i - 1][j - 1] + (a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1)));
                }
            }
        }
        return dp[a.length()][b.length()];
    }
}