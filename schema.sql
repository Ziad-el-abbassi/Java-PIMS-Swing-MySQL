CREATE DATABASE IF NOT EXISTS personal_inventory;
USE personal_inventory;

CREATE TABLE IF NOT EXISTS users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS items (
    item_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    item_name VARCHAR(255) NOT NULL,
    category VARCHAR(100),
    purchase_date DATE,
    expiry_date DATE NULL,
    quantity INT DEFAULT 1,
    condition_status VARCHAR(50),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS maintenance (
    maintenance_id INT AUTO_INCREMENT PRIMARY KEY,
    item_id INT NOT NULL,
    last_maintenance DATE,
    next_maintenance DATE,
    notes TEXT,
    FOREIGN KEY (item_id) REFERENCES items(item_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS lending (
    lending_id INT AUTO_INCREMENT PRIMARY KEY,
    item_id INT NOT NULL,
    person_name VARCHAR(255),
    date_lent DATE,
    due_date DATE,
    return_status VARCHAR(20) DEFAULT 'Pending',
    FOREIGN KEY (item_id) REFERENCES items(item_id) ON DELETE CASCADE
);
