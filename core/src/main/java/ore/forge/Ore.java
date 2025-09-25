package ore.forge;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import ore.forge.Expressions.Operands.ValueOfInfluence;
import ore.forge.Items.Blocks.Worker;
import ore.forge.Strategies.OreEffects.BundledOreEffect;
import ore.forge.Strategies.OreEffects.Burning;
import ore.forge.Strategies.OreEffects.ObserverOreEffect;
import ore.forge.Strategies.OreEffects.OreEffect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

/**
 * @author Nathan Ulmen
 */
public class Ore {
    //Ore can be classified by name, id, type. Ores can have multiple types.
    protected final static ItemMap itemMap = ItemMap.getSingleton();
    protected final static OreRealm oreRealm = OreRealm.getSingleton();
    private final static Filter collisionFilter = new Filter();

    static {
        collisionFilter.categoryBits = CollisionRules.ORE.getBit();
        collisionFilter.maskBits = CollisionRules.combineBits(CollisionRules.ORE, CollisionRules.ORE_PROCESSOR);
    }


    private Body body;
    private final HashMap<String, UpgradeTag> tagMap;
    private final Vector2 position, destination;
    private final ArrayList<OreEffect> effects;
    private final Stack<OreEffect> removalStack;
    private final ArrayList<ObserverOreEffect> observerEffects;
    private String oreName, id;
    private double oreValue;
    private int upgradeCount, multiOre;
    private float oreTemperature;
    private float moveSpeed, speedScalar;
    private float direction;
    private boolean isDoomed;
    private float deltaTime;
    private boolean isActive;
    private int resetCount;

    private HashMap<UpgradeTag, UpgradeCooldown> cooldownLookup;

    public Ore() {
        this.oreValue = 0;
        this.oreTemperature = 0;
        this.oreName = "";
        this.id = "";
        this.upgradeCount = 0;
        this.multiOre = 1;
        this.speedScalar = 1;
        this.isDoomed = false;
        tagMap = new HashMap<>();
        position = new Vector2();
        destination = new Vector2();
        direction = Direction.NORTH.getAngle();
        effects = new ArrayList<>();
        removalStack = new Stack<>();
        observerEffects = new ArrayList<>();
        cooldownLookup = new HashMap<>();
        this.resetCount = 0;
    }

    public void act(float deltaTime) {
        this.deltaTime = deltaTime;
        if (!effects.isEmpty()) {
            updateEffects(deltaTime);
        }
        if (position.x != destination.x || position.y != destination.y) {
            move(deltaTime);
        } else {
            activateBlock();
        }
        //End Step effects like invincibility;
        if (this.isDoomed()) {
            //notify listeners that this ore is doomed so that it can be saved.
            oreRealm.takeOre(this);
        }
    }

    private void updateEffects(float deltaTime) {
        for (OreEffect effect : effects) {
            effect.activate(deltaTime, this);
        }
        removeOldEffects();
    }

    private void removeOldEffects() {
        while (!removalStack.empty()) {
            effects.remove(removalStack.pop());
        }
    }

    private void move(float deltaTime) {
        position.x = updatePosition(position.x, destination.x,moveSpeed * deltaTime);
        position.y = updatePosition(position.y, destination.y,moveSpeed * deltaTime);

        if (position.idt(destination)) {
            activateBlock();
        }
        //Ore has arrived at destination.
    }

    private float updatePosition(float currentPosition, float targetDestination, float moveDistance) {
        float delta = targetDestination - currentPosition;

        if (Math.abs(delta) <= moveDistance) {
            return targetDestination;
        }

        return currentPosition + Math.signum(delta) * moveDistance;
    }

    public void activateBlock() {
        if ((itemMap.getBlock(position) instanceof Worker worker)) {
            worker.handle(this);
        } else {
            setIsDoomed(true);
        }
    }

    public void applyEffect(OreEffect strategy) {
        switch (strategy) {
            case null -> {
                return;
            }
            case BundledOreEffect bundledOreEffect -> {
                for (OreEffect effect : bundledOreEffect.getStrategies()) {
                    applyEffect(effect);
                } //Base case
            }
            case ObserverOreEffect observerOreEffect ->
                observerEffects.add((ObserverOreEffect) strategy.cloneOreEffect());
            default -> effects.add(strategy.cloneOreEffect());
        }
    }

    public void removeEffect(OreEffect effectToRemove) {
        assert effects.contains(effectToRemove);
        removalStack.add(effectToRemove);
    }

    private void notifyObserverEffects(ValueOfInfluence mutatedField) {
        for (ObserverOreEffect observer : observerEffects) {
            if (observer.getObservedField() == mutatedField) {
                observer.activate(deltaTime, this);
            }
        }
        removeOldEffects();
    }

    public Ore applyBaseStats(double oreValue, float oreTemp, int multiOre, String oreName, String id, OreEffect strategy) {
        this.oreValue = oreValue;
        this.id = id;
        this.oreTemperature = oreTemp;
        this.multiOre = multiOre;
        this.oreName = oreName;
        applyEffect(strategy);
        return this;
    }

    public Ore setDestination(Vector2 target, float speed, float direction) {
        this.destination.set(target);
        this.direction = direction;
        setMoveSpeed(speed);
        return this;
    }

    public void setDestination(float x, float y, float speed, float direction) {
        this.destination.set(x, y);
        this.direction = direction;
        setMoveSpeed(speed);
    }

    public void deepReset() {
//        if (map.getBlock(position)!= null) {
//            map.getBlock(position).setFull(false);
//        }
        this.oreValue = 0;
        this.oreTemperature = 0;
        this.oreName = "";
        this.upgradeCount = 0;
        this.multiOre = 1;
        this.speedScalar = 1;
        effects.clear();
        removalStack.clear();
        isDoomed = false;
        isActive = false;
        this.resetCount = 0;
        resetAllTags();
    }

    public void resetNonResetterTags() {
        for (UpgradeTag tag : tagMap.values()) {
            if (!tag.isResetter()) {
                tag.reset();
            }
        }

        for (UpgradeCooldown cooldown : cooldownLookup.values()) {
            TimerUpdater.unregister(cooldown);
        }
        cooldownLookup.clear();

        resetCount++;
    }

    public void incrementTag(UpgradeTag tag) {
        tagMap.get(tag.getID()).incrementCurrentUpgrades();
        upgradeCount++;
    }

    public UpgradeTag getUpgradeTag(UpgradeTag tag) {
        String tagName = tag.getID();
        if (tagMap.containsKey(tagName)) {
            return tagMap.get(tagName);
        } else {
            UpgradeTag newTag = new UpgradeTag(tag);
            tagMap.put(tagName, newTag);
            return newTag;
        }
    }

    public boolean containsTag(String tagID) {
        return tagMap.containsKey(tagID);
    }

    public int tagUpgradeCount(String tagID) {
        if (tagMap.containsKey(tagID)) {
            return tagMap.get(tagID).getCurrentUpgrades();
        }
        return 0;
    }

    public void resetAllTags() {
        for (UpgradeTag tag : tagMap.values()) {
            tag.reset();
        }
    }

    public float getMoveSpeed() {
        return moveSpeed;
    }

    public void setMoveSpeed(float newSpeed) {
        moveSpeed = speedScalar * newSpeed;
    }

    public Vector2 getVector() {
        return position;
    }

    public Ore setVector(Vector2 vector) {
        this.position.set(vector);
        return this;
    }

    public boolean isDoomed() {
        return isDoomed;
    }

    public void setIsDoomed(boolean state) {
        isDoomed = state;
//        Gdx.app.log("State changed to: ", String.valueOf(isDoomed));
    }

    public Vector2 getDestination() {
        return destination;
    }

    public float getDirection() {
        return direction;
    }

    public void setSpeedScalar(float newScalar) {
        speedScalar = newScalar;
    }

    public float getSpeedScalar() {
        return speedScalar;
    }

    public void purgeEffects() {
        effects.clear();
    }

    public double getOreValue() {
        return oreValue;
    }

    public void setOreValue(double newValue) {
        oreValue = newValue;
    }

    public float getOreTemp() {
        return oreTemperature;
    }

    public void setTemp(float newTemp) {
        oreTemperature = newTemp;
    }

    public String getName() {
        return this.oreName;
    }

    public void setOreName(String oreName) {
        this.oreName = oreName;
    }

    public String getID() {
        return this.id;
    }

    public float getDeltaTime() {
        return deltaTime;
    }

    public int getUpgradeCount() {
        return upgradeCount;
    }

    public int getMultiOre() {
        return multiOre;
    }

    public void setMultiOre(int newMultiOre) {
        this.multiOre = newMultiOre;
    }

    public void setUpgradeCount(int upgradeCount) {
        this.upgradeCount = upgradeCount;
    }

    public void setIsActive(boolean state) {
        isActive = state;
    }

    public boolean isActive() {
        return isActive;
    }

    public int getResetCount() {
        return resetCount;
    }

    public void setResetCount(int resetCount) {
        this.resetCount = resetCount;
    }

    public boolean isUpgradable(UpgradeTag tag) {
        var oreTag = getUpgradeTag(tag);
        return !oreTag.atUpgradeLimit() && !cooldownLookup.containsKey(tag);
    }

    public void addUpgradeCooldown(UpgradeTag tag, UpgradeCooldown cooldown) {
        cooldownLookup.put(tag, cooldown);
    }

    public void removeUpgradeCooldown(UpgradeTag tag) {
        cooldownLookup.remove(tag);
    }

    public void setBody(Body body) {
        this.body = body;
        body.setUserData(this);
        for (Fixture fixture : body.getFixtureList()) {
            fixture.setFilterData(collisionFilter);
        }
    }

    public Body getBody() {
        return body;
    }

    public boolean isBurning() {
        for (OreEffect effect : effects) {
            if (effect.getClass() == Burning.class) {
                return true;
            }
        }
        return false;
    }

    public String toString() {
        //Name, Value, Temp, Multi-Ore, Upgrade Count, Position, Active Effects.
        StringBuilder s = new StringBuilder();
        s.append("Name: ")
            .append(oreName)
            .append("\tValue: ").append(oreValue)
            .append("\tTemp: ").append(oreTemperature)
            .append("\tMulti-Ore: ").append(multiOre)
            .append("\tPos: ").append(position)
            .append("\tSpeed: ").append(moveSpeed)
            .append("\tisDoomed: ").append(isDoomed)
            .append("\nEffects: ");
        for (OreEffect effect : effects) {
            s.append("\n").append(effect.toString());
        }
        return String.valueOf(s);
    }

}


