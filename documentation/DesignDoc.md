# Ore Forge — Design Document 

## 1) High Concept
**Ore Forge** is an incremental 3D build your own tycoon game. 
Players place **Droppers/Extractors → Upgraders/Refiners → Furnaces/Smelters** (plus movement tools like conveyors)
to produce ore, modify it for higher payouts, then sell it to scale their factory. Progression is driven by collecting
and assembling unique items, completing quests, unlocking locations, and prestiging for powerful new build options.

**Genre/Descriptors:** Incremental 3D Tycoon Builder • Factory • Collectathon (item dictionary)

---

## 2) Design Pillars
1. **Simple core loop, optional depth**  
    players can succeed with straightforward “value up → sell,” while advanced players optimize order, statuses, and risk.
2. **Buildcrafting Matters**  
   Item order, synergy, and theme choices create meaningful and rewarding strategy.
3. **Finite, legible complexity**  
   Statuses/tags are a **finite set** and readable. You should be able to identify status effects by looking at
the ore. Tags should be visible in the inspect ore menu. 


---

## 3) Core Gameplay Loop
1. **Place Droppers** to spawn ore.
2. **Route/move ore** using conveyors
3. **Apply Upgrades** to change ore stats/statuses/tags.
4. **Sell via Furnaces** for cash and special points.
5. **Spend cash** to buy items.
6. **Complete quests/achievements** to unlock items and gain other rewards.
7. **Prestige** spend your cash to reset for long-term power via new items.

Target: **first prestige within ~10 minutes**, no need to drag it out as it's the main gameplay loop.

---

## 4) Player-Facing Systems

### 4.1 Items
- **Droppers:** Produce ore with an identity and baseline stats.
- **Upgraders:** Modify ore (primarily value) and may also move ore.
- **Conveyors:** Control throughput, routing, and timing.
- **Furnaces:** Sell ore, often with a **bonus** or conditional reward.

### 4.2 Item Rarity/Themes
- **Industrial:** Simple, general-purpose multipliers/utility. Always useful, not flashy.
- **Nature:** Cleansing/removal of Status Effects and bonuses (good *and* bad)
- **Exotics:** Scripted, unique, high-impact items (“exotics”-style uniqueness).

---

## 5) Ore Stats

### 5.1 Core Ore Stats (Primary Fields)
- **Ore Value:** Main payout driver.
- **Multi Ore / Yield:** When sold, ore counts as multiple units (effectively multiplies payouts).
- **Temperature:** Cold/neutral/hot axis (0 neutral; >0 hot; <0 cold).
- **Durability / Health:** Enables damage, decay, survival checks, and more interesting tradeoffs.

### 5.2 Metadata / Tracking Fields
- **Status Effects:** Temporary or persistent modifiers (see Ecosystems).
- **Tags:** Finite set of flags used by machines to branch logic (add/remove).
- **UpgradeTags:** Per-machine marker for how many times that specific machine upgraded the ore.
- **UpgradeCount:** Total times an ore has been upgraded.
- **ResetCount:** Number of times the ore’s UpgradeTags have been reset.
- **oreName:** Display name.
- **oreId:** Links ore to its source dropper ID.
- **Bonuses/Buffs:** Broad effects that cannot be targeted on an individual basis. 
EX: Cant check to see if it has a specific buff/bonus. These should be treated as transparent/not seeable by other systems 
except for cleansing type items which should operate on all given an operation. EX: Remove all not just one.

### 5.3 Physics Fields (Optional/Advanced)
Upgraders can query/modify physics: **velocity, mass, friction, transform**, etc. (Supports “physical buildcraft” items and 3D routing.)

---

## 6) Core Ecosystems (Buildcrafting)

Design intent: Players are “pushed” to commit to **two of three** ecosystems for synergy and tradeoffs.

### 6.1 Fire — Speed / Throughput / Volume
**Fantasy:** Fast factory, high output, but fragile.

- **Core Status: Heated**
  - Increases ore speed and builds heat stacks.

- **Secondary Statuses**
  - **Fueled:** Increases heat applied by sources (amplifier).
  - **Ignited:** Applies lots of heat stacks over time; ends by making ore **Overheated**.
  - **Overheated (downside):** Too much heat penalizes durability.  
    Example: ore durability reduced by **X every X seconds** while Overheated.

**Tradeoff:** Great throughput, but you must manage durability loss and overheating. Think great burst DPS.

---

### 6.2 Ice — Slow / Consistent / Durable / Scaling
**Fantasy:** Patient routing, stable gains, quality over quantity.

- **Core Status: Chilled**
  - Slows ore and builds cold stacks.

- **Secondary Statuses**
  - **Frozen:** Increases durability based on cold stacks (resilience scaling).
  - **Glacial:** Dramatically slows ore; provides damage reduction.
  - **Crystallized:** Converts cold stacks into a **yield** increase.

**Tradeoff:** Lower throughput, but stronger survivability and scaling payoffs. Think great sustained DPS.

---

### 6.3 Radiation — Risk / Mutation / Duplication
**Fantasy:** High variance, high ceiling, and occasional “oops.”

- **Core Status: Irradiated**
  - Higher irradiation increases chance of catastrophic event (e.g., explosion; revisit later if needed).

- **Secondary Statuses**
  - **Mutable:** Every X seconds, a random ore stat mutates. Magnitude influenced by irradiation level.  
    Needs a defined list of mutable stats + bounds (value/yield/temp/durability/physics?).
  - **Volatile:** Spreads radiation to other ore (fixed amount per application; lasts X seconds or X applications).
  - **Degrading:** Ore value decreases by X every X seconds, but gains X radiation.

**Tradeoff:** Can spike power or chain-react into losses; requires containment and planning. Think risky DPS.

---

## 7) Resetters (Special Rule)
- **Resetter items** can reset UpgradeTags but:
  - **Do not reset themselves**
  - **Can only be applied once**
  - Only **1–2** exist in the entire game

Creates a “one-time undo/repurpose” strategic lever.

---

## 8) Economy & Progression

### 8.1 Currencies
- **Cash**
  - Earned by selling ore.
  - Used to buy shop items.
  - Certain amount required to prestige, influences how much prestige currency is rewarded on prestige.

- **Special Points**
  - Earned from furnaces after selling a certain amount of ore.
  - Also from quests/achievements (planned).
  - Used to unlock items for purchase (cash items and/or prestige items) and new locations.

- **Prestige Currency**
  - Earned based on cash at prestige.
  - Prestige grants: **(1) random prestige item + (2) prestige currency**
  - Prestige currency buys prestige items and acts as “drop protection” against bad RNG.

### 8.2 Progression Beats
- Early game:
- Mid game: 
- Late game:

---

## 9) Quests / Achievements / Collection
- **Quests/Achievments:** Reward items, currencies, and progression unlocks.
- **Item Dictionary Completion:** Major completionist goal (“own all items/complete the dictionary”).

---

## 10) Locations / Zones
Locations exist to:
- Add **modifiers** (global buffs/debuffs), alter **base dimensions**, and push different strategies.
- Gate **unlockable items** (some items only obtainable/unlocked in specific zones).
- Enable **prestige variants** (prestiging in a location can affect rewards or item pools).

---

## 11) Future/Optional Features: 
#### Player Item Creator
Add a creator as progression:
- Spend currency to design custom items.
- Effects have a cost curve (stronger/more complex effects cost more).
#### Upgradable Items
Turn in multiple copies of an item to get a stronger version.
#### 3D Platforms 
Platforms that allow you to place items on top of them and below them.

---

## 12) Technical Goals
- **Performance:** 1080p/60fps on my 2020 laptop (AMD 4750u with integrated graphics).
- **Modability/Content Creation:** Systems are easy to extend and make additions to. JSON driven items/statuses/tags. 
Clear interfaces for custom behaviors.
---

## 13) Key Balancing Principles (Rules of Thumb)
- **Value is king**, but yield/temperature/statuses create build diversity.
- Status systems should be:
  - **Predictable enough** to plan around
  - **Deep enough** to reward optimization
  - **Finite and readable** (no arbitrary tag explosion) 
- No one build should be far better than others.
  - What makes a build good?
      - How fast(on average) to prestige?
      - How much prestige currency is rewarded on prestige?
  
---

## 14) Open Questions / To-Define (Next Design Pass)
1. **Exact list of Tags** (finite set) 
2. **Mutation rules** for Radiation:
   - Which stats can mutate?
   - Allowed ranges, scaling curve, and failure modes.
3. **Explosion behavior** 
   - What’s the penalty? Ore loss only, damage to nearby ore, machine downtime?
4. **Durability interactions:**
   - How is damage/durability communicated visually?
5. **UI readability:**
   - How players see stacks/statuses/tags quickly while routing ore at speed. **_Slow Time Mode?_**
6. **Prestige reward pool logic:**
   - How “drop protection” mathematically works.
