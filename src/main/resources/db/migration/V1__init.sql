CREATE TABLE IF NOT EXISTS players (
    player_id INTEGER PRIMARY KEY AUTOINCREMENT,
    last_account_name TEXT NOT NULL,
    uuid TEXT UNIQUE NOT NULL,
    ip_address TEXT NOT NULL,
    first_join BIGINT NOT NULL,
    last_seen BIGINT NOT NULL,
    total_playtime BIGINT NOT NULL
);

CREATE INDEX idx_players_uuid ON players(uuid);
CREATE INDEX idx_players_playtime ON players(total_playtime);

CREATE TABLE IF NOT EXISTS player_last_locations (
    player_id INTEGER PRIMARY KEY,
    x DOUBLE NOT NULL,
    y DOUBLE NOT NULL,
    z DOUBLE NOT NULL,
    pitch DOUBLE NOT NULL,
    yaw DOUBLE NOT NULL,
    world TEXT NOT NULL,
    FOREIGN KEY (player_id) REFERENCES players(player_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS player_homes (
    player_home_id INTEGER PRIMARY KEY AUTOINCREMENT,
    player_id INTEGER NOT NULL UNIQUE,
    x DOUBLE NOT NULL,
    y DOUBLE NOT NULL,
    z DOUBLE NOT NULL,
    pitch DOUBLE NOT NULL,
    yaw DOUBLE NOT NULL,
    world TEXT NOT NULL,
    FOREIGN KEY (player_id) REFERENCES players(player_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS warps (
    warp_id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT COLLATE NOCASE UNIQUE NOT NULL,
    x DOUBLE NOT NULL,
    y DOUBLE NOT NULL,
    z DOUBLE NOT NULL,
    pitch DOUBLE NOT NULL,
    yaw DOUBLE NOT NULL,
    world TEXT NOT NULL
);

CREATE INDEX idx_warps_name ON warps(name);