package inventory.service;

public class StockNotifier {

    public static void notifyVendor(String product) {
        System.out.println("[VENDOR ALERT] Reorder required for: " + product);
    }

    public static void notifyProduction(String product) {
        System.out.println("[PRODUCTION ALERT] Start production for: " + product);
    }
}

