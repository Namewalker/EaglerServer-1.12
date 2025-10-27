Creative server

This server is configured as a pure creative server:
- All players will be placed into Creative mode (server.properties sets gamemode=1 and force-gamemode=true).
- World name: `creative_world`
- Port: 25568
- Plugins: WorldEdit (copied from `servers/survival`), LuckPerms (copied)

Notes:
- LoginSecurity is NOT installed on this server.
- Minecraft 1.12 has a hard-coded world height limit (256). "No build limits" beyond that is not possible without running a more modern server/protocol.

Start/stop:
- Use `./start.sh` and `./stop.sh` inside the `servers/creative` folder.
