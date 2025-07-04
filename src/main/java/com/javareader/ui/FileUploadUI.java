package com.javareader.ui;

import com.javareader.logic.CodeAnalyzer;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Main UI class for the Java Code Analyzer application
 */
public class FileUploadUI extends VBox {
    
    private final CodeAnalyzer codeAnalyzer;
    private final CodeDisplayPanel codeDisplayPanel;
    private final TableView<ViolationTableItem> violationTable;
    private final Label statusLabel;
    private final Button uploadButton;
    private final Button saveButton;
    private final Button editButton;
    private final Button refreshButton;
    private final VBox descriptionBox;
    private final Map<CodeAnalyzer.ViolationType, Integer> violationNavIndex = new HashMap<>();
    private List<CodeAnalyzer.Violation> lastViolations = null;
    
    public FileUploadUI() {
        this.codeAnalyzer = new CodeAnalyzer();
        this.codeDisplayPanel = new CodeDisplayPanel();
        this.violationTable = createViolationTable();
        this.statusLabel = new Label("Ready to analyze Java files");
        this.uploadButton = createUploadButton();
        this.saveButton = createSaveButton();
        this.editButton = createEditButton();
        this.refreshButton = createRefreshButton();
        this.descriptionBox = createDescriptionBox();
        
        setupLayout();
        // Move the refresh button from the header to the code view area beside the filename
        codeDisplayPanel.setupLayoutWithRefreshButton(refreshButton);
        setupStyles();
    }
    
    private void setupLayout() {
        setSpacing(10);
        setPadding(new Insets(20));
        
        // Header without refresh button (now moved to code view area)
        Label titleLabel = new Label("Java Code Analyzer");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #333;");
        HBox headerBar = new HBox();
        headerBar.setAlignment(Pos.CENTER_LEFT);
        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        headerBar.getChildren().addAll(titleLabel, spacer);

        // Upload section (without refresh button)
        VBox uploadSection = new VBox(10);
        uploadSection.setAlignment(Pos.CENTER);
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(uploadButton, editButton, saveButton);
        uploadSection.getChildren().addAll(
            new Label("Upload a .java file to analyze:"),
            buttonBox,
            statusLabel
        );

        // Main content area
        HBox mainContent = new HBox(20);
        mainContent.setAlignment(Pos.TOP_LEFT);
        
        // Code display panel (left side, 70%)
        VBox codeSection = new VBox(10);
        codeSection.getChildren().addAll(
            new Label("Code View:"),
            codeDisplayPanel
        );
        VBox.setVgrow(codeDisplayPanel, Priority.ALWAYS);
        codeSection.setMinWidth(0);
        codeSection.setMaxWidth(Double.MAX_VALUE);
        
        // Violation table and description (right side, 30%)
        VBox tableSection = new VBox(10);
        tableSection.getChildren().addAll(
            new Label("Violations Summary:"),
            violationTable,
            new Label("Violation Descriptions:"),
            descriptionBox
        );
        VBox.setVgrow(violationTable, Priority.ALWAYS);
        VBox.setVgrow(descriptionBox, Priority.NEVER);
        tableSection.setMinWidth(0);
        tableSection.setMaxWidth(Double.MAX_VALUE);
        
        mainContent.getChildren().addAll(codeSection, tableSection);
        HBox.setHgrow(codeSection, Priority.ALWAYS);
        HBox.setHgrow(tableSection, Priority.ALWAYS);
        mainContent.widthProperty().addListener((obs, oldVal, newVal) -> {
            codeSection.setPrefWidth(newVal.doubleValue() * 0.7);
            tableSection.setPrefWidth(newVal.doubleValue() * 0.3);
        });
        
        // Add all sections to main layout
        getChildren().addAll(headerBar, uploadSection, mainContent);
        VBox.setVgrow(mainContent, Priority.ALWAYS);
        
        violationTable.setOnMouseClicked(event -> {
            ViolationTableItem selected = violationTable.getSelectionModel().getSelectedItem();
            if (selected != null && lastViolations != null) {
                CodeAnalyzer.ViolationType type = getViolationTypeByDescription(selected.typeProperty().get());
                if (type != null) {
                    List<CodeAnalyzer.Violation> matches = lastViolations.stream()
                        .filter(v -> v.getType() == type)
                        .collect(Collectors.toList());
                    if (!matches.isEmpty()) {
                        int idx = violationNavIndex.getOrDefault(type, 0) % matches.size();
                        CodeAnalyzer.Violation v = matches.get(idx);
                        codeDisplayPanel.scrollToLineAndHighlight(v.getLineNumber(), type);
                        violationNavIndex.put(type, idx + 1);
                    }
                }
            }
        });
    }
    
    private void setupStyles() {
        setStyle("-fx-background-color: white;");
    }
    
    private Button createUploadButton() {
        Button button = new Button("Choose Java File");
        button.setStyle("-fx-background-color: #007acc; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20;");
        button.setOnAction(e -> handleFileUpload());
        return button;
    }
    
    private Button createSaveButton() {
        Button button = new Button("Compile File");
        button.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20;");
        button.setDisable(true);
        button.setOnAction(e -> handleSaveFile());
        return button;
    }
    
    private Button createEditButton() {
        Button button = new Button("Edit File");
        button.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20;");
        button.setDisable(true);
        button.setOnAction(e -> handleEditFile());
        return button;
    }

    private Button createRefreshButton() {
        Button button = new Button("Refresh");
        button.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20;");
        button.setDisable(false);
        button.setOnAction(e -> handleRefreshFile());
        return button;
    }

    private void handleRefreshFile() {
        if (codeDisplayPanel.getCurrentFilePath() != null) {
            analyzeFile(codeDisplayPanel.getCurrentFilePath());
            statusLabel.setText("File refreshed");
        } else {
            statusLabel.setText("No file to refresh");
        }
    }
    
    private void handleEditFile() {
        if (codeDisplayPanel.getCurrentFilePath() != null) {
            codeDisplayPanel.setEditMode(true);
            saveButton.setDisable(false);
        }
    }
    
    private void handleSaveFile() {
        if (codeDisplayPanel.saveFile()) {
            statusLabel.setText("File saved successfully");
            saveButton.setDisable(true);
            codeDisplayPanel.setEditMode(false);
            analyzeFile(codeDisplayPanel.getCurrentFilePath());
        } else {
            showError("Failed to save file");
        }
    }
    
    @SuppressWarnings("unchecked")
    private TableView<ViolationTableItem> createViolationTable() {
        TableView<ViolationTableItem> table = new TableView<>();
        
        // Violation type column
        TableColumn<ViolationTableItem, String> typeColumn = new TableColumn<>("Violation Type");
        typeColumn.setCellValueFactory(data -> data.getValue().typeProperty());
        typeColumn.setPrefWidth(200);
        
        // Count column
        TableColumn<ViolationTableItem, Integer> countColumn = new TableColumn<>("Count");
        countColumn.setCellValueFactory(data -> data.getValue().countProperty().asObject());
        countColumn.setPrefWidth(100);
        
        table.getColumns().addAll(typeColumn, countColumn);
        table.setPlaceholder(new Label("No violations found"));
        
        return table;
    }
    
    private void handleFileUpload() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Java File");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Java Files", "*.java")
        );
        
        File selectedFile = fileChooser.showOpenDialog(getScene().getWindow());
        if (selectedFile != null) {
            analyzeFile(selectedFile.toPath());
        }
    }
    
    private void analyzeFile(Path filePath) {
        statusLabel.setText("Analyzing file: " + filePath.getFileName());
        uploadButton.setDisable(true);
        saveButton.setDisable(true);
        editButton.setDisable(true);
        
        // Run analysis in background thread
        new Thread(() -> {
            try {
                CodeAnalyzer.AnalysisResult result = codeAnalyzer.analyzeFile(filePath);
                
                // Update UI on JavaFX thread
                Platform.runLater(() -> {
                    displayResults(result, filePath);
                    statusLabel.setText("Analysis complete: " + filePath.getFileName());
                    uploadButton.setDisable(false);
                    editButton.setDisable(false);
                });
                
            } catch (IOException e) {
                Platform.runLater(() -> {
                    showError("Error reading file: " + e.getMessage());
                    statusLabel.setText("Error analyzing file");
                    uploadButton.setDisable(false);
                    saveButton.setDisable(true);
                    editButton.setDisable(true);
                });
            } catch (IllegalArgumentException e) {
                Platform.runLater(() -> {
                    showError("Invalid file: " + e.getMessage());
                    statusLabel.setText("Invalid file selected");
                    uploadButton.setDisable(false);
                    saveButton.setDisable(true);
                    editButton.setDisable(true);
                });
            }
        }).start();
    }
    
    private void displayResults(CodeAnalyzer.AnalysisResult result, Path filePath) {
        // Clear the code display panel first to prevent duplicates
        codeDisplayPanel.clear();
        
        // Update violation table - show ALL violation types with counts
        violationTable.getItems().clear();
        Map<CodeAnalyzer.ViolationType, Integer> counts = result.getViolationCounts();
        lastViolations = result.getViolations();
        violationNavIndex.clear();
        
        // Always show all violation types, even with 0 count
        for (CodeAnalyzer.ViolationType type : CodeAnalyzer.ViolationType.values()) {
            int count = counts.getOrDefault(type, 0);
            violationTable.getItems().add(new ViolationTableItem(type, count));
        }
        
        // Update code display
        codeDisplayPanel.displayCodeWithViolations(result, filePath);
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private VBox createDescriptionBox() {
        VBox box = new VBox(5);
        box.setPadding(new Insets(5, 0, 0, 0));
        for (CodeAnalyzer.ViolationType type : CodeAnalyzer.ViolationType.values()) {
            Label label = new Label(codeAnalyzer.getHighlightUtil().getViolationDescription(type));
            label.setWrapText(true);
            box.getChildren().add(label);
        }
        return box;
    }
    
    /**
     * Table item for displaying violations
     */
    public static class ViolationTableItem {
        private final javafx.beans.property.SimpleStringProperty type;
        private final javafx.beans.property.SimpleIntegerProperty count;
        
        public ViolationTableItem(CodeAnalyzer.ViolationType type, int count) {
            this.type = new javafx.beans.property.SimpleStringProperty(type.getDescription());
            this.count = new javafx.beans.property.SimpleIntegerProperty(count);
        }
        
        public javafx.beans.property.StringProperty typeProperty() { return type; }
        public javafx.beans.property.IntegerProperty countProperty() { return count; }
    }
    
    private CodeAnalyzer.ViolationType getViolationTypeByDescription(String desc) {
        for (CodeAnalyzer.ViolationType t : CodeAnalyzer.ViolationType.values()) {
            if (t.getDescription().equals(desc)) return t;
        }
        return null;
    }
} 