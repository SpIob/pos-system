package model;

import java.sql.Timestamp;

public class Transaction {
    private int transactionId;
    private int sessionId;
    private int userId;
    private double totalAmount;
    private double amountPaid;
    private double changeGiven;
    private Timestamp transactionDate;

    public Transaction() {}

    public Transaction(int transactionId, int sessionId, int userId,
                       double totalAmount, double amountPaid, double changeGiven,
                       Timestamp transactionDate) {
        this.transactionId = transactionId; this.sessionId = sessionId;
        this.userId = userId; this.totalAmount = totalAmount;
        this.amountPaid = amountPaid; this.changeGiven = changeGiven;
        this.transactionDate = transactionDate;
    }

    public int getTransactionId()         { return transactionId; }
    public int getSessionId()             { return sessionId; }
    public int getUserId()                { return userId; }
    public double getTotalAmount()        { return totalAmount; }
    public double getAmountPaid()         { return amountPaid; }
    public double getChangeGiven()        { return changeGiven; }
    public Timestamp getTransactionDate() { return transactionDate; }

    public void setTransactionId(int v)        { this.transactionId = v; }
    public void setSessionId(int v)            { this.sessionId = v; }
    public void setUserId(int v)               { this.userId = v; }
    public void setTotalAmount(double v)       { this.totalAmount = v; }
    public void setAmountPaid(double v)        { this.amountPaid = v; }
    public void setChangeGiven(double v)       { this.changeGiven = v; }
    public void setTransactionDate(Timestamp v){ this.transactionDate = v; }

    public String getFormattedId()     { return String.format("TXN-%04d", transactionId); }
    public boolean isLinkedToSession() { return sessionId > 0; }

    @Override
    public String toString() {
        return "Transaction{transactionId=" + transactionId + ", total=" + totalAmount + ", date=" + transactionDate + "}";
    }
}