package ore.forge.engine;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.*;

import java.util.HashMap;

public class UISchemaBuilder {
    private final HashMap<String, JsonValue> loadedSchemas = new HashMap<>();

    public UISchemaBuilder() {
    }

    public Actor foo(String schemaFileName) {
        JsonValue schemaData = loadedSchemas.computeIfAbsent(schemaFileName, this::loadSchema);

        //Walk through and construct our UI from the schema
        String schemaName = schemaData.getString("schemaName");
        String schemaId = schemaData.getString("schemaId");
        int version = schemaData.getInt("version");

        VisTable root = new VisTable();
        return buildMenu(root, schemaData.get("fields"));
    }

    public Actor buildMenu(VisTable root, JsonValue schemaData) {
        for (JsonValue field : schemaData) {
            String fieldName = field.getString("name");
            SchemaFieldType fieldType = SchemaFieldType.fromString(field.getString("type"));
            boolean required = field.getBoolean("required");
            String editorDescription = field.getString("description");

            VisTable label = createLabel(fieldName, editorDescription);

            switch (fieldType) {
                case STRING, INT, FLOAT -> {
                    label.addActor(labeledField(fieldName, fieldType, editorDescription, required));
                }
                case BOOL -> {
                    //create a checkbox
                    label.addActor(toggleBox(fieldName, fieldType, editorDescription, required));
                }
                case ENUM -> {
                    //Drop Down menu
                    label.addActor(dropDown(fieldName, extractEnums(field.get("enum_options")), editorDescription, required));
                }
                case OBJECT -> {
                    VisTable newRoot = new VisTable();
                    System.out.println("Here!");
                    label.add(buildMenu(newRoot, field.get("fields")));
                }
                default -> throw new IllegalArgumentException("Unsupported field type: " + fieldType);
            }
            root.add(label);
        }
        return root;
    }

    public VisTable createLabel(String name, String description) {
        VisTable table = new VisTable();
        VisLabel label = new VisLabel(name);
        table.add(label);
        table.addSeparator();
        return table;
    }

    public VisTable labeledField(String name, SchemaFieldType type, String description, boolean required) {
        //Create base table and label
        VisTable table = createLabel(name, description);

        //TODO: set hover over to display description.

        VisTextField textField = new VisTextField();
        //TODO: Configure what field accepts.

        table.add(textField);

        return table;
    }

    public VisTable toggleBox(String name, SchemaFieldType type, String description, boolean required) {
        VisTable table = createLabel(name, description);

        VisCheckBox checkBox = new VisCheckBox(description);
        table.add(checkBox);

        return table;
    }

    public VisTable dropDown(String name, Array<String> enums, String description, boolean required) {
        VisTable table = createLabel(name, description);

        for (String val : enums) {

        }

        return table;
    }

    public Array<String> extractEnums(JsonValue enumOptions) {
        Array<String> enums = new Array<>(enumOptions.size);
        for (JsonValue option : enumOptions) {
            enums.add(option.asString());
        }
        return enums;
    }


    private JsonValue loadSchema(String schemaName) {
        JsonReader reader = new JsonReader();
        return reader.parse(Gdx.files.internal(schemaName));
    }

}
