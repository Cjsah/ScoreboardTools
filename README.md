# ScoreboardTools

> Due to Fabric Api's data synchronization behavior, this mod does not register some new Scoreboard Criterion. So you need to create a scoreboard of criteria `dummy` and then bind the score type with the `/scbt`command

## Added Scoreboard Criterion
- `minedCount`
- `placedCount`
- `onlineTime`
- `level`
- `elytraFlyingDistance`

## Commands
- `/scbt [bind/unbind] [type] [scoreboard objective]` Bind or Unbind score criterion to a scoreboard objective
- `/scbt loop [slot] [add/remove] [scoreboard objective]` Add or Remove a scoreboard objective form slot's scorlling list
- `/scbt loop [slot] [enable/disable]` Switch scoreboard scroll on slot
- `/scbt loop [slot] schedule [ticks]` Change scoreboard scoll intervals(ticks) on slot
- `/scbt fakePlayerScore [true/false]` Global set whether fake player counts on the scoreboard
