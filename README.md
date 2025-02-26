# PixelEssentials

PixelEssentials is a Paper plugin designed to provide some essential features for SMP servers, inspired by EssentialsX.

This plugin is not compatible with Spigot due to the use of `AsyncChatEvent` and the Adventure API.

**This plugin is still in development and may not be stable. Use at your own risk.**

## Building

### Compiling from source

```bash
git clone https://github.com/kyleseven/PixelEssentials.git
cd PixelEssentials/
./gradlew clean build
```

Once built, the jar can be found in the `build/libs` directory.

## Installation

1. Install [Vault](https://www.spigotmc.org/resources/vault.34315/) by placing the jar the `plugins/` directory.
2. Install PixelEssentials by placing the jar in the `plugins/` directory.
3. The `config.yml` and SQLite database will be created when the server is started.

## Command Reference

### PixelEssentials Commands

| Command        | Description                                                           | Permission                                                                                      |
|----------------|-----------------------------------------------------------------------|-------------------------------------------------------------------------------------------------|
| `/afk`         | Toggle your AFK status.                                               | `pixelessentials.afk`                                                                           |
| `/tpa`         | Request to teleport to a player.                                      | `pixelessentials.tpa`                                                                           |
| `/tpahere`     | Request a player to teleport to you.                                  | `pixelessentials.tpahere`                                                                       |
| `/tpaall`      | Request all players to teleport to you.                               | `pixelessentials.tpaall`                                                                        |
| `/tpall`       | Teleport all players to you immediately.                              | `pixelessentials.tpall`                                                                         |
| `/tpaccept`    | Accept a teleport request.                                            | `pixelessentials.tpaccept`                                                                      |
| `/tpdeny`      | Deny a teleport request.                                              | `pixelessentials.tpdeny`                                                                        |
| `/tpacancel`   | Cancel an outgoing teleport request.                                  | `pixelessentials.tpacancel`                                                                     |
| `/back`        | Teleport to your previous location (before teleport or death).        | `pixelessentials.back`<br/>`pixelessentials.back.onteleport`<br/>`pixelessentials.back.ondeath` |
| `/home`        | Teleport to your home location.                                       | `pixelessentials.home`                                                                          |
| `/sethome`     | Set a home location.                                                  | `pixelessentials.sethome`                                                                       |
| `/delhome`     | Delete your home location.                                            | `pixelessentials.sethome`                                                                       |
| `/warp`        | Teleport to a warp location or list warps if no argument is provided. | `pixelessentials.warp`<br/>`pixelessentials.warp.list`                                          |
| `/setwarp`     | Set a warp location.                                                  | `pixelessentials.setwarp`                                                                       |
| `/delwarp`     | Delete a warp location.                                               | `pixelessentials.delwarp`                                                                       |
| `/spawn`       | Teleport to the spawn location.                                       | `pixelessentials.spawn`                                                                         |
| `/setspawn`    | Set the spawn location.                                               | `pixelessentials.setspawn`                                                                      |
| `/delspawn`    | Delete the spawn location.                                            | `pixelessentials.delspawn`                                                                      |
| `/msg`         | Send a private message to a player.                                   | `pixelessentials.msg`                                                                           |
| `/reply`       | Send a private message to the last person who messaged you.           | `pixelessentials.msg`                                                                           |
| `/motd`        | See the message of the day.                                           | `pixelessentials.motd`                                                                          |
| `/list`        | See a list of all online players.                                     | `pixelessentials.list`                                                                          |
| `/ping`        | See the ping of yourself or another player.                           | `pixelessentials.ping`                                                                          |
| `/seen`        | See when a player was last online.                                    | `pixelessentials.seen`                                                                          |
| `/whois`       | See various info about a player, including UUID, IP, Ban Status.      | `pixelessentials.whois`                                                                         |
| `/invsee`      | Open another player's inventory.                                      | `pixelessentials.invsee`                                                                        |
| `/enderchest`  | Open another player's ender chest.                                    | `pixelessentials.enderchest`                                                                    |
| `/playtimetop` | Show a leaderboard of players ordered by play time.                   | `pixelessentials.playtimetop`                                                                   |

### Alias Commands

These commands serve as shortcuts for commonly used default Minecraft commands.
They function identically to their original counterparts and require the same permissions to execute.

| Command   | Description                     | Permission                   |
|-----------|---------------------------------|------------------------------|
| `/gmc`    | Alias for `/gamemode creative`  | `minecraft.command.gamemode` |
| `/gms`    | Alias for `/gamemode survival`  | `minecraft.command.gamemode` |
| `/gma`    | Alias for `/gamemode adventure` | `minecraft.command.gamemode` |
| `/gmsp`   | Alias for `/gamemode spectator` | `minecraft.command.gamemode` |
| `/i`      | Alias for `/give` (for self)    | `minecraft.command.give`     |
| `/tphere` | Alias for `/tp {target} {self}` | `minecraft.command.teleport` |
