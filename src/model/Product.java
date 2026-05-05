package model;

import java.sql.Timestamp;

public class Product {
    private int productId;
    private String productName;
    private String category;
    private double price;
    private int stockQuantity;
    private int lowStockThreshold;
    private Timestamp createdAt;

    public Product() {}

    public Product(int productId, String productName, String category,
                   double price, int stockQuantity, int lowStockThreshold,
                   Timestamp createdAt) {
        this.productId = productId; this.productName = productName;
        this.category = category; this.price = price;
        this.stockQuantity = stockQuantity; this.lowStockThreshold = lowStockThreshold;
        this.createdAt = createdAt;
    }

    public int getProductId()              { return productId; }
    public String getProductName()         { return productName; }
    public String getCategory()            { return category; }
    public double getPrice()               { return price; }
    public int getStockQuantity()          { return stockQuantity; }
    public int getLowStockThreshold()      { return lowStockThreshold; }
    public Timestamp getCreatedAt()        { return createdAt; }

    public void setProductId(int v)           { this.productId = v; }
    public void setProductName(String v)      { this.productName = v; }
    public void setCategory(String v)         { this.category = v; }
    public void setPrice(double v)            { this.price = v; }
    public void setStockQuantity(int v)       { this.stockQuantity = v; }
    public void setLowStockThreshold(int v)   { this.lowStockThreshold = v; }
    public void setCreatedAt(Timestamp v)     { this.createdAt = v; }

    public boolean isLowStock()    { return stockQuantity <= lowStockThreshold; }
    public String getStockStatus() { return isLowStock() ? "LOW" : "OK"; }

    @Override
    public String toString() {
        return "Product{productId=" + productId + ", productName='" + productName + "', price=" + price + ", stock=" + stockQuantity + "}";
    }
}