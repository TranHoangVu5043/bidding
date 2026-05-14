package Client.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import Client.controller.Product;

public class SellerController {

    @FXML private TableView<Product> tableView;
    @FXML private TableColumn<Product, String> colName;
    @FXML private TableColumn<Product, Double> colPrice;
    @FXML private TableColumn<Product, Integer> colStock;
    @FXML private TableColumn<Product, String> colStatus;

    @FXML private TextField txtSearch;
    @FXML private TextField txtName;
    @FXML private TextField txtPrice;
    @FXML private TextField txtStock;

    private ObservableList<Product> masterData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        tableView.setItems(masterData);
    }

    @FXML
    private void handleAddProduct() {
        try {
            String name = txtName.getText().trim();
            String priceStr = txtPrice.getText().trim();
            String stockStr = txtStock.getText().trim();

            if (name.isEmpty() || priceStr.isEmpty() || stockStr.isEmpty()) {
                return;
            }

            double price = Double.parseDouble(priceStr);
            int stock = Integer.parseInt(stockStr);

            Product newProduct = new Product(name, price, stock, "Active");
            masterData.add(newProduct);

            clearFields();
        } catch (NumberFormatException e) {
            System.out.println("Invalid input");
        }
    }

    @FXML
    private void handleSearch() {
        String keyword = txtSearch.getText().toLowerCase().trim();

        if (keyword.isEmpty()) {
            tableView.setItems(masterData);
            return;
        }

        ObservableList<Product> filteredData = FXCollections.observableArrayList();
        for (Product p : masterData) {
            if (p.getName().toLowerCase().contains(keyword)) {
                filteredData.add(p);
            }
        }
        tableView.setItems(filteredData);
    }

    @FXML
    private void showCancel() {
        clearFields();
    }

    private void clearFields() {
        txtName.clear();
        txtPrice.clear();
        txtStock.clear();
    }

    @FXML private void showDashboard() {}
    @FXML private void showInventory() {}
    @FXML private void showOrders() {}
    @FXML private void showRevenue() {}
    @FXML private void showNotification() {}
    @FXML private void showHistory() {}
    @FXML private void showLogout() { System.exit(0); }
}

