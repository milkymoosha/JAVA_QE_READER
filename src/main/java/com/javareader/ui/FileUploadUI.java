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
import java.nio.file.Paths;
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
    private final Button scrapButton;
    private final Button analyzeScrapButton;
    private final VBox descriptionBox;
    private final Map<CodeAnalyzer.ViolationType, Integer> violationNavIndex = new HashMap<>();
    private List<CodeAnalyzer.Violation> lastViolations = null;
    private boolean isScrapMode = false;
    private boolean isScrapEditMode = false;
    private String lastScrapCode = "";
    private Button editScrapButton;
    private Path virtualScrapPath = Paths.get("Scrap.java");
    private String scrapContent = "";
    private TextField filePathField;
    private Button openPathButton;
    
    private static final String READ_BUTTON_STYLE = "-fx-background-color: #1976D2; -fx-text-fill: white; -fx-font-size: 14px; -fx-pref-width: 140px; -fx-pref-height: 38px; -fx-padding: 0 20;";
    private static final String WRITE_BUTTON_STYLE = "-fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-size: 14px; -fx-pref-width: 140px; -fx-pref-height: 38px; -fx-padding: 0 20;";
    
    public FileUploadUI() {
        this.codeAnalyzer = new CodeAnalyzer();
        this.codeDisplayPanel = new CodeDisplayPanel();
        this.violationTable = createViolationTable();
        this.statusLabel = new Label("Ready to analyze Java files");
        this.uploadButton = createUploadButton();
        this.saveButton = createSaveButton();
        this.editButton = createEditButton();
        this.refreshButton = createRefreshButton();
        this.scrapButton = createScrapButton();
        this.analyzeScrapButton = createAnalyzeScrapButton();
        this.editScrapButton = createEditScrapButton();
        this.filePathField = new TextField();
        this.openPathButton = createOpenPathButton();
        this.descriptionBox = createDescriptionBox();
        
        setupLayout();
        setupStyles();
        // Move the refresh button from the header to the code view area beside the filename
        codeDisplayPanel.setupLayoutWithRefreshButton(refreshButton, () -> handleCloseAllFiles());

    }

    /**
     * Handler to clear all files and reset the UI.
     */
    private void handleCloseAllFiles() {
        codeDisplayPanel.clear();
        violationTable.getItems().clear();
        statusLabel.setText("Ready to analyze Java files");
        saveButton.setDisable(true);
        editButton.setDisable(true);
        scrapContent = "";
        scrapButton.setVisible(true);
        analyzeScrapButton.setVisible(false);
        editScrapButton.setVisible(false);
        // Optionally clear any other state as needed
    }
    
    private void setupLayout() {
        setSpacing(0);
        setPadding(new Insets(0));

        // --- Menu Bar (top, like a text editor) ---
        MenuBar menuBar = new MenuBar();
        Menu fileMenu = new Menu("File");
        MenuItem openFileItem = new MenuItem("Open File");
        openFileItem.setOnAction(e -> handleFileUpload());
        MenuItem closeFileItem = new MenuItem("Close File");
        closeFileItem.setOnAction(e -> handleCloseAllFiles());
        MenuItem newFileItem = new MenuItem("New File");
        newFileItem.setOnAction(e -> {/* Optionally implement new file logic */});
        fileMenu.getItems().addAll(openFileItem, closeFileItem, newFileItem);
        menuBar.getMenus().add(fileMenu);

        // --- Toolbar (edit, compile, status) ---
        ToolBar toolBar = new ToolBar();
        filePathField.setPromptText("Paste file path here...");
        filePathField.setPrefWidth(220);
        filePathField.setOnAction(e -> handleOpenPath());
        toolBar.getItems().addAll(uploadButton, editButton, saveButton, scrapButton, analyzeScrapButton, editScrapButton, new Separator(), filePathField, openPathButton, new Separator(), statusLabel);

        // --- Main content area ---
        BorderPane mainContent = new BorderPane();
        mainContent.setPadding(new Insets(0));

        // Code display area (center, fills most of the space)
        VBox codeSection = new VBox(0);
        codeSection.getChildren().addAll(codeDisplayPanel);
        VBox.setVgrow(codeDisplayPanel, Priority.ALWAYS);
        codeSection.setMinWidth(0);
        codeSection.setMaxWidth(Double.MAX_VALUE);
        mainContent.setCenter(codeSection);

        // Violation table and description (right side, fixed width)
        VBox tableSection = new VBox(16); // more spacing
        violationTable.setPrefHeight(320);
        descriptionBox.setPrefHeight(180);
        tableSection.getChildren().addAll(
            new Label("Violations Summary:"),
            violationTable,
            new Label("Violation Descriptions:"),
            descriptionBox
        );
        VBox.setVgrow(violationTable, Priority.ALWAYS);
        VBox.setVgrow(descriptionBox, Priority.NEVER);
        tableSection.setMinWidth(420);
        tableSection.setMaxWidth(520);
        mainContent.setRight(tableSection);

        // Add all sections to main layout
        getChildren().clear();
        getChildren().addAll(menuBar, toolBar, mainContent);
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
        Button button = new Button("Choose File");
        button.setStyle(READ_BUTTON_STYLE);
        button.setOnAction(e -> handleFileUpload());
        return button;
    }
    
    private Button createSaveButton() {
        Button button = new Button("Compile File");
        button.setStyle(WRITE_BUTTON_STYLE);
        button.setDisable(true);
        button.setOnAction(e -> handleSaveFile());
        return button;
    }
    
    private Button createEditButton() {
        Button button = new Button("Edit File");
        button.setStyle(WRITE_BUTTON_STYLE);
        button.setDisable(true);
        button.setOnAction(e -> handleEditFile());
        return button;
    }

    private Button createRefreshButton() {
        Button button = new Button("Refresh");
        button.setStyle(READ_BUTTON_STYLE);
        button.setDisable(false);
        button.setOnAction(e -> handleRefreshFile());
        return button;
    }

    private Button createScrapButton() {
        Button button = new Button("Scrap");
        button.setStyle(READ_BUTTON_STYLE);
        button.setOnAction(e -> openScrapFile());
        return button;
    }
    private void openScrapFile() {
        scrapContent = "";
        codeDisplayPanel.showScrapEditArea(scrapContent);
        statusLabel.setText("Scrap mode: Paste or type code and click Analyze");
        violationTable.getItems().clear();
        scrapButton.setVisible(false);
        analyzeScrapButton.setVisible(true);
        editScrapButton.setVisible(false);
    }

    private Button createAnalyzeScrapButton() {
        Button button = new Button("Analyze");
        button.setStyle(WRITE_BUTTON_STYLE);
        button.setOnAction(e -> analyzeScrapCode());
        button.setVisible(false);
        return button;
    }
    private Button createEditScrapButton() {
        Button button = new Button("Edit");
        button.setStyle(WRITE_BUTTON_STYLE);
        button.setOnAction(e -> enterScrapEditMode());
        button.setVisible(false);
        return button;
    }
    private void enterScrapEditMode() {
        isScrapEditMode = true;
        scrapButton.setVisible(false);
        analyzeScrapButton.setVisible(true);
        editScrapButton.setVisible(false);
        codeDisplayPanel.showScrapEditArea(lastScrapCode);
        statusLabel.setText("Scrap mode: Paste or type code and click Analyze");
        violationTable.getItems().clear();
    }
    private void analyzeScrapCode() {
        String code = codeDisplayPanel.getScrapCode();
        if (code == null || code.trim().isEmpty()) {
            showError("Please enter some code to analyze.");
            return;
        }
        scrapContent = code;
        statusLabel.setText("Analyzing scrap code...");
        uploadButton.setDisable(true);
        saveButton.setDisable(true);
        editButton.setDisable(true);
        new Thread(() -> {
            try {
                CodeAnalyzer.AnalysisResult result = codeAnalyzer.analyzeString(code);
                Platform.runLater(() -> {
                    codeDisplayPanel.showScrapDisplayArea(code, result);
                    updateViolationTable(result);
                    statusLabel.setText("Analysis complete: Scrap code");
                    uploadButton.setDisable(false);
                    editButton.setDisable(false);
                    scrapButton.setVisible(false);
                    analyzeScrapButton.setVisible(false);
                    editScrapButton.setVisible(true);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showError("Error analyzing code: " + e.getMessage());
                    statusLabel.setText("Error analyzing code");
                    uploadButton.setDisable(false);
                    saveButton.setDisable(true);
                    editButton.setDisable(true);
                });
            }
        }).start();
    }

    // Helper to update the violation table (extracted from displayResults)
    private void updateViolationTable(CodeAnalyzer.AnalysisResult result) {
        violationTable.getItems().clear();
        Map<CodeAnalyzer.ViolationType, Integer> counts = result.getViolationCounts();
        lastViolations = result.getViolations();
        violationNavIndex.clear();
        for (CodeAnalyzer.ViolationType type : CodeAnalyzer.ViolationType.values()) {
            int count = counts.getOrDefault(type, 0);
            violationTable.getItems().add(new ViolationTableItem(type, count));
        }
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
        updateViolationTable(result);
        
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
    
    private Button createOpenPathButton() {
        Button button = new Button("Open");
        button.setStyle(READ_BUTTON_STYLE);
        button.setOnAction(e -> handleOpenPath());
        return button;
    }
    private void handleOpenPath() {
        String path = filePathField.getText();
        if (path == null || path.trim().isEmpty()) {
            showError("Please enter a file path.");
            return;
        }
        path = path.trim();
        if ((path.startsWith("\"") && path.endsWith("\"")) || (path.startsWith("'") && path.endsWith("'"))) {
            path = path.substring(1, path.length() - 1);
        }
        File file = new File(path);
        if (!file.exists() || !file.isFile() || !file.getName().endsWith(".java")) {
            showError("File does not exist or is not a .java file.");
            return;
        }
        analyzeFile(file.toPath());
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