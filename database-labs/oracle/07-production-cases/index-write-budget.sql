-- Review segment size and usage before adding another finance index.
SELECT segment_name,segment_type,bytes/1024/1024 mb
FROM user_segments
WHERE segment_name LIKE 'SETTLEMENT%'
ORDER BY bytes DESC;
-- Pair this evidence with redo/undo and DML benchmark for candidate indexes; finance write cost matters as much as read plan improvement.
