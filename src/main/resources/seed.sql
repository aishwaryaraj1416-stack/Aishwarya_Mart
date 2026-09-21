
-- ============================================================
-- AISHWARYA MART SEED DATA
-- ============================================================
-- Initial/demo data only.
-- Database structure belongs in schema.sql.
-- ============================================================


-- ============================================================
-- ADMIN
-- ============================================================

-- Password: admin123
INSERT INTO users (
    name,
    email,
    password_hash,
    role
)
SELECT
    'Aishwarya Mart Admin',
    'admin@aishwaryamart.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'ADMIN'
WHERE NOT EXISTS (
    SELECT 1
    FROM users
    WHERE email = 'admin@aishwaryamart.com'
);


-- ============================================================
-- SELLER
-- ============================================================

-- Password: seller123
INSERT INTO users (
    name,
    email,
    password_hash,
    role
)
SELECT
    'Aishwarya Seller',
    'seller@aishwaryamart.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'SELLER'
WHERE NOT EXISTS (
    SELECT 1
    FROM users
    WHERE email = 'seller@aishwaryamart.com'
);


-- ============================================================
-- DEMO BUYER
-- ============================================================

-- Password: buyer123
INSERT INTO users (
    name,
    email,
    password_hash,
    role
)
SELECT
    'Demo Customer',
    'buyer@aishwaryamart.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'BUYER'
WHERE NOT EXISTS (
    SELECT 1
    FROM users
    WHERE email = 'buyer@aishwaryamart.com'
);


-- ============================================================
-- PRODUCTS
-- ============================================================

INSERT INTO products (
    seller_id,
    name,
    description,
    price,
    stock_qty,
    category,
    image_url
)
SELECT
    u.id,
    'Elegant Handbag',
    'Stylish and spacious handbag for everyday use.',
    1499.00,
    20,
    'Fashion',
    'https://images.unsplash.com/photo-1584917865442-de89df76afd3'
FROM users u
WHERE u.email = 'seller@aishwaryamart.com'
  AND NOT EXISTS (
      SELECT 1
      FROM products
      WHERE name = 'Elegant Handbag'
  );


INSERT INTO products (
    seller_id,
    name,
    description,
    price,
    stock_qty,
    category,
    image_url
)
SELECT
    u.id,
    'Classic Watch',
    'Elegant classic watch suitable for everyday wear.',
    2499.00,
    15,
    'Fashion',
    'https://images.unsplash.com/photo-1524805444758-089113d48a6d'
FROM users u
WHERE u.email = 'seller@aishwaryamart.com'
  AND NOT EXISTS (
      SELECT 1
      FROM products
      WHERE name = 'Classic Watch'
  );


INSERT INTO products (
    seller_id,
    name,
    description,
    price,
    stock_qty,
    category,
    image_url
)
SELECT
    u.id,
    'Wireless Headphones',
    'Comfortable wireless headphones with clear sound.',
    2999.00,
    25,
    'Electronics',
    'https://images.unsplash.com/photo-1505740420928-5e560c06d30e'
FROM users u
WHERE u.email = 'seller@aishwaryamart.com'
  AND NOT EXISTS (
      SELECT 1
      FROM products
      WHERE name = 'Wireless Headphones'
  );


INSERT INTO products (
    seller_id,
    name,
    description,
    price,
    stock_qty,
    category,
    image_url
)
SELECT
    u.id,
    'Smartphone',
    'Modern smartphone with powerful performance and camera.',
    18999.00,
    10,
    'Electronics',
    'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9'
FROM users u
WHERE u.email = 'seller@aishwaryamart.com'
  AND NOT EXISTS (
      SELECT 1
      FROM products
      WHERE name = 'Smartphone'
  );


INSERT INTO products (
    seller_id,
    name,
    description,
    price,
    stock_qty,
    category,
    image_url
)
SELECT
    u.id,
    'Luxury Perfume',
    'Premium fragrance with a long-lasting elegant scent.',
    1999.00,
    18,
    'Beauty',
    'https://images.unsplash.com/photo-1541643600914-78b084683601'
FROM users u
WHERE u.email = 'seller@aishwaryamart.com'
  AND NOT EXISTS (
      SELECT 1
      FROM products
      WHERE name = 'Luxury Perfume'
  );


INSERT INTO products (
    seller_id,
    name,
    description,
    price,
    stock_qty,
    category,
    image_url
)
SELECT
    u.id,
    'Skincare Set',
    'Complete skincare set for a simple daily routine.',
    1299.00,
    20,
    'Beauty',
    'https://images.unsplash.com/photo-1556228578-8c89e6adf883'
FROM users u
WHERE u.email = 'seller@aishwaryamart.com'
  AND NOT EXISTS (
      SELECT 1
      FROM products
      WHERE name = 'Skincare Set'
  );


INSERT INTO products (
    seller_id,
    name,
    description,
    price,
    stock_qty,
    category,
    image_url
)
SELECT
    u.id,
    'Decorative Lamp',
    'Beautiful decorative lamp for a warm home atmosphere.',
    899.00,
    12,
    'Home',
    'https://images.unsplash.com/photo-1507473885765-e6ed057f782c'
FROM users u
WHERE u.email = 'seller@aishwaryamart.com'
  AND NOT EXISTS (
      SELECT 1
      FROM products
      WHERE name = 'Decorative Lamp'
  );


INSERT INTO products (
    seller_id,
    name,
    description,
    price,
    stock_qty,
    category,
    image_url
)
SELECT
    u.id,
    'Premium Coffee Mug',
    'Premium ceramic coffee mug for everyday use.',
    499.00,
    30,
    'Home',
    'https://images.unsplash.com/photo-1514228742587-6b1558fcca3d'
FROM users u
WHERE u.email = 'seller@aishwaryamart.com'
  AND NOT EXISTS (
      SELECT 1
      FROM products
      WHERE name = 'Premium Coffee Mug'
  );


-- ============================================================
-- END OF SEED DATA
-- ============================================================

