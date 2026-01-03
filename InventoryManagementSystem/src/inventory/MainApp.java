package inventory;

import inventory.model.InventoryItem;
import inventory.model.SalesRecord;
import inventory.service.ForecastService;
import inventory.service.StockNotifier;
import inventory.util.ValidationUtil;

import javafx.application.Application;
import javafx.collections.*;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.*;

public class MainApp extends Application {

    private ObservableList<InventoryItem> inventory =
            FXCollections.observableArrayList();

    private TableView<InventoryItem> table = new TableView<>();

    private TextField idField = new TextField();
    private TextField nameField = new TextField();
    private TextField qtyField = new TextField();
    private TextField priceField = new TextField();
    private TextField searchField = new TextField();
    private TextField salesQtyField = new TextField();

    // Sales history per item
    private Map<Integer, List<SalesRecord>> salesHistory = new HashMap<>();

    @Override
    public void start(Stage stage) {

        // Table columns
        TableColumn<InventoryItem, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<InventoryItem, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<InventoryItem, Integer> qtyCol = new TableColumn<>("Quantity");
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        TableColumn<InventoryItem, Double> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));

        table.getColumns().add(idCol);
        table.getColumns().add(nameCol);
        table.getColumns().add(qtyCol);
        table.getColumns().add(priceCol);


        // Search
        FilteredList<InventoryItem> filtered =
                new FilteredList<>(inventory, p -> true);

        searchField.setPromptText("Search product...");
        searchField.textProperty().addListener((obs, old, val) ->
                filtered.setPredicate(i ->
                        i.getName().toLowerCase().contains(val.toLowerCase()))
        );

        table.setItems(filtered);

        // Input prompts
        idField.setPromptText("ID");
        nameField.setPromptText("Name");
        qtyField.setPromptText("Initial Stock");
        priceField.setPromptText("Price");
        salesQtyField.setPromptText("Sales Qty");

        // Buttons
        Button addBtn = new Button("Add");
        Button updateBtn = new Button("Update");
        Button deleteBtn = new Button("Delete");
        Button saleBtn = new Button("Make Sale");
        Button forecastBtn = new Button("Forecast");

        addBtn.setOnAction(e -> addItem());
        updateBtn.setOnAction(e -> updateItem());
        deleteBtn.setOnAction(e -> deleteItem());
        saleBtn.setOnAction(e -> makeSale());
        forecastBtn.setOnAction(e -> forecast());

        table.setOnMouseClicked(e -> fillForm());

        // Form layout
        GridPane form = new GridPane();
        form.setPadding(new Insets(10));
        form.setHgap(10);
        form.setVgap(10);

        form.add(idField, 0, 0);
        form.add(nameField, 1, 0);
        form.add(qtyField, 0, 1);
        form.add(priceField, 1, 1);

        form.add(addBtn, 0, 2);
        form.add(updateBtn, 1, 2);
        form.add(deleteBtn, 2, 2);

        form.add(new Label("Sales Qty"), 0, 3);
        form.add(salesQtyField, 1, 3);
        form.add(saleBtn, 2, 3);

        form.add(forecastBtn, 1, 4);

        VBox root = new VBox(10, searchField, table, form);
        root.setPadding(new Insets(10));

        stage.setTitle("Inventory Management System");
        stage.setScene(new Scene(root, 750, 550));
        stage.show();
    }

    // ---------- LOGIC METHODS ----------

    private void addItem() {
        if (!validInput()) return;

        InventoryItem item = new InventoryItem(
                Integer.parseInt(idField.getText()),
                nameField.getText(),
                Integer.parseInt(qtyField.getText()),
                Double.parseDouble(priceField.getText())
        );

        inventory.add(item);
        checkStock(item);
        clear();
    }

    private void updateItem() {
        InventoryItem item = table.getSelectionModel().getSelectedItem();
        if (item == null || !validInput()) return;

        item.setName(nameField.getText());
        item.setQuantity(Integer.parseInt(qtyField.getText()));
        item.setPrice(Double.parseDouble(priceField.getText()));

        table.refresh();
        checkStock(item);
        clear();
    }

    private void deleteItem() {
        InventoryItem item = table.getSelectionModel().getSelectedItem();
        if (item != null) inventory.remove(item);
        clear();
    }

    private void fillForm() {
        InventoryItem item = table.getSelectionModel().getSelectedItem();
        if (item != null) {
            idField.setText(String.valueOf(item.getId()));
            nameField.setText(item.getName());
            qtyField.setText(String.valueOf(item.getQuantity()));
            priceField.setText(String.valueOf(item.getPrice()));
        }
    }

    private void makeSale() {
        InventoryItem item = table.getSelectionModel().getSelectedItem();
        if (item == null) {
            alert("Error", "Select an item to sell.");
            return;
        }

        int sold;
        try {
            sold = Integer.parseInt(salesQtyField.getText());
        } catch (Exception e) {
            alert("Error", "Invalid sales quantity.");
            return;
        }

        if (sold <= 0 || sold > item.getQuantity()) {
            alert("Error", "Invalid sales quantity.");
            return;
        }

        item.reduceStock(sold);
        table.refresh();

        salesHistory
                .computeIfAbsent(item.getId(), k -> new ArrayList<>())
                .add(new SalesRecord(LocalDate.now(), sold));

        checkStock(item);
        alert("Sale Successful", sold + " units sold.");
        salesQtyField.clear();
    }

    private void forecast() {
        InventoryItem item = table.getSelectionModel().getSelectedItem();
        if (item == null) {
            alert("Error", "Select an item.");
            return;
        }

        List<SalesRecord> history =
                salesHistory.getOrDefault(item.getId(), new ArrayList<>());

        

        int demand = ForecastService.forecastDemand(history, 7);
alert("Forecast", "Next 7 days demand: " + demand);

// Show chart
showSalesChart(item, history, 7);

    }

    private void checkStock(InventoryItem item) {
        if (item.isOutOfStock()) {
            StockNotifier.notifyVendor(item.getName());
            StockNotifier.notifyProduction(item.getName());
            alert("Out of Stock", item.getName() + " is out of stock!");
        } else if (item.isLowStock()) {
            alert("Low Stock", item.getName() + " stock is low.");
        }
    }

    private boolean validInput() {
        if (!ValidationUtil.isNumeric(idField.getText()) ||
            !ValidationUtil.isNumeric(qtyField.getText()) ||
            !ValidationUtil.isNumeric(priceField.getText()) ||
            nameField.getText().isEmpty()) {

            alert("Invalid Input", "Enter valid values.");
            return false;
        }
        return true;
    }

    private void clear() {
        idField.clear();
        nameField.clear();
        qtyField.clear();
        priceField.clear();
    }

    private void alert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setContentText(msg);
        a.show();
    }

    public static void main(String[] args) {
        launch();
    }

    private void showSalesChart(InventoryItem item, List<SalesRecord> history, int forecastDays) {

    Stage chartStage = new Stage();
    chartStage.setTitle("Sales Forecast - " + item.getName());

    // Axes
    CategoryAxis xAxis = new CategoryAxis();
    xAxis.setLabel("Date");

    NumberAxis yAxis = new NumberAxis();
    yAxis.setLabel("Quantity Sold");

    LineChart<String, Number> lineChart =
            new LineChart<>(xAxis, yAxis);

    lineChart.setTitle("Sales Trend");

    // Past sales series
    XYChart.Series<String, Number> salesSeries =
            new XYChart.Series<>();
    salesSeries.setName("Actual Sales");

    int total = 0;
    for (SalesRecord record : history) {
        salesSeries.getData().add(
                new XYChart.Data<>(
                        record.getDate().toString(),
                        record.getQuantitySold()
                )
        );
        total += record.getQuantitySold();
    }

    // Forecast series
    XYChart.Series<String, Number> forecastSeries =
            new XYChart.Series<>();
    forecastSeries.setName("Forecast");

    int avgPerDay = history.isEmpty() ? 0 : total / history.size();
    int forecastValue = avgPerDay * forecastDays;

    forecastSeries.getData().add(
            new XYChart.Data<>("Next " + forecastDays + " days", forecastValue)
    );

    lineChart.getData().addAll(salesSeries, forecastSeries);

    Scene scene = new Scene(lineChart, 600, 400);
    chartStage.setScene(scene);
    chartStage.show();
}

}
