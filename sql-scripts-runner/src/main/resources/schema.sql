
-- Create Schema: `order_ingestion_inventory_projection`
IF NOT EXISTS(select * FROM sys.schemas WHERE name='order_ingestion_inventory_projection') EXEC('CREATE SCHEMA order_ingestion_inventory_projection');

-- Create the PickTicket ID generator sequence
IF NOT EXISTS(
    SELECT * FROM sys.sequences WHERE name = 'pt_id_seq' AND schema_id =
        (SELECT schema_id FROM sys.schemas WHERE name = 'order_ingestion_inventory_projection'))
BEGIN
   CREATE SEQUENCE "order_ingestion_inventory_projection".pt_id_seq START WITH 4724690 INCREMENT BY 1
END;


-- Create Table: `GlobalGtinInventory`
IF NOT EXISTS(SELECT * FROM sys.tables t WHERE SCHEMA_NAME(t.schema_id) = 'order_ingestion_inventory_projection' AND t.name='GlobalGtinInventory')
BEGIN
    CREATE TABLE order_ingestion_inventory_projection.GlobalGtinInventory(
        FcId nvarchar(10) NOT NULL,
        Gtin nvarchar(20) NOT NULL,
        SellerId nvarchar(50) NOT NULL,
        SellableCount INT NOT NULL,
        AllocatedCount INT NOT NULL,
        LastUpdated datetimeoffset NOT NULL default(current_timestamp),
        CONSTRAINT PK_GlobalGtinInventory PRIMARY KEY (FcId, Gtin, SellerId)
    )
END;

-- Create Table: `GtinContainer`
IF NOT EXISTS(SELECT * FROM sys.tables t WHERE SCHEMA_NAME(t.schema_id) = 'order_ingestion_inventory_projection' AND t.name='GtinContainer')
BEGIN
    CREATE TABLE order_ingestion_inventory_projection.GtinContainer(
        FcId nvarchar(10) NOT NULL,
        Gtin nvarchar(20) NOT NULL,
        SellerId nvarchar(50) NOT NULL,
        ContainerId nvarchar(50) NOT NULL,
        Quantity INT NOT NULL,
        LastUpdated datetimeoffset NOT NULL default(current_timestamp),
        OccurredOn datetimeoffset(7),
        CONSTRAINT PK_GtinContainer PRIMARY KEY (FcId, Gtin, SellerId, ContainerId)
    )
END;

-- Create Table: `Allocation`
IF NOT EXISTS(SELECT * FROM sys.tables t WHERE SCHEMA_NAME(t.schema_id) = 'order_ingestion_inventory_projection' AND t.name='Allocation')
BEGIN
    CREATE TABLE order_ingestion_inventory_projection.Allocation(
        FcId nvarchar(10) NOT NULL,
        Gtin nvarchar(20) NOT NULL,
        SellerId nvarchar(50) NOT NULL,
        OrderId nvarchar(50) NOT NULL,
        PickTicketId nvarchar(50) NOT NULL,
        Quantity INT NOT NULL,
        LastUpdated datetimeoffset NOT NULL default(current_timestamp),
        CONSTRAINT PK_Allocation PRIMARY KEY(FcId, Gtin, SellerId, OrderId, PickTicketId)
    )
END;

-- Create Table: IdempotencyToken
IF NOT EXISTS(SELECT * FROM sys.tables t WHERE SCHEMA_NAME(t.schema_id) = 'order_ingestion_inventory_projection' AND t.name='IdempotencyToken')
BEGIN
    CREATE TABLE order_ingestion_inventory_projection.IdempotencyToken(
        IdempotencyKey nvarchar(64) NOT NULL PRIMARY KEY
    )
END;

IF NOT EXISTS(SELECT * FROM sys.triggers t WHERE t.name='GlobalGtinInventoryLastUpdated')
exec('CREATE TRIGGER [order_ingestion_inventory_projection].[GlobalGtinInventoryLastUpdated] ON [order_ingestion_inventory_projection].[GlobalGtinInventory] AFTER INSERT, UPDATE AS UPDATE [order_ingestion_inventory_projection].[GlobalGtinInventory] SET LastUpdated = current_timestamp FROM Inserted i WHERE [order_ingestion_inventory_projection].[GlobalGtinInventory].FcId = i.FcId AND [order_ingestion_inventory_projection].[GlobalGtinInventory].Gtin = i.Gtin AND [order_ingestion_inventory_projection].[GlobalGtinInventory].SellerId = i.SellerId')

IF NOT EXISTS(SELECT * FROM sys.triggers t WHERE t.name='GtinContainerLastUpdated')
exec('CREATE TRIGGER [order_ingestion_inventory_projection].[GtinContainerLastUpdated] ON [order_ingestion_inventory_projection].[GtinContainer] AFTER INSERT, UPDATE AS UPDATE [order_ingestion_inventory_projection].[GtinContainer] SET LastUpdated =current_timestamp FROM Inserted i WHERE [order_ingestion_inventory_projection].[GtinContainer].FcId = i.FcId AND [order_ingestion_inventory_projection].[GtinContainer].Gtin = i.Gtin AND [order_ingestion_inventory_projection].[GtinContainer].SellerId = i.SellerId AND [order_ingestion_inventory_projection].[GtinContainer].ContainerId = i.ContainerId')

IF NOT EXISTS(SELECT * FROM sys.triggers t WHERE t.name='AllocationLastUpdated')
exec('CREATE TRIGGER [order_ingestion_inventory_projection].[AllocationLastUpdated] ON [order_ingestion_inventory_projection].[Allocation] AFTER INSERT, UPDATE AS UPDATE [order_ingestion_inventory_projection].[Allocation] SET LastUpdated =current_timestamp FROM Inserted i WHERE [order_ingestion_inventory_projection].[Allocation].FcId = i.FcId AND [order_ingestion_inventory_projection].[Allocation].Gtin = i.Gtin AND [order_ingestion_inventory_projection].[Allocation].SellerId = i.SellerId AND [order_ingestion_inventory_projection].[Allocation].OrderId = i.OrderId AND [order_ingestion_inventory_projection].[Allocation].PickTicketId = i.PickTicketId')