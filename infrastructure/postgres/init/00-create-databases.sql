CREATE USER keycloak WITH PASSWORD 'keycloak_password';
CREATE DATABASE keycloak_db OWNER keycloak;

CREATE USER authorization_app WITH PASSWORD 'authorization_password';
CREATE DATABASE authorization_db OWNER authorization_app;

CREATE USER catalog WITH PASSWORD 'catalog_password';
CREATE DATABASE catalog_db OWNER catalog;

CREATE USER media_user WITH PASSWORD 'media_password';
CREATE DATABASE media_db OWNER media_user;

CREATE USER pricing_app WITH PASSWORD 'pricing_password';
CREATE DATABASE pricing_db OWNER pricing_app;

CREATE USER promotion_app WITH PASSWORD 'promotion_password';
CREATE DATABASE promotion_db OWNER promotion_app;

CREATE USER inventory_app WITH PASSWORD 'inventory_password';
CREATE DATABASE inventory_db OWNER inventory_app;

CREATE USER checkout_app WITH PASSWORD 'checkout_password';
CREATE DATABASE checkout_db OWNER checkout_app;

CREATE USER payment_app WITH PASSWORD 'payment_password';
CREATE DATABASE payment_db OWNER payment_app;

CREATE USER return_user WITH PASSWORD 'return_password';
CREATE DATABASE return_db OWNER return_user;

CREATE USER audit_app WITH PASSWORD 'audit_password';
CREATE DATABASE audit_db OWNER audit_app;

CREATE USER operations_user WITH PASSWORD 'operations_password';
CREATE DATABASE operations_db OWNER operations_user;
