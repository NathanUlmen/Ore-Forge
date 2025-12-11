# Droppers:

## Random Ideas

**Sievite Mine/Dropper** 
    (Inspired By Siva and Outbreak Perfected/Prime from Destiny): 

    Effects: 
    - On upgrade Sievite Ore has an X% chance to replicate, replicated ore have an X% chance to gain the replicate effect.
    - Ore that replicated has its Value increased by 1.07x
    - Drops Ore in bursts of 3. Produces 450 per minute.

**The Prancing Stallion** 
    
    - Prestige Item that is incredibly Rare (0.1?). Will have special/unique purchase and sell values.
    - Procues 950 ore per minute, the same as REDACTED power.
    - Ore is worth an incredible ammount.
    - Have a tie into prestige cause the REDACTED is prestigious. Ore_Value = Base Ore Value * 1 + (PRESTIGE_LEVEL*1.02) 
    - Drops Ore that looks like a horse.
    - Ore has an Aura/Intrinsic effect that causes it to have its ore value increased by 1.02x everytime it's upgraded.

**Utility Dropper**

    Effects:
    - Every X Seconds Activates all activateable Items on your base.
    

**Saryn Upgrader**

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
    - (ORE_VALUE = BASE_ORE_VALUE * DROP_COUNT^1.2)
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
**Simple yet potent Upgraders**
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
        - Removes all status effects and upgrade tags from ore


- **Sacrifice**
    - Effects:
        - Multiplies ore by the number of upgrade tags removed from it
        - Removes all status effects and upgrade tags


- **Resetter 2** **@REVIEW**
    - Effects:
        - Upgrades ore by 20x
        - Removes all status effects and upgrade tags


## Build Arounds
**These Items incentivise you to build you tycoon in a specific way.**
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

> Dog
- **Vent System/Coolant Chamber**
    - Effects:
        - Upgrades ore by 3x
        - Normalizes the temp (If ore Temp is greater than X or less than -X then set temperature to 0.)
        - If the ore was normalized, upgrades ore by 9x


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
        - Upgrades ore based on the number of prestige currency you have
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
 
    
At the beginning of the semester I decided to port my game from 2D to 3D. 
This required the of existing systems(physics, rendering, controls, camera, etc.) and the addition of new
ones (Loading 3D models). The most difficult of these to adapt to/learn was rendering.
In computer graphics if you want to add visual effects to you scene beyond displaying the base model
you will need to use these things called shader programs. Shaders are little programs that 
run on the GPU. There are two main types of shaders, vertex shaders and fragment shaders.
Vertex shaders are responsible from applying transformations and other effects to the vertices of a 3D model.
Fragment shaders are responsible have one job and that's determining the color of a pixel. Through these two
Through these two shaders you get many of effects that are cornerstones of modern day graphics, such as lighting, shadows,
reflections, textures, etc. You can also use them to achieve more novel effects like fire, or EXPAND.
I also applied alot of what I learned in my Linear algebra class.

So in summary I learned more about the 3D rendering pipeline, shaders, 3D transformations.
