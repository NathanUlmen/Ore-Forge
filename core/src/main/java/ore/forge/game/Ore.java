package ore.forge.game;

import ore.forge.game.behaviors.OreEffects.BundledOreEffect;
import ore.forge.game.behaviors.OreEffects.Burning;
import ore.forge.game.behaviors.OreEffects.ObserverOreEffect;
import ore.forge.game.behaviors.OreEffects.OreEffect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

/**
 * @author Nathan Ulmen
 */
public class Ore {
    private int durability;
    private final HashMap<String, UpgradeTag> tagMap;
    private final ArrayList<OreEffect> effects;
    private final Stack<OreEffect> removalStack;
    private final ArrayList<ObserverOreEffect> observerEffects;
    private String oreName, id;
    private double oreValue; //value of the ore
    private int upgradeCount;
    private int multiOre;
    private float oreTemperature;
    private boolean isDoomed; //Flag denoting ore will be destroyed next update cycle. Invincibility effects can use this.
    private int resetCount; //Times ore has had its UpgradeTags reset by a resetter type item
    private HashMap<UpgradeTag, UpgradeCooldown> cooldownLookup;

    public Ore() {
        this.oreValue = 0;
        this.oreTemperature = 0;
        this.oreName = "";
        this.id = "";
        this.upgradeCount = 0;
        this.multiOre = 1;
        this.isDoomed = false;
        tagMap = new HashMap<>();
        effects = new ArrayList<>();
        removalStack = new Stack<>();
        observerEffects = new ArrayList<>();
        cooldownLookup = new HashMap<>();
        this.resetCount = 0;
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

    public Ore applyBaseStats(double oreValue, float oreTemp, int multiOre, String oreName, String id, OreEffect strategy) {
        this.oreValue = oreValue;
        this.id = id;
        this.oreTemperature = oreTemp;
        this.multiOre = multiOre;
        this.oreName = oreName;
        applyEffect(strategy);
        return this;
    }

    public void deepReset() {
        this.oreValue = 0;
        this.oreTemperature = 0;
        this.oreName = "";
        this.upgradeCount = 0;
        this.multiOre = 1;
        effects.clear();
        removalStack.clear();
        isDoomed = false;
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

    public boolean isDoomed() {
        return isDoomed;
    }

    public void setIsDoomed(boolean state) {
        isDoomed = state;
//        Gdx.app.log("State changed to: ", String.valueOf(isDoomed));
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

    public void setTemperature(float newTemp) {
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
            .append("\tisDoomed: ").append(isDoomed)
            .append("\nEffects: ");
        for (OreEffect effect : effects) {
            s.append("\n").append(effect.toString());
        }
        return String.valueOf(s);
    }

}


