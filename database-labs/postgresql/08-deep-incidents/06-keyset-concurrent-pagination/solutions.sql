-- First page
SELECT id,created_at,status FROM lab_order_header ORDER BY created_at DESC,id DESC LIMIT 101;
-- Next page uses the last visible tuple from previous page.
SELECT id,created_at,status FROM lab_order_header WHERE (created_at,id)<(:cursor_created_at,:cursor_id) ORDER BY created_at DESC,id DESC LIMIT 101;
-- Fetch size+1 to derive hasNext; encode both values in the opaque cursor.
