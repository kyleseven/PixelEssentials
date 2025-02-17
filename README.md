# PixelEssentials

PixelEssentials is a Spigot plugin designed to provide some essential features for SMP servers.

## Building

### Compiling from source

```bash
git clone https://github.com/kyleseven/PixelEssentials.git
cd PixelEssentials/
./gradlew clean build
```

Once built, the jar can be found in the `build/libs` directory.

## Command Reference

### PixelEssentials Commands

| Command      | Description                                                      | Permission                                                                                      |
|--------------|------------------------------------------------------------------|-------------------------------------------------------------------------------------------------|
| `/tpa`       | Request to teleport to a player.                                 | `pixelessentials.tpa`                                                                           |
| `/tpahere`   | Request a player to teleport to you.                             | `pixelessentials.tpahere`                                                                       |
| `/tpaall`    | Request all players to teleport to you.                          | `pixelessentials.tpaall`                                                                        |
| `/tpall`     | Teleport all players to you immediately.                         | `pixelessentials.tpall`                                                                         |
| `/tpaccept`  | Accept a teleport request.                                       | `pixelessentials.tpaccept`                                                                      |
| `/tpdeny`    | Deny a teleport request.                                         | `pixelessentials.tpdeny`                                                                        |
| `/tpacancel` | Cancel an outgoing teleport request.                             | `pixelessentials.tpacancel`                                                                     |
| `/back`      | Teleport to your previous location (before teleport or death).   | `pixelessentials.back`<br/>`pixelessentials.back.onteleport`<br/>`pixelessentials.back.ondeath` |
| `/home`      | Teleport to your home location.                                  | `pixelessentials.home`                                                                          |
| `/sethome`   | Set a home location.                                             | `pixelessentials.sethome`                                                                       |
| `/delhome`   | Delete your home location.                                       | `pixelessentials.sethome`                                                                       |
| `/warp`      | Teleport to a warp location.                                     | `pixelessentials.warp`                                                                          |
| `/setwarp`   | Set a warp location.                                             | `pixelessentials.setwarp`                                                                       |
| `/delwarp`   | Delete a warp location.                                          | `pixelessentials.delwarp`                                                                       |
| `/spawn`     | Teleport to the spawn location.                                  | `pixelessentials.spawn`                                                                         |
| `/setspawn`  | Set the spawn location.                                          | `pixelessentials.setspawn`                                                                      |
| `/delspawn`  | Delete the spawn location.                                       | `pixelessentials.delspawn`                                                                      |
| `/motd`      | See the message of the day.                                      | `pixelessentials.motd`                                                                          |
| `/list`      | See a list of all online players.                                | `pixelessentials.list`                                                                          |
| `/ping`      | See the ping of yourself or another player.                      | `pixelessentials.ping`                                                                          |
| `/seen`      | See when a player was last online.                               | `pixelessentials.seen`                                                                          |
| `/whois`     | See various info about a player, including UUID, IP, Ban Status. | `pixelessentials.whois`                                                                         |

### Alias Commands

These commands serve as shortcuts for commonly used default Minecraft commands.
They function identically to their original counterparts and require the same permissions to execute.

| Command | Description                     | Permission                   |
|---------|---------------------------------|------------------------------|
| `/gmc`  | Alias for `/gamemode creative`  | `minecraft.command.gamemode` |
| `/gms`  | Alias for `/gamemode survival`  | `minecraft.command.gamemode` |                              |
| `/gma`  | Alias for `/gamemode adventure` | `minecraft.command.gamemode` |
| `/gmsp` | Alias for `/gamemode spectator` | `minecraft.command.gamemode` |
| `/i`    | Alias for `/give` (for self)    | `minecraft.command.give`     |