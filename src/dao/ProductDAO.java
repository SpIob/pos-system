package dao;

import database.DBConnection;
import model.Product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * ProductDAO.java
 * Handles all database operations for the products table.
 */
public class ProductDAO {

    // Get all products (used by ProductsPanel and SalesPanel)
    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();

        String sql = "SELECT product_id, product_name, category, price, "
                   + "stock_quantity, low_stock_threshold, created_at "
                   + "FROM products ORDER BY product_name ASC";

        Connection conn = DBConnection.getConnection();
        if (conn == null) return products;

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                products.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("[ProductDAO] getAllProducts() failed.");
            e.printStackTrace();
        } finally {
            DBConnection.closeConnection(conn);
        }

        return products;
    }

    // Get only low-stock products (used by alert banners)
    public List<Product> getLowStockProducts() {
        List<Product> products = new ArrayList<>();

        String sql = "SELECT product_id, product_name, category, price, "
                   + "stock_quantity, low_stock_threshold, created_at "
                   + "FROM products "
                   + "WHERE stock_quantity <= low_stock_threshold "
                   + "ORDER BY product_name ASC";
        

        Connection conn = DBConnection.getConnection();
        if (conn == null) return products;

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                products.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("[ProductDAO] getLowStockProducts() failed.");
            e.printStackTrace();
        } finally {
            DBConnection.closeConnection(conn);
        }

        return products;
    }

    // Count of low-stock products (used by tab badges)
    public int getLowStockCount() {
        String sql = "SELECT COUNT(*) FROM products "
                   + "WHERE stock_quantity <= low_stock_threshold";

        Connection conn = DBConnection.getConnection();
        if (conn == null) return 0;

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) return rs.getInt(1);

        } catch (SQLException e) {
            System.err.println("[ProductDAO] getLowStockCount() failed.");
            e.printStackTrace();
        } finally {
            DBConnection.closeConnection(conn);
        }

        return 0;
    }

    // Add a new product
    public boolean addProduct(Product product) {
        String sql = "INSERT INTO products "
                   + "(product_name, category, price, "
                   + "stock_quantity, low_stock_threshold) "
                   + "VALUES (?, ?, ?, ?, ?)";

        Connection conn = DBConnection.getConnection();
        if (conn == null) return false;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, product.getProductName());
            stmt.setString(2, product.getCategory().toLowerCase());
            stmt.setDouble(3, product.getPrice());
            stmt.setInt(4,    product.getStockQuantity());
            stmt.setInt(5,    product.getLowStockThreshold());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[ProductDAO] addProduct() failed.");
            e.printStackTrace();
        } finally {
            DBConnection.closeConnection(conn);
        }

        return false;
    }

    // Update an existing product
    public boolean updateProduct(Product product) {
        String sql = "UPDATE products SET "
                   + "product_name = ?, category = ?, price = ?, "
                   + "stock_quantity = ?, low_stock_threshold = ? "
                   + "WHERE product_id = ?";

        Connection conn = DBConnection.getConnection();
        if (conn == null) return false;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, product.getProductName());
            stmt.setString(2, product.getCategory().toLowerCase());
            stmt.setDouble(3, product.getPrice());
            stmt.setInt(4,    product.getStockQuantity());
            stmt.setInt(5,    product.getLowStockThreshold());
            stmt.setInt(6,    product.getProductId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[ProductDAO] updateProduct() failed.");
            e.printStackTrace();
        } finally {
            DBConnection.closeConnection(conn);
        }

        return false;
    }

    // Delete a product by ID
    public boolean deleteProduct(int productId) {
        String sql = "DELETE FROM products WHERE product_id = ?";

        Connection conn = DBConnection.getConnection();
        if (conn == null) return false;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, productId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[ProductDAO] deleteProduct() failed.");
            e.printStackTrace();
        } finally {
            DBConnection.closeConnection(conn);
        }

        return false;
    }

    // Reduce stock by quantity sold (called during a transaction)
    public boolean reduceStock(int productId, int quantitySold) {
        String sql = "UPDATE products "
                   + "SET stock_quantity = stock_quantity - ? "
                   + "WHERE product_id = ? AND stock_quantity >= ?";

        Connection conn = DBConnection.getConnection();
        if (conn == null) return false;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, quantitySold);
            stmt.setInt(2, productId);
            stmt.setInt(3, quantitySold); // prevents going below 0

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[ProductDAO] reduceStock() failed.");
            e.printStackTrace();
        } finally {
            DBConnection.closeConnection(conn);
        }

        return false;
    }

    // Map a ResultSet row to a Product object
    private Product mapRow(ResultSet rs) throws SQLException {
        return new Product(
            rs.getInt("product_id"),
            rs.getString("product_name"),
            rs.getString("category"),
            rs.getDouble("price"),
            rs.getInt("stock_quantity"),
            rs.getInt("low_stock_threshold"),
            rs.getTimestamp("created_at")
        );
    }
}