package ore.forge;

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

public record OreDefinition(String name, String id, double oreValue, int multiOre, float oreTemperature, Model model,
                            btCollisionShape oreShape) {

    static Model tempModel;
    static btCollisionShape tempShape;
    static {
        ModelBuilder modelBuilder = new ModelBuilder();
        tempModel = modelBuilder.createBox(
            1f, 1f, 1f,                             // width, height, depth (1x1x1)
            new Material(ColorAttribute.createDiffuse(Color.GRAY)),
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal
        );

        tempShape = new btBoxShape(new Vector3(0.5f, 0.5f, 0.5f));
    }

    public static OreDefinition fromJson(JsonValue json) {
        String name = json.getString("oreName");
        String id = json.getString("id");
        double oreValue = json.getDouble("oreValue");
        int multiOre = json.getInt("multiOre");
        float oreTemperature = json.getFloat("oreTemperature");
        //TODO replace test model and shape with robust things in future.
        return new OreDefinition(name, id, oreValue, multiOre, oreTemperature, tempModel, tempShape);
    }
}
