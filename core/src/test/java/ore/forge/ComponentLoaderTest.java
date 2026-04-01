package ore.forge;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.SerializationException;
import ore.forge.engine.components.DirectionC;
import ore.forge.engine.components.TransformC;
import ore.forge.engine.serialization.ComponentLoader;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    private Component createComponent(String fixtureName) {
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
    public void testUnsupportedComponentTypeThrows() {
        assertThrows(
            SerializationException.class,
            () -> createComponent("unsupportedComponent.json")
        );
    }
}
