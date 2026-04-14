package model;

import java.sql.Timestamp;

/**
 * Represents a product sold at the café.
 * Maps to the `products` table in the database.
 */
public class Product {

    private int       productId;
    private String    productName;
    private String    category;         // "snack", "beverage", "other"
    private double    price;
    private int       stockQuantity;
    private int       lowStockThreshold;
    private Timestamp createdAt;

    // Constructors
    public Product() {}

    public Product(int productId, String productName, String category,
                   double price, int stockQuantity,
                   int lowStockThreshold, Timestamp createdAt) {
        this.productId         = productId;
        this.productName       = productName;
        this.category          = category;
        this.price             = price;
        this.stockQuantity     = stockQuantity;
        this.lowStockThreshold = lowStockThreshold;
        this.createdAt         = createdAt;
    }

    // Getters
    public int getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public String getCategory() {
        return category;
    }

    public double getPrice() {
        return price;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public int getLowStockThreshold() {
        return lowStockThreshold;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    // Setters
    public void setProductId(int productId) {
        this.productId = productId;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public void setLowStockThreshold(int lowStockThreshold) {
        this.lowStockThreshold = lowStockThreshold;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    // Convenience
    public boolean isLowStock() {
        return stockQuantity <= lowStockThreshold;
    }

    /**
     * Returns "LOW" or "OK" — used directly by table renderers.
     */
    public String getStockStatus() {
        return isLowStock() ? "LOW" : "OK";
    }

    @Override
    public String toString() {
        return "Product{productId=" + productId
             + ", productName='" + productName + "'"
             + ", price=" + price
             + ", stock=" + stockQuantity + "}";
    }
}