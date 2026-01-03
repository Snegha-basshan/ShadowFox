package inventory.model;

public class Vendor {
    private String vendorId;
    private String name;
    private String email;

    public Vendor(String vendorId, String name, String email) {
        this.vendorId = vendorId;
        this.name = name;
        this.email = email;
    }

    public String getVendorId() { return vendorId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
}

