INSERT INTO users (email, password, first_name, last_name, phone)
VALUES ('test1@example.com', '{noop}password', 'Test1', 'User', '+1000000001'),
       ('test2@example.com', '{noop}password', 'Test2', 'User', '+1000000002'),
       ('test3@example.com', '{noop}password', 'Test3', 'User', '+1000000003'),
       ('trainer1@example.com', '{noop}password', 'Trainer1', 'User', '+1000000004'),
       ('trainer2@example.com', '{noop}password', 'Trainer2', 'User', '+1000000005'),
       ('admin1@example.com', '{noop}password', 'Admin1', 'User', '+1000000006');

INSERT INTO user_roles (user_id, role_id)
VALUES (1, 1),
       (2, 1),
       (3, 1),
       (4, 2),
       (5, 2),
       (6, 3);

INSERT INTO rooms (name, capacity)
VALUES ('room1', 10),
       ('room2', 15),
       ('room3', 20),
       ('room4', 25);

INSERT INTO training_classes (name, description, trainer_id, room_id, start_time, end_time, capacity, average_rating)
VALUES ('class1', 'desc1', 4, 1, '2026-01-10 08:00:00', '2026-01-10 09:00:00', 10, 4.1),
       ('class2', 'desc2', 4, 2, '2026-01-10 10:00:00', '2026-01-10 11:00:00', 12, 3.9),
       ('class3', 'desc3', 5, 3, '2026-01-11 09:00:00', '2026-01-11 10:00:00', 15, 4.3),
       ('class4', 'desc4', 5, 4, '2026-01-11 17:00:00', '2026-01-11 18:00:00', 20, 4.7),
       ('class5', 'desc5', 4, 1, '2026-01-12 08:00:00', '2026-01-12 09:00:00', 8, 4.5),
       ('class6', 'desc6', 5, 2, '2026-01-12 18:00:00', '2026-01-12 19:00:00', 10, 4.9);

INSERT INTO memberships (user_id, type, start_date, end_date, price, active)
VALUES (1, 'MONTHLY', '2026-01-01', '2026-02-01', 99.99, true),
       (2, 'MONTHLY', '2026-01-01', '2026-02-01', 99.99, true),
       (3, 'ANNUAL', '2026-01-01', '2027-01-01', 899.99, true),
       (1, 'MONTHLY', '2025-10-01', '2025-11-01', 99.99, false);

INSERT INTO reservations (user_id, training_class_id, reservation_date, status)
VALUES (1, 1, '2026-01-05 10:00:00', 'CONFIRMED'),
       (2, 2, '2026-01-05 10:05:00', 'CONFIRMED'),
       (3, 3, '2026-01-05 10:10:00', 'CONFIRMED'),
       (1, 4, '2026-01-05 10:15:00', 'PENDING'),
       (2, 5, '2026-01-05 10:20:00', 'CANCELLED'),
       (3, 6, '2026-01-05 10:25:00', 'CONFIRMED');

INSERT INTO equipment (name, quantity, room_id, status)
VALUES ('equip1', 5, 1, 'AVAILABLE'),
       ('equip2', 10, 2, 'AVAILABLE'),
       ('equip3', 7, 3, 'AVAILABLE'),
       ('equip4', 3, 4, 'AVAILABLE'),
       ('equip5', 2, 1, 'BROKEN');