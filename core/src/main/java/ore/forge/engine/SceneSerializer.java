package ore.forge.engine;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import ore.forge.engine.definitions.DefinitionTree;
import ore.forge.engine.serialization.ComponentLoader;

import java.io.FileReader;
import java.util.List;

public class SceneSerializer {

    public Component[] loadEntity(String filePath) {
        ComponentLoader loader = new ComponentLoader();
        JsonValue entity = new JsonReader().parse(Gdx.files.internal(filePath));
        DefinitionTree defTree = loader.load(entity).getFirst();

        return defTree.getComponents();
    }
}
