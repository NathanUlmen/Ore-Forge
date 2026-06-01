package ore.forge.engine;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisProgressBar;

import java.util.HashMap;

public class UISchemaBuilder {
    private final HashMap<String, Actor> loadedSchemas = new HashMap<>();

    public UISchemaBuilder() {
        VisUI.load();


    }

    public Actor foo(String schemaFileName) {
        JsonValue schemaData = loadSchema(schemaFileName);

        //Walk through and construct our UI from the schema
        String schemaName = schemaData.getString("schemaName");
        String schemaId = schemaData.getString("schemaId");
        int version =  schemaData.getInt("version");
        WidgetGroup root = new WidgetGroup();

        for (JsonValue field : schemaData.get("fields")) {
            String fieldName =  field.getString("name");
            SchemaFieldType fieldType = SchemaFieldType.fromString(field.getString("type"));
            boolean required = field.getBoolean("required");
            String editorDescription =  field.getString("description");
            //logic here
            switch (fieldType) {
                case STRING -> {}
                case BOOL -> {}
                case INT -> {}
                case FLOAT -> {}
                case ENUM -> {}
                case LIST -> {}
                case OBJECT -> {}
                default -> throw new IllegalArgumentException("Unsupported field type: " + fieldType);
            }
        }

        return null;
    }


    private JsonValue loadSchema(String schemaName) {
        Json json = new Json();

        return null;
    }

}
