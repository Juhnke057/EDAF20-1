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
CookieName varchar(100),
IngredientName varchar(100),
Amount Integer,
Primary Key(CookieName,IngredientName),
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

SET FOREIGN_KEY_CHECKS=1;