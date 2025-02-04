CREATE TABLE IF NOT EXISTS players (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    last_account_name TEXT NOT NULL,
    uuid TEXT UNIQUE NOT NULL,
    ip_address TEXT NOT NULL,
    first_join INTEGER NOT NULL,
    last_seen INTEGER NOT NULL,
    total_playtime INTEGER NOT NULL,
    is_banned BOOLEAN NOT NULL DEFAULT 0,
    ban_reason TEXT
);

CREATE TABLE IF NOT EXISTS player_last_locations (
    player_id INTEGER PRIMARY KEY,
    x REAL NOT NULL,
    y REAL NOT NULL,
    z REAL NOT NULL,
    pitch REAL NOT NULL,
    yaw REAL NOT NULL,
    world TEXT NOT NULL,
    FOREIGN KEY (player_id) REFERENCES players(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS player_homes (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    player_id INTEGER NOT NULL,
    x REAL NOT NULL,
    y REAL NOT NULL,
    z REAL NOT NULL,
    pitch REAL NOT NULL,
    yaw REAL NOT NULL,
    world TEXT NOT NULL,
    FOREIGN KEY (player_id) REFERENCES players(id) ON DELETE CASCADE
);

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
