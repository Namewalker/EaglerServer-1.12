WebRender plugin (prototype)

What this does (prototype):
- Provides a `/web` command with `open` and `ping` subcommands.
- Contacts a headless renderer service (default http://localhost:3000) to request page renders.

Planned features:
- Convert rendered PNG to in-game maps and item-frame displays.
- Hotspot mapping for clicks on item-frames.
- Caching and admin controls.

Configuration:
- `config.yml` will support `renderer.url` to change the renderer endpoint.

Build:
- Use `mvn -f servers/survival/plugins/WebRender/pom.xml clean package -DskipTests` to build.

Renderer service:
- See `tools/webrender-renderer` for a Node + Puppeteer prototype.

Eagler integration:
- `web/webrender-ui` contains a simple web UI that Eagler clients can open to see full interactive pages.
