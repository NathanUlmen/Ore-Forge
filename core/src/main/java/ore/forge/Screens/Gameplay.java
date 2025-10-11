package ore.forge.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import ore.forge.*;
import ore.forge.Items.Experimental.FurnaceBlueprint;
import ore.forge.Items.Experimental.ItemBlueprint;
import ore.forge.Items.Experimental.UpgraderBlueprint;

import java.util.ArrayDeque;
import java.util.Deque;

public class Gameplay extends CustomScreen {
    private final GameWorld gameWorld;
    private final OrthographicCamera camera;
    private final ShapeRenderer shapeRenderer;
    private final ShapeRenderer shapeDrawer;
    private final Body player, conveyor;
    private final ItemBlueprint testItemBlueprint;
    private final Deque<Body> bodyStack;
    private float rotation;

    public Gameplay(OreForge game, ItemManager itemManager, GameWorld gameWorld) {
        super(game, itemManager);
        this.gameWorld = gameWorld;

        // Camera setup
        camera = new OrthographicCamera(16, 9);
        camera.position.set(0, 0, 0);
        camera.zoom = 5f;
        camera.update();

        // ShapeDrawer setup
        shapeRenderer = new ShapeRenderer();
        shapeRenderer.setAutoShapeType(true);
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        TextureRegion whitePixel = new TextureRegion(new Texture(pixmap));
        pixmap.dispose();
        shapeDrawer = new ShapeRenderer();

        // Create player and static objects
        Ore ore = new Ore();
        ore.setBody(createDynamicBox(-0,-0, 1,1));
        player = ore.getBody();
        ore.setOreValue(10);

        FileHandle handle = Gdx.files.local("testItem.json");
        JsonReader reader = new JsonReader();
        JsonValue value = reader.parse(handle);
        value = value.child;

        testItemBlueprint = new UpgraderBlueprint(value);

        var furnace = new FurnaceBlueprint(value.next);
        Body testFurnace = furnace.bindBehaviors();
        testFurnace.setTransform(new Vector2(5, 0), 0);

//        var dropper = new ItemBlueprint(value.next.next);
//        Body testDropper = dropper.spawnItem();
//        testDropper.setTransform(new Vector2(10, 0), 0);


        conveyor = testItemBlueprint.bindBehaviors();
        System.out.println(conveyor);
        assert conveyor != null;
        conveyor.setTransform(conveyor.getPosition(), 90 * MathUtils.degreesToRadians);
        bodyStack = new ArrayDeque<>();
        rotation = 0;
    }

    private Body createDynamicBox(float x, float y, float width, float height) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(x, y);

        Body body = gameWorld.physicsWorld().createBody(bodyDef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(width / 2f, height / 2f);
        float[] shapeDef = new float[] { 0, -1, 1.25f, .75f, 1,1 , -1, 1, -1.25f, .75f};

        for (int i = 0; i < shapeDef.length; i++) {
            shapeDef[i] *= .75f;
        }

        shape.set(shapeDef);


        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 1f;
        fixtureDef.friction = 0.4f;
        fixtureDef.restitution = 0.1f;

        var fixture = body.createFixture(fixtureDef);
        var ore = new Ore();
        ore.setOreValue(2);
        fixture.setUserData(ore);
        shape.dispose();

        return body;
    }

    @Override
    public void render(float delta) {
        handleInput();

        gameWorld.physicsWorld().step(delta, 6, 3);
        gameWorld.updateTouchingPairs();
        TimerUpdater.update(delta);
        camera.update();
        camera.position.set(new Vector3(player.getPosition().x, player.getPosition().y, 0));

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        shapeRenderer.setProjectionMatrix(camera.combined);

        // Draw all bodies individually
        var bodies = new Array<Body>();
        gameWorld.physicsWorld().getBodies(bodies);
        for (Body body : bodies) {
            drawBox(body);
        }
        drawBox(player);

        shapeRenderer.end();
    }

    private void handleInput() {
        Vector2 velocity = new Vector2(0, 0);
        float speed = 5f;

        if (Gdx.input.isKeyPressed(Input.Keys.W)) velocity.y += speed;
        if (Gdx.input.isKeyPressed(Input.Keys.S)) velocity.y -= speed;
        if (Gdx.input.isKeyPressed(Input.Keys.A)) velocity.x -= speed;
        if (Gdx.input.isKeyPressed(Input.Keys.D)) velocity.x += speed;

        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            rotation += 90;
            rotation %= 360;
//            conveyor.setTransform(conveyor.getPosition().x, conveyor.getPosition().y, (conveyor.getAngle() + 5 * MathUtils.degreesToRadians));
        }

        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            var temp = testItemBlueprint.bindBehaviors();
            float x = Gdx.input.getX();
            float y = Gdx.input.getY();
            Vector3 mouseWorld = camera.unproject(new Vector3(x, y, 0));
            System.out.println(mouseWorld);
            temp.setTransform(MathUtils.round(mouseWorld.x), MathUtils.round(mouseWorld.y), rotation * MathUtils.degreesToRadians);
            bodyStack.push(temp);
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.Z)) {
            var temp = bodyStack.pop();
            GameWorld.getInstance().physicsWorld().destroyBody(temp);
        }

        player.applyForceToCenter(velocity, true);
    }

    private void drawBox(Body body) {
        // Random but consistent color per body
        int id = System.identityHashCode(body);
        Color color = new Color(
            ((id >> 0) & 0xFF) / 255f,
            ((id >> 8) & 0xFF) / 255f,
            ((id >> 16) & 0xFF) / 255f,
            1f
        );

        for (Fixture fixture : body.getFixtureList()) {
            Shape shape = fixture.getShape();
            if (!(shape instanceof PolygonShape polygon)) continue;

            int vertexCount = polygon.getVertexCount();
            Vector2[] vertices = new Vector2[vertexCount];
            for (int i = 0; i < vertexCount; i++) {
                vertices[i] = new Vector2();
                polygon.getVertex(i, vertices[i]);
                // Transform local vertex to world coords
                body.getTransform().mul(vertices[i]);
            }

            // Filled polygon
            shapeRenderer.setColor(color);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.triangle(
                vertices[0].x, vertices[0].y,
                vertices[1].x, vertices[1].y,
                vertices[2].x, vertices[2].y
            );
            if (vertexCount == 4) {
                shapeRenderer.triangle(
                    vertices[0].x, vertices[0].y,
                    vertices[2].x, vertices[2].y,
                    vertices[3].x, vertices[3].y
                );
            }
            shapeRenderer.end();

            // Outline
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(Color.WHITE);
            for (int i = 0; i < vertexCount; i++) {
                Vector2 v1 = vertices[i];
                Vector2 v2 = vertices[(i + 1) % vertexCount];
                shapeRenderer.line(v1, v2);
            }
            shapeRenderer.end();
        }
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
    }
}
