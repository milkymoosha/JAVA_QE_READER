public class SampleClass {
    private String veryLongVariableNameThatExceedsTheMaximumAllowedLengthForCamelCaseNamingConventionAndShouldBeFlaggedAsAViolation;
    
    public void methodWithImproperIndentation() {
        String variableName = "test";
        if (variableName.equals("test")) {
                System.out.println("This line has improper indentation - it should be indented by 2 spaces from the if statement, not 4");
        }
    }
    
    public void anotherMethod() {
        String repeatedString = "This string is repeated multiple times";
        System.out.println(repeatedString);
        System.out.println("This string is repeated multiple times");
        System.out.println("This string is repeated multiple times");
    }
    
    public void methodWithLongLine() {
        System.out.println("This is a very long line that exceeds the maximum allowed length of 120 characters and should be flagged as a violation in the code analysis");
    }
    
    public void methodWithNonCamelCaseVariable() {
        String VariableName = "This variable name does not follow camelCase convention";
        String CONSTANT_NAME = "This should be a constant";
    }
    
    public void methodWithEmptyLines() {
        String test = "test";
        
        
        System.out.println(test);
    }
} 