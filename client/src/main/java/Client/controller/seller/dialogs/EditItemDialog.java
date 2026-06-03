package Client.controller.seller.dialogs;

import Client.model.item.Item;
import Client.networking.ApiResponse;
import Client.networking.endpoints.ItemApi;
import Client.util.DialogUtil;
import Client.util.SceneUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

public final class EditItemDialog {

    private EditItemDialog() {}

    public static void show(Window owner, Item item, ItemApi itemApi, Runnable onSaved) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(owner);
        stage.setTitle("Chỉnh sửa sản phẩm");
        stage.setResizable(false);

        //  Header 
        Label titleLabel = new Label("✏  Chỉnh sửa: " + item.getName());
        titleLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(360);

        VBox header = new VBox(titleLabel);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(20, 28, 16, 28));
        header.setStyle("-fx-background-color: " + DialogUtil.categoryGradient(item.getCategory()) + ";");

        //  Form fields 
        Label lblName = new Label("Tên sản phẩm *");
        lblName.setStyle("-fx-font-size: 11; -fx-font-weight: bold; -fx-text-fill: #374151;");
        TextField tfName = new TextField(item.getName() != null ? item.getName() : "");
        tfName.setStyle("-fx-background-radius: 6; -fx-border-color: #D1D5DB; " +
                "-fx-border-radius: 6; -fx-padding: 7; -fx-font-size: 12;");

        Label lblDesc = new Label("Mô tả");
        lblDesc.setStyle("-fx-font-size: 11; -fx-font-weight: bold; -fx-text-fill: #374151;");
        TextArea taDesc = new TextArea(item.getDescription() != null ? item.getDescription() : "");
        taDesc.setWrapText(true);
        taDesc.setPrefRowCount(3);
        taDesc.setStyle("-fx-background-radius: 6; -fx-border-color: #D1D5DB; " +
                "-fx-border-radius: 6; -fx-font-size: 12;");

        Label lblCat = new Label("Danh mục *");
        lblCat.setStyle("-fx-font-size: 11; -fx-font-weight: bold; -fx-text-fill: #374151;");
        ComboBox<String> cbCat = new ComboBox<>();
        cbCat.setItems(FXCollections.observableArrayList("ELECTRONICS", "ART", "VEHICLE"));
        cbCat.setValue(item.getCategory());
        cbCat.setMaxWidth(Double.MAX_VALUE);

        Label lblCond = new Label("Tình trạng *");
        lblCond.setStyle("-fx-font-size: 11; -fx-font-weight: bold; -fx-text-fill: #374151;");
        ComboBox<String> cbCond = new ComboBox<>();
        cbCond.setItems(FXCollections.observableArrayList("NEW", "USED", "REFURBISHED"));
        cbCond.setValue(item.getCondition());
        cbCond.setMaxWidth(Double.MAX_VALUE);

        VBox form = new VBox(10, lblName, tfName, lblDesc, taDesc, lblCat, cbCat, lblCond, cbCond);
        form.setPadding(new Insets(20, 28, 16, 28));

        //  Buttons 
        Button btnSave = new Button("💾 Lưu thay đổi");
        btnSave.setMaxWidth(Double.MAX_VALUE);
        btnSave.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-font-size: 13; -fx-background-radius: 8; " +
                "-fx-padding: 10; -fx-cursor: hand;");
        btnSave.setOnAction(e -> {
            String newName = tfName.getText().trim();
            String newDesc = taDesc.getText().trim();
            String newCat  = cbCat.getValue();
            String newCond = cbCond.getValue();

            if (newName.isEmpty()) { SceneUtil.showAlert("Thiếu thông tin", "Tên sản phẩm không được để trống."); return; }
            if (newCat  == null)   { SceneUtil.showAlert("Thiếu thông tin", "Vui lòng chọn danh mục."); return; }
            if (newCond == null)   { SceneUtil.showAlert("Thiếu thông tin", "Vui lòng chọn tình trạng."); return; }

            btnSave.setDisable(true);
            btnSave.setText("Đang lưu...");

            new Thread(() -> {
                ApiResponse<Void> res = itemApi.updateItem(item.getId(), newName, newDesc, newCat, newCond);
                Platform.runLater(() -> {
                    btnSave.setDisable(false);
                    btnSave.setText("💾 Lưu thay đổi");
                    if (res != null && res.getStatus() == 200) {
                        // Mutate in-memory object so the card reflects the change immediately
                        item.setName(newName);
                        item.setDescription(newDesc);
                        item.setCategory(newCat);
                        item.setCondition(newCond);
                        stage.close();
                        SceneUtil.showAlert("Thành công", "Sản phẩm đã được cập nhật.");
                        if (onSaved != null) onSaved.run();
                    } else {
                        SceneUtil.showAlert("Cập nhật thất bại",
                                res != null ? res.getMessage() : "Mất kết nối");
                    }
                });
            }).start();
        });

        Button btnCancel = new Button("Hủy");
        btnCancel.setMaxWidth(Double.MAX_VALUE);
        btnCancel.setStyle("-fx-background-color: transparent; -fx-text-fill: #6B7280; " +
                "-fx-background-radius: 8; -fx-padding: 10; -fx-cursor: hand; " +
                "-fx-border-color: #D1D5DB; -fx-border-radius: 8; -fx-border-width: 1;");
        btnCancel.setOnAction(e -> stage.close());

        VBox btnBox = new VBox(8, btnSave, btnCancel);
        btnBox.setPadding(new Insets(4, 28, 24, 28));

        VBox root = new VBox(header, form, btnBox);
        root.setStyle("-fx-background-color: #f8fafc;");

        stage.setScene(new Scene(root, 420, 530));
        stage.showAndWait();
    }
}
