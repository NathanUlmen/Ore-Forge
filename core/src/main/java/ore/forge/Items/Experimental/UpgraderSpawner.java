package ore.forge.Items.Experimental;

import com.badlogic.gdx.utils.JsonValue;
import ore.forge.UpgradeTag;

public class UpgraderSpawner extends ItemSpawner {
    protected final UpgradeTag upgradeTag;

    public UpgraderSpawner(JsonValue jsonValue) {
        super(jsonValue);
        upgradeTag = new UpgradeTag(jsonValue.get("upgradeTag"));

    }

    public UpgradeTag getUpgradeTag() {
        return upgradeTag;
    }

}
