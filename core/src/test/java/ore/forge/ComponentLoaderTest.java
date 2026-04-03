package ore.forge;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.SerializationException;
import ore.forge.engine.components.DirectionC;
import ore.forge.engine.PhysicsBodyType;
import ore.forge.engine.PhysicsMotionType;
import ore.forge.engine.components.TransformC;
import ore.forge.engine.definitions.*;
import ore.forge.engine.serialization.ComponentLoader;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ComponentLoaderTest {
    private static final String FIXTURE_ROOT = "component-loader/";

    private final JsonReader jsonReader = new JsonReader();

    private JsonValue loadFixture(String fixtureName) {
        try (InputStream stream = fixture(fixtureName)) {
            String json = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            return jsonReader.parse(json);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read fixture: " + fixtureName, e);
        }
    }

    private InputStream fixture(String fixtureName) {
        String resourcePath = FIXTURE_ROOT + fixtureName;
        InputStream stream = Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream(resourcePath);

        assertNotNull(stream, "Missing fixture: " + resourcePath);
        return stream;
    }

    private JsonValue loadComponentFixture(String fixtureName) {
        JsonValue fixture = loadFixture(fixtureName);
        assertEquals(JsonValue.ValueType.array, fixture.type());
        assertNotNull(fixture.child, "Fixture must contain at least one component definition");
        return fixture.child;
    }

    private Object createComponent(String fixtureName) {
        return new ComponentLoader().createComponent(loadComponentFixture(fixtureName));
    }

    private void assertVectorEquals(Vector3 expected, Vector3 actual) {
        assertEquals(expected.x, actual.x, 0.0001f);
        assertEquals(expected.y, actual.y, 0.0001f);
        assertEquals(expected.z, actual.z, 0.0001f);
    }

    private void assertQuaternionEquals(Quaternion expected, Quaternion actual) {
        assertEquals(expected.x, actual.x, 0.0001f);
        assertEquals(expected.y, actual.y, 0.0001f);
        assertEquals(expected.z, actual.z, 0.0001f);
        assertEquals(expected.w, actual.w, 0.0001f);
    }

    private void assertMatrixEquals(Matrix4 expected, Matrix4 actual) {
        for (int i = 0; i < expected.val.length; i++) {
            assertEquals(expected.val[i], actual.val[i], 0.0001f);
        }
    }

    @Test
    public void testTransformComponentLoad() {
        var result = createComponent("transformComponent.json");

        assertNotNull(result);
        TransformC c = (TransformC) result;
        assertVectorEquals(new Vector3(1, 2, 3), c.localPosition);
        assertQuaternionEquals(new Quaternion(1, 2, 3, 4), c.localRotation);
        assertVectorEquals(new Vector3(1, 1, 1), c.localScale);
    }

    @Test
    public void testDirectionComponentLoad() {
        var result = createComponent("directionComponent.json");

        assertNotNull(result);
        DirectionC c = (DirectionC) result;
        assertVectorEquals(new Vector3(0, 1, 0), c.directionOffset);
    }

    @Test
    public void testPhysicsCompIRLoad() {
        var result = createComponent("physicsComponent.json");

        assertNotNull(result);
        PhysicsCompIR c = (PhysicsCompIR) result;
        assertEquals(PhysicsBodyType.RIGID, c.bodyType());
        assertEquals(PhysicsMotionType.DYNAMIC, c.motionType());
        assertEquals(SphereShapeIR.class, c.collisionShape().getClass());
        SphereShapeIR sphereShape = (SphereShapeIR) c.collisionShape();
        assertEquals(2.5f, sphereShape.radius(), 0.0001f);
    }

    @Test
    public void testPhysicsCompIRLoadWithCapsuleShape() {
        JsonValue physicsComponent = jsonReader.parse("""
            {
              "componentType": "PhysicsComponent",
              "bodyType": "GHOST",
              "motionType": "KINEMATIC",
              "collisionShape": {
                "id" : "abc",
                "shapeType": "Capsule",
                "radius": 1.25,
                "height": 3.5
              }
            }
            """);

        var result = new ComponentLoader().createComponent(physicsComponent);

        assertNotNull(result);
        PhysicsCompIR c = (PhysicsCompIR) result;
        assertEquals(PhysicsBodyType.GHOST, c.bodyType());
        assertEquals(PhysicsMotionType.KINEMATIC, c.motionType());
        assertEquals(CapsuleShapeIR.class, c.collisionShape().getClass());
        CapsuleShapeIR capsuleShape = (CapsuleShapeIR) c.collisionShape();
        assertEquals(1.25f, capsuleShape.radius(), 0.0001f);
        assertEquals(3.5f, capsuleShape.height(), 0.0001f);
    }

    @Test
    public void testNestedPhysicsCompIRLoad() {
        var result = createComponent("nestedPhysicsComponent.json");

        assertNotNull(result);
        PhysicsCompIR c = (PhysicsCompIR) result;
        //First level
        assertEquals(PhysicsBodyType.RIGID, c.bodyType());
        assertEquals(PhysicsMotionType.DYNAMIC, c.motionType());
        assertInstanceOf(CompoundShapeIR.class, c.collisionShape());
        //Second Level
        CompoundShapeIR compoundShape = (CompoundShapeIR) c.collisionShape();
        assertInstanceOf(SphereShapeIR.class, compoundShape.collisionShapes().getFirst());
        assertInstanceOf(CompoundShapeIR.class, compoundShape.collisionShapes().get(1));
        assertMatrixEquals(new Matrix4(new float[]{1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 2, 0, 0, 1}), compoundShape.transforms().getFirst());
        assertMatrixEquals(new Matrix4(new float[]{1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 3, 0, 1}), compoundShape.transforms().get(1));
        //Third level
        compoundShape = (CompoundShapeIR) compoundShape.collisionShapes().get(1);
        assertInstanceOf(CapsuleShapeIR.class, compoundShape.collisionShapes().getFirst());
        assertInstanceOf(BoxShapeIR.class, compoundShape.collisionShapes().get(1));
        assertMatrixEquals(new Matrix4(new float[]{1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 4, 1}), compoundShape.transforms().getFirst());
        assertMatrixEquals(new Matrix4(new float[]{1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, -1, 0, 0, 1}), compoundShape.transforms().get(1));

    }

    @Test
    public void testUnsupportedComponentTypeThrows() {
        assertThrows(
            SerializationException.class,
            () -> createComponent("unsupportedComponent.json")
        );
    }
}
