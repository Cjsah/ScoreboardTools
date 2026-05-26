# ScoreboardTools

> Due to Fabric Api's data synchronization behavior, this mod does not register any new Scoreboard Criterion.
> 
> Therefore, all newly created objectives are `dummy`, and their scores are updated through an internal binding mechanism.

## Added Scoreboard Criterion
- `minedCount`
- `placedCount`
- `onlineTime`
- `level`
- `elytraFlyingDistance`
- `finishFishing`

## Commands
- `/scoreboard objective add <name> <criteria> [type]` Create a new scoreboard objective with criteria
- `/scoreboard loop [slot] [add/remove] [scoreboard objective]` Add or Remove a scoreboard objective form slot's scorlling list
- `/scoreboard loop [slot] [enable/disable]` Switch scoreboard scroll on slot
- `/scoreboard loop [slot] schedule [ticks]` Change scoreboard scoll intervals(ticks) on slot
- `/scoreboard fakePlayerScore [true/false]` Global set whether fake player counts on the scoreboard
