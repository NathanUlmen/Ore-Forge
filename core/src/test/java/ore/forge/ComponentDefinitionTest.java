package ore.forge;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import ore.forge.engine.components.*;
import ore.forge.engine.components.definitions.*;
import ore.forge.game.Tickable;
import ore.forge.game.UpdatableScriptC;
import ore.forge.game.UpdatableScriptDefinition;
import ore.forge.game.collisions.CollisionHandlerC;
import ore.forge.game.collisions.CollisionHandlerDefinition;
import ore.forge.game.temp.ItemComponent;
import ore.forge.game.temp.ItemComponentDefinition;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

public class ComponentDefinitionTest {
    @Test
    void transformDefinitionCopiesLocalPoseIntoCurrentAndPreviousState() {
        TransformC component = new TransformDefinition(
            new Vector3(1f, 2f, 3f),
            new Quaternion(1f, 2f, 3f, 4f),
            new Vector3(4f, 5f, 6f)
        ).create();

        assertEquals(new Vector3(1f, 2f, 3f), component.localPosition);
        assertEquals(new Quaternion(1f, 2f, 3f, 4f), component.localRotation);
        assertEquals(new Vector3(4f, 5f, 6f), component.localScale);
        assertEquals(component.localPosition, component.prevLocalPosition);
        assertEquals(component.localRotation, component.prevLocalRotation);
        assertEquals(component.localScale, component.prevLocalScale);
    }

    @Test
    void worldTransformDefinitionCopiesMatrixIntoCurrentAndPreviousState() {
        Matrix4 expected = new Matrix4(new float[]{1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 3, 4, 5, 1});

        WorldTransformC component = new WorldTransformDefinition(expected).create();

        assertEquals(expected, component.currentTransform);
        assertEquals(expected, component.previousTransform);
    }

    @Test
    void teleportRequestDefinitionCopiesTargetMatrix() {
        Matrix4 expected = new Matrix4().setToTranslation(8f, 9f, 10f);

        TeleportRequestC component = new TeleportRequestDefinition(expected).create();

        assertEquals(expected, component.targetRootWorld);
    }

    @Test
    void simpleComponentDefinitionsCreateExpectedValues() {
        NameC name = new NameDefinition("Assembler").create();
        ParentC parent = new ParentDefinition(false).create();
        TypeC type = new TypeDefinition().create();
        AnimationC animation = new AnimationDefinition().create();
        ItemComponent item = new ItemComponentDefinition().create();
        CollisionHandlerC collision = new CollisionHandlerDefinition().create();

        assertEquals("Assembler", name.name);
        assertEquals(false, parent.destroyChildrenWithParent);
        assertNotNull(type);
        assertNotNull(animation);
        assertNotNull(item);
        assertNotNull(collision);
    }

    @Test
    void updatableScriptDefinitionCopiesScripts() {
        Tickable scriptA = delta -> { };
        Tickable scriptB = delta -> { };
        Array<Tickable> scripts = new Array<>();
        scripts.add(scriptA, scriptB);

        UpdatableScriptC component = new UpdatableScriptDefinition(scripts).create();

        assertEquals(2, component.scripts.size);
        assertSame(scriptA, component.scripts.get(0));
        assertSame(scriptB, component.scripts.get(1));
    }
}
