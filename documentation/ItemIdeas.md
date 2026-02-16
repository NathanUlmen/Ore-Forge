Ore forge is an incremental build your own tycoon game. In Ore forge the goal is to upgrade then sell ore produced by your
factory. Droppers produce ore, upgraders operate on ore by improving its stats, (mainly value) and furnaces sell ore, often times with
a slight bonus. You build the tycoon yourself by assembling unique items you gain from gameplay. After reaching enough money
you can prestige to gain one of many powerful new items. There is also quests which reward items and stuff too. The 
game is inspired by the roblox game, Miners Haven.

Ore Fields/Stats
Ore Value - Value of ore. this is the main stat that should be cared about.
Multi Ore/Yield(How many units the ore sells as) - multiplier applied when an ore is sold, effectively makes one ore count as more than one.
Temperature - How hot or cold the ore is. 0 is neutral, greater than 0 is hot and less than zero is cold.
durability/health - really like this one, adds lots more options and features.

Status Effects - Described Below
Tags - flags that upgraders can use to determine their functionality. Can be added and removed. Aren't arbitrary, finite set of these. Adds more depth to droppers and Upgraders
UpgradeTags - a marker denoting the number of times an ore has been upgraded by a specific machine. 
UpgradeCount - times an ore has been "upgraded"
ResetCount - times an ore has had its UpgradeTags reset by a resetter item. 
oreName - display name of ore. 
oreId - id of ore, same as the droppers id. 
Bonuses/Buffs - misc effects that cant be targeted/operated on an individual basis. 

Can also query and modify fields tied to its physics body like velocity, mass, and friction, and transform.

There will be three Core Ecosystems: 
### Fire - Speed, Throughput, Pushing volume, Fast, low durability
Core Status - Heated - Increases speed and increases heat stacks
#### Secondary:
- Fueled - Increases heat applied by sources.
- Ignited - Applies lots of heat stacks over time but at end of effect makes ore Overheated.
- Overheated - Too much heat has penalties/downside.
Ore has its durability reduced by X every X seconds.
    
### Ice - Slow, Quality Over Quantity, Scaling, Consistent, Durable
 __Core Status:__ Chilled - Slows Ore but builds cold stacks
#### __Secondary:__ 
- Frozen - Makes ore more Resilient by increasing durability based on number of cold stacks.
- Glacial - Dramatically slows ore down but provides damage reduction.
- Crystallized - Converts to cold stacks to a yield applies.
    
### Radiation - Risky, Mutation, Duplication
__Core Status:__ Irradiated - The more irradiated an ore is the high chance it has to explode?
I don't like this but can revisit later.
#### __Secondary:__
- Mutable - Ore stats mutated in random ways. Every X seconds a RANDOM? ore stat is mutated.
The magnitude of the mutation is determined/influenced by how irradiated the ore is.
    - TODO: Expand more on what stats can be mutated. 

- Volatile - Spreads radiation to other ore.
The amount of radiation spread is a constant value determined at start/application of effect.
Effect will last either X seconds or until it applies the radiation X times. 

- Degrading - While degrading ore value decreases by X every X seconds, and it gains the X radiation. 

Notes:
Will force/push you to pick two of the three. 
Resetter items dont reset themselves, can ONLY be applied ONCE. At most one or two in the game. 

Other themes:
Nature - Cleansing/Removing Status Effects (Good and bad).
Industrial - Generic simple items, often have a flat multiplier or some other form of utility, useful in all builds but not super potent or exciting.
Exotics - Unique items that are very powerful and have unique effects with scripts written just for them (Think exotics from Destiny).

--------------------

## Descriptors/Genre: Incremental 3D Tycoon Builder

**Overview:**
Players build their tycoon using the following items: Droppers(Produce Ore), Upgraders(Upgrade Ore(move ore too)), Conveyors(Help move ore), Furnaces(Sell Ore).
Players use droppers which produce ore. Ore is upgraded and then sold for cash. Cash is used to purchase items from the shop and to prestige. Prestiging
grants you unique powerful items which help improve your tycoon.

**Technical Features/Goals:** Easy to mod. Performant(Runs on 2020 laptop with integrated graphics at 1080p 60fps)

--------------------
# Gameplay Specifics

## Currencies: Cash, Special Points, Prestige Currency

### Cash:
* Obtained by selling ore.
* Purchase Items from shop.
* Cash is used to purchase items from the shop and is used to determine prestige currency rewarded.

### Special Points:
* Obtained from furnaces after they sell a specific amount of ore. Also obtained when from quests and achievements?
* Used to unlock items for purchase (Cash Items, prestige items?)

### Prestige Currency:
* Prestige Currency will be obtained based on how much money you prestige with.
* On prestige you will be given a random prestige item and prestige Currency.
* Prestige currency will be used to buy prestige items, used as a form of drop protection.

## Why Tycoon Has Depth:

Ore has the following properties:
Value, Temperature, Multiore

Value is the main stat that is cared about, its what gives you cash.
Temperature is a stat that can be modified. It rewards you for using specific types/themed items and makes you think about the order you place items in.
EX: ORE_TEMPERATURE > 10 ? ORE_VALUE * 2 : ORE_VALUE * 1.5

Multiore is used when ore is sold. It multiplies its value an by it.


We will make first prestige easier to reach as that is main road block to getting into game. Prestige within first 10 minutes of game.

## Features To Improve/Make More Fun:
#### Locations
Locations would provide the following:
* Spice up gameplay by adding modifiers and or changing base dimensions to incentivise player to try different strategies.
* Visual variety
* Specific items could only be unlocked in specific zones.
* Could be unlocked locations - locations could have special effects/perks/modifiers. Items only achieved by prestige in that location


#### What if we incorporate the item creator into gameplay/progression?
Allow player to spend currency to build/design their own custom items.
Effects/behaviors have cost currency, more currency you spend more it costs.

#### Take advantage of 3D aspect and allow building in 3D with platforms?
* Adds more depth to game(maybe add later on as to not overwhelm)

Simple yet has optional depth.

## Goals/Reasons To Play:
* Own all items/complete dictionary
* Complete all achievements

--------------------

# Droppers:

## Random Ideas

**Sievite Mine/Dropper** 
    (Inspired By Siva and Outbreak Perfected/Prime from Destiny): 
    EXOTIC

    Effects: 
    - On upgrade Sievite Ore has an 10% chance to replicate, replicated ore have an 10% chance to gain the replicate effect. max replication # of 3.
    - Ore that replicated has its Value increased by 1.07x
    - Drops Ore in bursts of 3. Produces 450 per minute.

**The Prancing Stallion** 
    
    - Prestige Item that is incredibly Rare (0.1?). Will have special/unique purchase and sell values.
    - Procues 950 ore per minute, the same as REDACTED power.
    - Ore is worth an incredible ammount.
    - Have a tie into prestige cause the REDACTED is prestigious. Ore_Value = Base Ore Value * 1 + (PRESTIGE_LEVEL*1.02) 
    - Drops Ore that looks like a horse.
    - Ore has an Aura/Intrinsic effect that causes it to have its ore value increased by 1.02x everytime it's upgraded.
    Exotic

**Utility Dropper**
    INDUSTRIAL

    Effects:
    - Every X Seconds Activates all activateable Items on your base. 
    

**Saryn Upgrader**
    EXOTIC

    Effects:
    - Upgraded Ore has the SPORES?? effect applied to it. 
    - SPORES?? effect activates every 3 seconds.
    - On activation spores increase value of all "infected" by the following each tick: 1+ (0.02 * #OF Infected ORE)
    - Spores last for 12 seconds.



## Generic Droppers
**Simple yet potent/useful droppers**

- **Early Game Prestige Dropper**
    - Produces 1 ore every .75 seconds worth 5 million.


- **Anti Fire Dropper**
    - Produces ore worth 75 Million.
    - Ore is immune to effects that increase its temperature && Burning.
    - Only X ore can be active at one time.

- **Anti Ice Dropper**
    - Produces Ore worth 250 Million
    - Ore is immune to effects that chill it and Frostbite.
    - Only X ore can be active at one time.

- **Scaling Dropper**
    - Drops Ore whose value is based off the number of ore that this specific dropper has produced.
    - Initial Value 1 Million.
    - (ORE_VALUE = BASE_ORE_VALUE * DROP_COUNT)
    - Max Value of X.

- **MultiOre Dropper**
    - Produces ore which have a MultiOre Value of 2

    

# Furnaces:

**Cloning Furnace**
(Inspired by Ancient Magic from Miners Haven)

    **Effects:** 
    - Processed Ore is sold then teleported to another placed furnace where it can be sold again.

# Upgraders:
Terra, Geo, 

## Generic Upgraders
**Simple yet potent Upgraders, Intend for the early game**
- **Earth Upgrader**
    - Effects:
        - 3.5x multiplier


- **Fire Upgrader**
    - Effects:
        - 3x multiplier
        - Adds 25 heat/warmth


- **Jet Stream**
    - Effects:
        - 3.5x multiplier


- **Water Upgrader**
    - Effects:
        - 3.5x multiplier


- **Ice Upgrader**
    - Effects:
        - 3x multiplier
        - Adds 25 chill

## Utility Upgraders
**These Upgraders do something else useful alongside upgrading ore value.**
- **Nature Upgrader**
    - Effects:
        - 2x multiplier
        - Removes all ore status **@REVIEW**
        - If status is removed, 3x multiplier


- **Resetter**
    - Effects:
        - 1.5x multiplier
        - Removes all status effects and upgrade tags from ore allowing it to be upgraded again


- **Sacrifice**
    - Effects:
        - Multiplies ore by the number of upgrade tags removed from it
        - Removes all status effects and upgrade tags


- **Resetter 2** **@REVIEW**
    - Effects:
        - Upgrades ore by 20x
        - Removes all status effects and upgrade tags


## Build Arounds
**These Items incentivize you to build you tycoon in a specific way.**
- **Random Upgrader**
    - Effects:
        - Multiplies ore by 1.75x – 3.25x
        - 2 seconds later upgrades ore by 2.25x-4.5x


- **The Great Equalizer**
    - Effects:
        - Upgrades ore by 12x
        - Destroys ore that are too warm(+100) or cool(-100)
        - Destroys ore that have status effects 

- **Lava Pools**
    - Effects:
        - Upgrades ore based on how hot they are.
          - ((ORE_TEMPERATURE * log(ORE_TEMPERATURE))^1.3 /100 + 1 )
        - min multiplier 1.05x
        - max multiplier  **NA**


- **The Grill**
    - Effects:
        - Upgrades ore by 3x
        - Increases ore temp by 1.25x


- **Fire Storm**
    - Effects:
        - Upgrades ore by 5x
        - Increases ore temp by a sizable amount (+75)
        - Ignites the ore.
        - If the ore is already on fire, upgrades by 8x and adds status buff that increases warm/heat gained by +5


- **Torch**
    - Effects:
        - Upgrades ore by 6x
        - Warms/heats the ore(+35)
        - Ignites it


- **Dragon Blaster**
    - Effects:
        - Upgrades ore by 3.5x
        - 10% chance to explode the ore (destroy)
        - Warms/heats the ore (+15)
        - Can upgrade multiple times


- **Glacier**
    - Effects:
        - Upgrades ore based on how cold they are
        - ((ORE_TEMPERATURE * log(ORE_TEMPERATURE * -1))/ -30)^1.03 + 1
        - Min multiplier 1.5x
        - max Multiplier **NA**


- **Frigid Winds**
    - Effects:
        - Upgrades ore by 3x
        - Chills the ore (-45)


- **Snowflake** **@REVIEW**
    - Effects:
        - Upgrades ore by 2x
        - Chills the ore (-25)
        - Increases the amount of chill stacks ore receives by 2x for X seconds.
        - Reduces the amount of heat stacks ore receives by 0.5x


- **Deep Freezer**
    - Effects:
        - Upgrades ore by 5x
        - Chills the ore a large amount (-75)

    
- **Vent System/Coolant Chamber @REVIEW**
    - Effects:
        - Upgrades ore by 3x
        - Normalizes the temp (If ore Temp is greater than X or less than -X then set temperature to 0.)
        - If the ore was normalized, upgrades ore by 9x, risk destroying it?


- **Contaminator** **@REVIEW**
    - Effects:
        - Upgrades ore by 8x
        - Makes it radioactive


- **Nuclear Leach @REVIEW**
    - Effects:
        - Upgrades ore by 10x if ore is radioactive


- **Elephants Foot @REVIEW**
    - Effects:
        - Upgrades ore by 6x
        - Makes it radioactive


- **Fine Point @REVIEW**
    - Effects:
        - Small upgrade beam
        - Upgrades ore by 6x


- **The Great Crystal**
    - Effects:
        - Upgrades ore based on the number of prestige currencyType you have
        - (PRESTIGE_CURRENCY/20 + 2)


- **Even Is Better**
    - Effects:
        - If the ore is even, upgrades by 10x
        - If ore is odd, upgrades by 5x


- **3rd Time's the Charm**
    - Multiplies ore value by 2x. Every Third upgrade multiplies ore Value by 5x.

    
- **Random Upgrader**
    - 55% chance to upgrade ore value 3.5x.
    - 10% Chance to increase multiore by + 1.
    - 35% Chance to multiply temp by 1.3x.

- **Random Upgrader 2**
    - Every other upgrade has an X% chance to multiply multiore by 2x.
    - Normally multiplies ore value by 3x.


- **Something**
  - Makes ore lighter increasing, which increases the speed at which they are transported by +X permanently.

## Pinnacle Tier
- **Exponential**
    - Effects:
        - Upgrades ore using the following equation `(sqr(x + x / 3 + 10) * 1.33)^2.055`


- **Piggy Back**
    - Effects:
        - Adds a XXX to ore and upgrades it by 1.1x
        - XXX status makes it so whenever ore is upgraded, it's also upgraded by 1.1x.




# Conveyors:
- **Basic Conveyor**
    - 3 Speed.

- **Advanced Conveyor**
    - 4 Speed.

- **Superior Conveyor**
    - 5 Speed.

- **Prestige Conveyor** Obtain 1x of these each time you prestige. First prestige you get 10x
    - 7 Speed.

- **Ultimate Conveyor**
    - 9 Speed.
