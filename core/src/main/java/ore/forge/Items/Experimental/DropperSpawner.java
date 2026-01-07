package ore.forge.Items.Experimental;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btBoxShape;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.utils.JsonValue;

public class DropperSpawner extends ItemSpawner {
    public OreBlueprint oreBlueprint;
    public btCollisionShape oreShape;
    public Model oreModel;

    public DropperSpawner(JsonValue jsonValue) {
        super(jsonValue);

        oreBlueprint = OreBlueprint.load(jsonValue.get("oreProperties"));
        ModelBuilder modelBuilder = new ModelBuilder();

        oreModel = modelBuilder.createBox(
            1f, 1f, 1f,                             // width, height, depth (1x1x1)
            new Material(ColorAttribute.createDiffuse(Color.GRAY)),
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal
        );

        oreShape = new btBoxShape(new Vector3(0.5f, 0.5f, 0.5f));
    }


    private record OreBlueprint(String oreName, double oreValue, int oreTemp, int multiOre) {

        public static OreBlueprint load(JsonValue jsonValue) {
            String oreName = jsonValue.get("oreName").asString();
            double oreValue = jsonValue.get("oreValue").asDouble();
            int oreTemp = jsonValue.get("oreTemperature").asInt();
            int multiOre = jsonValue.get("multiOre").asInt();
            return new OreBlueprint(oreName, oreValue, oreTemp, multiOre);
        }

    }

    public OreBlueprint getOreBlueprint() {
        return oreBlueprint;
    }
}
