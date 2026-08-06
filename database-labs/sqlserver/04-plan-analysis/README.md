# SQL Server actual plan checklist

Enable **Include Actual Execution Plan** plus `STATISTICS IO/TIME`. Compare clustered/table scan vs nonclustered seek, key lookups, estimated vs actual rows, logical reads, spills, memory grant, and sort operators. The INCLUDE columns are intentional: test whether they eliminate lookups for this exact projection.
