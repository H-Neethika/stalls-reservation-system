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

-- Hall
INSERT INTO hall (id, hall_name)
SELECT 1, 'Hall-A'
WHERE NOT EXISTS (SELECT 1 FROM hall WHERE hall_name = 'Hall-A');

INSERT INTO hall (id, hall_name)
SELECT 2, 'Hall-B'
WHERE NOT EXISTS (SELECT 1 FROM hall WHERE hall_name = 'Hall-B');

INSERT INTO hall (id, hall_name)
SELECT 3, 'Hall-C'
WHERE NOT EXISTS (SELECT 1 FROM hall WHERE hall_name = 'Hall-C');

INSERT INTO hall (id, hall_name)
SELECT 4, 'Hall-D'
WHERE NOT EXISTS (SELECT 1 FROM hall WHERE hall_name = 'Hall-D');

INSERT INTO hall (id, hall_name)
SELECT 5, 'Hall-F'
WHERE NOT EXISTS (SELECT 1 FROM hall WHERE hall_name = 'Hall-F');

INSERT INTO hall (id, hall_name)
SELECT 6, 'Hall-G'
WHERE NOT EXISTS (SELECT 1 FROM hall WHERE hall_name = 'Hall-G');

INSERT INTO hall (id, hall_name)
SELECT 7, 'Hall-K'
WHERE NOT EXISTS (SELECT 1 FROM hall WHERE hall_name = 'Hall-K');

INSERT INTO hall (id, hall_name)
SELECT 8, 'Hall-J'
WHERE NOT EXISTS (SELECT 1 FROM hall WHERE hall_name = 'Hall-J');

INSERT INTO hall (id, hall_name)
SELECT 9, 'Hall-R'
WHERE NOT EXISTS (SELECT 1 FROM hall WHERE hall_name = 'Hall-R');

INSERT INTO hall (id, hall_name)
SELECT 10, 'Hall-H'
WHERE NOT EXISTS (SELECT 1 FROM hall WHERE hall_name = 'Hall-H');

INSERT INTO hall (id, hall_name)
SELECT 11, 'Hall-H1'
WHERE NOT EXISTS (SELECT 1 FROM hall WHERE hall_name = 'Hall-H1');

INSERT INTO hall (id, hall_name)
SELECT 12, 'Hall-L'
WHERE NOT EXISTS (SELECT 1 FROM hall WHERE hall_name = 'Hall-L');

INSERT INTO hall (id, hall_name)
SELECT 13, 'Hall-M'
WHERE NOT EXISTS (SELECT 1 FROM hall WHERE hall_name = 'Hall-M');

INSERT INTO hall (id, hall_name)
SELECT 14, 'Hall-N'
WHERE NOT EXISTS (SELECT 1 FROM hall WHERE hall_name = 'Hall-N');

INSERT INTO hall (id, hall_name)
SELECT 15, 'Hall-P'
WHERE NOT EXISTS (SELECT 1 FROM hall WHERE hall_name = 'Hall-P');

INSERT INTO hall (id, hall_name)
SELECT 16, 'Hall-Q'
WHERE NOT EXISTS (SELECT 1 FROM hall WHERE hall_name = 'Hall-Q');



