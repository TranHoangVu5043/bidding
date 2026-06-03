package Client.util;

import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import java.time.LocalDateTime;

public final class DialogUtil {

    private DialogUtil() {}

    // Grid helper
    public static void addDetailRow(GridPane grid, int row, String labelText, String value) {
        Label lbl = new Label(labelText);
        lbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #374151; -fx-font-size: 12;");
        Label val = new Label(value != null ? value : "—");
        val.setStyle("-fx-text-fill: #1e293b; -fx-font-size: 12;");
        grid.add(lbl, 0, row);
        grid.add(val, 1, row);
    }

    public static String formatDisplayTime(String timeStr) {
        if (timeStr == null) return "—";
        try {
            LocalDateTime dt = LocalDateTime.parse(timeStr.replace(" ", "T"));
            return String.format("%02d/%02d/%d  %02d:%02d",
                dt.getDayOfMonth(), dt.getMonthValue(), dt.getYear(),
                dt.getHour(), dt.getMinute());
        } catch (Exception e) {
            return timeStr;
        }
    }

    //Category helpers

    public static String categoryEmoji(String cat) {
        if (cat == null) return "📦";
        return switch (cat.toUpperCase()) {
            case "ELECTRONICS" -> "📱";
            case "ART"         -> "🎨";
            case "VEHICLE"     -> "🚗";
            default            -> "📦";
        };
    }

    public static String categoryGradient(String cat) {
        if (cat == null) return "linear-gradient(to bottom, #f1f5f9, #cbd5e1)";
        return switch (cat.toUpperCase()) {
            case "ELECTRONICS" -> "linear-gradient(to bottom, #dbeafe, #93c5fd)";
            case "ART"         -> "linear-gradient(to bottom, #fce7f3, #f9a8d4)";
            case "VEHICLE"     -> "linear-gradient(to bottom, #d1fae5, #6ee7b7)";
            default            -> "linear-gradient(to bottom, #f1f5f9, #cbd5e1)";
        };
    }

    public static String categoryColor(String cat) {
        if (cat == null) return "#9ca3af";
        return switch (cat.toUpperCase()) {
            case "ELECTRONICS" -> "#3b82f6";
            case "ART"         -> "#ec4899";
            case "VEHICLE"     -> "#10b981";
            default            -> "#9ca3af";
        };
    }

    //Status helper

    public static String statusColor(String status) {
        if (status == null) return "#9ca3af";
        return switch (status.toUpperCase()) {
            case "ACTIVE"    -> "#22c55e";
            case "UPCOMING"  -> "#3b82f6";
            case "FINISHED"  -> "#6b7280";
            case "CANCELLED" -> "#ef4444";
            default          -> "#9ca3af";
        };
    }
}
