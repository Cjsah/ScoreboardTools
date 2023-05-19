# ScoreboardTools

Added 2 Scoreboard Criterion

- `minedCount`
- `placedCount`
- `killedCount`

Use `/scoreboard objectives setdisplay loop <slot> <add/remove/schedule> <objective/int>` to loop your scoreboard
Use `/scoreboard objectives preset <update/run> [name]` to run or update preset config

### config template
```json
{
    "presets":[
        {
            "name": "dmp",
            "display": "sidebar",
            "schedule": 200,
            "scoreboards": [
                {
                    "name": "death",
                    "criteria": "deathCount",
                    "text": "[{\"text\":\"[\",\"color\":\"gold\"},{\"text\":\"死亡榜\",\"color\":\"aqua\"},{\"text\":\"]\",\"color\":\"gold\"}]"
                },
                {
                    "name": "mined",
                    "criteria": "minedCount",
                    "text": "[{\"text\":\"[\",\"color\":\"gold\"},{\"text\":\"挖掘榜\",\"color\":\"aqua\"},{\"text\":\"]\",\"color\":\"gold\"}]"
                },
                {
                    "name": "place",
                    "criteria": "placedCount",
                    "text": "[{\"text\":\"[\",\"color\":\"gold\"},{\"text\":\"放置榜\",\"color\":\"aqua\"},{\"text\":\"]\",\"color\":\"gold\"}]"
                },
                {
                    "name": "killed",
                    "criteria": "killedCount",
                    "text": "[{\"text\":\"[\",\"color\":\"gold\"},{\"text\":\"杀生榜\",\"color\":\"aqua\"},{\"text\":\"]\",\"color\":\"gold\"}]"
                }
            ]
        }
    ]
}
```