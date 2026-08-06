# Oracle DBMS_XPLAN checklist

Compare `TABLE ACCESS FULL` with `INDEX RANGE SCAN`, A-Rows vs E-Rows, Buffers, predicates, sort/order-by stopkey behavior, and partition `Pstart/Pstop` when testing partition pruning. Gather statistics after loading the large dataset before judging optimizer choices.
