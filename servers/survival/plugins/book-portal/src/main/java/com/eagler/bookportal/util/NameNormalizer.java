package com.eagler.bookportal.util;

public class NameNormalizer {
    public static String normalize(String s) {
        if (s == null) return null;
        s = s.trim();
        if (s.isEmpty()) return "";
        // lower-case, replace non-alphanum with underscore, collapse underscores
        String t = s.toLowerCase().replaceAll("[^a-z0-9]+", "_");
        t = t.replaceAll("_+", "_");
        if (t.startsWith("_")) t = t.substring(1);
        if (t.endsWith("_")) t = t.substring(0, t.length()-1);
        return t;
    }
}
