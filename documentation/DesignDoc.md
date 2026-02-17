# Ore Forge — Design Document 

## Concept
**Ore Forge** is an incremental 3D build your own tycoon game. 
Players place **Droppers/Extractors, Upgraders/Refiners, Furnaces/Smelters** (also movement items like conveyors and teleporters)
to produce ore, modify it for higher payouts, then sell it to purchase/obtain new items to further improve their factory.
Progression is driven by collecting unique items, completing quests, unlocking locations, and prestiging for powerful new items that 
open up build options.

Isometric.

**Genre/Descriptors:** Incremental 3D Tycoon Builder / Factory / Collectathon (item dictionary)

---

## Design Pillars
1. **Simple, optional depth**  
    Players can succeed for a time with straightforward upgrade ore value and sell. 
    Will be eased into buildcrafting mechanics which encourage player to take into account order, 
    statuses, and tradeoffs.
2. **Buildcrafting**  
   Item order, synergy, and theme choices create meaningful and rewarding strategy.
3. **Visible**  
   Statuses/tags are a **finite set** and readable. You should be able to identify status effects by looking at
the ore. Tags should be visible in the inspect ore menu. 

---

## Gameplay 
1. **Place Droppers/Extractors** which spawn/produce ore.
2. **Move ore** using conveyors
3. **Apply Upgrades** to change ore stats/statuses/tags.
4. **Sell via Furnaces/Smelters** for cash and special points.
5. **Spend cash** to buy items.
6. **Complete quests/achievements** to unlock items and gain other rewards.
7. **Prestige** spend your cash to reset for long-term power via new items.

Target: **first prestige within ~10 minutes**, no need to drag it out as it's the main gameplay loop.

---

## Systems

### Items

#### Item Categories:
- **Droppers:** Produce ore with an identity and baseline stats.
- **Upgraders:** Modify ore (primarily value) and may also move ore.
- **Conveyors:** Move ore throughout the tycoon, mainly through corners.
- **Furnaces:** Sell ore, often with a bonus based on a condition.

#### Item Rarity/Themes
- **Industrial:** Simple, general purpose multipliers/utility. Always useful, not flashy.
- **Nature:** Cleansing/removal of Status Effects and bonuses (both good and bad)
- **Exotics:** Scripted, unique, high-impact items (think exotics from Destiny).

#### Resetters
- **Resetter items** can reset UpgradeTags, but there are some stipulations:
    - **Do not reset themselves**
    - **Can only be applied once**
    - Only **1–2** exist in the entire game 

### Ore 

#### Core Ore Stats (Primary Fields)
- **Ore Value:** The amount of cash rewarded when the ore is sold.
- **Multi Ore / Yield:** When sold, ore counts as multiple units.
- **Temperature:** Cold/neutral/hot axis (0 neutral, > 0 hot, < 0 cold).
- **Durability / Health:** Enables damage/decay and more interesting tradeoffs.

#### Metadata / Tracking Fields
- **Status Effects:** Temporary or persistent modifiers (see Ecosystems).
- **Tags:** Finite set of flags used by machines to branch logic.
- **UpgradeTags:** Per-machine marker for how many times that specific machine upgraded the ore.
- **UpgradeCount:** Total times an ore has been upgraded.
- **ResetCount:** Number of times the ore’s UpgradeTags have been reset.
- **oreName:** Display name.
- **oreId:** Links ore to its source dropper ID.
- **Bonuses/Buffs:** Broad effects that cannot be targeted on an individual basis. 
EX: Cant check to see if it has a specific buff/bonus. These should be treated as transparent/not seeable by other systems 
except for cleansing type items which should operate on all given an operation. EX: Remove all not just one.

#### Physics Body Fields 
Upgraders can query/modify properties of the physics body. 
EX: **velocity, mass, friction, transform**, etc. 

## "Ecosystems" 

Goal: Players are “pushed” to pick **two of three** ecosystems 

### Fire — Speed / Throughput / Volume
**Characteristics:** Fast factory, high output, but fragile. - SIMILAR TO BURST DAMAGE

- **Core Status: Heated**
  - Increases ore speed and builds heat stacks.

- **Secondary Statuses**
  - **Fueled:** Increases heat applied by sources.
  - **Ignited:** Applies lots of heat stacks over time, ends by making ore **Overheated**.
  - **Overheated:** Too much heat penalizes durability.  
    Ex: ore durability reduced by **X every X seconds** while Overheated.

---

### Ice — Slow / Consistent / Durable / Scaling
**Characteristics:** Patient routing, stable gains, quality over quantity. SIMILAR TO SUSTAINED/SCALING DAMAGE

- **Core Status: Chilled**
  - Slows ore and builds cold stacks.

- **Secondary Statuses**
  - **Frozen:** Increases durability based on cold stacks (resilience scaling).
  - **Glacial:** Dramatically slows ore; provides damage reduction.
  - **Crystallized:** Converts cold stacks into a **yield** increase.

---

### Radiation — Risk / Mutation / Duplication
**Characteristics:** High variance, high ceiling, and occasional “oops.” --- RISK MANAGEMENT

- **Core Status: Irradiated**
  - Higher irradiation increases chance of catastrophic event (e.g., explosion; revisit later if needed).

- **Secondary Statuses**
  - **Mutable:** Every X seconds, a random ore stat mutates. Magnitude influenced by irradiation level.  
    Needs a defined list of mutable stats + bounds (value/yield/temp/durability/physics?).
  - **Volatile:** Spreads radiation to other ore (fixed amount per application; lasts X seconds or X applications).
  - **Degrading:** Ore value decreases by X every X seconds, but gains X radiation.

---

## Economy & Progression

### Currencies
- **Cash**
  - Earned by selling ore at furnaces/smelters.
  - Used to buy shop items.
  - Certain amount required to prestige, influences how much prestige currency is rewarded on prestige.

- **Special Points**
  - Earned from furnaces after selling a certain amount of ore. The amount of special points rewarded
  and the ore sold threshold varies/depends on the furnace
  - Rewards from quests/achievements.
  - Used to unlock items for purchase and new locations.

- **Prestige Currency**
  - Earned based on cash at prestige.
  - Prestigng grants: **random prestige item and prestige currency**. Prestige currency is rewarded
  based on the amount of surplus money you have pass the base prestige cost threshold.
  - Prestige currency buys prestige items and acts as drop rate protection.

---

## Quests / Achievements / Collection
- **Quests/Achievments:** Reward items, currencies, and progression unlocks.
- **Item Dictionary Completion:** Completionist goal.

---

## Locations 
Locations exist to:
- Add **modifiers** (global buffs/debuffs), alter **base dimensions and layout** to push/encourage different strategies.
- Add **unlockable items** (some items only obtainable/unlocked in specific zones).

---

##  Balancing Principles 
- No one build should be far better than others.
  - What makes a build good?
      - How fast(on average) to prestige?
      - How much prestige currency is rewarded on prestige?
  
--- 

## Future/Optional Features:
#### Player Item Creator
Add an item creator as progression:
- Spend currency to design custom items.
- Effects have a cost curve (stronger/more complex effects cost more).
#### Upgradable Items
Turn in multiple copies of an item to get a stronger version.
#### 3D Platforms
Platforms that allow you to place items on top of them and below them.

---
