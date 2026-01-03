package inventory.model;

public class InventoryItem {

    private int id;            // changed from String itemId
    private String name;
    private int quantity;
    private double price;      // added price field
    private int reorderLevel = 5; // default reorder level

    // Constructor matching MainApp
    public InventoryItem(int id, String name, int quantity, double price) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.price = price;
    }

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public int getQuantity() { return quantity; }
    public double getPrice() { return price; }

    // Setters
    public void setName(String name) { this.name = name; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setPrice(double price) { this.price = price; }

    // Stock management
    public void reduceStock(int soldQty) {
        this.quantity -= soldQty;
    }

    public boolean isLowStock() {
        return quantity <= reorderLevel && quantity > 0;
    }

    public boolean isOutOfStock() {
        return quantity <= 0;
    }
}
