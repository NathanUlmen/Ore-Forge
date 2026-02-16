package ore.forge.engine;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.JsonValue;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

public class ReflectionLoader {
    private final static HashMap<String, Constructor<?>> cachedResults = new HashMap<>();

    public static <E> E load(JsonValue jsonValue, String fieldName) {
        if (jsonValue == null) {
            throw new IllegalArgumentException("JsonValue is null for field: " + fieldName);
        }

        String className = jsonValue.getString(fieldName, null);
        if (className == null) {
            throw new IllegalArgumentException("Missing field '" + fieldName + "' in JsonValue:\n" + jsonValue);
        }

        Constructor<?> constructor = cachedResults.get(className);
        try {
            if (constructor != null) {
                return (E) constructor.newInstance(jsonValue);
            }

            Class<?> clazz = Class.forName(className);
            constructor = clazz.getConstructor(JsonValue.class);
            cachedResults.put(className, constructor);

            return (E) constructor.newInstance(jsonValue);

        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            Gdx.app.error("ReflectionLoader", "Constructor of class '" + className + "' threw an exception.\nJSON:\n" + jsonValue, cause);
            throw new RuntimeException("Constructor exception in " + className, cause);

        } catch (ClassNotFoundException e) {
            Gdx.app.error("ReflectionLoader", "Class not found: '" + className + "'\nJSON:\n" + jsonValue, e);
            throw new RuntimeException("Missing class: " + className, e);

        } catch (NoSuchMethodException e) {
            Gdx.app.error("ReflectionLoader", "Missing constructor(JsonValue) in class '" + className + "'\nJSON:\n" + jsonValue, e);
            throw new RuntimeException("Constructor not found in " + className, e);

        } catch (InstantiationException | IllegalAccessException e) {
            Gdx.app.error("ReflectionLoader", "Error instantiating class '" + className + "'\nJSON:\n" + jsonValue, e);
            throw new RuntimeException("Failed to instantiate: " + className, e);
        }
    }

}
