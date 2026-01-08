package ore.forge.Items.Experimental;

import com.badlogic.gdx.utils.JsonValue;
import ore.forge.OreDefinition;

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
