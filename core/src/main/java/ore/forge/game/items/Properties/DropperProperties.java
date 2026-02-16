package ore.forge.game.items.Properties;

import com.badlogic.gdx.utils.JsonValue;
import ore.forge.game.OreDefinition;

public class DropperProperties implements ItemProperties {
    private OreDefinition oreDefinition;

    public DropperProperties(OreDefinition oreDefinition) {
        this.oreDefinition = oreDefinition;
    }

    public DropperProperties(JsonValue json) {

    }

    public OreDefinition oreDefinition() {
        return oreDefinition;
    }

}
