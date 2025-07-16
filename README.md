# Gestion de Stock - FSR

This is a comprehensive desktop stock management application developed during my internship at the Faculty of Sciences. It provides a complete solution for tracking inventory, managing user access, and generating official documents.

## Features

- **Secure User Authentication:** Login system with password hashing (jBCrypt).
- **Role-Based Access Control:**
    - **ADMIN:** Manages user accounts and roles.
    - **MAGAZINIER:** Manages the stock inventory.
- **Dynamic Role Management:** Admins can create and assign custom roles.
- **Configurable Database:** First-time setup allows users to configure their MySQL database connection details.
- **Complete Stock Lifecycle:**
    - **Stock In:** Add new `Matériel` (individually tracked) or `Consommables` (tracked in bulk).
    - **Serial Number Tracking:** Enforces entry of unique serial numbers for all `Matériel`.
    - **Stock Out:** Dispatch single or multiple specific items in a single transaction.
- **Document Generation:** Automatically generates official `Bon de Réception` or `Décharge` `.docx` files using the `docx4j` library.
- **History & Auditing:** Double-click any item to view its complete dispatch history.
- **Live Search & Filtering:** Instantly search the entire inventory across multiple fields.
- **Polished UI:** Features a splash screen and professional user interface built with JavaFX.

## Technologies Used

- **Language:** Java 17+
- **Framework:** JavaFX
- **Database:** MySQL
- **Build Tool:** Apache Maven
- **Key Libraries:**
    - `jbcrypt`: For secure password hashing.
    - `docx4j`: For generating and manipulating `.docx` files.
    - `mysql-connector-j`: For database connectivity.
    - `ControlsFX`: For advanced UI components.

## Setup and Running the Project

1.  **Prerequisites:**
    - Java JDK 17 or higher.
    - Apache Maven.
    - A running MySQL server (e.g., via XAMPP).

2.  **Database Setup:**
    - Create an empty database in MySQL (e.g., `gestion_stock_fsr`).
    - The application will create all necessary tables on first run.

3.  **Configuration:**
    - When you run the application for the first time, it may prompt you to configure the database connection if it cannot connect.
    - Enter your MySQL host, port, database name, username, and password. This will create a `gestion_de_stock_config.properties` file in your user home directory.

4.  **Running:**
    - Clone the repository.
    - Open the project in your IDE (e.g., IntelliJ IDEA).
    - Run the Maven goal: `mvn javafx:run`

5.  **Default Login:**
    - **Username:** `admin`
    - **Password:** `admin`

---
*This project was developed by sami hilali as part of an internship program.*