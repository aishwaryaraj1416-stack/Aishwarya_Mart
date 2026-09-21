
-- ============================================================
-- AISHWARYA MART DATABASE SCHEMA
-- ============================================================
-- Structure only.
-- Initial data belongs in seed.sql.
-- ============================================================


-- ============================================================
-- USERS
-- ============================================================

CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'BUYER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_user_role
        CHECK (role IN ('BUYER', 'SELLER', 'ADMIN'))
);


-- ============================================================
-- PRODUCTS
-- ============================================================

CREATE TABLE IF NOT EXISTS products (
    id INT AUTO_INCREMENT PRIMARY KEY,
    seller_id INT,
    name VARCHAR(150) NOT NULL,
    description VARCHAR(500),
    price DECIMAL(10,2) NOT NULL,
    stock_qty INT NOT NULL DEFAULT 10,
    category VARCHAR(50) NOT NULL,
    image_url VARCHAR(1000),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_product_seller
        FOREIGN KEY (seller_id)
        REFERENCES users(id)
        ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_products_seller
    ON products(seller_id);

CREATE INDEX IF NOT EXISTS idx_products_category
    ON products(category);


-- ============================================================
-- CART ITEMS
-- ============================================================

CREATE TABLE IF NOT EXISTS cart_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT user_product_unique
        UNIQUE (user_id, product_id),

    CONSTRAINT fk_cart_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_cart_product
        FOREIGN KEY (product_id)
        REFERENCES products(id)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_cart_user
    ON cart_items(user_id);

CREATE INDEX IF NOT EXISTS idx_cart_product
    ON cart_items(product_id);


-- ============================================================
-- ORDERS
-- ============================================================

CREATE TABLE IF NOT EXISTS orders (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_order_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT chk_order_status
        CHECK (
            status IN (
                'PENDING',
                'CONFIRMED',
                'SHIPPED',
                'DELIVERED',
                'CANCELLED'
            )
        )
);

CREATE INDEX IF NOT EXISTS idx_orders_user
    ON orders(user_id);

CREATE INDEX IF NOT EXISTS idx_orders_status
    ON orders(status);


-- ============================================================
-- ORDER ITEMS
-- ============================================================

CREATE TABLE IF NOT EXISTS order_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_order_item_order
        FOREIGN KEY (order_id)
        REFERENCES orders(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_order_item_product
        FOREIGN KEY (product_id)
        REFERENCES products(id)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_order_items_order
    ON order_items(order_id);

CREATE INDEX IF NOT EXISTS idx_order_items_product
    ON order_items(product_id);


-- ============================================================
-- REVIEWS
-- ============================================================

CREATE TABLE IF NOT EXISTS reviews (
    id INT AUTO_INCREMENT PRIMARY KEY,
    product_id INT NOT NULL,
    user_id INT NOT NULL,
    rating INT NOT NULL,
    comment VARCHAR(1000),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_review_product
        FOREIGN KEY (product_id)
        REFERENCES products(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_review_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT chk_review_rating
        CHECK (rating BETWEEN 1 AND 5),

    CONSTRAINT unique_user_product_review
        UNIQUE (user_id, product_id)
);

CREATE INDEX IF NOT EXISTS idx_reviews_product
    ON reviews(product_id);

CREATE INDEX IF NOT EXISTS idx_reviews_user
    ON reviews(user_id);


-- ============================================================
-- WISHLIST
-- ============================================================

CREATE TABLE IF NOT EXISTS wishlist (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    product_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT unique_user_product_wishlist
        UNIQUE (user_id, product_id),

    CONSTRAINT fk_wishlist_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_wishlist_product
        FOREIGN KEY (product_id)
        REFERENCES products(id)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_wishlist_user
    ON wishlist(user_id);

CREATE INDEX IF NOT EXISTS idx_wishlist_product
    ON wishlist(product_id);


-- ============================================================
-- END OF SCHEMA
-- ============================================================

