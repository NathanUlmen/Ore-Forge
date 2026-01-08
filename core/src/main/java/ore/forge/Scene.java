package ore.forge;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;

import java.util.ArrayList;
import java.util.List;

public class Scene {
    public static final List<EntityInstance> entities = new ArrayList<>();
    public static final Environment environment = new Environment();
    public static final ModelBatch modelBatch = new ModelBatch();

    public static void addEntity(EntityInstance e) {
        entities.add(e);
    }

    public static void removeEntity(EntityInstance entityInstance) {
        entities.remove(entityInstance);
    }

    public static void draw(Camera camera) {
        modelBatch.begin(camera);
        for (var instance : entities) {
            modelBatch.render(instance.visualComponent.modelInstance, environment);
        }
        modelBatch.end();
    }
}
