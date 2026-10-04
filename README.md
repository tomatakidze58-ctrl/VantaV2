# Vanta Client 26.2 v0.3.0

Original Fabric client source for Minecraft 26.2 / Java 25.

## Controls
- **Right Shift** — Vanta module menu
- **Right Ctrl** — draggable HUD editor
- **C (hold)** — Zoom

## Improved HUD
- AppleSkin-style saturation / food information
- Minimal armor HUD with item models and durability percentages
- Totem icon + inventory counter
- Status-effect bars with countdown timers
- FPS, ping, coordinates, direction, CPS, RAM, clock, server and player count
- Keystrokes
- Smooth eased HUD movement after repositioning

## Visual / Bactro-style clean-room features
- Fullbright via 26.2 `Lightmap`
- Night vision cleanup
- No pumpkin overlay
- Low fire
- Low shield
- First-person item scaling
- Boat map visibility helper
- Riptide + shield placement helper
- Lava, water, powder snow, blindness, darkness, atmospheric and render-distance fog controls

These features are original Vanta implementations based on user-facing behavior. No third-party mod source or assets are bundled.

## Performance
- Reversible FPS Boost preset
- Dynamic FPS when unfocused
- Render-distance cap
- Simulation-distance cap
- Entity-distance reduction
- Minimal particles
- View bobbing / entity-shadow controls

## Build
GitHub Actions is included. The workflow uses Java 25 and Gradle 9.7.1. The normal JAR is the file without `-sources` in the build artifact.

Note: Minecraft 26.2 uses the new extraction-based GUI/HUD renderer and Mojang/unobfuscated names. Renderer mixins are marked optional so one optional visual hook cannot stop the entire client from launching.

## Vanta controls in the menu
Low Fire, Low Shield, Item Scaling and Zoom have +/- controls in the selected-module panel. HUD positions are saved to `config/vantaclient.properties` when you release a dragged widget.

## Clean-room note
The saturation, armor, totem, status-effect and Bactro-style features are Vanta implementations of general user-facing behavior. The uploaded third-party JARs are reference-only and are not bundled, copied, or decompiled into this source.
