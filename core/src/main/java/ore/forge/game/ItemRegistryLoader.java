package ore.forge.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import ore.forge.game.items.Acquisition.AcquisitionInfo;
import ore.forge.game.items.ItemDefinition;

import java.util.ArrayList;
import java.util.List;

import ore.forge.game.items.ItemRole;
import ore.forge.game.items.Tier;


/** @author Nathan Ulmen
 *
 * Loads {@link ItemDefinition} json files from a directory and its
 * sub directories into a {@link ItemRegistry}
 *
 * - N.U. Feb 20, 2026
 */
public class ItemRegistryLoader {

    public static void load(ItemRegistry itemRegistry, String targetDirectory) {
        if (itemRegistry == null) throw new IllegalArgumentException("itemRegistry is null");
        if (targetDirectory == null || targetDirectory.isBlank()) {
            throw new IllegalArgumentException("targetDirectory is null/blank");
        }

        FileHandle root = Gdx.files.local(targetDirectory);
        if (!root.exists()) {
            fail("Item directory does not exist: " + root.path());
        }
        if (!root.isDirectory()) {
            fail("Item directory is not a directory: " + root.path());
        }

        JsonReader reader = new JsonReader();
        loadRecursive(itemRegistry, reader, root);
    }

    private static void loadRecursive(ItemRegistry registry, JsonReader reader, FileHandle dir) {
        for (FileHandle fileHandle : dir.list()) {
            if (fileHandle.isDirectory()) {
                loadRecursive(registry, reader, fileHandle);
                continue;
            }

            if (!fileHandle.extension().equalsIgnoreCase("json")) continue;

            JsonValue parsed;
            parsed = reader.parse(fileHandle);

            List<ItemDefinition> defs;
            try {
                defs = createDefs(parsed, fileHandle);
            } catch (Exception e) {
                fail("Failed to construct item definitions from: " + fileHandle.path(), e);
                continue;
            }

            for (ItemDefinition def : defs) {
                if (def == null) {
                    warn("Null ItemDefinition produced from: " + fileHandle.path());
                    continue;
                }

                if (registry.getDefinition(def.id()) != null) {
                    fail("Duplicate item id '" + def.id() + "' while loading file: " + fileHandle.path());
                }

                registry.addDefinition(def);
            }

        }

    }

    //Supports json array or single json object
    private static List<ItemDefinition> createDefs(JsonValue json, FileHandle sourceFile) {
        List<ItemDefinition> defs = new ArrayList<>();

        if (json == null) return defs;

        if (json.isArray()) {
            for (JsonValue entry = json.child; entry != null; entry = entry.next) {
                defs.add(constructDefinition(entry, sourceFile));
            }
        } else if (json.isObject()) {
            defs.add(constructDefinition(json, sourceFile));
        } else {
            warn("Unexpected JSON root type in " + sourceFile.path() + " (expected array or object).");
        }

        return defs;
    }

    private static ItemDefinition constructDefinition(JsonValue jsonValue, FileHandle sourceFile) {
        //All this is parent entity data
        String name = requireString(jsonValue, "name", sourceFile);
        String id = requireString(jsonValue, "id", sourceFile);
        String description = requireString(jsonValue, "description", sourceFile);

        int roleMask = 0;
        JsonValue roles = jsonValue.get("roles");
        for (JsonValue jsonRole : roles) {
            roleMask |= ItemRole.valueOf(jsonRole.asString().trim().toUpperCase()).mask;
        }

        Tier tier = Tier.valueOf(requireString(jsonValue, "tier", sourceFile).trim().toUpperCase());

        JsonValue acquisitionJson = jsonValue.get("acquisitionInfo");
        AcquisitionInfo acquisitionInfo = new AcquisitionInfo(acquisitionJson, tier);

        //TODO
        //Begin loading sub entity data (mesh, physics, Scripts/logic, etc)

        throw new UnsupportedOperationException(
            "constructDefinition not implemented for file: " + (sourceFile == null ? "<unknown>" : sourceFile.path())
        );
    }

    private static String requireString(JsonValue obj, String key, FileHandle src) {
        if (obj == null) throw new IllegalArgumentException("Null JSON object");
        JsonValue v = obj.get(key);
        if (v == null || v.isNull()) {
            fail("Missing required string '" + key + "' in " + src.path());
        }
        String s = v.asString();
        if (s == null || s.isBlank()) {
            fail("Required string '" + key + "' is blank in " + src.path());
        }
        return s;
    }

    private static String getString(JsonValue obj, String key, String defaultValue) {
        if (obj == null) return defaultValue;
        JsonValue v = obj.get(key);
        if (v == null || v.isNull()) return defaultValue;
        return v.asString();
    }

    private static int getInt(JsonValue obj, String key, int defaultValue) {
        if (obj == null) return defaultValue;
        JsonValue v = obj.get(key);
        if (v == null || v.isNull()) return defaultValue;
        try {
            return v.asInt();
        } catch (Exception ignored) {
            return defaultValue;
        }
    }

    private static boolean getBool(JsonValue obj, String key, boolean defaultValue) {
        if (obj == null) return defaultValue;
        JsonValue v = obj.get(key);
        if (v == null || v.isNull()) return defaultValue;
        try {
            return v.asBoolean();
        } catch (Exception ignored) {
            return defaultValue;
        }
    }

    private static void warn(String warning) {
        Gdx.app.error("ItemRegistryLoader", warning);
    }

    private static void fail(String message) {
        throw new RuntimeException(message);
    }

    private static void fail(String message, Throwable t) {
        throw new RuntimeException(message, t);
    }

}
