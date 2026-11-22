# Benny Tools (Fabric, Minecraft 1.21.11)

Features
- Zoom: hold C. Scroll while holding to change zoom (1.5x-30x). Smooth easing.
- Coordinate HUD (top left): XYZ, facing, biome, Nether/Overworld conversion, FPS, ping. H toggles the whole HUD.
- Waypoints (top right): nearest 4 in the current dimension, with an arrow and distance.
    /wp add <name>   /wp remove <name>   /wp list     (B = quick-save a waypoint here)
- Armor + totems (bottom left): durability %, totem count, action-bar warnings at 10% and when you run out of totems.
- Keystrokes + CPS (bottom right): WASD, space, left|right clicks per second (approximate, sampled per tick).
- Streamer mode (F9): hides every HUD panel, blocks the F3 screen, and redacts chat.
    /redact add <word>    /redact clear     (your own username is replaced with "Streamer" by default)
    Note: redacted chat lines lose colours/hover events; unmatched lines are untouched.

- Theme editor (K): pick a colour slot, drag RGBA sliders, apply a preset (Default, Pink, Mint, Midnight, Cyber, Light),
  toggle a rainbow accent, with a live preview. Saved to config/bennytools_theme.properties.
    /theme preset <name>   /theme hex <slot> <RRGGBB|AARRGGBB>   /theme chroma   /theme reset
    Slots: panel, text, accent, good, warn, bad, key

- Staff Protection (G opens settings): lists every player within a configurable radius (default 50, range 5-200)
  with distance, colour-coded by how close they are. Optional on-screen alert when someone enters the radius.
- Spectator Protection: lists every player currently in spectator mode (read from the tab list), with distance
  when that player is loaded near you. Optional on-screen alert when someone switches to spectator.
  Each panel has its own: Enabled, Screen alert, position (6 anchors + X/Y offset). Panels preview live behind the screen.
    /protect staff on|off   /protect spectator on|off   /protect radius <5-200>
  Limits: you only see players your client has loaded (servers cap entity range), and servers can hide spectators
  or vanished players from the tab list. Hidden in streamer mode like the other panels.

Config: config/bennytools.properties (toggle each panel), config/bennytools_waypoints.txt.

Build
1. Copy the Gradle wrapper from the official Fabric example mod (or run `gradle wrapper --gradle-version 9.2.1`).
2. ./gradlew build   (needs JDK 21)
3. Put build/libs/bennytools-1.0.0.jar in your mods folder with Fabric API.

If the build fails, the likely spots are the Minecraft-API names in:
- BennyTools.java (KeyMapping.Category, Identifier, client command classes)
- GameRendererMixin (getFov parameter list)
- ProtectionTracker.java (PlayerInfo.getGameMode/getProfile, ClientLevel.players)
- InfoHud.java, ThemeScreen.java and ProtectionScreen.java (GuiGraphics drawCenteredString / guiWidth, AbstractSliderButton)
