-- Database Name: excursinx
CREATE DATABASE IF NOT EXISTS excursinx;
USE excursinx;

-- 1. ADMIN TABLE (Separate table as requested)
CREATE TABLE Admins (
    admin_id VARCHAR(20) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    last_login TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. TEACHERS TABLE
CREATE TABLE Teachers (
    teacher_id VARCHAR(20) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    contact_no VARCHAR(15),
    role_description VARCHAR(50), -- e.g., 'Trip Coordinator' [cite: 224]
    password VARCHAR(255) NOT NULL
);

-- 3. STUDENTS TABLE
CREATE TABLE Students (
    student_id VARCHAR(20) PRIMARY KEY, -- PRN [cite: 11]
    name VARCHAR(100) NOT NULL,
    class VARCHAR(50),
    email VARCHAR(100) UNIQUE NOT NULL,
    contact_no VARCHAR(15),
    emergency_contact VARCHAR(15), [cite: 36]
    password VARCHAR(255) NOT NULL
);

-- 4. TRIPS TABLE
CREATE TABLE Trips (
    trip_id INT AUTO_INCREMENT PRIMARY KEY,
    destination VARCHAR(255) NOT NULL,
    start_date DATE,
    end_date DATE,
    capacity INT,
    total_budget DECIMAL(10, 2),
    trip_status VARCHAR(20) DEFAULT 'Upcoming' -- e.g., 'Upcoming', 'Active', 'Completed'
);

-- 5. ACTIVITIES TABLE (For scheduling and conflict detection) [cite: 35]
CREATE TABLE Activities (
    activity_id INT AUTO_INCREMENT PRIMARY KEY,
    trip_id INT,
    activity_name VARCHAR(255),
    time_slot TIME,
    location VARCHAR(255),
    FOREIGN KEY (trip_id) REFERENCES Trips(trip_id) ON DELETE CASCADE
);

-- 6. BOOKINGS TABLE (Seat allocation) [cite: 31]
CREATE TABLE Bookings (
    booking_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id VARCHAR(20),
    trip_id INT,
    seat_number INT,
    booking_status VARCHAR(20) DEFAULT 'Confirmed',
    booking_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES Students(student_id),
    FOREIGN KEY (trip_id) REFERENCES Trips(trip_id)
);

-- 7. PAYMENTS TABLE (Secure transactions) [cite: 33]
CREATE TABLE Payments (
    payment_id INT AUTO_INCREMENT PRIMARY KEY,
    booking_id INT,
    amount DECIMAL(10, 2),
    payment_mode VARCHAR(50),
    transaction_status VARCHAR(20) DEFAULT 'Pending',
    payment_date DATETIME,
    FOREIGN KEY (booking_id) REFERENCES Bookings(booking_id)
);

-- 8. EXPENSES TABLE (Budget monitoring) [cite: 34]
CREATE TABLE Expenses (
    expense_id INT AUTO_INCREMENT PRIMARY KEY,
    trip_id INT,
    category VARCHAR(100), -- e.g., Transport, Food, Entry Fees [cite: 22]
    amount DECIMAL(10, 2),
    expense_date DATE,
    description TEXT,
    FOREIGN KEY (trip_id) REFERENCES Trips(trip_id)
);
