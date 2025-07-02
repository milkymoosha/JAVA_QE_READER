# Java Code Analyzer

A complete Java-based application that performs code analysis on uploaded .java files with a clean, minimalist UI.

## Features

### 🔍 Code Review Rules

The analyzer detects and highlights the following code violations:

- **🔴 Line Length**: Lines exceeding 120 characters are marked in red
- **🟡 Indentation Issues**: Improper indentation (more than 2 spaces) is marked in yellow
- **🔵 Repeated Strings**: String literals used 2+ times are highlighted in blue with suggestions for constants
- **🟣 Empty Lines**: Excessive or redundant empty lines are marked in purple
- **🟢 Naming Conventions**: Non-camelCase variable/method names are marked in green

### 🎨 UI Features

- **Minimalist Design**: Clean black & white theme
- **File Upload**: Simple button to select and upload .java files
- **Code Viewer**: Displays code with line numbers and violation markers
- **Violation Summary**: Dynamic table showing violation counts and descriptions
- **Real-time Analysis**: Background processing with progress indicators

## Requirements

- Java 14 or higher
- Maven 3.6 or higher
- JavaFX (included in dependencies)

## Installation & Setup

1. **Clone or download the project**
   ```bash
   git clone <https://github.com/milkymoosha/JAVA_QE_READER.git>
   cd JAVA-reader
   ```

2. **Build the project**
   ```bash
   mvn clean compile
   ```

3. **Run the application**
   ```bash
   mvn javafx:run
   ```

   Or build and run the JAR:
   ```bash
   mvn clean package
   java -jar target/java-code-analyzer-1.0.0.jar
   ```

## Usage

1. **Launch the application** - The main window will appear with a clean interface
2. **Upload a Java file** - Click "Choose Java File" and select a .java file
3. **View results** - The application will analyze the file and display:
   - Code with violation markers in the left panel
   - Violation summary table in the right panel
4. **Interpret the markers**:
   - 🔴 Red: Line too long
   - 🟡 Yellow: Indentation issue
   - 🔵 Blue: Repeated string
   - 🟣 Purple: Empty line
   - 🟢 Green: Naming convention violation

## Project Structure

```
JAVA-reader/
├── src/
│   ├── main/
│   │   ├── java/com/javareader/
│   │   │   ├── Main.java                 # Application entry point
│   │   │   ├── ui/
│   │   │   │   ├── FileUploadUI.java     # Main UI controller
│   │   │   │   └── CodeDisplayPanel.java # Code display component
│   │   │   └── logic/
│   │   │       ├── CodeAnalyzer.java     # Main analysis orchestrator
│   │   │       ├── RuleChecker.java      # Code review rules implementation
│   │   │       └── HighlightUtil.java    # UI highlighting utilities
│   │   └── resources/
│   │       ├── styles/
│   │       │   └── style.css             # CSS styling
│   │       └── assets/
│   │           └── sample.java           # Sample file for testing
├── pom.xml                               # Maven configuration
└── README.md                             # This file
```

## Technical Details

### Backend Logic
- **CodeAnalyzer**: Orchestrates the analysis process
- **RuleChecker**: Implements specific code review rules using regex and string parsing
- **HighlightUtil**: Provides color schemes and styling for violations

### Frontend
- **JavaFX**: Modern UI framework for desktop applications
- **FileUploadUI**: Main application window with file upload and results display
- **CodeDisplayPanel**: Specialized component for code viewing with violation markers

### Analysis Rules
- **Line Length**: Checks if lines exceed 120 characters
- **Indentation**: Detects improper spacing (more than 2 spaces)
- **Repeated Strings**: Finds string literals used multiple times
- **Empty Lines**: Identifies excessive blank lines
- **Naming Conventions**: Validates camelCase for variables and methods

## Sample Output

When analyzing a Java file, you'll see:

```
Violations Summary:
┌─────────────────────┬───────┬─────────────────────────────────────┐
│ Violation Type      │ Count │ Description                         │
├─────────────────────┼───────┼─────────────────────────────────────┤
│ Line too long       │   2   │ 🔴 Line exceeds 120 characters      │
│ Repeated string     │   3   │ 🔵 Repeated string literal          │
│ Indentation issue   │   1   │ 🟡 Improper indentation             │
│ Naming convention   │   2   │ 🟢 Naming convention violation      │
└─────────────────────┴───────┴─────────────────────────────────────┘
```

## Customization

### Adding New Rules
1. Add a new violation type to `CodeAnalyzer.ViolationType`
2. Implement the rule logic in `RuleChecker`
3. Add highlighting in `HighlightUtil`
4. Update the UI to display the new rule

### Modifying Styles
Edit `src/main/resources/styles/style.css` to customize the appearance.

## Troubleshooting

### Common Issues

1. **JavaFX not found**: Ensure you're using Java 14+ and the JavaFX dependencies are included
2. **File upload fails**: Make sure the file is a valid .java file
3. **Analysis errors**: Check that the Java file is properly formatted and accessible

### Build Issues
- Clean and rebuild: `mvn clean compile`
- Check Java version: `java -version`
- Verify Maven: `mvn -version`

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

## License

This project is open source and available under the MIT License.

---

**Note**: This application is designed for educational and code review purposes. It provides suggestions for code improvements but should not replace professional code review tools or practices. 
