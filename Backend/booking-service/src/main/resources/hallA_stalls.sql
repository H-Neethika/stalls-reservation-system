-- ============================================================
-- HALL A (hall_id = 1)
-- ============================================================

-- SMALL
INSERT INTO stall (stall_name, hall_id, stall_type, booking_status_id, is_active, price)
SELECT 'S01', 1, 'SMALL', 1, TRUE, 5000 WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S01');
INSERT INTO stall (stall_name, hall_id, stall_type, booking_status_id, is_active, price)
SELECT 'S02', 1, 'SMALL', 1, TRUE, 5000 WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S02');
INSERT INTO stall (stall_name, hall_id, stall_type, booking_status_id, is_active, price)
SELECT 'S11', 1, 'SMALL', 1, TRUE, 5000 WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S11');
INSERT INTO stall (stall_name, hall_id, stall_type, booking_status_id, is_active, price)
SELECT 'S13', 1, 'SMALL', 1, TRUE, 5000 WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S13');
INSERT INTO stall (stall_name, hall_id, stall_type, booking_status_id, is_active, price)
SELECT 'S14', 1, 'SMALL', 1, TRUE, 5000 WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S14');
INSERT INTO stall (stall_name, hall_id, stall_type, booking_status_id, is_active, price)
SELECT 'S15', 1, 'SMALL', 1, TRUE, 5000 WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S15');
INSERT INTO stall (stall_name, hall_id, stall_type, booking_status_id, is_active, price)
SELECT 'S18', 1, 'SMALL', 1, TRUE, 5000 WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S18');
INSERT INTO stall (stall_name, hall_id, stall_type, booking_status_id, is_active, price)
SELECT 'S19', 1, 'SMALL', 1, TRUE, 5000 WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S19');
INSERT INTO stall (stall_name, hall_id, stall_type, booking_status_id, is_active, price)
SELECT 'S20', 1, 'SMALL', 1, TRUE, 5000 WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S20');
INSERT INTO stall (stall_name, hall_id, stall_type, booking_status_id, is_active, price)
SELECT 'S23', 1, 'SMALL', 1, TRUE, 5000 WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S23');
INSERT INTO stall (stall_name, hall_id, stall_type, booking_status_id, is_active, price)
SELECT 'S24', 1, 'SMALL', 1, TRUE, 5000 WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S24');
INSERT INTO stall (stall_name, hall_id, stall_type, booking_status_id, is_active, price)
SELECT 'S25', 1, 'SMALL', 1, TRUE, 5000 WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S25');
INSERT INTO stall (stall_name, hall_id, stall_type, booking_status_id, is_active, price)
SELECT 'S27', 1, 'SMALL', 1, TRUE, 5000 WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S27');
INSERT INTO stall (stall_name, hall_id, stall_type, booking_status_id, is_active, price)
SELECT 'S29', 1, 'SMALL', 1, TRUE, 5000 WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S29');
INSERT INTO stall (stall_name, hall_id, stall_type, booking_status_id, is_active, price)
SELECT 'S30', 1, 'SMALL', 1, TRUE, 5000 WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S30');
INSERT INTO stall (stall_name, hall_id, stall_type, booking_status_id, is_active, price)
SELECT 'S31', 1, 'SMALL', 1, TRUE, 5000 WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S31');
INSERT INTO stall (stall_name, hall_id, stall_type, booking_status_id, is_active, price)
SELECT 'S33', 1, 'SMALL', 1, TRUE, 5000 WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S33');
INSERT INTO stall (stall_name, hall_id, stall_type, booking_status_id, is_active, price)
SELECT 'S34', 1, 'SMALL', 1, TRUE, 5000 WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S34');
INSERT INTO stall (stall_name, hall_id, stall_type, booking_status_id, is_active, price)
SELECT 'S36', 1, 'SMALL', 1, TRUE, 5000 WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S36');
INSERT INTO stall (stall_name, hall_id, stall_type, booking_status_id, is_active, price)
SELECT 'S37', 1, 'SMALL', 1, TRUE, 5000 WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S37');
INSERT INTO stall (stall_name, hall_id, stall_type, booking_status_id, is_active, price)
SELECT 'S38', 1, 'SMALL', 1, TRUE, 5000 WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S38');
INSERT INTO stall (stall_name, hall_id, stall_type, booking_status_id, is_active, price)
SELECT 'S40', 1, 'SMALL', 1, TRUE, 5000 WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S40');
INSERT INTO stall (stall_name, hall_id, stall_type, booking_status_id, is_active, price)
SELECT 'S41', 1, 'SMALL', 1, TRUE, 5000 WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S41');
INSERT INTO stall (stall_name, hall_id, stall_type, booking_status_id, is_active, price)
SELECT 'S43', 1, 'SMALL', 1, TRUE, 5000 WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S43');
INSERT INTO stall (stall_name, hall_id, stall_type, booking_status_id, is_active, price)
SELECT 'S44', 1, 'SMALL', 1, TRUE, 5000 WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S44');
INSERT INTO stall (stall_name, hall_id, stall_type, booking_status_id, is_active, price)
SELECT 'S45', 1, 'SMALL', 1, TRUE, 5000 WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S45');
INSERT INTO stall (stall_name, hall_id, stall_type, booking_status_id, is_active, price)
SELECT 'S47', 1, 'SMALL', 1, TRUE, 5000 WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S47');
INSERT INTO stall (stall_name, hall_id, stall_type, booking_status_id, is_active, price)
SELECT 'S48', 1, 'SMALL', 1, TRUE, 5000 WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S48');
INSERT INTO stall (stall_name, hall_id, stall_type, booking_status_id, is_active, price)
SELECT 'S50', 1, 'SMALL', 1, TRUE, 5000 WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S50');
INSERT INTO stall (stall_name, hall_id, stall_type, booking_status_id, is_active, price)
SELECT 'S51', 1, 'SMALL', 1, TRUE, 5000 WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S51');
INSERT INTO stall (stall_name, hall_id, stall_type, booking_status_id, is_active, price)
SELECT 'S52', 1, 'SMALL', 1, TRUE, 5000 WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S52');
INSERT INTO stall (stall_name, hall_id, stall_type, booking_status_id, is_active, price)
SELECT 'S53', 1, 'SMALL', 1, TRUE, 5000 WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S53');
INSERT INTO stall (stall_name, hall_id, stall_type, booking_status_id, is_active, price)
SELECT 'S55', 1, 'SMALL', 1, TRUE, 5000 WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S55');
INSERT INTO stall (stall_name, hall_id, stall_type, booking_status_id, is_active, price)
SELECT 'S56', 1, 'SMALL', 1, TRUE, 5000 WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S56');
INSERT INTO stall (stall_name, hall_id, stall_type, booking_status_id, is_active, price)
SELECT 'S57', 1, 'SMALL', 1, TRUE, 5000 WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S57');
INSERT INTO stall (stall_name, hall_id, stall_type, booking_status_id, is_active, price)
SELECT 'S58', 1, 'SMALL', 1, TRUE, 5000 WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S58');
INSERT INTO stall (stall_name, hall_id, stall_type, booking_status_id, is_active, price)
SELECT 'S60', 1, 'SMALL', 1, TRUE, 5000 WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S60');
INSERT INTO stall (stall_name, hall_id, stall_type, booking_status_id, is_active, price)
SELECT 'S61', 1, 'SMALL', 1, TRUE, 5000 WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S61');
INSERT INTO stall (stall_name, hall_id, stall_type, booking_status_id, is_active, price)
SELECT 'S62', 1, 'SMALL', 1, TRUE, 5000 WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S62');
INSERT INTO stall (stall_name, hall_id, stall_type, booking_status_id, is_active, price)
SELECT 'S63', 1, 'SMALL', 1, TRUE, 5000 WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S63');
INSERT INTO stall (stall_name, hall_id, stall_type, booking_status_id, is_active, price)
SELECT 'S64', 1, 'SMALL', 1, TRUE, 5000 WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S64');
INSERT INTO stall (stall_name, hall_id, stall_type, booking_status_id, is_active, price)
SELECT 'S66', 1, 'SMALL', 1, TRUE, 5000 WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S66');
INSERT INTO stall (stall_name, hall_id, stall_type, booking_status_id, is_active, price)
SELECT 'S67', 1, 'SMALL', 1, TRUE, 5000 WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S67');
INSERT INTO stall (stall_name, hall_id, stall_type, booking_status_id, is_active, price)
SELECT 'S68', 1, 'SMALL', 1, TRUE, 5000 WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S68');
INSERT INTO stall (stall_name, hall_id, stall_type, booking_status_id, is_active, price)
SELECT 'S69', 1, 'SMALL', 1, TRUE, 5000 WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S69');
INSERT INTO stall (stall_name, hall_id, stall_type, booking_status_id, is_active, price)
SELECT 'S70', 1, 'SMALL', 1, TRUE, 5000 WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S70');
INSERT INTO stall (stall_name, hall_id, stall_type, booking_status_id, is_active, price)
SELECT 'S72', 1, 'SMALL', 1, TRUE, 5000 WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S72');
INSERT INTO stall (stall_name, hall_id, stall_type, booking_status_id, is_active, price)
SELECT 'S73', 1, 'SMALL', 1, TRUE, 5000 WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S73');
INSERT INTO stall (stall_name, hall_id, stall_type, booking_status_id, is_active, price)
SELECT 'S75', 1, 'SMALL', 1, TRUE, 5000 WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S75');
INSERT INTO stall (stall_name, hall_id, stall_type, booking_status_id, is_active, price)
SELECT 'S76', 1, 'SMALL', 1, TRUE, 5000 WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S76');
INSERT INTO stall (stall_name, hall_id, stall_type, booking_status_id, is_active, price)
SELECT 'S77', 1, 'SMALL', 1, TRUE, 5000 WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S77');
INSERT INTO stall (stall_name, hall_id, stall_type, booking_status_id, is_active, price)
SELECT 'S78', 1, 'SMALL', 1, TRUE, 5000 WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S78');
INSERT INTO stall (stall_name, hall_id, stall_type, booking_status_id, is_active, price)
SELECT 'S79', 1, 'SMALL', 1, TRUE, 5000 WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S79');
INSERT INTO stall (stall_name, hall_id, stall_type, booking_status_id, is_active, price)
SELECT 'S81', 1, 'SMALL', 1, TRUE, 5000 WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S81');
INSERT INTO stall (stall_name, hall_id, stall_type, booking_status_id, is_active, price)
SELECT 'S82', 1, 'SMALL', 1, TRUE, 5000 WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S82');

-- MEDIUM
INSERT INTO stall (stall_name, hall_id, stall_type, booking_status_id, is_active, price)
SELECT 'M05', 1, 'MEDIUM', 1, TRUE, 7000 WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='M05');
INSERT INTO stall (stall_name, hall_id, stall_type, booking_status_id, is_active, price)
SELECT 'M06', 1, 'MEDIUM', 1, TRUE, 7000 WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='M06');
INSERT INTO stall (stall_name, hall_id, stall_type, booking_status_id, is_active, price)
SELECT 'M07', 1, 'MEDIUM', 1, TRUE, 7000 WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='M07');
INSERT INTO stall (stall_name, hall_id, stall_type, booking_status_id, is_active, price)
SELECT 'M08', 1, 'MEDIUM', 1, TRUE, 7000 WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='M08');
INSERT INTO stall (stall_name, hall_id, stall_type, booking_status_id, is_active, price)
SELECT 'M12', 1, 'MEDIUM', 1, TRUE, 7000 WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='M12');
INSERT INTO stall (stall_name, hall_id, stall_type, booking_status_id, is_active, price)
SELECT 'M16', 1, 'MEDIUM', 1, TRUE, 7000 WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='M16');
INSERT INTO stall (stall_name, hall_id, stall_type, booking_status_id, is_active, price)
SELECT 'M17', 1, 'MEDIUM', 1, TRUE, 7000 WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='M17');
INSERT INTO stall (stall_name, hall_id, stall_type, booking_status_id, is_active, price)
SELECT 'M21', 1, 'MEDIUM', 1, TRUE, 7000 WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='M21');
INSERT INTO stall (stall_name, hall_id, stall_type, booking_status_id, is_active, price)
SELECT 'M22', 1, 'MEDIUM', 1, TRUE, 7000 WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='M22');
INSERT INTO stall (stall_name, hall_id, stall_type, booking_status_id, is_active, price)
SELECT 'M26', 1, 'MEDIUM', 1, TRUE, 7000 WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='M26');
INSERT INTO stall (stall_name, hall_id, stall_type, booking_status_id, is_active, price)
SELECT 'M28', 1, 'MEDIUM', 1, TRUE, 7000 WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='M28');
INSERT INTO stall (stall_name, hall_id, stall_type, booking_status_id, is_active, price)
SELECT 'M32', 1, 'MEDIUM', 1, TRUE, 7000 WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='M32');
INSERT INTO stall (stall_name, hall_id, stall_type, booking_status_id, is_active, price)
SELECT 'M35', 1, 'MEDIUM', 1, TRUE, 7000 WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='M35');
INSERT INTO stall (stall_name, hall_id, stall_type, booking_status_id, is_active, price)
SELECT 'M39', 1, 'MEDIUM', 1, TRUE, 7000 WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='M39');
INSERT INTO stall (stall_name, hall_id, stall_type, booking_status_id, is_active, price)
SELECT 'M42', 1, 'MEDIUM', 1, TRUE, 7000 WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='M42');
INSERT INTO stall (stall_name, hall_id, stall_type, booking_status_id, is_active, price)
SELECT 'M46', 1, 'MEDIUM', 1, TRUE, 7000 WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='M46');
INSERT INTO stall (stall_name, hall_id, stall_type, booking_status_id, is_active, price)
SELECT 'M54', 1, 'MEDIUM', 1, TRUE, 7000 WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='M54');
INSERT INTO stall (stall_name, hall_id, stall_type, booking_status_id, is_active, price)
SELECT 'M59', 1, 'MEDIUM', 1, TRUE, 7000 WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='M59');
INSERT INTO stall (stall_name, hall_id, stall_type, booking_status_id, is_active, price)
SELECT 'M65', 1, 'MEDIUM', 1, TRUE, 7000 WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='M65');
INSERT INTO stall (stall_name, hall_id, stall_type, booking_status_id, is_active, price)
SELECT 'M71', 1, 'MEDIUM', 1, TRUE, 7000 WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='M71');
INSERT INTO stall (stall_name, hall_id, stall_type, booking_status_id, is_active, price)
SELECT 'M74', 1, 'MEDIUM', 1, TRUE, 7000 WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='M74');
INSERT INTO stall (stall_name, hall_id, stall_type, booking_status_id, is_active, price)
SELECT 'M80', 1, 'MEDIUM', 1, TRUE, 7000 WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='M80');

-- LARGE
INSERT INTO stall (stall_name, hall_id, stall_type, booking_status_id, is_active, price)
SELECT 'L02', 1, 'LARGE', 1, TRUE, 10000 WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='L02');
INSERT INTO stall (stall_name, hall_id, stall_type, booking_status_id, is_active, price)
SELECT 'L03', 1, 'LARGE', 1, TRUE, 10000 WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='L03');
INSERT INTO stall (stall_name, hall_id, stall_type, booking_status_id, is_active, price)
SELECT 'L04', 1, 'LARGE', 1, TRUE, 10000 WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='L04');
INSERT INTO stall (stall_name, hall_id, stall_type, booking_status_id, is_active, price)
SELECT 'L09', 1, 'LARGE', 1, TRUE, 10000 WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='L09');
INSERT INTO stall (stall_name, hall_id, stall_type, booking_status_id, is_active, price)
SELECT 'L10', 1, 'LARGE', 1, TRUE, 10000 WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='L10');
INSERT INTO stall (stall_name, hall_id, stall_type, booking_status_id, is_active, price)
SELECT 'L49', 1, 'LARGE', 1, TRUE, 10000 WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='L49');

