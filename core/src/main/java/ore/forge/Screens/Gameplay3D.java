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
import ore.forge.Input.CameraController3D;

public class Gameplay3D implements Screen {
    private final PerspectiveCamera camera;
    private final ModelBatch  modelBatch;
    private final Environment environment;
    private CameraInputController cameraController;
    private final CameraController3D cameraController3D;

    private final Model model;
    private final ModelInstance instance;

    public Gameplay3D() {
        //Config camera
        camera  = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.near = 1f; //Min distance can see/draw
        camera.far = 1000f; //Max distance can see/draw
        camera.position.set(10, 10, 10);
        camera.lookAt(0, 0, 0);

        //Config cameraController;
//        cameraController = new CameraInputController(camera);
        cameraController3D = new CameraController3D(camera);

        //Config ModelBatch
        modelBatch = new ModelBatch();


        //Config Environment
        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, .8f, .8f, .8f, 1f));
        environment.add(new DirectionalLight().set(1f, 1f, 1f, -1f, -0.8f, -0.2f));


        // Create a test object to draw
        ModelBuilder builder = new ModelBuilder();
        model = builder.createBox(5,5,5,
            new Material(ColorAttribute.createDiffuse(Color.GREEN)),
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
        instance = new ModelInstance(model);


        Matrix4 transform = new Matrix4();
        transform = transform.translate(0,0,0);
        instance.transform.set(transform);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(cameraController);

    }

    @Override
    public void render(float delta) {
//        cameraController.update();
        cameraController3D.update();
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        camera.update();
        modelBatch.begin(camera); modelBatch.render(instance, environment);
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

}
