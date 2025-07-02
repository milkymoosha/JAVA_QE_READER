package com.example;

import java.util.List;
import java.util.ArrayList;

public class SampleClass {
    
    @SuppressWarnings("unused")
    private String very_long_variable_name_that_exceeds_the_maximum_line_length_and_should_be_flagged_by_the_analyzer;
    
    @SuppressWarnings("unused")
    private static final String REPEATED_STRING = "This is a repeated string that appears multiple times in the code";
    
    public void method_with_poor_naming() {
        String another_repeated_string = "This is a repeated string that appears multiple times in the code";
        
        if (true) {
                String improperly_indented_variable = "This line has too many spaces for indentation";
        }
        
        String normalVariable = "This follows camelCase convention";
        
        // This line is intentionally very long to test the line length checker: Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.
        
        List<String> list = new ArrayList<>();
        list.add("This is a repeated string that appears multiple times in the code");
        
        String third_occurrence = "This is a repeated string that appears multiple times in the code";
        
    }
    
    public void anotherMethod() {
        String variable_with_underscores = "This should be camelCase";
        
        if (true) {
            String properlyIndented = "This is correctly indented";
        }
    }
    
} 