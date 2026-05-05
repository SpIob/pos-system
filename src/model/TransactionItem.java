package model;

public class TransactionItem {
    private int itemId;
    private int transactionId;
    private int productId;
    private String itemDescription;
    private int quantity;
    private double unitPrice;
    private double subtotal;

    public TransactionItem() {}

    public TransactionItem(int itemId, int transactionId, int productId,
                           String itemDescription, int quantity,
                           double unitPrice, double subtotal) {
        this.itemId = itemId; this.transactionId = transactionId;
        this.productId = productId; this.itemDescription = itemDescription;
        this.quantity = quantity; this.unitPrice = unitPrice; this.subtotal = subtotal;
    }

    public int getItemId()              { return itemId; }
    public int getTransactionId()       { return transactionId; }
    public int getProductId()           { return productId; }
    public String getItemDescription()  { return itemDescription; }
    public int getQuantity()            { return quantity; }
    public double getUnitPrice()        { return unitPrice; }
    public double getSubtotal()         { return subtotal; }

    public void setItemId(int v)             { this.itemId = v; }
    public void setTransactionId(int v)      { this.transactionId = v; }
    public void setProductId(int v)          { this.productId = v; }
    public void setItemDescription(String v) { this.itemDescription = v; }
    public void setQuantity(int v)           { this.quantity = v; }
    public void setUnitPrice(double v)       { this.unitPrice = v; }
    public void setSubtotal(double v)        { this.subtotal = v; }

    public void recalculateSubtotal()  { this.subtotal = this.quantity * this.unitPrice; }
    public boolean isProductItem()     { return productId > 0; }

    @Override
    public String toString() {
        return "TransactionItem{itemId=" + itemId + ", description='" + itemDescription + "', qty=" + quantity + ", subtotal=" + subtotal + "}";
    }
}