-- =============================================================
--  ExcursionX — Smart Student Excursion & Trip Management System
--  Database Schema  |  MySQL 8.x
-- =============================================================

CREATE DATABASE IF NOT EXISTS excursionx;
USE excursionx;

-- ─── DROP TABLES (in safe order, children first) ──────────────
DROP TABLE IF EXISTS EmergencyLogs;
DROP TABLE IF EXISTS Complaints;
DROP TABLE IF EXISTS Payments;
DROP TABLE IF EXISTS Bookings;
DROP TABLE IF EXISTS Expenses;
DROP TABLE IF EXISTS Activities;
DROP TABLE IF EXISTS Trips;
DROP TABLE IF EXISTS Students;
DROP TABLE IF EXISTS Teachers;
DROP TABLE IF EXISTS Admins;

-- ─── 1. ADMINS ─────────────────────────────────────────────────
CREATE TABLE Admins (
    admin_id    VARCHAR(20)  PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    email       VARCHAR(150) NOT NULL UNIQUE,
    password    VARCHAR(256) NOT NULL
);

-- ─── 2. TEACHERS ───────────────────────────────────────────────
CREATE TABLE Teachers (
    teacher_id  VARCHAR(20)  PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    email       VARCHAR(150) NOT NULL UNIQUE,
    password    VARCHAR(256) NOT NULL,
    contact_no  VARCHAR(15)
);

-- ─── 3. STUDENTS ───────────────────────────────────────────────
CREATE TABLE Students (
    student_id        VARCHAR(20)  PRIMARY KEY,
    name              VARCHAR(100) NOT NULL,
    email             VARCHAR(150) NOT NULL UNIQUE,
    password          VARCHAR(256) NOT NULL,
    class             VARCHAR(20),
    emergency_contact VARCHAR(15)
);

-- ─── 4. TRIPS ──────────────────────────────────────────────────
CREATE TABLE Trips (
    trip_id      INT          AUTO_INCREMENT PRIMARY KEY,
    destination  VARCHAR(200) NOT NULL,
    start_date   DATE,
    end_date     DATE,
    capacity     INT          NOT NULL DEFAULT 30,
    total_budget DOUBLE       NOT NULL DEFAULT 0,
    teacher_id   VARCHAR(20),
    CONSTRAINT fk_trip_teacher FOREIGN KEY (teacher_id) REFERENCES Teachers(teacher_id) ON DELETE SET NULL
);

-- ─── 5. BOOKINGS ───────────────────────────────────────────────
CREATE TABLE Bookings (
    booking_id        INT         AUTO_INCREMENT PRIMARY KEY,
    student_id        VARCHAR(20) NOT NULL,
    trip_id           INT         NOT NULL,
    booking_date      TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    payment_status    ENUM('Pending','Paid','Refunded') DEFAULT 'Pending',
    attendance_status ENUM('Present','Absent','Excused','Not Marked') DEFAULT 'Not Marked',
    CONSTRAINT fk_booking_student FOREIGN KEY (student_id) REFERENCES Students(student_id) ON DELETE CASCADE,
    CONSTRAINT fk_booking_trip    FOREIGN KEY (trip_id)    REFERENCES Trips(trip_id)    ON DELETE CASCADE,
    UNIQUE KEY uq_student_trip (student_id, trip_id)
);

-- ─── 6. PAYMENTS ───────────────────────────────────────────────
CREATE TABLE Payments (
    payment_id     INT         AUTO_INCREMENT PRIMARY KEY,
    booking_id     INT         NOT NULL,
    student_id     VARCHAR(20) NOT NULL,
    trip_id        INT         NOT NULL,
    amount         DOUBLE      NOT NULL DEFAULT 0,
    payment_method ENUM('UPI','Card','Cash','Net Banking') DEFAULT 'UPI',
    payment_status ENUM('Pending','Paid','Failed') DEFAULT 'Pending',
    paid_at        TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_payment_booking FOREIGN KEY (booking_id) REFERENCES Bookings(booking_id) ON DELETE CASCADE
);

-- ─── 7. EXPENSES ───────────────────────────────────────────────
CREATE TABLE Expenses (
    expense_id  INT          AUTO_INCREMENT PRIMARY KEY,
    trip_id     INT          NOT NULL,
    category    VARCHAR(100) NOT NULL,
    amount      DOUBLE       NOT NULL DEFAULT 0,
    description VARCHAR(500),
    added_at    TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_expense_trip FOREIGN KEY (trip_id) REFERENCES Trips(trip_id) ON DELETE CASCADE
);

-- ─── 8. ACTIVITIES ─────────────────────────────────────────────
CREATE TABLE Activities (
    activity_id   INT          AUTO_INCREMENT PRIMARY KEY,
    trip_id       INT          NOT NULL,
    activity_name VARCHAR(200) NOT NULL,
    activity_date DATE,
    location      VARCHAR(200),
    status        ENUM('Planned','Ongoing','Completed','Cancelled') DEFAULT 'Planned',
    CONSTRAINT fk_activity_trip FOREIGN KEY (trip_id) REFERENCES Trips(trip_id) ON DELETE CASCADE
);

-- ─── 9. COMPLAINTS ─────────────────────────────────────────────
CREATE TABLE Complaints (
    complaint_id INT         AUTO_INCREMENT PRIMARY KEY,
    student_id   VARCHAR(20) NOT NULL,
    teacher_id   VARCHAR(20),
    issue        TEXT        NOT NULL,
    date_filed   TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_complaint_student FOREIGN KEY (student_id) REFERENCES Students(student_id) ON DELETE CASCADE,
    CONSTRAINT fk_complaint_teacher FOREIGN KEY (teacher_id) REFERENCES Teachers(teacher_id) ON DELETE SET NULL
);

-- ─── 10. EMERGENCY LOGS ─────────────────────────────────────────
CREATE TABLE EmergencyLogs (
    emergency_id   INT          AUTO_INCREMENT PRIMARY KEY,
    trip_id        INT          NOT NULL,
    reported_by    VARCHAR(20),
    description    TEXT         NOT NULL,
    contact_number VARCHAR(15),
    logged_at      TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_emergency_trip FOREIGN KEY (trip_id) REFERENCES Trips(trip_id) ON DELETE CASCADE
);

-- =============================================================
--  SAMPLE DATA
-- =============================================================

-- Admins (password = SHA2("Admin@123", 256))
INSERT INTO Admins (admin_id, name, email, password) VALUES
('AD-0001', 'Super Admin', 'admin@excursionx.com',
 SHA2('Admin@123', 256));

-- Teachers (password = SHA2("Teacher@123", 256))
INSERT INTO Teachers (teacher_id, name, email, password, contact_no) VALUES
('TE-0001', 'Priya Sharma',   'priya@excursionx.com',  SHA2('Teacher@123', 256), '9876543210'),
('TE-0002', 'Rahul Mehta',    'rahul@excursionx.com',  SHA2('Teacher@123', 256), '9123456780');

-- Students (password = SHA2("Student@123", 256))
INSERT INTO Students (student_id, name, email, password, class, emergency_contact) VALUES
('ST-0001', 'Aarav Singh',    'aarav@excursionx.com',  SHA2('Student@123', 256), '10-A', '9000000001'),
('ST-0002', 'Meera Patel',    'meera@excursionx.com',  SHA2('Student@123', 256), '10-B', '9000000002'),
('ST-0003', 'Rohan Gupta',    'rohan@excursionx.com',  SHA2('Student@123', 256), '11-A', '9000000003');

-- Trips
INSERT INTO Trips (destination, start_date, end_date, capacity, total_budget, teacher_id) VALUES
('Manali Snow Adventure',  '2025-12-20', '2025-12-25', 40, 35000, 'TE-0001'),
('Goa Beach Retreat',      '2026-01-10', '2026-01-14', 50, 28000, 'TE-0002'),
('Ladakh Expedition',      '2026-03-01', '2026-03-07', 25, 55000, 'TE-0001');

-- Bookings
INSERT INTO Bookings (student_id, trip_id, payment_status, attendance_status) VALUES
('ST-0001', 1, 'Paid',    'Present'),
('ST-0002', 1, 'Pending', 'Absent'),
('ST-0003', 2, 'Paid',    'Present'),
('ST-0001', 2, 'Pending', 'Not Marked');

-- Payments
INSERT INTO Payments (booking_id, student_id, trip_id, amount, payment_method, payment_status) VALUES
(1, 'ST-0001', 1, 35000, 'UPI',  'Paid'),
(3, 'ST-0003', 2, 28000, 'Card', 'Paid');

-- Expenses
INSERT INTO Expenses (trip_id, category, amount, description) VALUES
(1, 'Transport', 12000, 'Volvo bus hire Manali round trip'),
(1, 'Hotel',     15000, 'Snow Palace hotel 5 nights'),
(2, 'Transport',  8000, 'Flight tickets Goa'),
(2, 'Food',       5000, 'Group meals and snacks');

-- Activities
INSERT INTO Activities (trip_id, activity_name, activity_date, location, status) VALUES
(1, 'Skiing Lesson',       '2025-12-21', 'Solang Valley',  'Planned'),
(1, 'Camp Fire Night',     '2025-12-22', 'Camp Base',       'Planned'),
(2, 'Beach Volleyball',    '2026-01-11', 'Calangute Beach', 'Planned'),
(2, 'Dolphin Boat Ride',   '2026-01-12', 'Candolim',        'Planned');

-- Emergency Logs
INSERT INTO EmergencyLogs (trip_id, reported_by, description, contact_number) VALUES
(1, 'TE-0001', 'Student ST-0002 sprained ankle during skiing. First aid administered.', '9876543210');

-- Complaints
INSERT INTO Complaints (student_id, teacher_id, issue) VALUES
('ST-0002', 'TE-0001', 'Repeated misbehaviour during Manali trip — broke hotel property.');
