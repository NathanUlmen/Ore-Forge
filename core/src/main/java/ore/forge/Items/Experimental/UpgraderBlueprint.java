package ore.forge.Items.Experimental;

import com.badlogic.gdx.utils.JsonValue;
import ore.forge.UpgradeTag;


public class UpgraderBlueprint extends ItemBlueprint {
    private final UpgradeTag upgradeTag;

    public UpgraderBlueprint(JsonValue jsonValue) {
        super(jsonValue);
        upgradeTag = new UpgradeTag(jsonValue.get("upgradeTag"));
    }

    public UpgradeTag getUpgradeTag() {
        return upgradeTag;
    }
}
