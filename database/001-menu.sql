-- Menu dan harga contoh untuk demo lokal, bukan daftar harga kedai yang terverifikasi.
CREATE TABLE IF NOT EXISTS menu_items (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(120) NOT NULL,
    category VARCHAR(50) NOT NULL,
    price DECIMAL(12, 2) NOT NULL CHECK (price >= 0),
    image_path VARCHAR(255) NOT NULL
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

INSERT INTO menu_items (name, category, price, image_path) VALUES
('Bruschetta', 'Snacks', 25000, 'images/bruschetta.jpg'),
('French Fries', 'Snacks', 20000, 'images/french_fries.jpg'),
('Nachos', 'Snacks', 28000, 'images/nachos.jpg'),
('Onion Rings', 'Snacks', 22000, 'images/onion_rings.jpg'),
('Spring Rolls', 'Snacks', 24000, 'images/spring_rolls.jpg'),
('Pizza', 'Main Dishes', 65000, 'images/pizza.jpg'),
('Salmon Fillet', 'Main Dishes', 85000, 'images/salmon_fillet.jpg'),
('Spaghetti Carbonara', 'Main Dishes', 48000, 'images/spaghetti_carbonara.jpg'),
('Steak', 'Main Dishes', 90000, 'images/steak.jpg'),
('Coffee', 'Coffees', 22000, 'images/coffee.jpg'),
('Cappuccino', 'Coffees', 28000, 'images/cappuccino.jpg'),
('Espresso', 'Coffees', 20000, 'images/espresso.jpg'),
('Mocha', 'Coffees', 30000, 'images/mocha.jpg'),
('Mineral Water', 'Non-Coffee Drinks', 10000, 'images/mineral_water.jpg'),
('Orange Juice', 'Non-Coffee Drinks', 25000, 'images/orange_juice.jpg'),
('Tea', 'Teas', 15000, 'images/tea.jpg'),
('Matcha Latte', 'Milk-Based Drinks', 30000, 'images/matcha_latte.jpg'),
('Milkshake', 'Milk-Based Drinks', 32000, 'images/milkshake.jpg');
