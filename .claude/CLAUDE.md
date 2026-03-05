# Border Rank Battle (BRB) - Project Documentation

## Project Overview

Border Rank Battle (BRB) is a competitive Minecraft PvP plugin featuring a trigger-based combat system inspired by the anime "World Trigger". Players equip different triggers (special abilities) to customize their playstyle and compete in ranked matches. The system tracks performance across weapons and maintains season-based rankings.

## Technology Stack

- **Server**: Paper 1.21.11 (Spigot fork)
- **Language**: Java 21
- **Build System**: Gradle 8.5 with Kotlin DSL + Shadow plugin
- **Database**: MySQL 8.0 with HikariCP connection pooling (allowPublicKeyRetrieval=true required)
- **Testing**: JUnit 5, Mockito
- **GitHub**: https://github.com/buruburull/border-rank-battle.git

## Infrastructure (GCP)

- **VM**: GCE e2-medium (2 vCPU, 4GB RAM)
- **Server JVM**: -Xmx3G (do NOT reduce to 2G - causes timeout)
- **Project dir**: ~/border-rank-battle
- **Minecraft server dir**: ~/minecraft-server
- **Gradle location**: ~/border-rank-battle/gradle-8.5/bin/gradle (local install, NOT wrapper)
- **Deploy script**: ~/deploy.sh (pull, build, deploy, restart)

## Build & Deploy

Build: ./gradle-8.5/bin/gradle :core-plugin:shadowJar
Deploy JAR: cp core-plugin/build/libs/BorderRankBattle-0.1.0-SNAPSHOT.jar ~/minecraft-server/plugins/BorderRankBattle.jar
Server restart: screen -S mc -X stuff "stop\r" then cd ~/minecraft-server && screen -S mc -dm bash start.sh
Or use: ~/deploy.sh

## Project Structure (Actual)

common/ - Shared module (models, database, utils)
  database/ - DatabaseManager.java, PlayerDAO.java, LoadoutDAO.java
  model/ - BRBPlayer, Team, Loadout, Trigger, TriggerData, TriggerCategory, WeaponRP, WeaponType, RankClass
  util/ - MessageUtil.java

core-plugin/ - Main plugin module
  BRBPlugin.java - Main entry point
  arena/ - ArenaInstance.java, MatchManager.java
  command/ - RankCommand, TeamCommand, TriggerCommand, AdminCommand
  listener/ - CombatListener, TriggerUseListener, PlayerConnectionListener
  manager/ - RankManager, TrionManager, QueueManager, LoadoutManager, TriggerRegistry, MapManager, ScoreboardManager

## Completed Features (Tested & Working)

1. Player System - Registration, persistence, cache
2. Trigger System - 15 triggers, 4 categories, equip/view/remove/list
3. Trion System - 1000 max, HP leak, sustain cost, XP bar, warnings, bailout
4. Combat & Death - Backstab 1.5x, respawn to hub (NO auto-respawn, NO spectator)
5. Match System - Solo queue, auto-match, ArenaInstance lifecycle, time limits
6. RP & Ranking - Elo formula, per-weapon RP, rank tiers S/A/B/C, decorated stats/top display
7. Team System - create/invite/accept/deny/leave/info with pending invites
8. Team Match (code ready, needs 4-player test) - friendly fire off, team win condition
9. Admin Commands - trigger reload, forcestart, rp set, season stub

## Pending / TODO Features

### Priority 1: Season System (Next Task)
DB schema exists (seasons, season_snapshots). AdminCommand has stub. Need:
- RankManager.startSeason(name): Insert season row, set is_active=true
- RankManager.endSeason(): Snapshot RP to season_snapshots, reset all RP to 1000
- /rank stats show current season info

### Priority 2: Trigger Balance & Mechanics
- Implement actual trigger effects (currently only trion cost works)
- Hound homing, Viper curve, Egret/Ibis charge, Meteora explosion, Raygust shield, Bagworm DR, Red Bullet glow

### Priority 3: UI Improvements
- Scoreboard during match, tab list rank colors, chat prefix, boss bar timer, kill feed

### Priority 4: Additional Features
- Match history to DB, loadout save/load, multiple arenas, spectator for non-participants, practice mode

## Known Issues & Gotchas

- NEVER use auto-respawn (spigot().respawn()) - causes frozen state
- NEVER use spectator mode for match participants - conflicts with respawn
- Trion bailout must NOT kill player - teleport to hub and call match.onKill(null, uuid)
- Server needs -Xmx3G (2G causes timeout)
- JDBC URL needs allowPublicKeyRetrieval=true
- WeaponType enum mismatch: DB uses item names, Java uses ATTACKER/SHOOTER/SNIPER
- External IP changes on VM restart

## Coding Conventions

- Package: com.borderrank.battle
- camelCase methods/variables, PascalCase classes, 4 spaces indent, 120 char max
- Japanese for player-facing messages, English for code/logs
- Trion always through TrionManager
- DB: prepared statements only via PlayerDAO/LoadoutDAO
- Always null-check player data lookups
