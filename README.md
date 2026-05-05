# ☕ ByteZone Café POS System

> A Point of Sale system for an internet café and gaming shop — built with Java Swing, MySQL on Railway, and a clean layered architecture.

![Java](https://img.shields.io/badge/Java-17%2B-orange?logo=java)
![MySQL](https://img.shields.io/badge/Database-MySQL-blue?logo=mysql)
![Railway](https://img.shields.io/badge/Hosted%20on-Railway-purple?logo=railway)
![NetBeans](https://img.shields.io/badge/IDE-Apache%20NetBeans-green)
![Status](https://img.shields.io/badge/Status-In%20Development-yellow)

---

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [Innovative Features](#2-innovative-features)
3. [Technical Layout](#3-technical-layout)
4. [Database Design](#4-database-design)
5. [System Flowchart](#5-system-flowchart)
6. [Getting Started](#6-getting-started)
7. [Project Structure](#7-project-structure)
8. [Coding Standards](#8-coding-standards)
9. [Git Workflow](#9-git-workflow)
10. [Team Roles](#10-team-roles)
11. [Deliverables Checklist](#11-deliverables-checklist)

---

## 1. Project Overview

**ByteZone Café POS** is a desktop Point of Sale application designed for an internet café and gaming shop. The system handles two core revenue streams simultaneously:

- **PC Station Billing** — time-based billing for Regular (₱20/hr) and VIP (₱40/hr) stations
- **Product Sales** — snacks, beverages, and peripheral rentals (headsets, USB drives)

Two staff roles are supported:

| Role | Access Level |
|------|-------------|
| Admin | Full access: products, reports, user management, sales history |
| Cashier | Sales transactions, session management, receipt generation |

### Why MySQL?

MySQL was chosen over SQLite for the following reasons:

- **Concurrent access** — multiple cashier terminals can connect to a single shared database without file-locking issues, unlike SQLite.
- **Railway hosting** — the team can share one live database during development without each member running a local MySQL server.
- **Production readiness** — the schema can migrate directly to any production MySQL host with zero changes.
- **ENUM and FK support** — MySQL enforces data integrity (roles, station types, session statuses) at the database level, reducing defensive code in Java.

---

## 2. Innovative Features

Beyond the minimum POS requirements, ByteZone includes the following innovations:

### 🖥️ Dual Billing Mode
The system handles station time billing and product sales **in the same transaction**. A customer ending a gaming session can have snacks added to their bill and pay everything in one receipt.

### 📊 Admin Analytics Dashboard
- Daily and weekly revenue summaries
- Top-selling products
- Peak-hour station occupancy chart (hour-by-hour bar chart using Java2D)
- Low-stock alerts displayed on the dashboard home screen

### ⚠️ Inventory Alert System
Products with `stock_quantity` at or below `low_stock_threshold` are flagged automatically. The cashier UI shows a warning badge; the admin dashboard shows a full low-stock report.

### 🧾 Itemized Receipt Generation
Receipts are generated as formatted text output (printable via `javax.print`) with:
- Line-by-line product breakdown
- PC session duration and charge
- Amount paid and change given
- Timestamp and transaction ID

### 🔐 Role-Based Access Control (RBAC)
Login determines which screens are accessible. Cashiers cannot access product management or sales reports. All UI navigation is gated by the authenticated user's role stored in the session.

### 🕒 Live Station Status Board
The main cashier screen shows a real-time grid of all stations — color-coded by status (green = available, red = occupied, grey = maintenance) with elapsed time displayed for active sessions.

---

## 3. Technical Layout

### Architecture Overview

The application follows a **four-layer architecture** that separates concerns cleanly:

```
┌─────────────────────────────────────────┐
│              UI Layer (ui/)             │  ← Java Swing JFrames, JPanels
│   LoginFrame, DashboardFrame, POSPanel  │
├─────────────────────────────────────────┤
│           DAO Layer (dao/)              │  ← Data Access Objects
│   UserDAO, ProductDAO, SessionDAO, etc. │
├─────────────────────────────────────────┤
│          Model Layer (model/)           │  ← Plain Java Objects (POJOs)
│   User, Product, Station, Transaction   │
├─────────────────────────────────────────┤
│        Database Layer (database/)       │  ← JDBC Connection Management
│   DBConnection (loads config.properties)│
└─────────────────────────────────────────┘
                     │
                     ▼
         MySQL Database on Railway
```

### Components

| Component | Technology | Purpose |
|-----------|-----------|---------|
| GUI Framework | Java Swing | All screen rendering and user interaction |
| IDE | Apache NetBeans 21 | Project management, build, run |
| Language | Java 17 (LTS) | Application logic |
| Database | MySQL 8 via Railway | Persistent storage |
| JDBC Driver | MySQL Connector/J | Java ↔ MySQL communication |
| Config | `config.properties` | Credential management (not in Git) |
| Version Control | GitHub (feature branches) | Collaborative development |
| Design | Figma, Canva | UI wireframes and mockups |

### Tools and Technologies

```
Java 17+                 — Core language
Apache NetBeans 21       — IDE and build tool
MySQL 8 (Railway)        — Cloud-hosted relational database
MySQL Connector/J JAR    — JDBC driver (added to NetBeans Libraries)
GitHub                   — Version control and PR review
GitHub Desktop           — GUI Git client for teammates
Railway                  — MySQL hosting (public proxy for local dev)
Figma / Canva            — UI design and wireframes
Python + PIL             — Annotated wireframe PNG generation
```

---

## 4. Database Design

The database is named `cafe_pos_db` and consists of six tables.

### Entity Relationship Summary

```
users ──────────────────────┐
  │                         │
  │ (manages sessions)       │ (processes transactions)
  ▼                         ▼
sessions ◄──── stations    transactions ◄──── transaction_items
                                                     │
                                              references products
```

### Tables

| Table | Description |
|-------|-------------|
| `users` | Staff accounts with role (`admin` / `cashier`) |
| `stations` | PC stations with type (`regular` / `vip`), hourly rate, and availability status |
| `sessions` | Active and completed PC usage sessions, linked to a station and the cashier who opened it |
| `products` | Café inventory (snacks, beverages, other) with price, stock, and low-stock threshold |
| `transactions` | Payment records linked to an optional session; stores amount paid and change given |
| `transaction_items` | Line items for each transaction — either a product or a PC session charge |

### Key Schema Decisions

- `sessions.user_id` references the **cashier** who opened the session, not the customer (the system has no customer accounts).
- `transaction_items.product_id` is nullable — PC session charges are inserted as description-only items (e.g., "PC-01 — 2h 30m").
- `transactions.session_id` is nullable — product-only sales (walk-in snack purchase) have no associated session.
- Passwords are stored using `SHA2(..., 256)`. In production, upgrade to bcrypt via a Java library.

### Setting Up the Database

Run `db-schema.sql` once against your Railway MySQL instance to create all tables and seed sample data:

```sql
-- In MySQL Workbench or any MySQL client connected to Railway:
SOURCE /path/to/db-schema.sql;
```

Or paste the file contents directly into the Railway MySQL console.

---

## 5. System Flowchart

### Login Flow

```
Start
  │
  ▼
Show Login Screen
  │
  ▼
User enters username + password
  │
  ▼
Query users table (SHA2 hash match)
  ├── No match ──► Show "Invalid credentials" ──► Back to Login
  │
  └── Match ──► Load role from DB
                    │
                    ├── role = 'admin' ──► Admin Dashboard
                    └── role = 'cashier' ──► Cashier Dashboard
```

### Sales Transaction Flow

```
Cashier Dashboard
  │
  ├─── [New PC Session] ──────────────────────────────┐
  │        │                                          │
  │        ▼                                          │
  │    Select available station                       │
  │        │                                          │
  │        ▼                                          │
  │    Insert session record (status = 'active')      │
  │        │                                          │
  │        ▼                                          │
  │    Station turns RED on status board              │
  │                                                   │
  ├─── [End Session / Checkout] ◄─────────────────────┘
  │        │
  │        ▼
  │    Calculate duration → session_charge
  │        │
  │        ▼
  │    Optionally add products to cart
  │        │
  │        ▼
  │    Show total → cashier enters amount paid
  │        │
  │        ▼
  │    INSERT transaction + transaction_items
  │        │
  │        ▼
  │    UPDATE session status = 'completed'
  │        │
  │        ▼
  │    UPDATE station status = 'available'
  │        │
  │        ▼
  │    Print / display receipt
  │
  └─── [Product-only Sale]
           │
           ▼
       Add products to cart
           │
           ▼
       Enter amount paid → compute change
           │
           ▼
       INSERT transaction + transaction_items (no session)
           │
           ▼
       Deduct stock_quantity for each product
           │
           ▼
       Display receipt
```

### Admin: Product Management Flow

```
Admin Dashboard → Product Management
  │
  ├── [Add Product] → Enter name, category, price, qty, threshold → INSERT
  ├── [Edit Product] → Select product → Modify fields → UPDATE
  └── [Delete Product] → Select product → Confirm → DELETE
                              │
                              └── Check: product in transaction_items?
                                    └── If yes: soft-delete or block (prevent FK violation)
```

---

## 6. Getting Started

### Prerequisites

| Software | Minimum Version | Download |
|----------|----------------|----------|
| Apache NetBeans | 21 (LTS) | https://netbeans.apache.org |
| Java JDK | 17 (LTS) | https://adoptium.net |
| MySQL Connector/J | 8.x | https://dev.mysql.com/downloads/connector/j/ |
| Git | Any recent | https://git-scm.com |
| GitHub Desktop | Any recent | https://desktop.github.com |

---

### Step 1 — Clone the Repository

```bash
# Using terminal
git clone https://github.com/SpIob/pos-system.git
cd pos-system
```

Or use **GitHub Desktop → File → Clone Repository** and search for `pos-system`.

---

### Step 2 — Open in NetBeans

1. Open **Apache NetBeans**
2. **File → Open Project**
3. Navigate to your cloned folder and open it
4. If red error marks appear, continue to Step 3

---

### Step 3 — Add the JDBC Driver

1. In the **Projects** panel, right-click **Libraries → Add JAR/Folder**
2. Navigate to `mysql-connector-j-X.X.X.jar` (from your Downloads)
3. Click **Open** — red errors should clear

---

### Step 4 — Create config.properties

This file is excluded from Git (it contains database credentials). Create it manually:

```
src/
└── config/
    └── config.properties    ← create this file
```

Paste the following template and fill in credentials from the team leader:

```properties
db.host=YOUR_RAILWAY_PUBLIC_HOST
db.port=YOUR_RAILWAY_PORT
db.database=railway
db.username=root
db.password=YOUR_RAILWAY_PASSWORD
db.timezone=Asia/Manila
```

> **NEVER commit this file.** It is already listed in `.gitignore`.

---

### Step 5 — Verify the Connection

1. In NetBeans, expand **Source Packages → test**
2. Right-click `TestConnection.java` → **Run File**
3. Check the Output panel — you should see:

```
[✔] Successfully connected to Railway MySQL!
[✔] Table found: users
[✔] Table found: stations
[✔] Table found: sessions
[✔] Table found: products
[✔] Table found: transactions
[✔] Table found: transaction_items
```

If any table shows `[✘]`, run `db-schema.sql` against the Railway database first.

---

## 7. Project Structure

```
pos-system/
│
├── src/
│   ├── config/
│   │   └── config.properties          ← DB credentials (NOT in Git)
│   │
│   ├── database/
│   │   └── DBConnection.java          ← JDBC connection manager
│   │
│   ├── model/
│   │   ├── User.java                  ← POJO for users table
│   │   ├── Station.java               ← POJO for stations table
│   │   ├── Session.java               ← POJO for sessions table
│   │   ├── Product.java               ← POJO for products table
│   │   ├── Transaction.java           ← POJO for transactions table
│   │   └── TransactionItem.java       ← POJO for transaction_items table
│   │
│   ├── dao/
│   │   ├── UserDAO.java               ← CRUD + login query for users
│   │   ├── StationDAO.java            ← Station availability management
│   │   ├── SessionDAO.java            ← Open/close/calculate sessions
│   │   ├── ProductDAO.java            ← Product CRUD + stock updates
│   │   ├── TransactionDAO.java        ← Insert transactions, fetch history
│   │   └── TransactionItemDAO.java    ← Insert/fetch line items
│   │
│   ├── ui/
│   │   ├── LoginFrame.java            ← Login screen
│   │   ├── AdminDashboard.java        ← Admin home + analytics
│   │   ├── CashierDashboard.java      ← Cashier home + station board
│   │   ├── POSPanel.java              ← Transaction / checkout screen
│   │   ├── ProductManagementPanel.java← Add/Edit/Delete products (admin)
│   │   ├── ReportsPanel.java          ← Sales and inventory reports
│   │   └── ReceiptDialog.java         ← Receipt display + print
│   │
│   └── test/
│       └── TestConnection.java        ← One-time connection verifier
│
├── db-schema.sql                      ← Full database setup script
├── .gitignore                         ← Excludes config.properties, build/
└── README.md                          ← This file
```

---

## 8. Coding Standards

All Java code in this project follows these conventions. Consistency makes code review faster and collaboration smoother.

### Naming Conventions

```java
// Classes: PascalCase
public class TransactionDAO { }
public class ProductManagementPanel extends JPanel { }

// Methods: camelCase, verb-noun
public Product findProductById(int productId) { }
public boolean updateStockQuantity(int productId, int newQuantity) { }
public void displayLowStockAlert() { }

// Variables: camelCase, descriptive
int sessionDurationMinutes;
double totalAmountDue;
boolean isStationAvailable;

// Constants: UPPER_SNAKE_CASE
private static final String CONFIG_PATH = "/config/config.properties";
private static final int LOW_STOCK_THRESHOLD_DEFAULT = 5;
private static final double REGULAR_RATE_PER_HOUR = 20.00;
```

### DAO Pattern

Every DAO class follows the same structure: one method per database operation, PreparedStatements only (no string concatenation), and explicit connection closing in `finally` blocks.

```java
// GOOD — parameterized query, proper resource closing
public Product findProductById(int productId) {
    String sql = "SELECT * FROM products WHERE product_id = ?";
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setInt(1, productId);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            return mapRowToProduct(rs);
        }
    } catch (SQLException e) {
        System.err.println("[ProductDAO] Error fetching product: " + e.getMessage());
    }
    return null;
}

// BAD — never do this (SQL injection risk)
String sql = "SELECT * FROM products WHERE product_id = " + productId;
```

### Model (POJO) Pattern

Model classes are plain data holders with no logic. They match the database table columns one-to-one.

```java
public class Product {
    private int productId;
    private String productName;
    private String category;
    private double price;
    private int stockQuantity;
    private int lowStockThreshold;

    // Constructor, getters, setters only
    // No database calls inside a model class
}
```

### UI Layer Rules

- UI classes extend `JFrame` (for top-level windows) or `JPanel` (for embedded screens).
- All database operations are called via DAO methods — **never write SQL inside a UI class**.
- User input is validated before any DAO call (empty field checks, numeric format checks).
- Use `SwingUtilities.invokeLater()` for all UI updates that originate from non-EDT threads.

### Error Handling

```java
// GOOD — catch specific exceptions, print descriptive messages
try {
    Connection conn = DBConnection.getConnection();
    // ...
} catch (SQLException e) {
    System.err.println("[SessionDAO] Failed to open session: " + e.getMessage());
    JOptionPane.showMessageDialog(null, "Database error. Please try again.", 
        "Error", JOptionPane.ERROR_MESSAGE);
}

// BAD — silent catch, or catch-all with no context
} catch (Exception e) {
    e.printStackTrace();
}
```

### No Magic Numbers

```java
// BAD
if (stockQuantity <= 5) { showAlert(); }

// GOOD
private static final int LOW_STOCK_THRESHOLD_DEFAULT = 5;
if (stockQuantity <= LOW_STOCK_THRESHOLD_DEFAULT) { showAlert(); }
```

---

## 9. Git Workflow

We use **feature branches** — never commit directly to `main`.

### Branch Naming

```
feature/your-name-what-you-built

Examples:
  feature/maria-login-ui
  feature/juan-product-dao
  feature/jim-session-billing
  feature/ana-reports-panel
```

### Daily Workflow

```bash
# 1. Get the latest changes before starting
git checkout main
git pull origin main

# 2. Create your feature branch
git checkout -b feature/your-name-feature-name

# 3. Work in NetBeans, then commit often
git add .
git commit -m "Add ProductDAO findAll method with category filter"

# 4. Push your branch
git push origin feature/your-name-feature-name

# 5. When done: open a Pull Request on GitHub
#    → Go to github.com/SpIob/pos-system
#    → Click "Compare & pull request"
#    → Add a short description
#    → Request review from the team leader
```

### Commit Message Format

```
<action> <what was changed>

Good examples:
  Add UserDAO login authentication method
  Fix station status not updating after session close
  Refactor DBConnection to use try-with-resources
  Update product stock after transaction insert

Bad examples:
  fix stuff
  changes
  wip
```

### What NOT to Commit

The following are already in `.gitignore` — double-check before pushing:

```
config.properties        ← contains Railway credentials
build/                   ← NetBeans compiled output
dist/                    ← packaged JAR
nbproject/private/       ← local NetBeans settings
*.class                  ← compiled bytecode
```

---

## 10. Team Roles

| Role | Responsibilities |
|------|-----------------|
| **Team Leader / Tech Lead** | Architecture decisions, PR review and merge, Railway and GitHub admin, final integration |
| **UI Designer** | Java Swing screen layout, Figma/Canva wireframes, component styling |
| **Database Admin** | Railway MySQL management, schema updates, running `db-schema.sql`, credentials sharing |
| **Backend Developer(s)** | Model classes, DAO layer, business logic (billing calculations, stock deduction) |

---

## 11. Deliverables Checklist

Track your progress against the supervisor's required deliverables:

### System Design Documents
- [ ] Technical Layout (architecture diagram, component list, tools)
- [ ] Flowchart (login, sales, inventory, reports flows)

### Implementation
- [ ] User login with role-based access (Admin / Cashier)
- [ ] Product management — Add, Edit, Delete (Admin only)
- [ ] PC station session management — Open, Monitor, Close
- [ ] Sales transaction processing — products + session billing combined
- [ ] Receipt generation — itemized, printable
- [ ] Inventory tracking — stock deduction on sale, low-stock alerts
- [ ] Admin analytics dashboard (innovative feature)
- [ ] Live station status board (innovative feature)

### Code Quality
- [ ] Layered architecture: `database/` → `model/` → `dao/` → `ui/`
- [ ] No SQL strings inside UI classes
- [ ] All DB credentials in `config.properties` (not hardcoded)
- [ ] `config.properties` in `.gitignore`
- [ ] `TestConnection.java` passes all six table checks
- [ ] Meaningful commit history (feature branches, descriptive messages)

---

## Notes

- The `config.properties` file is never stored in this repository. Contact the **team leader** or **database admin** to get the Railway credentials.
- The default seeded credentials are `admin / admin123` and `cashier1 / cashier123`. **Change these in the database after first login.**
- All monetary values use `DECIMAL(10, 2)` in MySQL and `double` in Java — format display output with `String.format("₱%.2f", amount)`.
- Railway provides separate **internal** and **public** hostnames. Use the **public proxy host and port** in `config.properties` when developing locally. The internal hostname only works inside Railway's private network.

---

*ByteZone Café POS — Internet Café & Gaming Shop Management System*
