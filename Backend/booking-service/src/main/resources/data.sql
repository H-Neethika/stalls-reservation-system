-- BookingStatus
INSERT INTO booking_status (id, status, color)
SELECT 1, 'AVAILABLE', '#ffffff'
WHERE NOT EXISTS (SELECT 1 FROM booking_status WHERE id = 1);

INSERT INTO booking_status (id, status, color)
SELECT 2, 'PENDING', '#ffc107'
WHERE NOT EXISTS (SELECT 1 FROM booking_status WHERE id = 2);

INSERT INTO booking_status (id, status, color)
SELECT 3, 'BOOKED', '#6c757d'
WHERE NOT EXISTS (SELECT 1 FROM booking_status WHERE id = 3);

INSERT INTO booking_status (id, status, color)
SELECT 4, 'UNAVAILABLE', '#dc3545'
WHERE NOT EXISTS (SELECT 1 FROM booking_status WHERE id = 4);

-- Genre
INSERT INTO genre (id, name)
SELECT 1, 'Music'
WHERE NOT EXISTS (SELECT 1 FROM genre WHERE id = 1);

INSERT INTO genre (id, name)
SELECT 2, 'Art'
WHERE NOT EXISTS (SELECT 1 FROM genre WHERE id = 2);

INSERT INTO genre (id, name)
SELECT 3, 'Technology'
WHERE NOT EXISTS (SELECT 1 FROM genre WHERE id = 3);

INSERT INTO genre (id, name)
SELECT 4, 'Food & Beverage'
WHERE NOT EXISTS (SELECT 1 FROM genre WHERE id = 4);

INSERT INTO genre (id, name)
SELECT 5, 'Fashion'
WHERE NOT EXISTS (SELECT 1 FROM genre WHERE id = 5);

INSERT INTO genre (id, name)
SELECT 6, 'Sports'
WHERE NOT EXISTS (SELECT 1 FROM genre WHERE id = 6);

INSERT INTO genre (id, name)
SELECT 7, 'Education'
WHERE NOT EXISTS (SELECT 1 FROM genre WHERE id = 7);

INSERT INTO genre (id, name)
SELECT 8, 'Health & Wellness'
WHERE NOT EXISTS (SELECT 1 FROM genre WHERE id = 8);

INSERT INTO genre (id, name)
SELECT 9, 'Automotive'
WHERE NOT EXISTS (SELECT 1 FROM genre WHERE id = 9);

INSERT INTO genre (id, name)
SELECT 10, 'Film & Entertainment'
WHERE NOT EXISTS (SELECT 1 FROM genre WHERE id = 10);

-- Insert halls only if they don't already exist
INSERT INTO hall (hall_name, rows, columns)
SELECT 'Hall-A', 10, 20
WHERE NOT EXISTS (SELECT 1 FROM hall WHERE hall_name = 'Hall-A');

INSERT INTO hall (hall_name, rows, columns)
SELECT 'Hall-B', 12, 25
WHERE NOT EXISTS (SELECT 1 FROM hall WHERE hall_name = 'Hall-B');

INSERT INTO hall (hall_name, rows, columns)
SELECT 'Hall-C', 8, 15
WHERE NOT EXISTS (SELECT 1 FROM hall WHERE hall_name = 'Hall-C');

INSERT INTO hall (hall_name, rows, columns)
SELECT 'Hall-D', 10, 18
WHERE NOT EXISTS (SELECT 1 FROM hall WHERE hall_name = 'Hall-D');

INSERT INTO hall (hall_name, rows, columns)
SELECT 'Hall-F', 9, 16
WHERE NOT EXISTS (SELECT 1 FROM hall WHERE hall_name = 'Hall-F');

INSERT INTO hall (hall_name, rows, columns)
SELECT 'Hall-G', 11, 20
WHERE NOT EXISTS (SELECT 1 FROM hall WHERE hall_name = 'Hall-G');

INSERT INTO hall (hall_name, rows, columns)
SELECT 'Hall-K', 10, 22
WHERE NOT EXISTS (SELECT 1 FROM hall WHERE hall_name = 'Hall-K');

INSERT INTO hall (hall_name, rows, columns)
SELECT 'Hall-J', 14, 30
WHERE NOT EXISTS (SELECT 1 FROM hall WHERE hall_name = 'Hall-J');

INSERT INTO hall (hall_name, rows, columns)
SELECT 'Hall-R', 7, 14
WHERE NOT EXISTS (SELECT 1 FROM hall WHERE hall_name = 'Hall-R');

INSERT INTO hall (hall_name, rows, columns)
SELECT 'Hall-H', 9, 17
WHERE NOT EXISTS (SELECT 1 FROM hall WHERE hall_name = 'Hall-H');

INSERT INTO hall (hall_name, rows, columns)
SELECT 'Hall-H1', 12, 24
WHERE NOT EXISTS (SELECT 1 FROM hall WHERE hall_name = 'Hall-H1');

INSERT INTO hall (hall_name, rows, columns)
SELECT 'Hall-L', 8, 18
WHERE NOT EXISTS (SELECT 1 FROM hall WHERE hall_name = 'Hall-L');

INSERT INTO hall (hall_name, rows, columns)
SELECT 'Hall-M', 10, 20
WHERE NOT EXISTS (SELECT 1 FROM hall WHERE hall_name = 'Hall-M');

INSERT INTO hall (hall_name, rows, columns)
SELECT 'Hall-N', 11, 21
WHERE NOT EXISTS (SELECT 1 FROM hall WHERE hall_name = 'Hall-N');

INSERT INTO hall (hall_name, rows, columns)
SELECT 'Hall-P', 13, 26
WHERE NOT EXISTS (SELECT 1 FROM hall WHERE hall_name = 'Hall-P');

INSERT INTO hall (hall_name, rows, columns)
SELECT 'Hall-Q', 10, 20
WHERE NOT EXISTS (SELECT 1 FROM hall WHERE hall_name = 'Hall-Q');

-- ✅ Reset sequences after inserts
SELECT setval('genre_id_seq', COALESCE((SELECT MAX(id) + 1 FROM genre), 1), false);
SELECT setval('booking_status_id_seq', COALESCE((SELECT MAX(id) + 1 FROM booking_status), 1), false);
SELECT setval('hall_id_seq', COALESCE((SELECT MAX(id) + 1 FROM hall), 1), false);

