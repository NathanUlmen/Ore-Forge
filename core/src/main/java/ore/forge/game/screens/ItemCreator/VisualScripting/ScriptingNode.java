package ore.forge.game.screens.ItemCreator.VisualScripting;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;
import com.badlogic.gdx.utils.JsonValue;
import ore.forge.game.expressions.Condition;
import ore.forge.game.expressions.Function;
import ore.forge.game.expressions.Operands.NumericOreProperties;
import ore.forge.game.expressions.Operators.NumericOperator;
import ore.forge.game.screens.ItemCreator.VisualScripting.Forums.ConstantHiddenForum;
import ore.forge.game.behaviors.UpgradeStrategies.BasicUpgrade;
import ore.forge.game.behaviors.UpgradeStrategies.BundledUpgrade;
import ore.forge.game.behaviors.UpgradeStrategies.ConditionalUpgrade;
import ore.forge.game.behaviors.UpgradeStrategies.UpgradeStrategy;
import ore.forge.game.ui.UIHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Nathan Ulmen
 * A scripting node is a tree like structure.
 * A scripting node will be able to create what it models.
 * So for example if you have a tree of ScriptingNodes of type UpgradeStrategy it will return an
 * UpgradeStrategy by calling create() recursively on itself
 * <p>
 * ScriptingNodes should be able to add fields
 * Field's are key value pairs that correspond to a property of the Object the ScriptingNode is modeling.
 * Fields can take the form of TextInputFields, Drop DownMenus, toggles/checkboxes Or they can accept/require
 * other ScriptingNodes
 * Thus Fields will have a visual aspect to them.
 * <p>
 * Fields should be easily extendable to enable features for things like {@link Condition} and
 * {@link Function}
 * Fields should be able to validate themselves.
 */
public class ScriptingNode<E> extends Table {

    private String name, arrayName;
    private ScriptingNode<E> parentNode; //"input"
    private LinkBehavior<E> behavior; //Link behavior determines # of children, how they are added, and what should happen when added.
    private final List<ScriptingNode<E>> childNodes; //output
    private final ArrayList<Field> fields;
    private final JsonValue.ValueType type;
    private boolean selected;
    private static final float SNAP_DISTANCE = 100;
    private static float elapsedTime = 0; //used for highlight effect.

    public ScriptingNode(Field... fields) {
        this.childNodes = new ArrayList<>();
        this.fields = new ArrayList<>();
        for (Field field : fields) {
            var cell = addField(field);
            if (cell != null) {
                cell.expandX().fillX().row();
            }
        }
        this.type = null;
        this.setTouchable(Touchable.enabled);
        this.setBackground(UIHelper.createBorder(1000, Color.WHITE, Color.NAVY));

        this.addListener(new DragListener() {
            private final Vector2 dragOffset = new Vector2();

            @Override
            public void dragStart(InputEvent event, float x, float y, int pointer) {
                dragOffset.set(x, y);
            }

            @Override
            public void drag(InputEvent event, float x, float y, int pointer) {
                move(x, y, dragOffset);
            }

            @Override
            public void dragStop(InputEvent event, float x, float y, int pointer) {
                ScriptingNode<E> closestNode = getClosestNode();

                if (closestNode != null) {
                    System.out.println(closestNode.getX() + " " + closestNode.getY());
                    setPosition(closestNode.getX(), closestNode.getY() - getHeight());
                    closestNode.link(ScriptingNode.this);
                } else if (parentNode != null) {
                    ScriptingNode.this.unlink(parentNode);
                }
            }
        });

        this.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                toFront();
                select(true);
                return true;
            }


            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                select(false);
            }

        });
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        if (selected) {
            this.setColor(Color.WHITE.cpy().lerp(Color.NAVY, (float) (0.5 * (MathUtils.sin(MathUtils.PI2 * .5f * elapsedTime) + 1))));
            if (Gdx.input.isKeyJustPressed(Input.Keys.FORWARD_DEL) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                for (ScriptingNode<E> child : childNodes) {
                    child.remove();
//                    canvas.getScriptingNodes().remove(child);
                }
//                canvas.getScriptingNodes().remove(this);
                remove();
            }
        } else {
            this.setColor(Color.WHITE);
        }
    }

    private void select(boolean state) {
        selected = state;
        for (ScriptingNode<E> child : childNodes) {
            child.select(state);
        }
    }

    public static void updateElapsed(float delta) {
        elapsedTime += delta;
    }

    private ScriptingNode<E> getClosestNode() {
        ScriptingNode<E> closestNode = null;
        float closestDistance = SNAP_DISTANCE;

        for (Actor actor : getStage().getActors()) {
            if (actor instanceof ScriptingNode<?> node && node != ScriptingNode.this) {
                float dx = Math.abs(getX() - node.getX());
                float dy = Math.abs(getY() - node.getY());
                float distance = (float) Math.sqrt(dx * dx + dy * dy);

                if (distance < closestDistance) {
                    closestNode = (ScriptingNode<E>) node;
                    closestDistance = distance;
                }
            }
        }
        return closestNode;
    }

    private void move(float x, float y, Vector2 dragOffset) {
        moveBy(x - dragOffset.x, y - dragOffset.y);
        for (ScriptingNode<E> child : childNodes) {
            child.move(x, y, dragOffset);
        }
    }

    public ScriptingNode(List<Field> fields) {
        this(fields.toArray(new Field[0]));
    }

    public void setLinkBehavior(LinkBehavior<E> behavior) {
        this.behavior = behavior;
    }

    public ScriptingNode(JsonValue.ValueType type, String arrayName, Field... fields) {
        this.arrayName = arrayName;
        this.childNodes = new ArrayList<>();
        this.fields = new ArrayList<>();
        this.fields.addAll(Arrays.asList(fields));
        assert type == JsonValue.ValueType.array;
        this.type = type;
    }

    public JsonValue create() {
        JsonValue jsonValue = new JsonValue(JsonValue.ValueType.object);
        jsonValue.name = this.name;
        for (Field field : fields) {
            jsonValue.addChild(field.getValue());
        }
        JsonValue childAdder;
        if (type == JsonValue.ValueType.array) {
            childAdder = new JsonValue(JsonValue.ValueType.array);
            assert arrayName != null && !arrayName.isEmpty();
            childAdder.name = arrayName;
            jsonValue.addChild(childAdder);
        } else {
            childAdder = jsonValue;
        }
        for (ScriptingNode<E> child : childNodes) {
            childAdder.addChild(child.create());
        }
        return jsonValue;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the cell of the field added for method chaining
     */
    public Cell<Actor> addField(Field field) {
        assert field != null;
        if (!fields.contains(field)) {
            fields.add(field);
        }
        if (field.field instanceof Actor actor) {
            return this.add(actor);
        }
        return null;
    }

    public void link(ScriptingNode<E> child) {
        if (behavior != null) {
            this.behavior.onLink(this, child);
        }
    }

    public void unlink(ScriptingNode<E> toUnlink) {
        if (behavior != null) {
            behavior.onUnlink(toUnlink, this);
        }
    }

    public void addChild(ScriptingNode<E> child) {
        if (!childNodes.contains(child)) {
            childNodes.add(child);
            child.parentNode = this;
        }
    }

    public void addChild(ScriptingNode<E> child, String childName) {
        child.setName(childName);
        this.addChild(child);
    }

    public void removeChild(ScriptingNode<E> child) {
        this.childNodes.remove(child);
        child.parentNode = null;
    }

    public ScriptingNode<E> getParentNode() {
        return parentNode;
    }

    public ValidationResult<E> validateContent() {
        return validateContent(new ValidationResult<>());
    }

    private ValidationResult<E> validateContent(ValidationResult<E> result) {
        result.visit(this);
        for (Field field : fields) {
            if (!field.isValid()) {
                result.addError(field.getError());
                result.recordErrorNode(this);
            }
        }
        for (ScriptingNode<E> child : childNodes) {
            child.validateContent(result);
        }
        return result;
    }

    public List<ScriptingNode<E>> getChildNodes() {
        return childNodes;
    }

    public static class ValidationResult<E> {
        private final List<String> errors;
        private final List<ScriptingNode<E>> visitedNodes;
        private final List<ScriptingNode<E>> errorNodes;

        public ValidationResult() {
            errors = new ArrayList<>();
            visitedNodes = new ArrayList<>();
            errorNodes = new ArrayList<>();
        }

        public void addError(String error) {
            errors.add(error);
        }

        public List<String> getErrors() {
            return errors;
        }

        public void recordErrorNode(ScriptingNode<E> errorNode) {
            if (!errorNodes.contains(errorNode)) {
                errorNodes.add(errorNode);
            }
        }

        public void visit(ScriptingNode<E> node) {
            visitedNodes.add(node);
        }

        public boolean isValid() {
            return errors.isEmpty();
        }

        public List<ScriptingNode<E>> getErrorNodes() {
            return errorNodes;
        }

        public List<ScriptingNode<E>> getVisitedNodes() {
            return visitedNodes;
        }

    }

    public static class Field {
        private final String key;
        private final ForumField<?> field; //InputField for the ValueType, actual UI element

        public Field(String key, ForumField<?> field) {
            this.field = field;
            this.key = key;
        }

        public JsonValue getValue() {
            JsonValue jsonValue = new JsonValue(field.getType());
            jsonValue.name = key;
            Object value = field.value();
            switch (value) {
                case String s -> jsonValue.set(s);
                case Double d -> jsonValue.set(d, String.valueOf(d));
                case Long l -> jsonValue.set(l, String.valueOf(l));
                case Boolean b -> jsonValue.set(b);
                default -> throw new IllegalStateException("Unexpected value: " + value);
            }
            return jsonValue;
        }

        public boolean isValid() {
            return field.isValid();
        }

        public String getError() {
            return field.getError();
        }

        public ForumField<?> getField() {
            return this.field;
        }
    }

    private static ScriptingNode<UpgradeStrategy> createBundledNode() {
        return new ScriptingNode<>(JsonValue.ValueType.array, "upgrades", new Field("upgradeName", new ConstantHiddenForum(BundledUpgrade.class.getName())));
    }

    private static ScriptingNode<UpgradeStrategy> createConditionalNode(String condition) {
        ArrayList<Field> fields = new ArrayList<>();
        fields.add(new Field("upgradeName", new ConstantHiddenForum(ConditionalUpgrade.class.getName())));
        fields.add(new Field("condition", new ForumField<String>() {
            private String conditionString = condition;

            @Override
            public String value() {
                return condition;
            }

            @Override
            public String getError() {
                return "Failed to compile condition:" + conditionString;
            }

            @Override
            public boolean isValid() {
                try {
                    Condition.compile(conditionString);
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }

            @Override
            public JsonValue.ValueType getType() {
                return JsonValue.ValueType.stringValue;
            }
        }));

        return new ScriptingNode<>(fields);
    }

    private static ScriptingNode<UpgradeStrategy> createBasicUpgradeNode(Double modifier, String vtm, String operator) {
        ArrayList<Field> fields = new ArrayList<>();
        fields.add(new Field("upgradeName", new ConstantHiddenForum(BasicUpgrade.class.getName())));
        fields.add(new Field("modifier", new ForumField<Double>() {
            @Override
            public Double value() {
                return modifier;
            }

            @Override
            public String getError() {
                return "Invalid modifier";
            }

            @Override
            public boolean isValid() {
                return true;
            }

            @Override
            public JsonValue.ValueType getType() {
                return JsonValue.ValueType.doubleValue;
            }
        }));
        fields.add(new Field("valueToModify", new ForumField<String>() {

            @Override
            public String value() {
                return vtm;
            }

            @Override
            public String getError() {
                return "Invalid vtm";
            }

            @Override
            public boolean isValid() {
                return NumericOreProperties.isProperty(vtm);
            }

            @Override
            public JsonValue.ValueType getType() {
                return JsonValue.ValueType.stringValue;
            }
        }));
        fields.add(new Field("operator", new ForumField<String>() {

            @Override
            public String value() {
                return operator;
            }

            @Override
            public String getError() {
                return "Invalid operator " + operator;
            }

            @Override
            public boolean isValid() {
                if (NumericOperator.isOperator(operator)) {
                    return true;
                }
                try {
                    NumericOperator.valueOf(operator);
                    return true;
                } catch (IllegalArgumentException e) {
                    return false;
                }
            }

            @Override
            public JsonValue.ValueType getType() {
                return JsonValue.ValueType.stringValue;
            }
        }));

        return new ScriptingNode<>(fields);
    }


}
