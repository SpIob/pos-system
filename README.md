# Internet Café / Gaming Shop — POS System

A Point-of-Sale system for an internet café built with **Java (Apache NetBeans)**, **MySQL on Railway**, and **GitHub** for team collaboration.

---

## Before You Start

Read this entire file top to bottom before touching anything. Every step depends on the one before it

---

## Part 1 — Install Required Software

Install everything below. All of it is free.

### 1.1 Apache NetBeans IDE
- Download: https://netbeans.apache.org/front/main/download/
- Get **NetBeans 21** (or the latest LTS version)
- During install, make sure **Java/JDK support** is checked
- If it offers to install a JDK alongside NetBeans, allow it
- Minimum JDK version required: **JDK 17**

### 1.2 Java JDK (only if not bundled with NetBeans)
- Download: https://adoptium.net/
- Get **Temurin JDK 17 (LTS)** for your operating system
- During install, make sure **Set JAVA_HOME** is checked
- Verify after install — open Command Prompt or Terminal and run:
  ```
  java -version
  ```
  You should see something like `java version "17.x.x"`

### 1.3 MySQL Connector/J (JDBC Driver)
- Download: https://dev.mysql.com/downloads/connector/j/
- Choose **Platform Independent** and download the ZIP
- Extract the ZIP — you will find a file named `mysql-connector-j-X.X.X.jar`
- Save it somewhere easy to find (Desktop or Downloads). You will add it to NetBeans in Part 3.

### 1.4 Git
- Download: https://git-scm.com/downloads
- During install (Windows): choose **"Use Git from the Windows Command Prompt"**
- Verify after install:
  ```
  git --version
  ```

### 1.5 Configure Git with Your Identity (required before any commit)
Run these two commands once in your terminal, using your own name and email:
```
git config --global user.email "youremail@gmail.com"
git config --global user.name "Your Name"
```
Without this, Git will refuse to let you commit anything.

### 1.6 GitHub Desktop (recommended for beginners)
- Download: https://desktop.github.com/
- Gives you a visual interface for Git so you don't have to use the terminal for most tasks

---

## Part 2 — Create a GitHub Account

- Sign up at https://github.com/ if you don't have an account
- Use your school email if possible — GitHub gives students free Pro features
- **Send your GitHub username to the team leader** so you can be added to this repository

---

## Part 3 — Clone and Open the Project

### 3.1 Accept the Repository Invitation
Check your email for an invite from GitHub. Accept it before trying to clone.

### 3.2 Clone the Repository
Open GitHub Desktop:
1. Click **File → Clone Repository**
2. Search for `pos-system` or paste the URL: `https://github.com/SpIob/pos-system`
3. Choose a local folder (e.g. `C:\Projects\` on Windows)
4. Click **Clone**

### 3.3 Open the Project in NetBeans
1. Open Apache NetBeans
2. Click **File → Open Project**
3. Navigate to the folder where you cloned the repo
4. Select the project folder and click **Open**
5. If you see red error marks, continue to step 3.4 — this is expected

### 3.4 Add the MySQL JDBC Driver
This is required or the project will not connect to the database.

1. In the NetBeans **Projects** panel on the left, find your project
2. Right-click **Libraries → Add JAR/Folder**
3. Navigate to the `mysql-connector-j-X.X.X.jar` file you downloaded in step 1.3
4. Select it and click **Open**
5. The red error marks should disappear

---

## Part 4 — Set Up the Database Credentials

The database credentials are **not included in this repository** for security reasons. You must create the config file manually.

### 4.1 Create the config folder
Inside the NetBeans project, right-click the `src` folder → **New → Folder** → name it `config`

### 4.2 Create config.properties
Inside the `config` folder you just created, create a new file named exactly:
```
config.properties
```

### 4.3 Paste the credentials
Ask the team leader for the credentials and paste them into `config.properties` in this format:
```
db.host=YOUR_RAILWAY_HOST
db.port=3306
db.database=railway
db.username=root
db.password=YOUR_RAILWAY_PASSWORD
db.timezone=Asia/Manila
```

> **NEVER share this file publicly or commit it to GitHub.**
> It is already blocked by `.gitignore`, but be careful anyway.

---

## Part 4B — About the Railway Database

Our MySQL database is hosted on Railway. You do **not** need to create a Railway account unless you are the database admin (team leader). Everyone else just uses the credentials shared by the team leader.

This section is here so you understand what Railway is and how to view the database if needed.

### What is Railway?
Railway is a cloud hosting platform. It runs our MySQL database 24/7 so the whole team connects to the same live database from their own computers. Think of it as the database living on the internet instead of on someone's laptop.

### Viewing the database (optional but useful for debugging)

If you want to browse the tables and data visually, you can use **MySQL Workbench** — a free GUI tool for MySQL.

**Download MySQL Workbench:**
- https://dev.mysql.com/downloads/workbench/
- Choose your operating system and install it

**Get the connection details from the team leader.**
You will need:
- Host (the Railway public hostname)
- Port (usually `3306` or a custom Railway port)
- Username (`root`)
- Password (same password as in your `config.properties`)
- Database name (`railway`)

**Connect in MySQL Workbench:**
1. Open MySQL Workbench
2. Click the **+** icon next to "MySQL Connections"
3. Fill in the fields:
   - Connection Name: `Railway POS`
   - Hostname: paste the host from the team leader
   - Port: paste the port number
   - Username: `root`
4. Click **Store in Vault** next to Password and enter the password
5. Click **Test Connection** — you should see "Successfully made the MySQL connection"
6. Click **OK**, then double-click the connection to open it
7. In the left panel, expand **railway** to see all 6 tables

> **Note:** Railway's internal host (`mysql.railway.internal`) only works from inside Railway's own network. For MySQL Workbench on your computer, the team leader must enable the **Public Networking** option in Railway and share the **public host and port** with you. These are different from the internal credentials.

### Important rules about the shared database
- Everyone on the team reads and writes to the **same database** — changes one person makes are immediately visible to everyone else
- Do not delete or modify existing table data unless your task specifically requires it
- If you accidentally break something (wrong data, deleted rows), tell the team leader immediately so it can be fixed

---

## Part 5 — Verify Your Setup

Once everything above is done, run the connection test:

1. In NetBeans, expand the project → **Source Packages → test package**
2. Right-click `TestConnection.java` → **Run File**
3. Check the **Output** panel at the bottom

You should see green checkmarks for all 6 tables:
```
[✔] Table found: users
[✔] Table found: stations
[✔] Table found: sessions
[✔] Table found: products
[✔] Table found: transactions
[✔] Table found: transaction_items
```

If you see red X marks, double-check your `config.properties` credentials and that the JDBC driver JAR was added in step 3.4.

---

## Part 6 — GitHub Workflow (How We Collaborate)

We use **feature branches** so everyone's work stays separate until it is reviewed and merged. Do **not** commit directly to `main`.

### 6.1 Before Starting Any Task

1. Open GitHub Desktop
2. Make sure you are on the `main` branch
3. Click **Fetch origin** to pull the latest changes
4. Click **Branch → New Branch**
5. Name your branch using this format:
   ```
   feature/your-name-feature-name
   ```
   Examples:
   ```
   feature/maria-login-ui
   feature/juan-product-dao
   ```
6. Click **Create Branch** — you are now working in your own branch

### 6.2 Committing Your Work

1. After making changes in NetBeans, open GitHub Desktop
2. You will see your changed files listed on the left
3. Write a short commit message describing what you did
   - Good: `Add LoginForm UI`
   - Good: `Add ProductDAO with CRUD methods`
   - Bad: `changes` or `update`
4. Click **Commit to feature/your-branch-name**
5. Click **Push origin** to upload to GitHub

### 6.3 When Your Feature is Done

1. Go to **github.com** and open this repository
2. You will see a prompt to open a Pull Request for your branch
3. Click **Compare & pull request**
4. Write a short description of what your feature does
5. Click **Create pull request**
6. The team leader will review and merge it into `main`

### 6.4 Syncing Your Computer After a Merge

Whenever the team leader merges someone's pull request into `main`, everyone else needs to update their local copy. If you skip this step, your code will fall behind and you may run into conflicts later.

Run these two commands in your terminal whenever you want to sync:
```
git checkout main
git pull origin main
```

`git checkout main` switches you back to the main branch. `git pull origin main` downloads all the latest merged changes from GitHub onto your computer.

**When to do this:**
- At the start of every new work session, before creating a new branch
- After the team leader announces a pull request has been merged
- Any time you are unsure if your local copy is up to date

After pulling, create your next feature branch as usual (step 6.1) — it will now be based on the latest code.

---

## Project Structure

```
src/
├── config/
│   └── config.properties       ← YOUR credentials (gitignored, create manually)
├── database/
│   └── DBConnection.java       ← Handles DB connection (reads from config.properties)
├── model/
│   ├── User.java
│   ├── Station.java
│   ├── Product.java
│   ├── Session.java
│   ├── Transaction.java
│   └── TransactionItem.java
├── dao/
│   ├── UserDAO.java
│   ├── ProductDAO.java
│   ├── StationDAO.java
│   ├── SessionDAO.java
│   └── TransactionDAO.java
├── ui/
│   ├── LoginFrame.java
│   ├── DashboardFrame.java
│   ├── ProductManagementPanel.java
│   ├── StationPanel.java
│   ├── SalesPanel.java
│   ├── ReceiptDialog.java
│   └── ReportsPanel.java
└── test/
    └── TestConnection.java     ← Run this to verify your DB connection
```

---

## Tech Stack

| Tool | Purpose |
|------|---------|
| Java + Apache NetBeans | Language and IDE |
| Java Swing | GUI / user interface |
| MySQL on Railway | Remote database hosting |
| MySQL Connector/J | JDBC driver for Java to MySQL |
| GitHub | Version control and collaboration |

---

## If Something Goes Wrong

| Problem | Fix |
|---------|-----|
| Red error marks in NetBeans | Add the JDBC JAR (step 3.4) |
| `config.properties not found` | Create `src/config/config.properties` (Part 4) |
| `Communications link failure` | Check your Railway host and port in config.properties |
| Can't connect in MySQL Workbench | Ask the team leader for the public host and port — the internal Railway host does not work outside Railway |
| `fatal: unable to auto-detect email` | Run the `git config` commands in step 1.5 |
| LF/CRLF warnings on Windows | Safe to ignore — this is normal on Windows |
| Pull request has merge conflicts | Ask the team leader for help before merging |
| My code is missing changes my teammate pushed | Run `git checkout main` then `git pull origin main` (step 6.4) |

---

*If your setup is complete and TestConnection passes, ask the team leader for your assigned task and branch name.*
