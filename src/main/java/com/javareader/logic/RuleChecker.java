package com.javareader.logic;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Implements specific code review rules for Java files
 */
public class RuleChecker {
    
    private static final int MAX_LINE_LENGTH = 120;
    private static final int MAX_INDENTATION_SPACES = 2;
    private static final Pattern CAMEL_CASE_PATTERN = Pattern.compile("^[a-z][a-zA-Z0-9]*$");
    private static final Pattern STRING_LITERAL_PATTERN = Pattern.compile("\"([^\"]*)\"");
    
    /**
     * Checks if a line exceeds the maximum allowed length
     */
    public boolean checkLineLength(String line) {
        return line.length() > MAX_LINE_LENGTH;
    }
    
    /**
     * Checks if a line has improper indentation
     * Compares with the previous line: should have 0 or 2 spaces difference
     */
    public boolean checkIndentation(String line, List<String> allLines, int currentLineIndex) {
        if (line.trim().isEmpty()) {
            return false; // Empty lines don't have indentation issues
        }
        
        // Find the nearest previous non-empty line
        int prevIndex = currentLineIndex - 1;
        while (prevIndex >= 0 && allLines.get(prevIndex).trim().isEmpty()) {
            prevIndex--;
        }
        if (prevIndex < 0) {
            return false; // No non-empty previous line, so no indentation check
        }
        String previousLine = allLines.get(prevIndex);
        
        // Get indentation levels (number of leading spaces)
        int currentIndentation = getIndentationLevel(line);
        int previousIndentation = getIndentationLevel(previousLine);
        
        // Calculate the difference in indentation
        int indentationDifference = currentIndentation - previousIndentation;
        
        // Proper indentation: 
        // - 0 spaces (same level)
        // - +2 spaces (one level deeper) 
        // - Any negative difference (going back/outdenting)
        // Any other positive difference is improper
        return indentationDifference > 2;
    }
    
    /**
     * Gets the indentation level of a line (number of leading spaces)
     */
    public int getIndentationLevel(String line) {
        int spaces = 0;
        for (char c : line.toCharArray()) {
            if (c == ' ') {
                spaces++;
            } else if (c == '\t') {
                spaces += 4; // Assume tab = 4 spaces
            } else {
                break;
            }
        }
        return spaces;
    }
    
    /**
     * Checks if a line is empty or contains only whitespace
     */
    public boolean checkEmptyLine(String line) {
        return line.trim().isEmpty();
    }
    
    /**
     * Checks if variable/method names follow camelCase conventions
     */
    public boolean checkNamingConventions(String line) {
        // Look for variable declarations and method calls
        String trimmedLine = line.trim();
        
        // Check for variable declarations: type variableName = value;
        if (trimmedLine.contains("=") && !trimmedLine.contains("==")) {
            String[] parts = trimmedLine.split("=");
            if (parts.length >= 2) {
                String beforeEquals = parts[0].trim();
                String[] beforeParts = beforeEquals.split("\\s+");
                if (beforeParts.length >= 2) {
                    String variableName = beforeParts[beforeParts.length - 1];
                    if (!isValidCamelCase(variableName)) {
                        return true;
                    }
                }
            }
        }
        
        // Check for method calls: methodName(
        if (trimmedLine.contains("(") && !trimmedLine.contains("if") && 
            !trimmedLine.contains("for") && !trimmedLine.contains("while")) {
            String beforeParen = trimmedLine.substring(0, trimmedLine.indexOf("(")).trim();
            String[] parts = beforeParen.split("\\s+");
            if (parts.length > 0) {
                String methodName = parts[parts.length - 1];
                if (!isValidCamelCase(methodName) && !methodName.contains(".")) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Validates if a name follows camelCase convention
     */
    private boolean isValidCamelCase(String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }
        
        // Remove any trailing characters that might be part of the declaration
        name = name.replaceAll("[;,\\s]+$", "");
        
        // Check if it's a valid camelCase identifier
        return CAMEL_CASE_PATTERN.matcher(name).matches();
    }
    
    /**
     * Finds repeated string literals in the code
     */
    public Map<String, List<Integer>> findRepeatedStrings(List<String> lines) {
        Map<String, List<Integer>> stringOccurrences = new HashMap<>();
        Map<String, List<Integer>> repeatedStrings = new HashMap<>();
        
        // Find all string literals and their line numbers
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            java.util.regex.Matcher matcher = STRING_LITERAL_PATTERN.matcher(line);
            
            while (matcher.find()) {
                String stringLiteral = matcher.group(1);
                // Skip empty strings and very short strings (likely not meaningful)
                if (stringLiteral.length() > 2) {
                    stringOccurrences.computeIfAbsent(stringLiteral, k -> new ArrayList<>())
                        .add(i + 1); // Line numbers are 1-indexed
                }
            }
        }
        
        // Find strings that appear more than once
        for (Map.Entry<String, List<Integer>> entry : stringOccurrences.entrySet()) {
            if (entry.getValue().size() >= 2) {
                repeatedStrings.put(entry.getKey(), entry.getValue());
            }
        }
        
        return repeatedStrings;
    }
    
    /**
     * Suggests a constant name for a repeated string
     */
    public String suggestConstantName(String stringLiteral) {
        if (stringLiteral == null || stringLiteral.isEmpty()) {
            return "EMPTY_STRING";
        }
        
        // Convert to uppercase and replace spaces/special chars with underscores
        String constantName = stringLiteral
            .toUpperCase()
            .replaceAll("[^a-zA-Z0-9]", "_")
            .replaceAll("_+", "_")
            .replaceAll("^_|_$", "");
        
        // Ensure it starts with a letter
        if (!Character.isLetter(constantName.charAt(0))) {
            constantName = "STRING_" + constantName;
        }
        
        return constantName;
    }
    
    /**
     * Checks if there are two or more consecutive empty lines (for highlighting)
     */
    public boolean checkConsecutiveEmptyLines(List<String> lines, int currentLineIndex) {
        if (lines == null || lines.size() < 2) return false;
        if (currentLineIndex == 0) return false;
        String prev = lines.get(currentLineIndex - 1).trim();
        String curr = lines.get(currentLineIndex).trim();
        return prev.isEmpty() && curr.isEmpty();
    }
    
    /**
     * Propagates improper indentation: if a line is marked as improper indentation,
     * mark all consecutive lines with the same indentation as also improper.
     * Returns a set of line indices (0-based) that should be marked as improper.
     */
    public Set<Integer> propagateImproperIndentation(List<String> lines, Set<Integer> initiallyImproper) {
        Set<Integer> allImproper = new HashSet<>(initiallyImproper);
        for (int i = 0; i < lines.size(); i++) {
            if (initiallyImproper.contains(i)) {
                int badIndent = getIndentationLevel(lines.get(i));
                int j = i + 1;
                while (j < lines.size() && getIndentationLevel(lines.get(j)) == badIndent && !lines.get(j).trim().isEmpty()) {
                    allImproper.add(j);
                    j++;
                }
            }
        }
        return allImproper;
    }
} 