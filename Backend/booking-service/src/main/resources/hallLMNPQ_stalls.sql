-- ============================================================
-- HALL L (hall_id = 12)
-- ============================================================
INSERT INTO stall (stall_name, hall_id, stall_type )
SELECT 'L-L', 12, 'LARGE'  WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='L01');

-- ============================================================
-- HALL M (hall_id = 13)
-- ============================================================
INSERT INTO stall (stall_name, hall_id, stall_type )
SELECT 'L-M', 13, 'LARGE'  WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='L01');


-- ============================================================
-- HALL N (hall_id = 14)
-- ============================================================
INSERT INTO stall (stall_name, hall_id, stall_type )
SELECT 'S01', 14, 'SMALL'  WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S01');
INSERT INTO stall (stall_name, hall_id, stall_type )
SELECT 'S02', 14, 'SMALL'  WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S02');
INSERT INTO stall (stall_name, hall_id, stall_type )
SELECT 'S03', 14, 'SMALL'  WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S03');
INSERT INTO stall (stall_name, hall_id, stall_type )
SELECT 'S04', 14, 'SMALL'  WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S04');
INSERT INTO stall (stall_name, hall_id, stall_type )
SELECT 'S05', 14, 'SMALL'  WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S05');
INSERT INTO stall (stall_name, hall_id, stall_type )
SELECT 'S06', 14, 'SMALL'  WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S06');
INSERT INTO stall (stall_name, hall_id, stall_type )
SELECT 'S07', 14, 'SMALL'  WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S07');
INSERT INTO stall (stall_name, hall_id, stall_type )
SELECT 'S08', 14, 'SMALL'  WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S08');
INSERT INTO stall (stall_name, hall_id, stall_type )
SELECT 'S09', 14, 'SMALL'  WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S09');
INSERT INTO stall (stall_name, hall_id, stall_type )
SELECT 'S10', 14, 'SMALL'  WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S10');
INSERT INTO stall (stall_name, hall_id, stall_type )
SELECT 'S11', 14, 'SMALL'  WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S11');
INSERT INTO stall (stall_name, hall_id, stall_type )
SELECT 'S12', 14, 'SMALL'  WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S12');
INSERT INTO stall (stall_name, hall_id, stall_type )
SELECT 'S13', 14, 'SMALL'  WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S13');
INSERT INTO stall (stall_name, hall_id, stall_type )
SELECT 'S14', 14, 'SMALL'  WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S14');
INSERT INTO stall (stall_name, hall_id, stall_type )
SELECT 'S15', 14, 'SMALL'  WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S15');
INSERT INTO stall (stall_name, hall_id, stall_type )
SELECT 'S16', 14, 'SMALL'  WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S16');

-- ============================================================
-- HALL P (hall_id = 15)
-- ============================================================
INSERT INTO stall (stall_name, hall_id, stall_type )
SELECT 'L-P', 15, 'LARGE'  WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='L-P');


-- ============================================================
-- HALL Q (hall_id = 16)
-- ============================================================
INSERT INTO stall (stall_name, hall_id, stall_type )
SELECT 'L-Q', 16, 'LARGE'  WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='L-Q');
