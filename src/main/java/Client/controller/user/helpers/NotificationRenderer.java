package Client.controller.user.helpers;

import Client.model.Notification;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;

public final class NotificationRenderer {

    private NotificationRenderer() {}

    // ── List rendering ────────────────────────────────────────────────────

    public static void render(List<Notification> list, VBox container) {
        if (container == null) return;
        container.getChildren().clear();
        if (list == null || list.isEmpty()) {
            Label empty = new Label("Không có thông báo nào.");
            empty.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 13; -fx-padding: 20;");
            container.getChildren().add(empty);
            return;
        }
        for (Notification n : list) container.getChildren().add(buildRow(n));
    }

    // ── Single row ────────────────────────────────────────────────────────

    public static HBox buildRow(Notification n) {
        Label icon = new Label(n.getMessage().contains("hủy") ? "🚫" : "🔔");
        icon.setStyle("-fx-font-size: 20;");

        Label msg = new Label(n.getMessage());
        msg.setWrapText(true);
        msg.setMaxWidth(500);
        msg.setStyle("-fx-font-size: 12; -fx-text-fill: " + (n.isRead() ? "#6B7280" : "#1e293b") + ";");
        HBox.setHgrow(msg, Priority.ALWAYS);

        Label time = new Label(formatTime(n.getCreatedAt()));
        time.setStyle("-fx-font-size: 10; -fx-text-fill: #9CA3AF;");

        HBox row = new HBox(12, icon, msg, time);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12));
        String bg     = n.isRead() ? "white"    : "#EFF6FF";
        String border = n.isRead() ? "#E5E7EB"  : "#BFDBFE";
        row.setStyle("-fx-background-color: " + bg + "; -fx-background-radius: 10; " +
                "-fx-border-color: " + border + "; -fx-border-radius: 10; -fx-border-width: 1;");
        return row;
    }

    // ── Utilities ─────────────────────────────────────────────────────────

    /** Returns true if the notification message is auction-related. */
    public static boolean isAuctionNotif(String msg) {
        if (msg == null) return false;
        String lower = msg.toLowerCase();
        return lower.contains("đấu giá") || lower.contains("auction")
                || lower.contains("vượt giá") || lower.contains("thắng")
                || lower.contains("giá hiện tại") || msg.contains("🎉") || msg.contains("📢");
    }

    /** Converts an ISO creation timestamp to human-readable relative time. */
    public static String formatTime(String isoTime) {
        if (isoTime == null) return "";
        try {
            java.time.LocalDateTime dt   = java.time.LocalDateTime.parse(isoTime);
            java.time.Duration      diff = java.time.Duration.between(
                    dt, java.time.LocalDateTime.now(java.time.ZoneOffset.UTC));
            if (diff.toMinutes() < 1)  return "Vừa xong";
            if (diff.toMinutes() < 60) return diff.toMinutes() + " phút trước";
            if (diff.toHours()   < 24) return diff.toHours()   + " giờ trước";
            return diff.toDays() + " ngày trước";
        } catch (Exception e) { return isoTime; }
    }
}
