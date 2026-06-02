package ore.forge.engine;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.TextTooltip;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.Separator;
import com.kotcrab.vis.ui.widget.VisCheckBox;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisSelectBox;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextField;

import java.util.HashMap;

public class UISchemaBuilder {
    private static final float ROOT_PADDING = 8f;
    private static final float FIELD_PADDING = 4f;
    private static final float FIELD_GAP = 12f;
    private static final float NESTED_PADDING = 18f;
    private static final float OBJECT_SECTION_PADDING = 10f;
    private static final float OBJECT_INNER_PADDING = 8f;
    private static final float OBJECT_HEADER_SCALE = 1.15f;
    private static final float INPUT_MIN_WIDTH = 220f;

    private final HashMap<String, JsonValue> loadedSchemas = new HashMap<>();

    public UISchemaBuilder() {
    }

    public Actor foo(String schemaFileName) {
        JsonValue schemaData = loadedSchemas.computeIfAbsent(schemaFileName, this::loadSchema);

        String schemaName = schemaData.getString("schemaName", "Schema");
        String schemaId = schemaData.getString("schemaId", "");
        String version = schemaData.getString("version", "");

        VisTable root = createVerticalTable(ROOT_PADDING);
        root.add(createSchemaHeader(schemaName, schemaId, version)).growX().row();

        return buildMenu(root, schemaData.get("fields"));
    }

    public Actor build(String schemaFileName) {
        return foo(schemaFileName);
    }

    public Actor buildMenu(VisTable root, JsonValue schemaData) {
        if (schemaData == null) {
            return root;
        }

        float labelWidth = calculateLabelColumnWidth(schemaData);

        for (JsonValue field : schemaData) {
            String fieldName = field.getString("name");
            SchemaFieldType fieldType = SchemaFieldType.fromString(field.getString("type"));
            boolean required = field.getBoolean("required", false);
            String editorDescription = field.getString("description", "");
            Actor fieldActor;

            switch (fieldType) {
                case STRING, INT, FLOAT ->
                    fieldActor = labeledField(fieldName, editorDescription, required, labelWidth);
                case BOOL -> fieldActor = toggleBox(fieldName, editorDescription, required, labelWidth);
                case ENUM ->
                    fieldActor = dropDown(fieldName, extractEnums(field.get("enum_options")), editorDescription, required, labelWidth);
                case OBJECT ->
                    fieldActor = buildObjectField(fieldName, editorDescription, required, field.get("fields"));
                default -> throw new IllegalArgumentException("Unsupported field type: " + fieldType);
            }

            root.add(fieldActor).growX().row();
        }
        return root;
    }

    public VisTable createLabel(String name, String description) {
        VisTable table = createLeftAlignedTable(0f);

        VisLabel label = new VisLabel(name);
        table.add(label).left();
        attachTooltip(label, description);

        return table;
    }

    public VisTable labeledField(String name, String description, boolean required) {
        return labeledField(name, description, required, INPUT_MIN_WIDTH);
    }

    private VisTable labeledField(String name, String description, boolean required, float labelWidth) {
        VisTable table = createFieldRow(name, description, required, labelWidth);

        VisTextField textField = new VisTextField();
        textField.setMessageText(description);
        addInput(table, textField, description);
        return table;
    }

    public VisTable toggleBox(String name,  String description, boolean required) {
        return toggleBox(name,  description, required, INPUT_MIN_WIDTH);
    }

    private VisTable toggleBox(String name, String description, boolean required, float labelWidth) {
        VisTable table = createFieldRow(name, description, required, labelWidth);

        VisCheckBox checkBox = new VisCheckBox("");
        addInput(table, checkBox, description);
        return table;
    }

    public VisTable dropDown(String name, Array<String> enums, String description, boolean required) {
        return dropDown(name, enums, description, required, INPUT_MIN_WIDTH);
    }

    private VisTable dropDown(String name, Array<String> enums, String description, boolean required, float labelWidth) {
        VisTable table = createFieldRow(name, description, required, labelWidth);

        VisSelectBox<String> selectBox = new VisSelectBox<>();
        if (enums.size > 0) {
            selectBox.setItems(enums);
        }
        addInput(table, selectBox, description);
        return table;
    }

    public Array<String> extractEnums(JsonValue enumOptions) {
        Array<String> enums = new Array<>();
        if (enumOptions == null) {
            return enums;
        }

        for (JsonValue option : enumOptions) {
            enums.add(option.asString());
        }
        return enums;
    }

    private VisTable buildObjectField(String name, String description, boolean required, JsonValue fields) {
        VisTable table = createVerticalTable(OBJECT_SECTION_PADDING);
        table.add(createObjectHeader(name, required, description)).growX().row();

        VisTable body = createVerticalTable(OBJECT_INNER_PADDING);
        body.padLeft(NESTED_PADDING);
        body.add(new Separator()).growX().padBottom(4f).row();

        VisTable nested = createVerticalTable(6f);
        buildMenu(nested, fields);

        body.add(nested).growX().row();
        body.add(new Separator()).growX().padTop(4f).row();

        table.add(body).growX().row();
        return table;
    }

    private VisTable createSchemaHeader(String schemaName, String schemaId, String version) {
        VisTable table = createVerticalTable(0f);

        table.add(new VisLabel(schemaName)).row();

        if (!schemaId.isBlank() || !version.isBlank()) {
            String meta = schemaId;
            if (!version.isBlank()) {
                meta = meta.isBlank() ? "v" + version : meta + "  v" + version;
            }
            table.add(new VisLabel(meta)).row();
        }

        table.add(new Separator()).growX().padTop(4f);
        return table;
    }

    private VisTable createFieldRow(String name, String description, boolean required) {
        return createFieldRow(name, description, required, INPUT_MIN_WIDTH);
    }

    private VisTable createFieldRow(String name, String description, boolean required, float labelWidth) {
        VisTable table = createLeftAlignedTable(FIELD_PADDING);

        table.add(createFieldTitle(name, required, description)).width(labelWidth).left().padRight(FIELD_GAP);
        return table;
    }

    private VisTable createFieldTitle(String name, boolean required, String description) {
        String fieldTitle = createFieldTitleText(name, required);
        return createLabel(fieldTitle, description);
    }

    private VisTable createObjectHeader(String name, boolean required, String description) {
        VisTable table = createLeftAlignedTable(FIELD_PADDING);
        VisLabel headerLabel = new VisLabel(createFieldTitleText(name, required));
        headerLabel.setFontScale(OBJECT_HEADER_SCALE);
        attachTooltip(headerLabel, description);

        VisLabel objectMarker = new VisLabel("(Object)");
        objectMarker.setFontScale(OBJECT_HEADER_SCALE);
        attachTooltip(objectMarker, description);

        table.add(headerLabel).left().padRight(FIELD_GAP);
        table.add(objectMarker).left();
        return table;
    }

    private void attachTooltip(Actor actor, String description) {
        if (description == null || description.isBlank()) {
            return;
        }

        TextTooltip tooltip = new TextTooltip(description, VisUI.getSkin());
        tooltip.setInstant(true);
        actor.addListener(tooltip);
    }

    private void addInput(VisTable table, Actor input, String description) {
        attachTooltip(input, description);
        if (input instanceof VisCheckBox) {
            table.add(input).left();
            return;
        }
        table.add(input).growX().minWidth(INPUT_MIN_WIDTH).left();
    }

    private float calculateLabelColumnWidth(JsonValue schemaData) {
        float labelWidth = 0f;

        for (JsonValue field : schemaData) {
            if (SchemaFieldType.fromString(field.getString("type")) == SchemaFieldType.OBJECT) {
                continue;
            }

            String fieldTitle = createFieldTitleText(field.getString("name"), field.getBoolean("required", false));
            labelWidth = Math.max(labelWidth, new VisLabel(fieldTitle).getPrefWidth());
        }

        return labelWidth;
    }

    private VisTable createVerticalTable(float pad) {
        VisTable table = new VisTable(true);
        table.top().left();
        table.defaults().growX().left().pad(pad);
        return table;
    }

    private VisTable createLeftAlignedTable(float pad) {
        VisTable table = new VisTable(true);
        table.top().left();
        table.defaults().left().pad(pad);
        return table;
    }

    private String createFieldTitleText(String name, boolean required) {
        return required ? name + ":" : name;
    }

    private JsonValue loadSchema(String schemaName) {
        JsonReader reader = new JsonReader();
        return reader.parse(Gdx.files.internal(schemaName));
    }
}
