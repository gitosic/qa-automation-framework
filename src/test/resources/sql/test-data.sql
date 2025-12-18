-- test-data.sql
-- Полный скрипт для создания таблиц и наполнения тестовыми данными

-- 1. Удаляем существующие таблицы (в обратном порядке из-за внешних ключей)
DROP TABLE IF EXISTS db_qa.order_items;
DROP TABLE IF EXISTS db_qa.orders;
DROP TABLE IF EXISTS db_qa.products;
DROP TABLE IF EXISTS db_qa.users;

-- 2. Создаем таблицы
CREATE TABLE db_qa.users (
                             id SERIAL PRIMARY KEY,
                             username VARCHAR(50) NOT NULL UNIQUE,
                             email VARCHAR(100) NOT NULL UNIQUE,
                             password_hash VARCHAR(255) NOT NULL,
                             first_name VARCHAR(50),
                             last_name VARCHAR(50),
                             is_active BOOLEAN DEFAULT true,
                             created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE db_qa.products (
                                id SERIAL PRIMARY KEY,
                                name VARCHAR(100) NOT NULL,
                                category VARCHAR(50),
                                price DECIMAL(10, 2) NOT NULL,
                                stock_quantity INTEGER DEFAULT 0,
                                is_available BOOLEAN DEFAULT true,
                                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE db_qa.orders (
                              id SERIAL PRIMARY KEY,
                              user_id INTEGER REFERENCES db_qa.users(id) ON DELETE CASCADE,
                              order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                              total_amount DECIMAL(10, 2),
                              status VARCHAR(20) DEFAULT 'PENDING',
                              created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE db_qa.order_items (
                                   id SERIAL PRIMARY KEY,
                                   order_id INTEGER REFERENCES db_qa.orders(id) ON DELETE CASCADE,
                                   product_id INTEGER REFERENCES db_qa.products(id) ON DELETE CASCADE,
                                   quantity INTEGER NOT NULL CHECK (quantity > 0),
                                   unit_price DECIMAL(10, 2) NOT NULL,
                                   created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. Создаем индексы
CREATE INDEX idx_users_username ON db_qa.users(username);
CREATE INDEX idx_users_email ON db_qa.users(email);
CREATE INDEX idx_users_active ON db_qa.users(is_active);

CREATE INDEX idx_products_category ON db_qa.products(category);
CREATE INDEX idx_products_available ON db_qa.products(is_available);
CREATE INDEX idx_products_price ON db_qa.products(price);

CREATE INDEX idx_orders_user_id ON db_qa.orders(user_id);
CREATE INDEX idx_orders_status ON db_qa.orders(status);
CREATE INDEX idx_orders_date ON db_qa.orders(order_date);

CREATE INDEX idx_order_items_order_id ON db_qa.order_items(order_id);
CREATE INDEX idx_order_items_product_id ON db_qa.order_items(product_id);

-- 4. Вставляем тестовых пользователей
INSERT INTO db_qa.users (username, email, password_hash, first_name, last_name, is_active) VALUES
                                                                                               ('john_doe', 'john.doe@example.com', '$2a$10$ABC123XYZ456', 'John', 'Doe', true),
                                                                                               ('jane_smith', 'jane.smith@example.com', '$2a$10$DEF456UVW789', 'Jane', 'Smith', true),
                                                                                               ('bob_johnson', 'bob.johnson@example.com', '$2a$10$GHI789RST012', 'Bob', 'Johnson', false),
                                                                                               ('alice_williams', 'alice.williams@example.com', '$2a$10$JKL012MNO345', 'Alice', 'Williams', true),
                                                                                               ('charlie_brown', 'charlie.brown@example.com', '$2a$10$MNO345PQR678', 'Charlie', 'Brown', true);

-- 5. Вставляем тестовые продукты
INSERT INTO db_qa.products (name, category, price, stock_quantity, is_available) VALUES
                                                                                     ('MacBook Pro 16"', 'Electronics', 2399.99, 5, true),
                                                                                     ('iPhone 15 Pro', 'Electronics', 999.00, 15, true),
                                                                                     ('Sony WH-1000XM5', 'Electronics', 349.99, 20, true),
                                                                                     ('Clean Code by Robert Martin', 'Books', 39.99, 100, true),
                                                                                     ('Design Patterns: Elements of Reusable OO Software', 'Books', 49.99, 50, true),
                                                                                     ('Ergonomic Office Chair', 'Furniture', 299.99, 10, true),
                                                                                     ('Standing Desk', 'Furniture', 499.99, 3, true),
                                                                                     ('Coffee Mug Set (6 pieces)', 'Home', 24.99, 200, true),
                                                                                     ('YETI Rambler Bottle', 'Home', 34.99, 30, true);

-- 6. Вставляем тестовые заказы
INSERT INTO db_qa.orders (user_id, total_amount, status) VALUES
                                                             (1, 2399.99, 'COMPLETED'),  -- John Doe купил MacBook
                                                             (1, 89.98, 'PENDING'),      -- John Doe купил книги
                                                             (2, 349.99, 'SHIPPED'),     -- Jane Smith купила наушники
                                                             (4, 824.97, 'PROCESSING'),  -- Alice Williams купила мебель
                                                             (5, 34.99, 'COMPLETED');    -- Charlie Brown купил бутылку

-- 7. Вставляем элементы заказа
INSERT INTO db_qa.order_items (order_id, product_id, quantity, unit_price) VALUES
                                                                               (1, 1, 1, 2399.99),  -- MacBook в заказе 1
                                                                               (2, 4, 1, 39.99),    -- Clean Code в заказе 2
                                                                               (2, 5, 1, 49.99),    -- Design Patterns в заказе 2
                                                                               (3, 3, 1, 349.99),   -- Наушники в заказе 3
                                                                               (4, 6, 1, 299.99),   -- Кресло в заказе 4
                                                                               (4, 7, 1, 499.99),   -- Стол в заказе 4
                                                                               (4, 8, 1, 24.99),    -- Кружки в заказе 4
                                                                               (5, 9, 1, 34.99);    -- Бутылка в заказе 5

-- 8. Выводим информацию для отладки
SELECT '=== DATABASE INITIALIZATION COMPLETE ===' as message;
SELECT 'Users count: ' || COUNT(*) as info FROM db_qa.users;
SELECT 'Products count: ' || COUNT(*) as info FROM db_qa.products;
SELECT 'Orders count: ' || COUNT(*) as info FROM db_qa.orders;