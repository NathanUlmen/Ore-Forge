package ore.forge.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.glutils.VertexArray;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import ore.forge.Input.CameraController3D;

import java.util.ArrayList;

public class Gameplay3D implements Screen {
    private final PerspectiveCamera camera;
    private final ModelBatch modelBatch;
    private final Environment environment;
    private final CameraController3D cameraController3D;
    private final ArrayList<ModelInstance> modelInstances;

    private Model model;
    private ModelInstance instance;

    public Gameplay3D() {
        //Config camera
        camera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.near = 1f; //Min distance can see/draw
        camera.far = 1000f; //Max distance can see/draw
        camera.position.set(10, 10, 10);
        camera.lookAt(0, 0, 0);

        //Config cameraController;
        cameraController3D = new CameraController3D(camera);

        //Config ModelBatch
        modelBatch = new ModelBatch();

        //Config Model Instance list
        modelInstances = new  ArrayList<>();

        //Config Environment
        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, .8f, .8f, .8f, 1f));
        environment.add(new DirectionalLight().set(1f, 1f, 1f, -1f, -0.8f, -0.2f));

        //create cube and apply transform
        Matrix4 transform = new Matrix4();
        model = createBox(5, 5, 5, Color.PINK);
        var instance = new ModelInstance(model);
        transform.translate(0, 2.5f,0);
        instance.transform.set(transform);
        modelInstances.add(instance);

        //create plane and apply transform
        instance = new ModelInstance(createBox(50, 0.01f , 50));
        var t2 = new Matrix4();
        t2.translate(0, 0, 0);
        instance.transform.set(t2);
        modelInstances.add(instance);

    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        cameraController3D.update();
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        camera.update();
        modelBatch.begin(camera);
        for (var instance : modelInstances) {
            modelBatch.render(instance, environment);
        }
        modelBatch.end();
        System.out.println(Gdx.graphics.getFramesPerSecond());
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

    }

    private Model createBox(float width, float height, float depth, Color color) {
        ModelBuilder builder = new ModelBuilder();
        return builder.createBox(width, height, depth,
            new Material(ColorAttribute.createDiffuse(color)),
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
    }

    private Model createBox(float width, float height, float depth) {
        return createBox(width, height, depth, Color.GREEN);
    }


}
