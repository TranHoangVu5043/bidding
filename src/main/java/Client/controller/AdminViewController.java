package Client.controller;

import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class AdminViewController {

    @FXML private Button btnDashboard;
    @FXML private Button btnSellers;
    @FXML private Button btnUsers;
    @FXML private Button btnRevenue;
    @FXML private Button btnNotifications;
    @FXML private Button btnHistory;
    @FXML private Button btnSettings;
    @FXML private Button btnLogout;

    @FXML private Label lblTotalRevenue;
    @FXML private LineChart<String, Number> chartRevenueTrend;

    @FXML private TableView<ActivityLog> tableActivity;
    @FXML private TableColumn<ActivityLog, String> colUser;
    @FXML private TableColumn<ActivityLog, String> colActivity;
    @FXML private TableColumn<ActivityLog, String> colStatus;

    @FXML
    public void initialize() {
        lblTotalRevenue.setText("$1,337,000");

        setupTable();
        setupChart();
        setupActions();
    }

    private void setupTable() {
        colUser.setCellValueFactory(new PropertyValueFactory<>("user"));
        colActivity.setCellValueFactory(new PropertyValueFactory<>("activity"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    private void setupChart() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.getData().add(new XYChart.Data<>("0", 10));
        series.getData().add(new XYChart.Data<>("25", 40));
        series.getData().add(new XYChart.Data<>("50", 30));
        series.getData().add(new XYChart.Data<>("75", 60));
        series.getData().add(new XYChart.Data<>("100", 20));
        chartRevenueTrend.getData().add(series);
    }

    private void setupActions() {
        btnLogout.setOnAction(e -> System.exit(0));

        btnDashboard.setOnAction(e -> handleNav("Dashboard"));
        btnSellers.setOnAction(e -> handleNav("Sellers"));
        btnUsers.setOnAction(e -> handleNav("Users"));
        btnRevenue.setOnAction(e -> handleNav("Revenue"));
        btnNotifications.setOnAction(e -> handleNav("Notifications"));
        btnHistory.setOnAction(e -> handleNav("History"));
        btnSettings.setOnAction(e -> handleNav("Settings"));
    }

    private void handleNav(String screen) {
        System.out.println("Switching to: " + screen);
    }
}