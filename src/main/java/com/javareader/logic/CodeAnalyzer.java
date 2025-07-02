package com.javareader.logic;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Main code analyzer that processes Java files and applies various code review rules
 */
public class CodeAnalyzer {
    
    private final RuleChecker ruleChecker;
    private final HighlightUtil highlightUtil;
    
    public CodeAnalyzer() {
        this.ruleChecker = new RuleChecker();
        this.highlightUtil = new HighlightUtil();
    }
    
    /**
     * Analyzes a Java file for code violations
     */
    public AnalysisResult analyzeFile(Path filePath) throws IOException {
        if (!filePath.toString().toLowerCase().endsWith(".java")) {
            throw new IllegalArgumentException("File must be a .java file");
        }
        
        List<String> lines = Files.readAllLines(filePath);
        List<Violation> violations = new ArrayList<>();
        
        // Check each line for violations
        int i = 0;
        while (i < lines.size()) {
            String line = lines.get(i);
            int lineNumber = i + 1;
            
            // Check line length
            if (ruleChecker.checkLineLength(line)) {
                violations.add(new Violation(ViolationType.LINE_TOO_LONG, lineNumber, line));
            }
            
            // Check indentation (now requires all lines and current index)
            if (ruleChecker.checkIndentation(line, lines, i)) {
                // Mark this line and all subsequent lines with the same indentation as improper
                int badIndent = ruleChecker.getIndentationLevel(line);
                int j = i;
                while (j < lines.size() && ruleChecker.getIndentationLevel(lines.get(j)) == badIndent && !lines.get(j).trim().isEmpty()) {
                    int badLineNumber = j + 1;
                    // Only add if not already present
                    boolean alreadyAdded = violations.stream().anyMatch(v -> v.getType() == ViolationType.IMPROPER_INDENTATION && v.getLineNumber() == badLineNumber);
                    if (!alreadyAdded) {
                        violations.add(new Violation(ViolationType.IMPROPER_INDENTATION, badLineNumber, lines.get(j)));
                    }
                    j++;
                }
                i = j; // Skip to the next line with different indentation
                continue;
            }
            
            // Check consecutive empty lines
            if (ruleChecker.checkConsecutiveEmptyLines(lines, i)) {
                violations.add(new Violation(ViolationType.EMPTY_LINE, lineNumber, line));
            }
            
            // Check naming conventions
            if (ruleChecker.checkNamingConventions(line)) {
                violations.add(new Violation(ViolationType.NAMING_CONVENTION, lineNumber, line));
            }
            i++;
        }
        
        // Check for repeated string literals
        Map<String, List<Integer>> repeatedStrings = ruleChecker.findRepeatedStrings(lines);
        for (Map.Entry<String, List<Integer>> entry : repeatedStrings.entrySet()) {
            for (Integer lineNum : entry.getValue()) {
                violations.add(new Violation(ViolationType.REPEATED_STRING, lineNum, lines.get(lineNum - 1)));
            }
        }
        
        return new AnalysisResult(violations);
    }
    
    /**
     * Gets the highlighting utility for UI display
     */
    public HighlightUtil getHighlightUtil() {
        return highlightUtil;
    }
    
    /**
     * Analysis result containing all violations found
     */
    public static class AnalysisResult {
        private final Map<ViolationType, Integer> violationCounts;
        private final List<Violation> violations;
        
        public AnalysisResult(List<Violation> violations) {
            this.violationCounts = new HashMap<>();
            this.violations = new ArrayList<>(violations);
            for (Violation violation : violations) {
                violationCounts.put(violation.getType(), violationCounts.getOrDefault(violation.getType(), 0) + 1);
            }
        }
        
        public Map<ViolationType, Integer> getViolationCounts() {
            return violationCounts;
        }
        
        public List<Violation> getViolations() {
            return violations;
        }
        
        public List<Violation> getViolationsByType(ViolationType type) {
            return violations.stream()
                .filter(v -> v.getType() == type)
                .collect(Collectors.toList());
        }
    }
    
    /**
     * Represents a single code violation
     */
    public static class Violation {
        private final ViolationType type;
        private final int lineNumber;
        private final String line;
        
        public Violation(ViolationType type, int lineNumber, String line) {
            this.type = type;
            this.lineNumber = lineNumber;
            this.line = line;
        }
        
        public ViolationType getType() { return type; }
        public int getLineNumber() { return lineNumber; }
        public String getLine() { return line; }
    }
    
    /**
     * Types of code violations
     */
    public enum ViolationType {
        LINE_TOO_LONG("Line exceeds 120 characters"),
        IMPROPER_INDENTATION("Improper indentation"),
        REPEATED_STRING("Repeated string literal"),
        EMPTY_LINE("Empty line"),
        NAMING_CONVENTION("Naming convention violation");
        
        private final String description;
        
        ViolationType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
} 