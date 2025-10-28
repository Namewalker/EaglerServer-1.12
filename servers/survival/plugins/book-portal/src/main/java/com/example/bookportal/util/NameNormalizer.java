package com.example.bookportal.util;

public class NameNormalizer {
    
    public static String normalize(String bookName) {
        if (bookName == null || bookName.isEmpty()) {
            return "default"; // Default name for empty or null book names
        }
        
        // Normalize the book name by trimming whitespace and converting to lowercase
        String normalized = bookName.trim().toLowerCase();
        
        // Replace spaces with underscores for easier processing
        normalized = normalized.replace(" ", "_");
        
        // Additional normalization rules can be added here
        
        return normalized;
    }
}