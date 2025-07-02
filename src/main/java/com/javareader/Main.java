package com.javareader;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.javareader.ui.FileUploadUI;

/**
 * Main application class for the Java Code Analyzer
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Create the main UI
            FileUploadUI fileUploadUI = new FileUploadUI();
            
            // Set up the scene
            Scene scene = new Scene(fileUploadUI, 1200, 800);
            scene.getStylesheets().add(getClass().getResource("/styles/style.css").toExternalForm());
            
            // Configure the stage
            primaryStage.setTitle("Java Code Analyzer");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(800);
            primaryStage.setMinHeight(600);
            primaryStage.show();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
} 