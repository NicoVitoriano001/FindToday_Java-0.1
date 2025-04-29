// Arquivo: SearchHelper.java
package com.app.fintoday;

public class SearchHelper {
    public static String formatSearchTerm(String term) {
        if (term == null || term.trim().isEmpty()) {
            return "%%";
        }
        // Substitui espaços por % e limita a 3 substituições
        String formatted = term.trim();
        int spaceCount = 0;
        for (int i = 0; i < formatted.length() && spaceCount < 3; i++) {
            if (formatted.charAt(i) == ' ') {
                formatted = formatted.substring(0, i) + "%" + formatted.substring(i + 1);
                spaceCount++;
            }
        }
        return "%" + formatted + "%";
    }
}