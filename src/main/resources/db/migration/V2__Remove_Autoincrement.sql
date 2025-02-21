CREATE TABLE players_temp (
    player_id INTEGER PRIMARY KEY,
    last_account_name TEXT NOT NULL,
    uuid TEXT UNIQUE NOT NULL,
    ip_address TEXT NOT NULL,
    first_join BIGINT NOT NULL,
    last_seen BIGINT NOT NULL,
    total_playtime BIGINT NOT NULL
);

CREATE TABLE player_homes_temp (
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

CREATE TABLE warps_temp (
    warp_id INTEGER PRIMARY KEY,
    name TEXT COLLATE NOCASE UNIQUE NOT NULL,
    x DOUBLE NOT NULL,
    y DOUBLE NOT NULL,
    z DOUBLE NOT NULL,
    pitch DOUBLE NOT NULL,
    yaw DOUBLE NOT NULL,
    world TEXT NOT NULL
);

-- Copy data from original tables to temp tables
INSERT INTO players_temp SELECT * FROM players;
INSERT INTO player_homes_temp SELECT * FROM player_homes;
INSERT INTO warps_temp SELECT * FROM warps;

-- Drop original indexes
DROP INDEX IF EXISTS idx_players_uuid;
DROP INDEX IF EXISTS idx_players_playtime;
DROP INDEX IF EXISTS idx_warps_name;

-- Drop original tables
DROP TABLE players;
DROP TABLE player_homes;
DROP TABLE warps;

-- Rename temp tables to original names
ALTER TABLE players_temp RENAME TO players;
ALTER TABLE player_homes_temp RENAME TO player_homes;
ALTER TABLE warps_temp RENAME TO warps;

-- Recreate indexes
CREATE INDEX idx_players_uuid ON players(uuid);
CREATE INDEX idx_players_playtime ON players(total_playtime);
CREATE INDEX idx_warps_name ON warps(name);