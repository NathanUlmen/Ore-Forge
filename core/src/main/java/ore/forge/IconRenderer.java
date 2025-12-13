package ore.forge;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.scenes.scene2d.ui.Image;

public class IconRenderer {
    private final ModelBatch modelBatch;
    private final Environment environment;
    private final PerspectiveCamera camera;
    private final int renderResolution;
    private final int saveResolution;

    public IconRenderer() {
        renderResolution = 8192;
        saveResolution = 1024;

        modelBatch = new ModelBatch();

        environment = new Environment();
        environment.set(
            new ColorAttribute(ColorAttribute.AmbientLight, 0.35f, 0.35f, 0.35f, 1f)
        );

        environment.add(new DirectionalLight().set(1f, 1f, 1f, -1f, -1f, -0.5f)); // key
        environment.add(new DirectionalLight().set(0.4f, 0.4f, 0.4f, 1f, -0.5f, 0.2f)); // fill
        environment.add(new DirectionalLight().set(0.2f, 0.2f, 0.2f, 0f, 1f, 0f)); // rim


        camera = new PerspectiveCamera(67, renderResolution, renderResolution);
        camera.position.set(2f, 2f, 2f);
        camera.lookAt(0, 0, 0);
        camera.near = 0.1f;
        camera.far = 100f;
        camera.update();
    }

    public Image renderIcon(VisualComponent visualComponent) {
        ModelInstance modelInstance = visualComponent.modelInstance;
        BoundingBox bb = modelInstance.calculateBoundingBox(new BoundingBox());

        //Center our model
        Vector3 center = bb.getCenter(new Vector3());
        modelInstance.transform.setTranslation(center.scl(-1)); //Shift by offsets to be centered

        Vector3 dimensions = bb.getDimensions(new Vector3());
        float maxDim = Math.max(dimensions.x,
            Math.max(dimensions.y, dimensions.z));

        //Position our camera to be looking at the model diagonally
        //TODO: Make it so big items look big by scaling non linearly or something
        camera.position.set(maxDim, maxDim * .75f, maxDim);
        camera.lookAt(Vector3.Zero); //look at item
        camera.update();

        FrameBuffer fbo = new FrameBuffer(
            Pixmap.Format.RGBA8888,
            renderResolution,
            renderResolution,
            true
        );

        fbo.begin();

        // Clear background (white / soft yellow)
        Gdx.gl.glViewport(0, 0, renderResolution, renderResolution);
//        Gdx.gl.glClearColor(1f, 1f, 0.8f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        ModelInstance instance = visualComponent.modelInstance;

        //Render model in our scene
        modelBatch.begin(camera);
        modelBatch.render(instance, environment);
        modelBatch.end();

        //Get pixmap from our buffer
        Pixmap pixmap = Pixmap.createFromFrameBuffer(0, 0, renderResolution, renderResolution);
        Pixmap downscaled = new Pixmap(saveResolution, saveResolution, pixmap.getFormat());
        pixmap.setFilter(Pixmap.Filter.BiLinear);
        downscaled.setFilter(Pixmap.Filter.BiLinear);
        downscaled.drawPixmap(pixmap,
            0, 0, pixmap.getWidth(), pixmap.getHeight(),
            0, 0, downscaled.getWidth(), downscaled.getHeight());

        toFile(downscaled, "test.png");

        fbo.end();
        fbo.dispose();

        //Convert to image
        Texture texture = new Texture(downscaled);
        Image image = new Image(texture);

        pixmap.dispose();
        downscaled.dispose();

        return image;
    }

    private static void toFile(Pixmap pixmap, String fileName) {
        int w = pixmap.getWidth();
        int h = pixmap.getHeight();

        //Flip vertically
        Pixmap flipped = new Pixmap(w, h, pixmap.getFormat());
        for (int y = 0; y < h; y++) {
            flipped.drawPixmap(
                pixmap,
                0, y, w, 1,
                0, h - y - 1, w, 1
            );
        }

        PixmapIO.writePNG(Gdx.files.local(fileName), flipped);
        flipped.dispose();
    }


}

