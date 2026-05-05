package dao;

import database.DBConnection;
import model.Product;
import util.GlobalSettings;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT product_id, product_name, category, price, "
                   + "stock_quantity, low_stock_threshold, created_at "
                   + "FROM products ORDER BY product_name ASC";
        Connection conn = DBConnection.getConnection();
        if (conn == null) return products;
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) products.add(mapRow(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        } finally { DBConnection.closeConnection(conn); }
        return products;
    }

    public List<Product> getLowStockProducts() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT product_id, product_name, category, price, "
                   + "stock_quantity, low_stock_threshold, created_at "
                   + "FROM products WHERE stock_quantity <= ? ORDER BY product_name ASC";
        Connection conn = DBConnection.getConnection();
        if (conn == null) return products;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, GlobalSettings.LOW_STOCK_THRESHOLD);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) products.add(mapRow(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        } finally { DBConnection.closeConnection(conn); }
        return products;
    }

    public int getLowStockCount() {
        Connection conn = DBConnection.getConnection();
        if (conn == null) return 0;
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT COUNT(*) FROM products WHERE stock_quantity <= ?")) {
            stmt.setInt(1, GlobalSettings.LOW_STOCK_THRESHOLD);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally { DBConnection.closeConnection(conn); }
        return 0;
    }

    public boolean addProduct(Product product) {
        String sql = "INSERT INTO products (product_name, category, price, "
                   + "stock_quantity, low_stock_threshold) VALUES (?, ?, ?, ?, ?)";
        Connection conn = DBConnection.getConnection();
        if (conn == null) return false;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, product.getProductName());
            stmt.setString(2, product.getCategory().toLowerCase());
            stmt.setDouble(3, product.getPrice());
            stmt.setInt(4,    product.getStockQuantity());
            stmt.setInt(5,    GlobalSettings.LOW_STOCK_THRESHOLD);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally { DBConnection.closeConnection(conn); }
        return false;
    }

    public boolean updateProduct(Product product) {
        String sql = "UPDATE products SET product_name = ?, category = ?, price = ?, "
                   + "stock_quantity = ?, low_stock_threshold = ? WHERE product_id = ?";
        Connection conn = DBConnection.getConnection();
        if (conn == null) return false;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, product.getProductName());
            stmt.setString(2, product.getCategory().toLowerCase());
            stmt.setDouble(3, product.getPrice());
            stmt.setInt(4,    product.getStockQuantity());
            stmt.setInt(5,    GlobalSettings.LOW_STOCK_THRESHOLD);
            stmt.setInt(6,    product.getProductId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally { DBConnection.closeConnection(conn); }
        return false;
    }

    public boolean deleteProduct(int productId) {
        Connection conn = DBConnection.getConnection();
        if (conn == null) return false;
        try (PreparedStatement stmt = conn.prepareStatement(
                "DELETE FROM products WHERE product_id = ?")) {
            stmt.setInt(1, productId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally { DBConnection.closeConnection(conn); }
        return false;
    }

    public boolean reduceStock(int productId, int qty) {
        Connection conn = DBConnection.getConnection();
        if (conn == null) return false;
        try (PreparedStatement stmt = conn.prepareStatement(
                "UPDATE products SET stock_quantity = stock_quantity - ? "
              + "WHERE product_id = ? AND stock_quantity >= ?")) {
            stmt.setInt(1, qty); stmt.setInt(2, productId); stmt.setInt(3, qty);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally { DBConnection.closeConnection(conn); }
        return false;
    }

    public boolean restoreStock(int productId, int qty) {
        Connection conn = DBConnection.getConnection();
        if (conn == null) return false;
        try (PreparedStatement stmt = conn.prepareStatement(
                "UPDATE products SET stock_quantity = stock_quantity + ? WHERE product_id = ?")) {
            stmt.setInt(1, qty); stmt.setInt(2, productId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally { DBConnection.closeConnection(conn); }
        return false;
    }

    private Product mapRow(ResultSet rs) throws SQLException {
        return new Product(rs.getInt("product_id"), rs.getString("product_name"),
            rs.getString("category"), rs.getDouble("price"),
            rs.getInt("stock_quantity"), rs.getInt("low_stock_threshold"),
            rs.getTimestamp("created_at"));
    }
}