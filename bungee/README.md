Start instructions for bungee/Velocity

This repository contains a Velocity (bungee) proxy that requires Java 17 to run.

To start the proxy (recommended):

1. Start as your normal user using the bundled script which calls Java 17 explicitly:

   ./start.sh

   - The script will check common ports (25577 and 8081) to avoid accidental duplicate instances.
   - Logs are written to `bungee.log`. A pid is written to `bungee.pid`.

2. To run in the foreground (useful for debugging):

   ./start.sh --foreground

If you prefer to run with sudo for any reason, use the explicit Java 17 binary to avoid mixing runtimes:

   sudo /usr/lib/jvm/java-17-openjdk-amd64/bin/java -jar bungee.jar

Alternatively, make Java 17 the default for your user by adding `/usr/lib/jvm/java-17-openjdk-amd64/bin` to the front of your PATH (the repository already appends this to `~/.bashrc`/`~/.profile`).
