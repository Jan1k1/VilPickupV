# VilPickupV

Pick up villagers with shift+right-click and place them again from an item.

## Features

- Shift+right-click pickup for villagers and zombie villagers
- Configurable item name, lore, material, sounds, and glow
- Villager head items with profession and age-specific textures when `PLAYER_HEAD` is used
- EntitySnapshot-first restore path with an NMS saver fallback in this codebase
- Optional MySQL storage through HikariCP
- Zombie villager equipment data stored in the pickup item and database

## Supported Entities

- Regular Villagers (all professions)
- Baby Villagers
- Zombie Villagers
- Baby Zombie Villagers
- Wandering Traders

## Commands & Permissions

**Commands:**
- /vilpickup reload - Reload configuration (Admin only)

**Permissions:**
- vilpickup.use (default: true) - Pick up villagers
- vilpickup.admin (default: op) - Admin commands

## Compatibility

- Built against Paper `26.1.2.build.69-stable`
- Java `25`
- Uses MySQL Connector/J and HikariCP inside the plugin jar
- Older or non-Paper servers may work only if the included saver path still matches their internals

## Links

- Site: [jan1k.org](https://jan1k.org)
- GitHub: [Jan1k1/VilPickupV](https://github.com/Jan1k1/VilPickupV)
- Issues: [github.com/Jan1k1/VilPickupV/issues](https://github.com/Jan1k1/VilPickupV/issues)

---

Made by Jan1k
