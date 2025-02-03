-- Table for storing player data
CREATE TABLE IF NOT EXISTS players (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    uuid TEXT NOT NULL,
    -- Last location
    last_x REAL NOT NULL,
    last_y REAL NOT NULL,
    last_z REAL NOT NULL,
    last_pitch REAL NOT NULL,
    last_yaw REAL NOT NULL,
    last_world TEXT NOT NULL,
    -- Home location (only one home per player; can be NULL if not set)
    home_x REAL,
    home_y REAL,
    home_z REAL,
    home_pitch REAL,
    home_yaw REAL,
    home_world TEXT,
    ip_address TEXT
);

-- Table for storing warps (server-wide)
CREATE TABLE IF NOT EXISTS warps (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT UNIQUE NOT NULL,
    x REAL NOT NULL,
    y REAL NOT NULL,
    z REAL NOT NULL,
    pitch REAL NOT NULL,
    yaw REAL NOT NULL,
    world TEXT NOT NULL
);