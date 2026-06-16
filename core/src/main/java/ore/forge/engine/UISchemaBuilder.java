package ore.forge.engine;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.TextTooltip;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.Separator;
import com.kotcrab.vis.ui.widget.VisCheckBox;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisSelectBox;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextField;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class UISchemaBuilder {
    private static final float ROOT_PADDING = 8f;
    private static final float FIELD_PADDING = 4f;
    private static final float FIELD_GAP = 12f;
    private static final float NESTED_PADDING = 18f;
    private static final float OBJECT_SECTION_PADDING = 10f;
    private static final float OBJECT_INNER_PADDING = 8f;
    private static final float OBJECT_HEADER_SCALE = 1.15f;
    private static final float INPUT_MIN_WIDTH = 220f;
    private static final float SCOPE_GUIDE_WIDTH = 2f;
    private static final float SCOPE_GUIDE_INSET = 3f;
    private static final Color SCOPE_GUIDE_COLOR = new Color(0.72f, 0.66f, 0.58f, 0.9f);

    private static Texture scopeGuideTexture;

    private final HashMap<String, JsonValue> loadedSchemas = new HashMap<>();
    private InputNode rootInputNode;

    public UISchemaBuilder() {
    }

    public Actor build(String schemaFileName) {
        JsonValue schemaData = loadedSchemas.computeIfAbsent(schemaFileName, this::loadSchema);

        String schemaName = schemaData.getString("schemaName", "Schema");
        String schemaId = schemaData.getString("schemaId", "");
        String version = schemaData.getString("version", "");

        VisTable root = createVerticalTable(ROOT_PADDING);
        root.add(createSchemaHeader(schemaName, schemaId, version)).growX().row();

        rootInputNode = new ObjectInputNode();
        return buildMenu(root, schemaData.get("fields"), (ObjectInputNode) rootInputNode);
    }

    public Actor buildMenu(VisTable root, JsonValue schemaData) {
        return buildMenu(root, schemaData, new ObjectInputNode());
    }

    private Actor buildMenu(VisTable root, JsonValue schemaData, ObjectInputNode parentNode) {
        if (schemaData == null) {
            return root;
        }

        float labelWidth = calculateLabelColumnWidth(schemaData);

        for (JsonValue field : schemaData) {
            String fieldName = getFieldDisplayName(field);
            String fieldKey = getFieldKey(field);
            SchemaFieldType fieldType = SchemaFieldType.fromString(field.getString("type"));
            boolean required = field.getBoolean("required", false);
            String editorDescription = field.getString("description", "");
            Actor fieldActor;

            switch (fieldType) {
                case STRING, INT, FLOAT ->
                    fieldActor = labeledField(fieldName, fieldKey, fieldType, editorDescription, required, labelWidth, parentNode);
                case BOOL -> fieldActor = toggleBox(fieldName, fieldKey, editorDescription, required, labelWidth, parentNode);
                case ENUM ->
                    fieldActor = dropDown(fieldName, fieldKey, extractEnums(field.get("enum_options")), editorDescription, required, labelWidth, parentNode);
                case OBJECT ->
                    fieldActor = buildObjectField(fieldName, fieldKey, editorDescription, required, field.get("fields"), parentNode);
                case SCHEMA_REF ->
                    fieldActor = buildSchemaRefField(fieldName, fieldKey, editorDescription, required, field.getString("schema_ref"), parentNode);
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

    private VisTable labeledField(String name, String key, SchemaFieldType type, String description, boolean required, float labelWidth, ObjectInputNode parentNode) {
        VisTable table = createFieldRow(name, key, description, required, labelWidth);

        VisTextField textField = new VisTextField();
        textField.setMessageText(description);
        addInput(table, textField, description);
        parentNode.put(key, new PrimitiveInputNode(type, textField));
        return table;
    }

    private VisTable toggleBox(String name, String key, String description, boolean required, float labelWidth, ObjectInputNode parentNode) {
        VisTable table = createFieldRow(name, key, description, required, labelWidth);

        VisCheckBox checkBox = new VisCheckBox("");
        addInput(table, checkBox, description);
        parentNode.put(key, new BooleanInputNode(checkBox));
        return table;
    }

    private VisTable dropDown(String name, String key, Array<String> enums, String description, boolean required, float labelWidth, ObjectInputNode parentNode) {
        VisTable table = createFieldRow(name, key, description, required, labelWidth);

        VisSelectBox<String> selectBox = new VisSelectBox<>();
        if (enums.size > 0) {
            selectBox.setItems(enums);
        }
        addInput(table, selectBox, description);
        parentNode.put(key, new EnumInputNode(selectBox));
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

    private VisTable buildObjectField(String name, String key, String description, boolean required, JsonValue fields, ObjectInputNode parentNode) {
        VisTable table = createVerticalTable(OBJECT_SECTION_PADDING);
        table.add(createObjectHeader(name, key, required, description)).growX().row();

        VisTable body = createVerticalTable(OBJECT_INNER_PADDING);
        body.add(new Separator()).growX().padBottom(4f).row();

        VisTable nested = createScopeGuideTable(6f);
        nested.padLeft(NESTED_PADDING);
        ObjectInputNode objectNode = new ObjectInputNode();
        parentNode.put(key, objectNode);
        buildMenu(nested, fields, objectNode);

        body.add(nested).growX().row();
        body.add(new Separator()).growX().padTop(4f).row();

        table.add(body).growX().row();
        return table;
    }

    private VisTable buildSchemaRefField(String name, String key, String description, boolean required, String schemaRef, ObjectInputNode parentNode) {
        JsonValue schemaData = loadedSchemas.computeIfAbsent(schemaRef, this::loadSchema);

        VisTable table = createVerticalTable(OBJECT_SECTION_PADDING);
        table.add(createSchemaRefHeader(name, key, required, description, schemaRef)).growX().row();

        VisTable body = createVerticalTable(OBJECT_INNER_PADDING);
        body.add(new Separator()).growX().padBottom(4f).row();

        VisTable nested = createScopeGuideTable(6f);
        nested.padLeft(NESTED_PADDING);
        nested.add(createSchemaHeader(
            schemaData.getString("schemaName", "Schema"),
            schemaData.getString("schemaId", ""),
            schemaData.getString("version", "")
        )).growX().row();
        ObjectInputNode schemaRefNode = new ObjectInputNode();
        parentNode.put(key, schemaRefNode);
        buildMenu(nested, schemaData.get("fields"), schemaRefNode);

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

    private VisTable createFieldRow(String name, String key, String description, boolean required, float labelWidth) {
        VisTable table = createLeftAlignedTable(FIELD_PADDING);

        table.add(createFieldTitle(name, key, required, description)).width(labelWidth).left().padRight(FIELD_GAP);
        return table;
    }

    private VisTable createFieldTitle(String name, String key, boolean required, String description) {
        String fieldTitle = createFieldTitleText(name, required);
        return createLabel(fieldTitle, buildFieldTooltip(description, key));
    }

    private VisTable createObjectHeader(String name, String key, boolean required, String description) {
        VisTable table = createLeftAlignedTable(FIELD_PADDING);
        VisLabel headerLabel = new VisLabel(createFieldTitleText(name, required));
        headerLabel.setFontScale(OBJECT_HEADER_SCALE);
        attachTooltip(headerLabel, buildFieldTooltip(description, key));

        VisLabel objectMarker = new VisLabel("(Object)");
        objectMarker.setFontScale(OBJECT_HEADER_SCALE);
        attachTooltip(objectMarker, buildFieldTooltip(description, key));

        table.add(headerLabel).left().padRight(FIELD_GAP);
        table.add(objectMarker).left();
        return table;
    }

    private VisTable createSchemaRefHeader(String name, String key, boolean required, String description, String schemaRef) {
        VisTable table = createLeftAlignedTable(FIELD_PADDING);

        VisLabel headerLabel = new VisLabel(createFieldTitleText(name, required));
        headerLabel.setFontScale(OBJECT_HEADER_SCALE);
        attachTooltip(headerLabel, buildFieldTooltip(description, key));

        VisLabel refMarker = new VisLabel("(Schema Ref)");
        refMarker.setFontScale(OBJECT_HEADER_SCALE);
        attachTooltip(refMarker, buildFieldTooltip(description, key));

        table.add(headerLabel).left().padRight(FIELD_GAP);
        table.add(refMarker).left().padRight(FIELD_GAP);

        if (schemaRef != null && !schemaRef.isBlank()) {
            VisLabel refValue = new VisLabel(schemaRef);
            attachTooltip(refValue, schemaRef);
            table.add(refValue).left();
        }

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
        if (input instanceof VisTextField) {
            attachCursor(input, Cursor.SystemCursor.Ibeam);
        } else {
            attachCursor(input, Cursor.SystemCursor.Hand);
        }
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

            String fieldTitle = createFieldTitleText(getFieldDisplayName(field), field.getBoolean("required", false));
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

    private VisTable createScopeGuideTable(float pad) {
        ScopeGuideTable table = new ScopeGuideTable();
        table.top().left();
        table.defaults().growX().left().pad(pad);
        return table;
    }

    private String createFieldTitleText(String name, boolean required) {
        return required ? name + ":" : name;
    }

    private String getFieldDisplayName(JsonValue field) {
        return field.getString("name", field.getString("key", "Unnamed Field"));
    }

    private String getFieldKey(JsonValue field) {
        return field.getString("key", field.getString("name", "unnamedField"));
    }

    private String buildFieldTooltip(String description, String key) {
        if (description == null || description.isBlank()) {
            return "Key: " + key;
        }
        return description + "\nKey: " + key;
    }

    public String exportInputAsJson() {
        if (rootInputNode == null) {
            return "{}";
        }

        Json json = new Json(JsonWriter.OutputType.json);
        return json.prettyPrint(rootInputNode.toValue());
    }

    private void attachCursor(Actor actor, Cursor.SystemCursor hoverCursor) {
        actor.addListener(new InputListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                Gdx.graphics.setSystemCursor(hoverCursor);
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow);
            }
        });
    }

    private JsonValue loadSchema(String schemaName) {
        JsonReader reader = new JsonReader();
        return reader.parse(Gdx.files.internal(schemaName));
    }

    private static Texture getScopeGuideTexture() {
        if (scopeGuideTexture == null) {
            Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pixmap.setColor(Color.WHITE);
            pixmap.fill();
            scopeGuideTexture = new Texture(pixmap);
            pixmap.dispose();
        }
        return scopeGuideTexture;
    }

    private static final class ScopeGuideTable extends VisTable {
        @Override
        public void draw(Batch batch, float parentAlpha) {
            float previousColor = batch.getPackedColor();
            batch.setColor(SCOPE_GUIDE_COLOR.r, SCOPE_GUIDE_COLOR.g, SCOPE_GUIDE_COLOR.b, SCOPE_GUIDE_COLOR.a * parentAlpha);
            batch.draw(
                getScopeGuideTexture(),
                getX() + (getPadLeft() * 0.5f) - (SCOPE_GUIDE_WIDTH * 0.5f),
                getY() + SCOPE_GUIDE_INSET,
                SCOPE_GUIDE_WIDTH,
                Math.max(0f, getHeight() - (SCOPE_GUIDE_INSET * 2f))
            );
            batch.setPackedColor(previousColor);
            super.draw(batch, parentAlpha);
        }
    }

    private interface InputNode {
        Object toValue();
    }

    private static final class ObjectInputNode implements InputNode {
        private final Map<String, InputNode> children = new LinkedHashMap<>();

        void put(String key, InputNode value) {
            children.put(key, value);
        }

        @Override
        public Object toValue() {
            Map<String, Object> values = new LinkedHashMap<>();
            for (Map.Entry<String, InputNode> entry : children.entrySet()) {
                values.put(entry.getKey(), entry.getValue().toValue());
            }
            return values;
        }
    }

    private static final class PrimitiveInputNode implements InputNode {
        private final SchemaFieldType type;
        private final VisTextField textField;

        PrimitiveInputNode(SchemaFieldType type, VisTextField textField) {
            this.type = type;
            this.textField = textField;
        }

        @Override
        public Object toValue() {
            String text = textField.getText();
            if (text == null || text.isBlank()) {
                return "";
            }

            return switch (type) {
                case INT -> Integer.parseInt(text.trim());
                case FLOAT -> Float.parseFloat(text.trim());
                default -> text;
            };
        }
    }

    private static final class BooleanInputNode implements InputNode {
        private final VisCheckBox checkBox;

        BooleanInputNode(VisCheckBox checkBox) {
            this.checkBox = checkBox;
        }

        @Override
        public Object toValue() {
            return checkBox.isChecked();
        }
    }

    private static final class EnumInputNode implements InputNode {
        private final VisSelectBox<String> selectBox;

        EnumInputNode(VisSelectBox<String> selectBox) {
            this.selectBox = selectBox;
        }

        @Override
        public Object toValue() {
            return selectBox.getSelected();
        }
    }
}
