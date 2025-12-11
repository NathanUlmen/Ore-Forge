package ore.forge;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ui.Image;

public class IconCreator {
    private final ModelBatch modelBatch;
    private final Environment environment;
    private final PerspectiveCamera camera;

    private int renderResolution;

    public IconCreator() {
        renderResolution = 256;

        modelBatch = new ModelBatch();

        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 1f, 1f, 1f, 1f));
        environment.add(new DirectionalLight().set(1f, 1f, 1f, -1f, -0.5f, -0.2f));

        camera = new PerspectiveCamera(67, renderResolution, renderResolution);
        camera.position.set(2f, 2f, 2f);
        camera.lookAt(0, 0, 0);
        camera.near = 0.1f;
        camera.far = 100f;
        camera.update();
    }

    public Image renderIcon(VisualComponent visualComponent) {
        visualComponent.modelInstance.transform.setTranslation(new Vector3(0,0,0));


        FrameBuffer fbo = new FrameBuffer(
            Pixmap.Format.RGBA8888,
            renderResolution,
            renderResolution,
            true
        );

        fbo.begin();

        // Clear background (white / soft yellow)
        Gdx.gl.glViewport(0, 0, renderResolution, renderResolution);
        Gdx.gl.glClearColor(1f, 1f, 0.8f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        // --- Render model ---
        ModelInstance instance = visualComponent.modelInstance;

        modelBatch.begin(camera);
        modelBatch.render(instance, environment);
        modelBatch.end();

        // --- Extract Pixmap ---
        Pixmap pixmap = Pixmap.createFromFrameBuffer(0, 0, renderResolution, renderResolution);

        fbo.end();
        fbo.dispose();

        // Convert to LibGDX Image if required:
        Texture texture = new Texture(pixmap);
        Image image = new Image(texture);

        pixmap.dispose();

        return image;
    }
}

