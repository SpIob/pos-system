package model;

import java.sql.Timestamp;

/**
 * Represents a completed sales transaction.
 * Maps to the `transactions` table in the database.
 */
public class Transaction {

    private int       transactionId;
    private int       sessionId;      // 0 if not linked to a session
    private int       userId;
    private double    totalAmount;
    private double    amountPaid;
    private double    changeGiven;
    private Timestamp transactionDate;

    // Constructors
    public Transaction() {}

    public Transaction(int transactionId, int sessionId, int userId,
                       double totalAmount, double amountPaid,
                       double changeGiven, Timestamp transactionDate) {
        this.transactionId   = transactionId;
        this.sessionId       = sessionId;
        this.userId          = userId;
        this.totalAmount     = totalAmount;
        this.amountPaid      = amountPaid;
        this.changeGiven     = changeGiven;
        this.transactionDate = transactionDate;
    }

    // Getters
    public int getTransactionId() {
        return transactionId;
    }

    public int getSessionId() {
        return sessionId;
    }

    public int getUserId() {
        return userId;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public double getAmountPaid() {
        return amountPaid;
    }

    public double getChangeGiven() {
        return changeGiven;
    }

    public Timestamp getTransactionDate() {
        return transactionDate;
    }

    // Setters
    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public void setAmountPaid(double amountPaid) {
        this.amountPaid = amountPaid;
    }

    public void setChangeGiven(double changeGiven) {
        this.changeGiven = changeGiven;
    }

    public void setTransactionDate(Timestamp transactionDate) {
        this.transactionDate = transactionDate;
    }

    // Convenience

    /**
     * Formats the transaction ID as "TXN-0042" for display on receipts
     * and in tables.
     */
    public String getFormattedId() {
        return String.format("TXN-%04d", transactionId);
    }

    public boolean isLinkedToSession() {
        return sessionId > 0;
    }

    @Override
    public String toString() {
        return "Transaction{transactionId=" + transactionId
             + ", total=" + totalAmount
             + ", date=" + transactionDate + "}";
    }
}