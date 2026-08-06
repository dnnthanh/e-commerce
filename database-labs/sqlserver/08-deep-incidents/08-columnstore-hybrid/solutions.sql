CREATE NONCLUSTERED COLUMNSTORE INDEX NCCI_lab_settlement_analytics ON dbo.lab_settlement(seller_id,business_date,gross,commission);
SELECT seller_id,SUM(gross),SUM(commission) FROM dbo.lab_settlement WHERE business_date>=DATEADD(day,-365,CAST(SYSUTCDATETIME() AS date)) GROUP BY seller_id;
-- Keep rowstore PK/operational indexes for point operations; measure delta-store/rowgroup health under writes.
