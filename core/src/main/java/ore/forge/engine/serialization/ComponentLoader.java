package ore.forge.engine.serialization;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.SerializationException;
import com.badlogic.gdx.utils.JsonWriter;
import ore.forge.engine.PhysicsBodyType;
import ore.forge.engine.PhysicsMotionType;
import ore.forge.engine.components.*;
import ore.forge.engine.definitions.*;

import java.util.ArrayList;
import java.util.List;

import static ore.forge.engine.definitions.DefinitionTree.*;

/**
 * This class is responsible for loading "primitive" components into an intermediary
 * form so that Builders can assemble their definition into a final product.
 *
 */
public class ComponentLoader {
    private final Json json;


    public ComponentLoader() {
        json = new Json();
    }


    /**
     * Returns an intermediate form of the Definition.
     * A Definition Builder will then assemble the Definition from
     * this intermediate form.
     * This method assumes our data takes on this form
     * <p>
     * list[{}]
     *
     */
    public List<DefinitionTree> load(JsonValue loadFrom) {
        List<DefinitionTree> defTrees = new ArrayList<>(loadFrom.size);

        //Iterate through each definition in the list
        for (JsonValue value : loadFrom) {
            DefinitionTree defTree = createDefinitionTree(value);
            defTrees.add(defTree);
        }

        return defTrees;
    }

    public DefinitionTree createDefinitionTree(JsonValue value) {

        for (JsonValue component : value) {
            //parse components and build our tree
            if (component.isObject()) {
                var componentNode = createComponentNode(component);
            }
        }

        return null;
    }

    public ComponentNode createComponentNode(JsonValue value) {
        Component data = (Component) createComponent(value);
        ComponentNode node = new ComponentNode(data);
        return null;
    }

    /**
     *
     * Valid Component types:
     * {@link ore.forge.engine.components.TransformC}
     * {@link ore.forge.engine.components.RenderC}
     * {@link ore.forge.engine.components.DirectionC}
     * {@link ore.forge.engine.components.PhysicsC}
     *
     *
     */
    public Object createComponent(JsonValue value) {
        return switch (value.getString("componentType")) {
            case "TransformComponent" -> {
                yield readComponentData(value, "transformComponent", TransformC.class);
            }
            case "RenderComponent" -> {

                //TODO: Rework RenderParts so its handle based to an asset if that makes sense.

                yield new RenderC();
            }
            case "DirectionComponent" -> {

                yield readComponentData(value, "directionComponent", DirectionC.class);
            }
            case "PhysicsComponent" -> {
                yield createPhysicsCompIR(value);
            }
            default ->
                throw new SerializationException("Unsupported Component Type: " + value.getString("componentType"));

        };

    }

    private <T> T readComponentData(JsonValue value, String key, Class<T> type) {
        JsonValue componentData = value.get(key);
        if (componentData == null || !componentData.isObject()) {
            throw new SerializationException("Missing component data for key: " + key);
        }

        return json.fromJson(type, componentData.toJson(JsonWriter.OutputType.json));
    }

    private PhysicsCompIR createPhysicsCompIR(JsonValue jsonValue) {
        PhysicsMotionType motionType = PhysicsMotionType.valueOf(jsonValue.getString("motionType"));
        PhysicsBodyType bodyType = PhysicsBodyType.valueOf(jsonValue.getString("bodyType"));
        PhysicsCollisionShapeIR ir = shapeIR(jsonValue.get("collisionShape"));

        return new PhysicsCompIR(bodyType, motionType, ir);
    }

    private PhysicsCollisionShapeIR shapeIR(JsonValue jsonValue) {
        String id = jsonValue.getString("id");
        System.out.println(id);
        return switch (jsonValue.getString("shapeType")) {
            case "Box" -> new BoxShapeIR(id, readComponentData(jsonValue, "boundingBox", BoundingBox.class));
            case "Sphere" -> new SphereShapeIR(id, jsonValue.getFloat("radius"));
            case "Capsule" -> new CapsuleShapeIR(id, jsonValue.getFloat("radius"), jsonValue.getFloat("height"));
            case "CompoundShape" -> {
                JsonValue children = jsonValue.get("children");
                if (children == null || children.size == 0) {
                    throw new SerializationException("CompoundShape missing children.");
                }

                List<Matrix4> transforms = new ArrayList<>(children.size);
                List<PhysicsCollisionShapeIR> collisionShapes = new ArrayList<>(children.size);

                for (JsonValue value : children) {
                    transforms.add(readComponentData(value, "transform", Matrix4.class));
                    collisionShapes.add(shapeIR(value.get("collisionShape")));
                }

                yield new CompoundShapeIR(id, transforms, collisionShapes);
            }
            default -> throw new SerializationException("Unsupported Shape Type: " + jsonValue.getString("shapeType"));
        };
    }

}
