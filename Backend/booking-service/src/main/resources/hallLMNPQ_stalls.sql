-- ============================================================
-- HALL L (hall_id = 12)
-- ============================================================
INSERT INTO stall (stall_name, hall_id, stall_type, booking_status_id, is_active, price)
SELECT 'L-L', 12, 'LARGE', 1, TRUE, 10000 WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='L01');

-- ============================================================
-- HALL M (hall_id = 13)
-- ============================================================
INSERT INTO stall (stall_name, hall_id, stall_type, booking_status_id, is_active, price)
SELECT 'L-M', 13, 'LARGE', 1, TRUE, 10000 WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='L01');


-- ============================================================
-- HALL N (hall_id = 14)
-- ============================================================
INSERT INTO stall (stall_name, hall_id, stall_type, booking_status_id, is_active, price)
SELECT 'S01', 14, 'SMALL', 1, TRUE, 5000 WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S01');
INSERT INTO stall (stall_name, hall_id, stall_type, booking_status_id, is_active, price)
SELECT 'S02', 14, 'SMALL', 1, TRUE, 5000 WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S02');
INSERT INTO stall (stall_name, hall_id, stall_type, booking_status_id, is_active, price)
SELECT 'S03', 14, 'SMALL', 1, TRUE, 5000 WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S03');
INSERT INTO stall (stall_name, hall_id, stall_type, booking_status_id, is_active, price)
SELECT 'S04', 14, 'SMALL', 1, TRUE, 5000 WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S04');
INSERT INTO stall (stall_name, hall_id, stall_type, booking_status_id, is_active, price)
SELECT 'S05', 14, 'SMALL', 1, TRUE, 5000 WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S05');
INSERT INTO stall (stall_name, hall_id, stall_type, booking_status_id, is_active, price)
SELECT 'S06', 14, 'SMALL', 1, TRUE, 5000 WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S06');
INSERT INTO stall (stall_name, hall_id, stall_type, booking_status_id, is_active, price)
SELECT 'S07', 14, 'SMALL', 1, TRUE, 5000 WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S07');
INSERT INTO stall (stall_name, hall_id, stall_type, booking_status_id, is_active, price)
SELECT 'S08', 14, 'SMALL', 1, TRUE, 5000 WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S08');
INSERT INTO stall (stall_name, hall_id, stall_type, booking_status_id, is_active, price)
SELECT 'S09', 14, 'SMALL', 1, TRUE, 5000 WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S09');
INSERT INTO stall (stall_name, hall_id, stall_type, booking_status_id, is_active, price)
SELECT 'S10', 14, 'SMALL', 1, TRUE, 5000 WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S10');
INSERT INTO stall (stall_name, hall_id, stall_type, booking_status_id, is_active, price)
SELECT 'S11', 14, 'SMALL', 1, TRUE, 5000 WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S11');
INSERT INTO stall (stall_name, hall_id, stall_type, booking_status_id, is_active, price)
SELECT 'S12', 14, 'SMALL', 1, TRUE, 5000 WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S12');
INSERT INTO stall (stall_name, hall_id, stall_type, booking_status_id, is_active, price)
SELECT 'S13', 14, 'SMALL', 1, TRUE, 5000 WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S13');
INSERT INTO stall (stall_name, hall_id, stall_type, booking_status_id, is_active, price)
SELECT 'S14', 14, 'SMALL', 1, TRUE, 5000 WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S14');
INSERT INTO stall (stall_name, hall_id, stall_type, booking_status_id, is_active, price)
SELECT 'S15', 14, 'SMALL', 1, TRUE, 5000 WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S15');
INSERT INTO stall (stall_name, hall_id, stall_type, booking_status_id, is_active, price)
SELECT 'S16', 14, 'SMALL', 1, TRUE, 5000 WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S16');

-- ============================================================
-- HALL P (hall_id = 15)
-- ============================================================
INSERT INTO stall (stall_name, hall_id, stall_type, booking_status_id, is_active, price)
SELECT 'L-P', 15, 'LARGE', 1, TRUE, 10000 WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='L-P');


-- ============================================================
-- HALL Q (hall_id = 16)
-- ============================================================
INSERT INTO stall (stall_name, hall_id, stall_type, booking_status_id, is_active, price)
SELECT 'L-Q', 16, 'LARGE', 1, TRUE, 10000 WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='L-Q');
