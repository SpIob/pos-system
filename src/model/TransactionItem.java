package model;

/**
 * Represents one line item inside a transaction.
 * Maps to the `transaction_items` table in the database.
 */
public class TransactionItem {

    private int    itemId;
    private int    transactionId;
    private int    productId;       // 0 if item is a session charge
    private String itemDescription;
    private int    quantity;
    private double unitPrice;
    private double subtotal;

    // Constructors
    public TransactionItem() {}

    public TransactionItem(int itemId, int transactionId, int productId,
                           String itemDescription, int quantity,
                           double unitPrice, double subtotal) {
        this.itemId          = itemId;
        this.transactionId   = transactionId;
        this.productId       = productId;
        this.itemDescription = itemDescription;
        this.quantity        = quantity;
        this.unitPrice       = unitPrice;
        this.subtotal        = subtotal;
    }

    // Getters
    public int getItemId() {
        return itemId;
    }

    public int getTransactionId() {
        return transactionId;
    }

    public int getProductId() {
        return productId;
    }

    public String getItemDescription() {
        return itemDescription;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public double getSubtotal() {
        return subtotal;
    }

    // Setters
    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public void setItemDescription(String itemDescription) {
        this.itemDescription = itemDescription;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    // Convenience
    /**
     * Recalculates subtotal from quantity × unitPrice.
     * Call this any time quantity or unitPrice changes.
     */
    public void recalculateSubtotal() {
        this.subtotal = this.quantity * this.unitPrice;
    }

    public boolean isProductItem() {
        return productId > 0;
    }

    @Override
    public String toString() {
        return "TransactionItem{itemId=" + itemId
             + ", description='" + itemDescription + "'"
             + ", qty=" + quantity
             + ", subtotal=" + subtotal + "}";
    }
}