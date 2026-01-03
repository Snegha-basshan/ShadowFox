package inventory.model;

import java.time.LocalDate;

public class SalesRecord {

    private LocalDate date;
    private int quantitySold;

    // Constructor
    public SalesRecord(LocalDate date, int quantity) {
        this.date = date;
        this.quantitySold = quantity;
    }

    // Getter for date (used in forecasting)
    public LocalDate getDate() {
        return date;
    }

    // Getter for quantity (used in demand calculation)
    public int getQuantitySold() {
        return quantitySold;
    }
}
