package dao;

import database.DBConnection;
import model.Transaction;
import model.TransactionItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * TransactionDAO.java
 * Handles all database operations for the transactions
 * and transaction_items tables.
 *
 * saveTransaction() writes both tables atomically using
 * a single Connection so either everything is saved or nothing is.
 */
public class TransactionDAO {

    // ---------------------------------------------------------------
    // Save a full transaction with all its line items
    // This is the main method called by SalesPanel on CHARGE
    // Returns the new transaction_id, or -1 on failure
    // ---------------------------------------------------------------
    public int saveTransaction(Transaction transaction,
                                List<TransactionItem> items) {
        String txnSql = "INSERT INTO transactions "
                      + "(session_id, user_id, total_amount, "
                      + "amount_paid, change_given) "
                      + "VALUES (?, ?, ?, ?, ?)";

        String itemSql = "INSERT INTO transaction_items "
                       + "(transaction_id, product_id, item_description, "
                       + "quantity, unit_price, subtotal) "
                       + "VALUES (?, ?, ?, ?, ?, ?)";

        Connection conn = DBConnection.getConnection();
        if (conn == null) return -1;

        try {
            // Disable auto-commit so both inserts happen together
            conn.setAutoCommit(false);

            // Insert the transaction header
            int newTransactionId;
            try (PreparedStatement txnStmt = conn.prepareStatement(
                    txnSql, PreparedStatement.RETURN_GENERATED_KEYS)) {

                // session_id is nullable — use 0 to mean "no session"
                if (transaction.getSessionId() > 0) {
                    txnStmt.setInt(1, transaction.getSessionId());
                } else {
                    txnStmt.setNull(1, java.sql.Types.INTEGER);
                }

                txnStmt.setInt(2,    transaction.getUserId());
                txnStmt.setDouble(3, transaction.getTotalAmount());
                txnStmt.setDouble(4, transaction.getAmountPaid());
                txnStmt.setDouble(5, transaction.getChangeGiven());
                txnStmt.executeUpdate();

                ResultSet keys = txnStmt.getGeneratedKeys();
                if (!keys.next()) {
                    conn.rollback();
                    return -1;
                }
                newTransactionId = keys.getInt(1);
            }

            // Insert each line item
            try (PreparedStatement itemStmt =
                    conn.prepareStatement(itemSql)) {

                for (TransactionItem item : items) {
                    itemStmt.setInt(1, newTransactionId);

                    if (item.getProductId() > 0) {
                        itemStmt.setInt(2, item.getProductId());
                    } else {
                        itemStmt.setNull(2, java.sql.Types.INTEGER);
                    }

                    itemStmt.setString(3, item.getItemDescription());
                    itemStmt.setInt(4,    item.getQuantity());
                    itemStmt.setDouble(5, item.getUnitPrice());
                    itemStmt.setDouble(6, item.getSubtotal());
                    itemStmt.addBatch();
                }
                itemStmt.executeBatch();
            }

            // All good — commit both inserts together
            conn.commit();
            return newTransactionId;

        } catch (SQLException e) {
            e.printStackTrace();
            try { conn.rollback(); }
            catch (SQLException ex) { ex.printStackTrace(); }

        } finally {
            try { conn.setAutoCommit(true); }
            catch (SQLException ex) { ex.printStackTrace(); }
            DBConnection.closeConnection(conn);
        }

        return -1;
    }

    // ---------------------------------------------------------------
    // Get the most recent N transactions
    // Used by DashboardPanel and ReportsPanel tables
    // ---------------------------------------------------------------
    public List<Transaction> getRecentTransactions(int limit) {
        List<Transaction> list = new ArrayList<>();

        String sql = "SELECT transaction_id, session_id, user_id, "
                   + "total_amount, amount_paid, change_given, "
                   + "transaction_date "
                   + "FROM transactions "
                   + "ORDER BY transaction_date DESC "
                   + "LIMIT ?";

        Connection conn = DBConnection.getConnection();
        if (conn == null) return list;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeConnection(conn);
        }

        return list;
    }

    // ---------------------------------------------------------------
    // Get total sales amount for today
    // Used by Dashboard and Reports KPI cards
    // ---------------------------------------------------------------
    public double getTodayTotal() {
        String sql = "SELECT COALESCE(SUM(total_amount), 0) "
                   + "FROM transactions "
                   + "WHERE DATE(transaction_date) = CURDATE()";

        Connection conn = DBConnection.getConnection();
        if (conn == null) return 0;

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) return rs.getDouble(1);

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeConnection(conn);
        }

        return 0;
    }

    // ---------------------------------------------------------------
    // Get number of transactions today
    // Used by Reports KPI card "Total Transactions"
    // ---------------------------------------------------------------
    public int getTodayCount() {
        String sql = "SELECT COUNT(*) FROM transactions "
                   + "WHERE DATE(transaction_date) = CURDATE()";

        Connection conn = DBConnection.getConnection();
        if (conn == null) return 0;

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) return rs.getInt(1);

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeConnection(conn);
        }

        return 0;
    }

    // ---------------------------------------------------------------
    // Get all line items for a specific transaction
    // Useful for reprinting a receipt
    // ---------------------------------------------------------------
    public List<TransactionItem> getItemsByTransaction(int transactionId) {
        List<TransactionItem> items = new ArrayList<>();

        String sql = "SELECT item_id, transaction_id, product_id, "
                   + "item_description, quantity, unit_price, subtotal "
                   + "FROM transaction_items "
                   + "WHERE transaction_id = ? "
                   + "ORDER BY item_id ASC";

        Connection conn = DBConnection.getConnection();
        if (conn == null) return items;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, transactionId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                items.add(mapItemRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeConnection(conn);
        }

        return items;
    }
    
    // ---------------------------------------------------------------
    // Void a transaction — marks it as voided and restores stock
    // Uses an atomic DB transaction so both changes succeed together
    // ---------------------------------------------------------------
    public boolean voidTransaction(int transactionId) {
        String markVoided =
            "UPDATE transactions SET status = 'voided' "
          + "WHERE transaction_id = ? AND status = 'completed'";

        String getItems =
            "SELECT product_id, quantity FROM transaction_items "
          + "WHERE transaction_id = ? AND product_id IS NOT NULL";

        String restoreStock =
            "UPDATE products "
          + "SET stock_quantity = stock_quantity + ? "
          + "WHERE product_id = ?";

        Connection conn = DBConnection.getConnection();
        if (conn == null) return false;

        try {
            conn.setAutoCommit(false);

            // Step 1: mark transaction as voided
            try (PreparedStatement stmt =
                    conn.prepareStatement(markVoided)) {
                stmt.setInt(1, transactionId);
                int updated = stmt.executeUpdate();
                if (updated == 0) {
                    // Already voided or doesn't exist
                    conn.rollback();
                    return false;
                }
            }

            // Step 2: restore stock for each product item
            try (PreparedStatement getStmt =
                    conn.prepareStatement(getItems)) {
                getStmt.setInt(1, transactionId);
                ResultSet rs = getStmt.executeQuery();

                try (PreparedStatement restoreStmt =
                        conn.prepareStatement(restoreStock)) {
                    while (rs.next()) {
                        restoreStmt.setInt(1, rs.getInt("quantity"));
                        restoreStmt.setInt(2, rs.getInt("product_id"));
                        restoreStmt.addBatch();
                    }
                    restoreStmt.executeBatch();
                }
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            try { conn.rollback(); }
            catch (SQLException ex) { ex.printStackTrace(); }
        } finally {
            try { conn.setAutoCommit(true); }
            catch (SQLException ex) { ex.printStackTrace(); }
            DBConnection.closeConnection(conn);
        }
        return false;
    }

    // ---------------------------------------------------------------
    // Rich rows — now also includes transaction status
    // Returns: {txnId, date, itemCount, total, username, status}
    // ---------------------------------------------------------------
    public java.util.List<Object[]> getRecentTransactionsRich(
            int limit) {
        java.util.List<Object[]> rows = new java.util.ArrayList<>();

        String sql =
            "SELECT t.transaction_id, "
          + "       t.transaction_date, "
          + "       COUNT(ti.item_id)  AS item_count, "
          + "       t.total_amount, "
          + "       u.username, "
          + "       t.status "
          + "FROM transactions t "
          + "LEFT JOIN transaction_items ti "
          + "       ON ti.transaction_id = t.transaction_id "
          + "LEFT JOIN users u "
          + "       ON u.user_id = t.user_id "
          + "GROUP BY t.transaction_id, t.transaction_date, "
          + "         t.total_amount, u.username, t.status "
          + "ORDER BY t.transaction_date DESC "
          + "LIMIT ?";

        Connection conn = DBConnection.getConnection();
        if (conn == null) return rows;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                rows.add(new Object[]{
                    rs.getInt("transaction_id"),
                    rs.getTimestamp("transaction_date"),
                    rs.getInt("item_count"),
                    rs.getDouble("total_amount"),
                    rs.getString("username"),
                    rs.getString("status")   // "completed" or "voided"
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeConnection(conn);
        }
        return rows;
    }
    
    // ---------------------------------------------------------------
    // Get the number of line items for a transaction
    // Used to populate the "Items" column in transaction tables
    // ---------------------------------------------------------------
    public int getItemCount(int transactionId) {
        String sql = "SELECT COUNT(*) FROM transaction_items "
                   + "WHERE transaction_id = ?";

        Connection conn = DBConnection.getConnection();
        if (conn == null) return 0;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, transactionId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1);

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeConnection(conn);
        }

        return 0;
    }

    // Map a ResultSet row to a Transaction object
    private Transaction mapRow(ResultSet rs) throws SQLException {
        return new Transaction(
            rs.getInt("transaction_id"),
            rs.getInt("session_id"),
            rs.getInt("user_id"),
            rs.getDouble("total_amount"),
            rs.getDouble("amount_paid"),
            rs.getDouble("change_given"),
            rs.getTimestamp("transaction_date")
        );
    }

    // Map a ResultSet row to a TransactionItem object
    private TransactionItem mapItemRow(ResultSet rs) throws SQLException {
        return new TransactionItem(
            rs.getInt("item_id"),
            rs.getInt("transaction_id"),
            rs.getInt("product_id"),
            rs.getString("item_description"),
            rs.getInt("quantity"),
            rs.getDouble("unit_price"),
            rs.getDouble("subtotal")
        );
    }
}