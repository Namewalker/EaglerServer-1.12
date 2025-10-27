Halloween plugin

This is a small spooky plugin that randomly triggers "jumpscares" for players by:

- Giving a short blindness + slowness effect
- Playing a ghast-like scream
- Showing a red title message
- Spawning a small firework and a skeleton + lightning effect briefly

Build

Run from the workspace root:

```bash
mvn -f servers/survival/plugins/Halloween/pom.xml clean package
```

Then copy the generated jar from `servers/survival/plugins/Halloween/target/` into `servers/survival/plugins/` (or run `mvn` with a plugin to copy it).

Notes

This was generated automatically. It is intentionally simple — tweak chances and effects in `HalloweenPlugin.java` if you want it more or less intense.
