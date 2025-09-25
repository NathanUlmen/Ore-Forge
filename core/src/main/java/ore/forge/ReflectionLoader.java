package ore.forge;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonValue;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

public class ReflectionLoader {
    private final static HashMap<String, Constructor<?>> cachedResults = new HashMap<>();


    @SuppressWarnings("unchecked")
//    public static <E> E load(JsonValue jsonValue, String fieldName) throws IllegalArgumentException {
//        if (jsonValue == null) {
//            throw new IllegalArgumentException("JsonValue with argument: " + fieldName + " is null.");
//        }
//
//        Constructor<?> constructor = cachedResults.get(jsonValue.getString(fieldName));
//        try {
//            if (constructor != null) {
//                return (E) constructor.newInstance(jsonValue);
//            }
//        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
//            Gdx.app.log("ReflectionLoader", Color.highlightString("Error loading class: " + constructor.getClass().getSimpleName() + " from JsonValue:\n" + jsonValue , Color.RED));
//            throw new RuntimeException(e);
//        }
//
//        Class<?> aClass;
//        try {
//            aClass = Class.forName(jsonValue.getString(fieldName));
//            constructor = aClass.getConstructor(JsonValue.class);
//            cachedResults.put(jsonValue.getString(fieldName), constructor);
//            return (E) constructor.newInstance(jsonValue);
//        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException |
//                 ClassNotFoundException e) {
//            Gdx.app.log("ReflectionLoader", Color.highlightString( "Error in:\n" + jsonValue + "\nWhen trying to retrieve value linked to key: " + fieldName, Color.RED));
//            throw new RuntimeException(e);
//        }
//    }
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


    public static Vector2 loadVector2(JsonValue jsonValue) {
        return new Vector2(jsonValue.getFloat("x"), jsonValue.getFloat("y"));
    }

}
