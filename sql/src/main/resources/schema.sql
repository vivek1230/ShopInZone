-- Create Schema: `ShopInZone`
IF NOT EXISTS(SELECT * FROM sys.schemas WHERE name='ShopInZone') EXEC('CREATE SCHEMA ShopInZone')

-- Create Table: `UserDetails`
IF NOT EXISTS(SELECT * FROM sys.tables t WHERE SCHEMA_NAME(t.schema_id) = 'ShopInZone' AND t.name = 'UserDetails')
BEGIN
    CREATE TABLE ShopInZone.UserDetails(
        UserId bigint IDENTITY,
        Name nvarchar(100) NOT NULL,
        Email nvarchar(100) NOT NULL,
        Mobile nvarchar(20) NOT NULL,
        Created datetimeoffset NOT NULL default(current_timestamp),
        LastUpdated datetimeoffset NOT NULL default(current_timestamp),
        CONSTRAINT PK_UserDetails PRIMARY KEY (Email)
    )
END;

-- Create Table: `UserEntity`
IF NOT EXISTS(SELECT * FROM sys.tables t WHERE SCHEMA_NAME(t.schema_id) = 'ShopInZone' AND t.name = 'UserEntity')
BEGIN
    CREATE TABLE ShopInZone.UserEntity(
        UserId bigint IDENTITY,
        Email nvarchar(100) NOT NULL,
        Mobile nvarchar(20) NOT NULL,
        Password nvarchar(100) NOT NULL,
        UserRoleType nvarchar(20) NOT NULL,
        Created datetimeoffset NOT NULL default(current_timestamp),
        LastUpdated datetimeoffset NOT NULL default(current_timestamp),
        CONSTRAINT PK_UserEntity PRIMARY KEY (Email)
    )
END;

-- Create Table: `ProfileEntity`
IF NOT EXISTS(SELECT * FROM sys.tables t WHERE SCHEMA_NAME(t.schema_id) = 'ShopInZone' AND t.name = 'ProfileEntity')
BEGIN
    CREATE TABLE ShopInZone.ProfileEntity(
        ProfileId bigint IDENTITY,
        Email nvarchar(100) NOT NULL,
        Mobile nvarchar(20) NOT NULL,
        Name nvarchar(100) NOT NULL,
        Gender nvarchar(20) NOT NULL,
        PinCode nvarchar(10) NOT NULL,
        City nvarchar(200) NOT NULL,
        Created datetimeoffset NOT NULL default(current_timestamp),
        LastUpdated datetimeoffset NOT NULL default(current_timestamp),
        CONSTRAINT PK_ProfileEntity PRIMARY KEY (Email)
    )
END;

-- Create Table: `AddressEntity`
IF NOT EXISTS(SELECT * FROM sys.tables t WHERE SCHEMA_NAME(t.schema_id) = 'ShopInZone' AND t.name = 'AddressEntity')
BEGIN
    CREATE TABLE ShopInZone.AddressEntity(
        AddressId bigint IDENTITY,
        Email nvarchar(100) NOT NULL,
        Mobile nvarchar(20) NOT NULL,
        Name nvarchar(100) NOT NULL,
        AddressType nvarchar(20) NOT NULL,
        LandMark nvarchar(200) NOT NULL,
        State nvarchar(100) NOT NULL,
        AddressText nvarchar(500) NOT NULL,
        PinCode nvarchar(10) NOT NULL,
        City nvarchar(200) NOT NULL,
        Created datetimeoffset NOT NULL default(current_timestamp),
        LastUpdated datetimeoffset NOT NULL default(current_timestamp)
    )
END;

-- Create Table: `CityEntity`
IF NOT EXISTS(SELECT * FROM sys.tables t WHERE SCHEMA_NAME(t.schema_id) = 'ShopInZone' AND t.name = 'CityEntity')
BEGIN
    CREATE TABLE ShopInZone.CityEntity(
        CityId bigint IDENTITY,
        CityName nvarchar(100) NOT NULL,
        Description nvarchar(2000) NOT NULL,
        Created datetimeoffset NOT NULL default(current_timestamp),
        LastUpdated datetimeoffset NOT NULL default(current_timestamp)
        CONSTRAINT PK_CityEntity PRIMARY KEY (CityName)
    )
END;

-- Create Table: `CityImageUrlMapping`
IF NOT EXISTS(SELECT * FROM sys.tables t WHERE SCHEMA_NAME(t.schema_id) = 'ShopInZone' AND t.name = 'CityImageUrlMapping')
BEGIN
    CREATE TABLE ShopInZone.CityImageUrlMapping(
        Id bigint IDENTITY,
        CityId bigint NOT NULL,
        ImageUrl nvarchar(2000) NOT NULL,
        Created datetimeoffset NOT NULL default(current_timestamp),
        LastUpdated datetimeoffset NOT NULL default(current_timestamp)
    )
END;

-- Create Table: `CityNameMapping`
IF NOT EXISTS(SELECT * FROM sys.tables t WHERE SCHEMA_NAME(t.schema_id) = 'ShopInZone' AND t.name = 'CityNameMapping')
BEGIN
    CREATE TABLE ShopInZone.CityNameMapping(
        Id bigint IDENTITY,
        CityId bigint NOT NULL,
        CityName nvarchar(200) NOT NULL,
        Created datetimeoffset NOT NULL default(current_timestamp),
        LastUpdated datetimeoffset NOT NULL default(current_timestamp)
    )
END;

-- Create Table: `CityPinCodeMapping`
IF NOT EXISTS(SELECT * FROM sys.tables t WHERE SCHEMA_NAME(t.schema_id) = 'ShopInZone' AND t.name = 'CityPinCodeMapping')
BEGIN
    CREATE TABLE ShopInZone.CityPinCodeMapping(
        Id bigint IDENTITY,
        CityId bigint NOT NULL,
        PinCode nvarchar(10) NOT NULL,
        Created datetimeoffset NOT NULL default(current_timestamp),
        LastUpdated datetimeoffset NOT NULL default(current_timestamp)
    )
END;

-- Create Table: `CategoryEntity`
IF NOT EXISTS(SELECT * FROM sys.tables t WHERE SCHEMA_NAME(t.schema_id) = 'ShopInZone' AND t.name = 'CategoryEntity')
BEGIN
    CREATE TABLE ShopInZone.CategoryEntity(
        CategoryId bigint IDENTITY,
        CategoryName nvarchar(100) NOT NULL,
        Description nvarchar(2000) NOT NULL,
        Created datetimeoffset NOT NULL default(current_timestamp),
        LastUpdated datetimeoffset NOT NULL default(current_timestamp)
        CONSTRAINT PK_CategoryEntity PRIMARY KEY (CategoryName)
    )
END;

-- Create Table: `CityCategoryMapping`
IF NOT EXISTS(SELECT * FROM sys.tables t WHERE SCHEMA_NAME(t.schema_id) = 'ShopInZone' AND t.name = 'CityCategoryMapping')
BEGIN
    CREATE TABLE ShopInZone.CityCategoryMapping(
        CityCategoryMappingId bigint IDENTITY,
        CityId bigint NOT NULL,
        CategoryId bigint NOT NULL,
        Created datetimeoffset NOT NULL default(current_timestamp),
        LastUpdated datetimeoffset NOT NULL default(current_timestamp),
        CONSTRAINT PK_CityCategoryMapping PRIMARY KEY (CityId, CategoryId)
    )
END;

-- Create Table: `ShopEntity`
IF NOT EXISTS(SELECT * FROM sys.tables t WHERE SCHEMA_NAME(t.schema_id) = 'ShopInZone' AND t.name = 'ShopEntity')
BEGIN
    CREATE TABLE ShopInZone.ShopEntity(
        ShopId bigint IDENTITY,
        ShopName nvarchar(100) NOT NULL,
        Description nvarchar(2000) NOT NULL,
        CityId bigint NOT NULL,
        AddressId bigint NOT NULL,
        Rating nvarchar(10) NOT NULL,
        ImageUrl nvarchar(2000) NOT NULL,
        Created datetimeoffset NOT NULL default(current_timestamp),
        LastUpdated datetimeoffset NOT NULL default(current_timestamp)
        CONSTRAINT PK_ShopEntity PRIMARY KEY (CityId, AddressId, ShopName)
    )
END;


-- Create Table: `CityCategoryShopMapping`
IF NOT EXISTS(SELECT * FROM sys.tables t WHERE SCHEMA_NAME(t.schema_id) = 'ShopInZone' AND t.name = 'CityCategoryShopMapping')
BEGIN
    CREATE TABLE ShopInZone.CityCategoryShopMapping(
        CityCategoryShopMappingId bigint IDENTITY,
        CityId bigint NOT NULL,
        CategoryId bigint NOT NULL,
        ShopId bigint NOT NULL,
        Created datetimeoffset NOT NULL default(current_timestamp),
        LastUpdated datetimeoffset NOT NULL default(current_timestamp),
        CONSTRAINT PK_CityCategoryShopMapping PRIMARY KEY (CityId, CategoryId, ShopId)
    )
END;