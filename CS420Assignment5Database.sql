create database CS420StockProject;

use CS420StockProject;

create table INVESTOR(InvestorID INT, Country VARCHAR(50), Phone INT, LastName VARCHAR(50), FirstName VARCHAR(50), Email VARCHAR(50));
alter table INVESTOR add constraint primaryKeyInvestor primary key (InvestorID);

create table BROKERAGEACCOUNT(AccountID INT, InvestorID INT, BrokerName VARCHAR(50),AccountType INT, Balance INT);
alter table BROKERAGEACCOUNT add constraint primaryKeyBrokerage primary key (AccountID);
alter table BROKERAGEACCOUNT add constraint foreignKeyBrokerage foreign key (InvestorID) references INVESTOR(InvestorID);

create table COMPANY(CompanyID INT, FoundedYear INT, Headquarters VARCHAR(50),CompanyName VARCHAR(50), Industry VARCHAR(50));
alter table COMPANY add constraint primaryKeyCompany primary key (CompanyID);

create table STOCK(StockID INT, CompanyID INT, TickerSymbol VARCHAR(50), ExchangeName VARCHAR(50), CurrentPrice INT);
alter table STOCK add constraint primaryKeyStock primary key (StockID);
alter table STOCK add constraint foreignKeyStock foreign key (CompanyID) references COMPANY(CompanyID);

create table TRADETRANSACTION(TransactionID INT, StockID INT, AccountID INT, PricePerShare INT, Quantity INT, TradeDate INT, TradeType VARCHAR(50));
alter table TRADETRANSACTION add constraint primaryKeyTrade primary key (TransactionID);
alter table TRADETRANSACTION add constraint foreignKeyTrade1 foreign key (StockID) references STOCK(StockID);
alter table TRADETRANSACTION add constraint foreignKeyTrade2 foreign key (AccountID) references BROKERAGEACCOUNT(AccountID);

insert into INVESTOR values(1,'America',555-555-555,'Stan','Bumppy','Stan@gmail.com');
insert into INVESTOR values(2,'Ireland',555-525-555,'Grump','Bumppy','Grump@gmail.com');
insert into INVESTOR values(3,'Vietnam',555-535-555,'Silver','Bumppy','Silver@gmail.com');

insert into BROKERAGEACCOUNT values(10,1,'John Run',245,3333355);
insert into BROKERAGEACCOUNT values(20,2,'Mouth Thomson',44,2345345);
insert into BROKERAGEACCOUNT values(30,3,'Dr. Bimpilious Sniff',322,43543543);

insert into COMPANY values(100, 859,'Seattle','Ivars Clam Chowser','Military Industrial Complex');
insert into COMPANY values(200, 12,'Tacoma','Marys cookie bakery','Big Pharma');
insert into COMPANY values(300, 2019,'Bremerton','Dr. Sniffs emporium','Global Logisticls');

insert into STOCK values(1000,100,'ICC','NYSE',4);
insert into STOCK values(2000,200,'MCB','NYSE',34);
insert into STOCK values(3000,300,'DSE','NYSE',33334);

insert into TRADETRANSACTION values(10000,1000,10,4,634,356332,'Immediate');
insert into TRADETRANSACTION values(20000,2000,20,34,34,524545,'Short position');
insert into TRADETRANSACTION values(30000,3000,30,33334,222,163322,'Immediate');

select * from TRADETRANSACTION;