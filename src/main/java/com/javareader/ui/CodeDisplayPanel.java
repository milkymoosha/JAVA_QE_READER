package com.javareader.ui;

import com.javareader.logic.CodeAnalyzer;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.layout.Region;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
/**
 * Panel for displaying Java code with syntax highlighting and violation markers (word-level)
 */
public class CodeDisplayPanel extends VBox {

    private final ScrollPane scrollPane;
    private final VBox codeLinesBox;
    private final TextArea editTextArea;
    private final Label fileNameLabel;
    private Path currentFilePath;
    private List<String> originalLines;
    private Map<Integer, List<CodeAnalyzer.Violation>> violationsByLine;
    private boolean isEditMode = false;
    private int highlightedLine = -1;
    private final StackPane codeAreaStack;

    public CodeDisplayPanel() {
        this.codeLinesBox = new VBox(0);
        this.editTextArea = new TextArea();
        this.scrollPane = new ScrollPane(codeLinesBox);
        this.fileNameLabel = new Label("No file selected");
        this.codeAreaStack = new StackPane(scrollPane, editTextArea);
        this.highlightedLine = -1;
        setupCodeDisplay();
        setupLayout();
    }

    private void setupCodeDisplay() {
        codeLinesBox.setStyle("-fx-background-color: #f8f8f8;");
        editTextArea.setFont(Font.font("Consolas", FontWeight.NORMAL, 12));
        editTextArea.setStyle("-fx-background-color: #f8f8f8; -fx-text-fill: #333; -fx-control-inner-background: #f8f8f8;");
        editTextArea.setEditable(true);
        editTextArea.setWrapText(false);
        editTextArea.setVisible(false);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
    }

    private void setupLayout() {
        setSpacing(5);
        setPadding(new Insets(10));
        fileNameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #333;");
        getChildren().addAll(fileNameLabel, codeAreaStack);
        VBox.setVgrow(codeAreaStack, Priority.ALWAYS);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        VBox.setVgrow(editTextArea, Priority.ALWAYS);
        codeAreaStack.setMinHeight(0);
        codeAreaStack.setMaxHeight(Double.MAX_VALUE);
        codeAreaStack.setPrefHeight(Region.USE_COMPUTED_SIZE);
        scrollPane.setMinHeight(0);
        scrollPane.setMaxHeight(Double.MAX_VALUE);
        scrollPane.setPrefHeight(Region.USE_COMPUTED_SIZE);
        editTextArea.setMinHeight(0);
        editTextArea.setMaxHeight(Double.MAX_VALUE);
        editTextArea.setPrefHeight(Region.USE_COMPUTED_SIZE);
        // Only one should be visible at a time
        scrollPane.setVisible(true);
        editTextArea.setVisible(false);
    }

    /**
     * Displays the code with word-level violation highlighting
     */
    public void displayCodeWithViolations(CodeAnalyzer.AnalysisResult result, Path filePath) {
        try {
            // Clear everything first to prevent duplicates
            clear();
            
            this.currentFilePath = filePath;
            this.originalLines = Files.readAllLines(filePath);
            this.violationsByLine = result.getViolations().stream()
                    .collect(Collectors.groupingBy(CodeAnalyzer.Violation::getLineNumber));
            
            fileNameLabel.setText("File: " + filePath.getFileName());
            
            // Ensure the codeLinesBox is completely empty
            codeLinesBox.getChildren().clear();
            codeLinesBox.setStyle("-fx-background-color: #f8f8f8;");

            for (int i = 0; i < originalLines.size(); i++) {
                int lineNumber = i + 1;
                String line = originalLines.get(i);
                List<CodeAnalyzer.Violation> lineViolations = violationsByLine.get(lineNumber);

                TextFlow textFlow = new TextFlow();
                textFlow.setLineSpacing(0.0);

                // Add line number
                Text lineNum = new Text(String.format("%3d: ", lineNumber));
                lineNum.setFont(Font.font("Consolas", FontWeight.BOLD, 12));
                // Default style for line number
                lineNum.setStyle("-fx-fill: #666;");
                // Highlight line number if this is the highlighted line (display or edit mode)
                if (highlightedLine == lineNumber) {
                    lineNum.setStyle("-fx-fill: white; -fx-font-weight: bold; -fx-background-color: #2196F3; -fx-padding: 2 8 2 8; -fx-background-radius: 6;");
                }
                textFlow.getChildren().add(lineNum);

                // Add the actual line content
                textFlow.getChildren().add(makeNormalText(line));

                if (!isEditMode) {
                    javafx.scene.layout.HBox lineBox = new javafx.scene.layout.HBox(textFlow);
                    lineBox.setMinHeight(24);
                    lineBox.setPrefWidth(Double.MAX_VALUE);
                    javafx.scene.layout.HBox.setHgrow(textFlow, javafx.scene.layout.Priority.ALWAYS);
                    StringBuilder style = new StringBuilder();
                    // 1. Violation background color
                    if (lineViolations != null && !lineViolations.isEmpty()) {
                        CodeAnalyzer.ViolationType mainType = getMostSevereViolation(lineViolations);
                        style.append("-fx-background-color: ").append(getLineHighlightColor(mainType)).append(";");
                    }
                    // 2. Highlighted line: always add blue border and light blue background if no violation
                    if (highlightedLine == lineNumber) {
                        if (lineViolations == null || lineViolations.isEmpty()) {
                            style.append("-fx-background-color: #E3F2FD;");
                        }
                        style.append("-fx-border-color: #2196F3; -fx-border-width: 3; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 4 8 4 8;");
                    } else {
                        style.append("-fx-background-radius: 8; -fx-padding: 4 8 4 8;");
                    }
                    lineBox.setStyle(style.toString());
                    codeLinesBox.getChildren().add(lineBox);
                }
            }
            
            // Switch to display mode
            setEditMode(false);
            
        } catch (IOException e) {
            codeLinesBox.getChildren().setAll(new Label("Error reading file: " + e.getMessage()));
        }
    }

    private Text makeNormalText(String s) {
        Text t = new Text(s);
        t.setFont(Font.font("Consolas", FontWeight.NORMAL, 12));
        t.setStyle("-fx-fill: #333;");
        return t;
    }

    /**
     * Clears the code display
     */
    public void clear() {
        // Clear all children and reset state
        codeLinesBox.getChildren().clear();
        editTextArea.clear();
        fileNameLabel.setText("No file selected");
        currentFilePath = null;
        originalLines = null;
        violationsByLine = null;
        highlightedLine = -1;
        setEditMode(false);
        
        // Force layout update
        codeLinesBox.requestLayout();
        if (codeLinesBox.getParent() != null) {
            codeLinesBox.getParent().requestLayout();
        }
    }

    /**
     * Scrolls to the given line and highlights it with a blue background
     */
    public void scrollToLineAndHighlight(int lineNumber, CodeAnalyzer.ViolationType type) {
        if (lineNumber <= 0 || lineNumber > codeLinesBox.getChildren().size()) return;

        if (isEditMode) {
            // Remove highlight from all line numbers
            for (javafx.scene.Node node : codeLinesBox.getChildren()) {
                if (node instanceof TextFlow) {
                    TextFlow tf = (TextFlow) node;
                    if (!tf.getChildren().isEmpty() && tf.getChildren().get(0) instanceof Text) {
                        Text lineNum = (Text) tf.getChildren().get(0);
                        // Reset to default style
                        lineNum.setStyle("-fx-fill: #666;");
                    }
                }
            }
            // Highlight the line number of the selected line
            TextFlow tf = (TextFlow) codeLinesBox.getChildren().get(lineNumber - 1);
            if (!tf.getChildren().isEmpty() && tf.getChildren().get(0) instanceof Text) {
                Text lineNum = (Text) tf.getChildren().get(0);
                // Set highlight style (blue background, white text)
                lineNum.setStyle("-fx-fill: white; -fx-font-weight: bold; -fx-background-color: #2196F3; -fx-padding: 2 6 2 6; -fx-background-radius: 4;");
            }
            highlightLineInTextArea(lineNumber);
        } else {
            // In display mode, set highlightedLine and refresh the code display
            this.highlightedLine = lineNumber;
            // Rebuild the code display to show the border
            if (violationsByLine != null && currentFilePath != null) {
                // Reconstruct the analysis result from violationsByLine
                java.util.List<com.javareader.logic.CodeAnalyzer.Violation> allViolations = new java.util.ArrayList<>();
                for (java.util.List<com.javareader.logic.CodeAnalyzer.Violation> vlist : violationsByLine.values()) {
                    allViolations.addAll(vlist);
                }
                com.javareader.logic.CodeAnalyzer.AnalysisResult result = new com.javareader.logic.CodeAnalyzer.AnalysisResult(allViolations);
                displayCodeWithViolations(result, currentFilePath);
            }
            double total = codeLinesBox.getChildren().size();
            double frac = (lineNumber - 1) / Math.max(1.0, total - 1);
            scrollPane.setVvalue(frac);
        }
    }
    
    private void highlightLineInTextArea(int lineNumber) {
        if (lineNumber <= 0 || originalLines == null || lineNumber > originalLines.size()) return;
        
        // Calculate the position in the TextArea
        int position = 0;
        for (int i = 0; i < lineNumber - 1; i++) {
            position += String.format("%3d: %s\n", i + 1, originalLines.get(i)).length();
        }
        
        // Select the entire line
        int lineStart = position;
        int lineEnd = position + String.format("%3d: %s", lineNumber, originalLines.get(lineNumber - 1)).length();
        
        editTextArea.selectRange(lineStart, lineEnd);
        editTextArea.requestFocus();
        
        // Calculate scroll position
        double totalLines = originalLines.size();
        double targetLine = lineNumber - 1;
        double scrollValue = targetLine / Math.max(1.0, totalLines - 1);
        // Note: TextArea doesn't have direct scroll control, so we'll just select the text
    }

    /**
     * Switches between display and edit modes
     */
    public void setEditMode(boolean editMode) {
        this.isEditMode = editMode;
        if (editMode) {
            scrollPane.setVisible(false);
            editTextArea.setVisible(true);
            // Only populate ONCE, clear before setting
            editTextArea.clear();
            StringBuilder codeBuilder = new StringBuilder();
            for (int i = 0; i < originalLines.size(); i++) {
                int lineNumber = i + 1;
                String line = originalLines.get(i);
                // Only append line number and code, no color comments
                codeBuilder.append(String.format("%3d: %s\n", lineNumber, line));
            }
            editTextArea.setText(codeBuilder.toString());
        } else {
            scrollPane.setVisible(true);
            editTextArea.setVisible(false);
            // Force refresh of highlighted line in display mode
            if (highlightedLine > 0) {
                // Rebuild the code display to show the border
                if (violationsByLine != null && currentFilePath != null) {
                    java.util.List<com.javareader.logic.CodeAnalyzer.Violation> allViolations = new java.util.ArrayList<>();
                    for (java.util.List<com.javareader.logic.CodeAnalyzer.Violation> vlist : violationsByLine.values()) {
                        allViolations.addAll(vlist);
                    }
                    com.javareader.logic.CodeAnalyzer.AnalysisResult result = new com.javareader.logic.CodeAnalyzer.AnalysisResult(allViolations);
                    displayCodeWithViolations(result, currentFilePath);
                }
            }
        }
        this.requestLayout();
        if (this.getParent() != null && this.getParent() instanceof VBox) {
            ((VBox)this.getParent()).requestLayout();
        }
    }

    /**
     * Saves the current content to the file
     */
    public boolean saveFile() {
        if (currentFilePath == null) return false;
        try {
            String content;
            if (isEditMode) {
                content = editTextArea.getText();
            } else {
                // Reconstruct content from display mode
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < originalLines.size(); i++) {
                    sb.append(originalLines.get(i)).append("\n");
                }
                content = sb.toString();
            }
            // Remove line numbers (with any spaces) before saving
            String[] lines = content.split("\n");
            StringBuilder cleanContent = new StringBuilder();
            for (String line : lines) {
                // Remove leading spaces, digits, colon, and space (e.g., '  1: ')
                cleanContent.append(line.replaceFirst("^\\s*\\d+:\\s", "")).append("\n");
            }
            Files.write(currentFilePath, cleanContent.toString().getBytes());
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    
    /**
     * Gets the current file path
     */
    public Path getCurrentFilePath() {
        return currentFilePath;
    }

    // Returns the most severe violation type (priority order)
    private CodeAnalyzer.ViolationType getMostSevereViolation(List<CodeAnalyzer.Violation> violations) {
        if (violations == null || violations.isEmpty()) return null;
        List<CodeAnalyzer.ViolationType> priority = List.of(
            CodeAnalyzer.ViolationType.LINE_TOO_LONG,
            CodeAnalyzer.ViolationType.IMPROPER_INDENTATION,
            CodeAnalyzer.ViolationType.REPEATED_STRING,
            CodeAnalyzer.ViolationType.EMPTY_LINE,
            CodeAnalyzer.ViolationType.NAMING_CONVENTION
        );
        for (CodeAnalyzer.ViolationType t : priority) {
            for (CodeAnalyzer.Violation v : violations) {
                if (v.getType() == t) return t;
            }
        }
        return violations.get(0).getType();
    }

    // Returns a solid, bold color for line highlight
    private String getLineHighlightColor(CodeAnalyzer.ViolationType type) {
        if (type == null) return "transparent";
        switch (type) {
            case LINE_TOO_LONG: return "#FF1744"; // Bold Red
            case IMPROPER_INDENTATION: return "#FF9100"; // Bold Orange
            case REPEATED_STRING: return "#2979FF"; // Bold Blue
            case EMPTY_LINE: return "#FFD600"; // Bold Yellow
            case NAMING_CONVENTION: return "#00C853"; // Bold Green
            default: return "transparent";
        }
    }
} 