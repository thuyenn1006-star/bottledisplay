# Bottle Display — Minecraft 26.2 Fabric

Requested design implemented in source:

- Only vanilla Glass Bottle, Potion, Splash Potion, Lingering Potion and Honey Bottle.
- No new inventory item or block.
- V = place (rebindable in Controls).
- R = standing/lying toggle (rebindable).
- Mouse wheel rotates through four 90° directions while holding a supported bottle (normal hotbar scrolling is intercepted only in that case).
- Maximum 4 bottles per block: 1 center, 2 diagonal, 3 triangle, 4 square.
- Display scale is approximately half a block.
- The real vanilla ItemStack is used, so Potion tint/color and bottle variants are preserved.
- Left-clicking a displayed bottle returns the original vanilla item.

## Build target

Minecraft 26.2, Fabric Loader 0.19.3, Fabric API 0.156.0+26.2, Java 25.
Fabric's 26.2 docs use Java 25 and Mojang mappings.

Run:

`./gradlew build`

Output: `build/libs/`

## Note

The provided execution environment does not contain Java 25 or the Gradle/Fabric dependency cache, so I could not truthfully attach a tested compiled jar from this environment. The files in this folder are the source/build project, not a fake jar.
