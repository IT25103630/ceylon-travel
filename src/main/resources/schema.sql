IF OBJECT_ID(N'[dbo].[users]', N'U') IS NULL
CREATE TABLE users (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(180) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('TOURIST','GUIDE','COMMUNITY','ADMIN')),
    phone VARCHAR(30) NOT NULL DEFAULT '',
    active BIT NOT NULL DEFAULT 1,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

IF OBJECT_ID(N'[dbo].[guide_profiles]', N'U') IS NULL
CREATE TABLE guide_profiles (
    user_id BIGINT PRIMARY KEY,
    bio VARCHAR(2000) NOT NULL,
    location VARCHAR(100) NOT NULL,
    daily_rate DECIMAL(10,2) NOT NULL CHECK (daily_rate>=0),
    verified BIT NOT NULL DEFAULT 0,
    image_url VARCHAR(500) NOT NULL DEFAULT '',
    FOREIGN KEY (user_id) REFERENCES users(id)
);

IF OBJECT_ID(N'[dbo].[guide_languages]', N'U') IS NULL
CREATE TABLE guide_languages (
    guide_id BIGINT NOT NULL,
    language VARCHAR(50) NOT NULL,
    PRIMARY KEY (guide_id, language),
    FOREIGN KEY (guide_id) REFERENCES guide_profiles(user_id)
);

IF OBJECT_ID(N'[dbo].[places]', N'U') IS NULL
CREATE TABLE places (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    submitted_by BIGINT NOT NULL,
    name VARCHAR(150) NOT NULL,
    description VARCHAR(4000) NOT NULL,
    location VARCHAR(150) NOT NULL,
    category VARCHAR(50) NOT NULL,
    tips VARCHAR(1500) NOT NULL DEFAULT '',
    image_url VARCHAR(500) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING','APPROVED','REJECTED','ARCHIVED')),
    reviewed_by BIGINT NULL,
    reviewed_at DATETIME NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (submitted_by) REFERENCES users(id),
    FOREIGN KEY (reviewed_by) REFERENCES users(id)
);

IF OBJECT_ID(N'[dbo].[availability]', N'U') IS NULL
CREATE TABLE availability (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    guide_id BIGINT NOT NULL,
    available_date DATE NOT NULL,
    UNIQUE(guide_id, available_date),
    FOREIGN KEY (guide_id) REFERENCES guide_profiles(user_id)
);

IF OBJECT_ID(N'[dbo].[bookings]', N'U') IS NULL
CREATE TABLE bookings (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    tourist_id BIGINT NOT NULL,
    guide_id BIGINT NOT NULL,
    place_id BIGINT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    guests INT NOT NULL CHECK (guests BETWEEN 1 AND 20),
    details VARCHAR(2000) NOT NULL DEFAULT '',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING','CONFIRMED','REJECTED','CANCELLED','COMPLETED')),
    total_price DECIMAL(12,2) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    CHECK (end_date>=start_date),
    FOREIGN KEY (tourist_id) REFERENCES users(id),
    FOREIGN KEY (guide_id) REFERENCES guide_profiles(user_id),
    FOREIGN KEY (place_id) REFERENCES places(id)
);

IF OBJECT_ID(N'[dbo].[conversations]', N'U') IS NULL
CREATE TABLE conversations (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    tourist_id BIGINT NOT NULL,
    guide_id BIGINT NOT NULL,
    UNIQUE(tourist_id, guide_id),
    FOREIGN KEY (tourist_id) REFERENCES users(id),
    FOREIGN KEY (guide_id) REFERENCES guide_profiles(user_id)
);

IF OBJECT_ID(N'[dbo].[messages]', N'U') IS NULL
CREATE TABLE messages (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    conversation_id BIGINT NOT NULL,
    sender_id BIGINT NOT NULL,
    body VARCHAR(2000) NOT NULL,
    edited BIT NOT NULL DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (conversation_id) REFERENCES conversations(id),
    FOREIGN KEY (sender_id) REFERENCES users(id)
);

IF OBJECT_ID(N'[dbo].[reviews]', N'U') IS NULL
CREATE TABLE reviews (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    author_id BIGINT NOT NULL,
    booking_id BIGINT NULL UNIQUE,
    place_id BIGINT NULL,
    rating INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment VARCHAR(2000) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PUBLISHED' CHECK (status IN ('PENDING','PUBLISHED','REJECTED')),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(author_id, place_id),
    CHECK ((booking_id IS NOT NULL AND place_id IS NULL) OR (booking_id IS NULL AND place_id IS NOT NULL)),
    FOREIGN KEY (author_id) REFERENCES users(id),
    FOREIGN KEY (booking_id) REFERENCES bookings(id),
    FOREIGN KEY (place_id) REFERENCES places(id)
);

IF OBJECT_ID(N'[dbo].[gallery_images]', N'U') IS NULL
CREATE TABLE gallery_images (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    place_id BIGINT NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    caption VARCHAR(300) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (place_id) REFERENCES places(id)
);

IF OBJECT_ID(N'[dbo].[reports]', N'U') IS NULL
CREATE TABLE reports (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type VARCHAR(20) NOT NULL CHECK (type IN ('EMERGENCY','COMPLAINT','SUPPORT')),
    subject VARCHAR(150) NOT NULL,
    description VARCHAR(3000) NOT NULL,
    location VARCHAR(200) NOT NULL DEFAULT '',
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN' CHECK (status IN ('OPEN','IN_PROGRESS','RESOLVED')),
    response VARCHAR(2000) NOT NULL DEFAULT '',
    handled_by BIGINT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (handled_by) REFERENCES users(id)
);

IF OBJECT_ID(N'[dbo].[notifications]', N'U') IS NULL
CREATE TABLE notifications (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    text VARCHAR(300) NOT NULL,
    is_read BIT NOT NULL DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);