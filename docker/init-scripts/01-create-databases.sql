-- User Service Database
IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = 'food_delivery_user_db')
CREATE DATABASE food_delivery_user_db;
GO

-- Restaurant Service Database  
IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = 'food_delivery_restaurant_db')
CREATE DATABASE food_delivery_restaurant_db;
GO

-- Order Service Database
IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = 'food_delivery_order_db')
CREATE DATABASE food_delivery_order_db;
GO

-- Delivery Service Database
IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = 'food_delivery_delivery_db')
CREATE DATABASE food_delivery_delivery_db;
GO

-- Payment Service Database
IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = 'food_delivery_payment_db')
CREATE DATABASE food_delivery_payment_db;
GO

-- Notification Service Database
IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = 'food_delivery_notification_db')
CREATE DATABASE food_delivery_notification_db;
GO