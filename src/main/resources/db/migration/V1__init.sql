CREATE TABLE players (
    player_id INTEGER PRIMARY KEY,
    last_account_name TEXT NOT NULL,
    uuid TEXT UNIQUE NOT NULL,
    ip_address TEXT NOT NULL,
    first_join BIGINT NOT NULL,
    last_seen BIGINT NOT NULL,
    total_playtime BIGINT NOT NULL
);

CREATE INDEX idx_players_account_name ON players(last_account_name);
CREATE INDEX idx_players_uuid ON players(uuid);
CREATE INDEX idx_players_playtime ON players(total_playtime);

CREATE TABLE player_last_locations (
    player_id INTEGER PRIMARY KEY,
    x DOUBLE NOT NULL,
    y DOUBLE NOT NULL,
    z DOUBLE NOT NULL,
    pitch DOUBLE NOT NULL,
    yaw DOUBLE NOT NULL,
    world TEXT NOT NULL,
    FOREIGN KEY (player_id) REFERENCES players(player_id) ON DELETE CASCADE
);

CREATE TABLE player_homes (
    player_home_id INTEGER PRIMARY KEY,
    player_id INTEGER NOT NULL UNIQUE,
    x DOUBLE NOT NULL,
    y DOUBLE NOT NULL,
    z DOUBLE NOT NULL,
    pitch DOUBLE NOT NULL,
    yaw DOUBLE NOT NULL,
    world TEXT NOT NULL,
    FOREIGN KEY (player_id) REFERENCES players(player_id) ON DELETE CASCADE
);

CREATE TABLE warps (
    warp_id INTEGER PRIMARY KEY,
    player_id INTEGER NOT NULL,
    name TEXT COLLATE NOCASE UNIQUE NOT NULL,
    x DOUBLE NOT NULL,
    y DOUBLE NOT NULL,
    z DOUBLE NOT NULL,
    pitch DOUBLE NOT NULL,
    yaw DOUBLE NOT NULL,
    world TEXT NOT NULL,
    FOREIGN KEY (player_id) REFERENCES players(player_id) ON DELETE CASCADE
);

CREATE INDEX idx_warps_name ON warps(name);
CREATE INDEX idx_warps_player_id ON warps(player_id);

CREATE TABLE spawn (
    spawn_id INTEGER PRIMARY KEY,
    x DOUBLE NOT NULL,
    y DOUBLE NOT NULL,
    z DOUBLE NOT NULL,
    pitch DOUBLE NOT NULL,
    yaw DOUBLE NOT NULL,
    world TEXT NOT NULL
);