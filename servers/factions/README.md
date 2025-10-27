Factions server

This server is prepared to run a Factions implementation for MC 1.12.
- World name: `factions_world`
- Port: 30067 (matches `bungee/velocity.toml` forced-host entry)
- Plugins included: WorldEdit, LuckPerms.

What you still need to do:
1. Add a Factions plugin jar (e.g. FactionsUUID or FactionsX compatible with MC 1.12) to this folder (`servers/factions/plugins/`).
2. (Optional but recommended) Add Vault and Essentials (or EssentialsX) if you want economy/teleport/home features that integrate with Factions.
3. Restart the server using `./start.sh`.

I could not automatically download a Factions jar due to distribution/licensing and marketplace access restrictions — please drop the chosen jar into `servers/factions/plugins/` and restart.
