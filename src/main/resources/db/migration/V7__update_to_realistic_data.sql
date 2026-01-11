TRUNCATE TABLE
    notifications,
    schedule_change_requests,
    personal_training_sessions,
    trainer_notes,
    equipment,
    reservations,
    memberships,
    training_classes,
    rooms,
    user_roles,
    users
    CASCADE;

-- Сбросить последовательности
ALTER SEQUENCE users_id_seq RESTART WITH 1;
ALTER SEQUENCE roles_id_seq RESTART WITH 1;
ALTER SEQUENCE rooms_id_seq RESTART WITH 1;
ALTER SEQUENCE training_classes_id_seq RESTART WITH 1;
ALTER SEQUENCE memberships_id_seq RESTART WITH 1;
ALTER SEQUENCE reservations_id_seq RESTART WITH 1;
ALTER SEQUENCE equipment_id_seq RESTART WITH 1;
ALTER SEQUENCE trainer_notes_id_seq RESTART WITH 1;
ALTER SEQUENCE personal_training_sessions_id_seq RESTART WITH 1;
ALTER SEQUENCE schedule_change_requests_id_seq RESTART WITH 1;
ALTER SEQUENCE notifications_id_seq RESTART WITH 1;



INSERT INTO users (email, password, first_name, last_name, phone)
VALUES
    ('jan.kowalski@example.com', '{noop}password123', 'Jan', 'Kowalski', '+48123456789'),
    ('anna.nowak@example.com', '{noop}password456', 'Anna', 'Nowak', '+48987654321'),
    ('piotr.wisniewski@example.com', '{noop}password789', 'Piotr', 'Wiśniewski', '+48111222333'),
    ('tomasz.jankowski@example.com', '{noop}trainerpass1', 'Tomasz', 'Jankowski', '+48444555666'),
    ('magdalena.lewandowska@example.com', '{noop}trainerpass2', 'Magdalena', 'Lewandowska', '+48777888999'),
    ('admin.system@example.com', '{noop}adminpass123', 'Admin', 'Systemowy', '+48111111111');

-- Назначение ролей пользователям
INSERT INTO user_roles (user_id, role_id)
VALUES
    (1, 1),
    (2, 1),
    (3, 1),
    (4, 2),
    (5, 2),
    (6, 3);

INSERT INTO rooms (name, capacity)
VALUES
    ('Sala A - Cardio', 20),
    ('Sala B - Siłownia', 25),
    ('Sala C - Joga/Pilates', 15),
    ('Sala D - Crossfit', 18);

INSERT INTO training_classes (name, description, trainer_id, room_id, start_time, end_time, capacity, average_rating)
VALUES
    ('Poranny trening cardio', 'Intensywny trening cardio na początek dnia', 4, 1, '2025-12-15 08:00:00', '2025-12-15 09:00:00', 20, 4.5),
    ('Joga dla początkujących', 'Łagodna sesja jogi wprowadzająca', 5, 3, '2025-12-16 10:00:00', '2025-12-16 11:00:00', 15, 4.8),

    ('Trening siłowy', 'Trening z ciężarami dla średniozaawansowanych', 4, 2, '2026-01-10 17:00:00', '2026-01-10 18:30:00', 25, 4.3),
    ('Crossfit', 'Intensywny trening funkcjonalny', 5, 4, '2026-01-12 18:00:00', '2026-01-12 19:00:00', 18, 4.7),

    ('Pilates', 'Trening wzmacniający mięśnie głębokie', 5, 3, '2026-01-20 09:00:00', '2026-01-20 10:00:00', 15, 4.6),
    ('HIIT - styczeń', 'Trening interwałowy o wysokiej intensywności', 4, 1, '2026-01-25 10:00:00', '2026-01-25 11:00:00', 20, 4.9),
    ('HIIT - luty', 'Trening interwałowy - zaawansowany', 4, 1, '2026-02-05 10:00:00', '2026-02-05 11:00:00', 20, NULL);

INSERT INTO memberships (user_id, type, start_date, end_date, price, active)
VALUES
    (1, 'MONTHLY', '2026-01-01', '2026-02-01', 129.99, true),
    (2, 'MONTHLY', '2026-01-01', '2026-02-01', 129.99, true),
    (3, 'ANNUAL', '2026-01-01', '2027-01-01', 1199.99, true),
    (1, 'MONTHLY', '2025-12-01', '2026-01-01', 129.99, false);

INSERT INTO reservations (user_id, training_class_id, reservation_date, status)
VALUES
    (1, 1, '2025-12-10 10:00:00', 'CONFIRMED'),
    (2, 2, '2025-12-11 10:30:00', 'CONFIRMED'),
    (3, 3, '2026-01-05 09:00:00', 'CONFIRMED'),
    (1, 4, '2026-01-07 08:00:00', 'PENDING'),
    (2, 5, '2026-01-08 12:00:00', 'CANCELLED'),
    (3, 6, '2026-01-09 14:00:00', 'CONFIRMED');

INSERT INTO equipment (name, quantity, room_id, status)
VALUES
    ('Bieżnia', 5, 1, 'AVAILABLE'),
    ('Rower stacjonarny', 8, 1, 'AVAILABLE'),
    ('Hantle (różne ciężary)', 15, 2, 'AVAILABLE'),
    ('Ławka treningowa', 4, 2, 'AVAILABLE'),
    ('Mata do jogi', 20, 3, 'AVAILABLE'),
    ('Guma oporowa', 10, 3, 'AVAILABLE'),
    ('Skakanka', 12, 4, 'AVAILABLE'),
    ('Kettlebell', 8, 4, 'BROKEN');

INSERT INTO trainer_notes (trainer_id, client_id, note, created_at)
VALUES
    (4, 1, 'Jan robi duże postępy w wytrzymałości. Zalecam kontynuację treningów cardio.', '2025-12-18 09:00:00'),
    (4, 2, 'Anna potrzebuje pracy nad techniką podnoszenia ciężarów. Umówić na konsultację.', '2025-12-20 10:00:00'),
    (5, 3, 'Piotr świetnie radzi sobie z treningami funkcjonalnymi. Może zwiększyć intensywność.', '2026-01-05 11:00:00');

INSERT INTO personal_training_sessions (trainer_id, client_id, start_time, end_time, session_goal, session_notes, status, created_at)
VALUES
    (4, 1, '2026-01-15 15:00:00', '2026-01-15 16:00:00', 'Poprawa techniki biegu', 'Skupić się na prawidłowej postawie', 'SCHEDULED', '2026-01-08 12:00:00'),
    (4, 2, '2026-01-16 16:00:00', '2026-01-16 17:00:00', 'Trening siłowy - technika', 'Omówić prawidłowe podnoszenie ciężarów', 'SCHEDULED', '2026-01-09 12:15:00'),
    (5, 3, '2025-12-20 14:00:00', '2025-12-20 15:00:00', 'Trening funkcjonalny', 'Ćwiczenia z wykorzystaniem masy ciała', 'COMPLETED', '2025-12-15 10:00:00');

INSERT INTO schedule_change_requests
(trainer_id, training_class_id, request_type, reason, class_name, class_description,
 requested_start_time, requested_end_time, requested_capacity, requested_room_id, status, created_at)
VALUES
    (4, NULL, 'ADD', 'Większe zainteresowanie porannymi treningami', 'Poranny crossfit',
     'Trening crossfit na początek dnia', '2026-02-10 07:00:00', '2026-02-10 08:00:00',
     15, 4, 'PENDING', '2026-01-10 08:00:00'),
    (5, NULL, 'MODIFY', 'Potrzeba więcej miejsc na popularne zajęcia', 'Zaawansowana joga',
     'Joga dla zaawansowanych - rozszerzenie grupy', '2026-01-25 17:00:00', '2026-01-25 18:30:00',
     20, 3, 'PENDING', '2026-01-10 09:30:00');


INSERT INTO notifications (recipient_id, message, sent_at, read)
VALUES
    (4, 'Nowa prośba o indywidualny trening od Jana Kowalskiego', '2026-01-08 10:00:00', false),
    (4, 'Przypomnienie: jutro zajęcia "Trening siłowy" o 17:00', '2026-01-09 16:00:00', false),
    (5, 'Anulowano rezerwację na zajęcia "Pilates" od Anny Nowak', '2026-01-08 15:00:00', true),
    (1, 'Twoja rezerwacja na "Trening siłowy" została potwierdzona', '2026-01-05 12:00:00', false),
    (2, 'Przypomnienie: Twoje zajęcia "Crossfit" zaczynają się za 2 godziny', '2026-01-12 16:00:00', false),
    (3, 'Twój abonament roczny wygasa za 11 miesięcy', '2026-01-02 09:00:00', true),
    (1, 'Twój trening cardio z 15 grudnia został oceniony na 5/5', '2025-12-16 10:00:00', true);