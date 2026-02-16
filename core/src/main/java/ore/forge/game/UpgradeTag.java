package ore.forge.game;

import com.badlogic.gdx.utils.JsonValue;

/**@author Nathan Ulmen*/
public class UpgradeTag {
    private final String name, id;
    private final int maxUpgrades;
    private int currentUpgrades;
    private final boolean isResetter;

    public UpgradeTag(String name, String id, int maxUpgrades, boolean isResetter) {
        this.name = name;
        this.id = id;
        this.maxUpgrades = maxUpgrades;
        currentUpgrades = 0;
        this.isResetter = isResetter;
    }

    public UpgradeTag(UpgradeTag tagToClone) {
        this.name = tagToClone.name;
        this.id = tagToClone.id;
        this.maxUpgrades = tagToClone.getMaxUpgrades();
        currentUpgrades = 0;
        this.isResetter = tagToClone.isResetter;
    }

    public UpgradeTag(JsonValue jsonValue) {
        this.name = jsonValue.getString("name");
        this.id = jsonValue.getString("id");
        this.maxUpgrades = jsonValue.getInt("maxUpgrades");
        this.isResetter = jsonValue.getBoolean("isResetter");
        currentUpgrades = 0;
    }

    public boolean isResetter() {
        return isResetter;
    }

    public boolean atUpgradeLimit() {
        return currentUpgrades >= maxUpgrades;
    }

    public String getID() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void reset() {
        this.currentUpgrades = 0;
    }

    public int getMaxUpgrades() {
        return maxUpgrades;
    }

    public int getCurrentUpgrades() {
        return currentUpgrades;
    }

    public void incrementCurrentUpgrades() {
        currentUpgrades++;
    }

    public String toString() {
        return "Name: " + name + "  Max Upgrades: " + maxUpgrades +
                "  CurrentUpgrades: " + currentUpgrades;
    }

    @Override
    public boolean equals(Object tag) {
        return tag instanceof UpgradeTag upgTag && this.id.equals(upgTag.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

}
