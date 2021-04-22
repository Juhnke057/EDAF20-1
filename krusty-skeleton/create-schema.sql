SET FOREIGN_KEY_CHECKS=0;

DROP TABLE IF EXISTS Cookies; 
DROP TABLE IF EXISTS Storage;
DROP TABLE IF EXISTS Recipe; 
DROP TABLE IF EXISTS Pallets;
DROP TABLE IF EXISTS Orders;
DROP TABLE IF EXISTS Customer;
DROP TABLE IF EXISTS OrderSpecification;

CREATE TABLE Cookies(
CookieName varchar(100),
PRIMARY KEY (CookieName)
);

CREATE TABLE Storage(
IngredientName varchar(100),
StockAmount Integer,
Unit varchar(10),
AmountDelivered Integer,
TimeDelivered DATE,
PRIMARY KEY(IngredientName)
);

CREATE TABLE Recipe(
Amount Integer,
CookieName varchar(100),
IngredientName varchar(100),
FOREIGN KEY(CookieName) REFERENCES Cookies(CookieName),
FOREIGN KEY(IngredientName) REFERENCES Storage(IngredientName)
);

CREATE TABLE Pallets(
PalletNbr Integer auto_increment,
CookieName varchar(100),
TimeProduced DATETIME,
DeliveredTime DATETIME, 
BlockedOrNot boolean,
PRIMARY KEY(PalletNbr),
FOREIGN KEY(CookieName) REFERENCES Cookies(CookieName)
);

CREATE TABLE Customer(
CustomerName varchar(100),
CustomerAddress varchar(100),
PRIMARY KEY(CustomerName)
);

CREATE TABLE Orders( 
OrderNbr Integer auto_increment,
OrderTime DATE,
DeliveredTime DATETIME,
PalletNbr Integer, 
CustomerName varchar(100),
PRIMARY KEY(OrderNbr),
FOREIGN KEY(PalletNbr) REFERENCES Pallets(PalletNbr),
FOREIGN KEY(CustomerName) REFERENCES Customer(CustomerName)
);

CREATE TABLE OrderSpecification(
Quantity Integer,
OrderNbr Integer,
CookieName varchar(100),
Primary Key(OrderNbr, CookieName),
FOREIGN KEY(CookieName) REFERENCES Cookies(CookieName),
FOREIGN KEY(OrderNbr) REFERENCES Orders(OrderNbr)
);

-- fyll databasen för testen 
INSERT INTO customer(CustomerName, CustomerAddress)
VALUES("Bjudkakor AB", "Ystad"),
    ("Finkakor AB", "Helsingborg"),
    ("Gästkakor AB",  "Hässleholm"),
     ("Kaffebröd AB", "Landskrona"),
    ("Kalaskakor AB","Trelleborg"),
    ("Partykakor AB","Kristianstad"),
     ("Skånekakor AB","Perstorp"),
    ("Småbröd AB",  "Malmö");

INSERT INTO Storage(IngredientName, StockAmount, Unit, AmountDelivered, TimeDelivered)
VALUES("Bread crumbs", 500000,  "g", null, null),
("Butter", 500000,  "g", null, null),
("Chocolate", 500000,"g", null, null),
("Chopped almonds", 500000,"g", null, null),
("Cinnamon", 500000, "g", null, null),
("Egg whites", 500000, "ml", null, null),
("Eggs", 500000, "g", null, null),
("Fine-ground nuts", 500000, "g", null, null),
("Flour", 500000,  "g", null, null),
("Ground, roasted nuts", 500000,"g", null, null),
("Icing sugar", 500000, "g", null, null),
("Marzipan", 500000, "g", null, null),
("Potato starch", 500000, "g", null, null),
("Roasted, chopped nuts", 500000, "g", null, null),
("Sodium bicarbonate", 500000, "g", null, null),
("Sugar",  500000, "g", null, null),
("Vanilla", 500000, "g", null, null),
("Vanilla sugar",  500000, "g", null, null),
("Wheat flour", 500000, "g", null, null);

INSERT INTO Cookies(CookieName)
VALUES("Almond delight"),
("Amneris"),
("Berliner"),
("Nut cookie"),
("Nut ring"),
("Tango")
;

SET FOREIGN_KEY_CHECKS=1;