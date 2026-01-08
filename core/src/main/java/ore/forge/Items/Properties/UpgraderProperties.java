package ore.forge.Items.Properties;

import com.badlogic.gdx.utils.JsonValue;
import ore.forge.UpgradeTag;

public record UpgraderProperties(UpgradeTag upgradeTag) implements ItemProperties {

    public static UpgraderProperties create(JsonValue json) {
        return new UpgraderProperties(new UpgradeTag(json.get("upgradeTag")));
    }

}
