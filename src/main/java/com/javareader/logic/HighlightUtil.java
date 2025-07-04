package com.javareader.logic;

import javafx.scene.paint.Color;
import javafx.scene.text.Text;

/**
 * Utility class for highlighting code violations in the UI
 */
public class HighlightUtil {
    
    // Color scheme for different violation types
    public static final Color LINE_TOO_LONG_COLOR = Color.RED;
    public static final Color INDENTATION_COLOR = Color.ORANGE;
    public static final Color REPEATED_STRING_COLOR = Color.BLUE;
    public static final Color EMPTY_LINE_COLOR = Color.PURPLE;
    public static final Color NAMING_CONVENTION_COLOR = Color.GREEN;
    public static final Color ENDS_WITH_EQUALS_COLOR = Color.MAGENTA;
    
    /**
     * Gets the color for a specific violation type
     */
    public Color getColorForViolation(CodeAnalyzer.ViolationType type) {
        switch (type) {
            case LINE_TOO_LONG:
                return LINE_TOO_LONG_COLOR;
            case IMPROPER_INDENTATION:
                return INDENTATION_COLOR;
            case REPEATED_STRING:
                return REPEATED_STRING_COLOR;
            case EMPTY_LINE:
                return EMPTY_LINE_COLOR;
            case NAMING_CONVENTION:
                return NAMING_CONVENTION_COLOR;
            case LINE_ENDS_WITH_EQUALS:
                return ENDS_WITH_EQUALS_COLOR;
            default:
                return Color.BLACK;
        }
    }
    
    /**
     * Creates a styled Text object for displaying code with violations
     */
    public Text createHighlightedText(String content, CodeAnalyzer.ViolationType violationType) {
        Text text = new Text(content);
        text.setFill(getColorForViolation(violationType));
        text.setStyle("-fx-font-weight: bold;");
        return text;
    }
    
    /**
     * Gets the CSS style for a violation type
     */
    public String getCssStyleForViolation(CodeAnalyzer.ViolationType type) {
        Color color = getColorForViolation(type);
        return String.format("-fx-background-color: rgba(%.0f, %.0f, %.0f, 0.3); -fx-font-weight: bold;",
            color.getRed() * 255,
            color.getGreen() * 255,
            color.getBlue() * 255);
    }
    
    /**
     * Gets a user-friendly description for a violation type
     */
    public String getViolationDescription(CodeAnalyzer.ViolationType type) {
        switch (type) {
            case LINE_TOO_LONG:
                return "Red: Line exceeds 120 characters";
            case IMPROPER_INDENTATION:
                return "Orange: Improper indentation (more than 2 spaces)";
            case REPEATED_STRING:
                return "Blue: Repeated string literal (consider using a constant)";
            case EMPTY_LINE:
                return "Yellow: Empty line detected";
            case NAMING_CONVENTION:
                return "Green: Naming convention violation (should be camelCase)";
            case LINE_ENDS_WITH_EQUALS:
                return "Magenta: Line ends with '=' (possible incomplete assignment)";
            default:
                return "Unknown violation";
        }
    }
    
    /**
     * Gets a short label for a violation type
     */
    public String getViolationLabel(CodeAnalyzer.ViolationType type) {
        switch (type) {
            case LINE_TOO_LONG:
                return "Line too long";
            case IMPROPER_INDENTATION:
                return "Indentation issue";
            case REPEATED_STRING:
                return "Repeated string";
            case EMPTY_LINE:
                return "Empty line";
            case NAMING_CONVENTION:
                return "Naming convention";
            case LINE_ENDS_WITH_EQUALS:
                return "Ends with '='";
            default:
                return "Unknown violation";
        }
    }
} 