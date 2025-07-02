package com.javareader;

import com.javareader.logic.CodeAnalyzer;
import com.javareader.logic.RuleChecker;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Test class for the CodeAnalyzer functionality
 */
public class CodeAnalyzerTest {
    
    private CodeAnalyzer codeAnalyzer;
    private RuleChecker ruleChecker;
    
    @BeforeEach
    void setUp() {
        codeAnalyzer = new CodeAnalyzer();
        ruleChecker = new RuleChecker();
    }
    
    @Test
    void testLineLengthCheck() {
        String shortLine = "This is a short line";
        String longLine = "This is a very long line that exceeds the maximum allowed length of 120 characters and should be flagged by the line length checker as a violation";
        
        assertFalse(ruleChecker.checkLineLength(shortLine));
        assertTrue(ruleChecker.checkLineLength(longLine));
    }
    
    @Test
    void testEmptyLineCheck() {
        String emptyLine = "";
        String whitespaceLine = "   ";
        String normalLine = "public class Test {";
        
        assertTrue(ruleChecker.checkEmptyLine(emptyLine));
        assertTrue(ruleChecker.checkEmptyLine(whitespaceLine));
        assertFalse(ruleChecker.checkEmptyLine(normalLine));
    }
    
    @Test
    void testNamingConventionCheck() {
        String camelCaseVariable = "camelCaseVariable";
        String underscoreVariable = "variable_with_underscores";
        // Note: This is a simplified test - the actual implementation checks for
        // variable declarations and method calls in context
        assertFalse(ruleChecker.checkNamingConventions("String " + camelCaseVariable + " = value;"));
        assertTrue(ruleChecker.checkNamingConventions("String " + underscoreVariable + " = value;"));
    }
    
    @Test
    void testRepeatedStringDetection() {
        List<String> lines = java.util.Arrays.asList(
            "String first = \"Hello World\";",
            "String second = \"Different String\";",
            "String third = \"Hello World\";",
            "String fourth = \"Another String\";",
            "String fifth = \"Hello World\";"
        );

        java.util.Map<String, java.util.List<Integer>> repeatedStrings = ruleChecker.findRepeatedStrings(lines);

        assertTrue(repeatedStrings.containsKey("Hello World"));
        assertEquals(3, repeatedStrings.get("Hello World").size());
        assertFalse(repeatedStrings.containsKey("Different String"));
    }
    
    @Test
    void testConstantNameSuggestion() {
        String input = "Hello World";
        String expected = "HELLO_WORLD";
        
        assertEquals(expected, ruleChecker.suggestConstantName(input));
    }
    
    @Test
    void testAnalyzerWithSampleFile() throws IOException {
        // Create a temporary test file
        Path tempFile = Files.createTempFile("test", ".java");
        try {
            String testCode = "package test;\n" +
                "\n" +
                "public class TestClass {\n" +
                "    private String variable_with_underscores = \"repeated string\";\n" +
                "\n" +
                "    public void method_with_poor_naming() {\n" +
                "        String another_variable = \"repeated string\";\n" +
                "\n" +
                "        if (true) {\n" +
                "                String improperly_indented = \"test\";\n" +
                "        }\n" +
                "    }\n" +
                "}\n";
            
            Files.write(tempFile, testCode.getBytes());
            
            CodeAnalyzer.AnalysisResult result = codeAnalyzer.analyzeFile(tempFile);
            
            // Should find violations
            assertTrue(result.getViolations().size() > 0);
            
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }
} 