package ore.forge;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalShadowLight;
import net.mgsx.gltf.loaders.glb.GLBAssetLoader;
import net.mgsx.gltf.loaders.gltf.GLTFAssetLoader;
import net.mgsx.gltf.scene3d.scene.Scene;
import net.mgsx.gltf.scene3d.scene.SceneAsset;
import net.mgsx.gltf.scene3d.scene.SceneManager;
import ore.forge.Input3D.IsometricCameraController;

public class GameScreen implements Screen {

    private PerspectiveCamera camera;

    private AssetManager assetManager;
    private Scene scene;

    private SceneManager sceneManager;
    private DirectionalShadowLight shadowLight;
    IsometricCameraController cameraController;


    @Override
    public void show() {

        // ---- Camera ----
        camera = new PerspectiveCamera(67,
            Gdx.graphics.getWidth(),
            Gdx.graphics.getHeight());
        camera.position.set(3f, 3f, 3f);
        camera.lookAt(0, 1, 0);
        camera.near = 0.1f;
        camera.far = 100f;
        camera.update();

        // ---- Scene Manager (PBR renderer) ----
        sceneManager = new SceneManager();
        sceneManager.setCamera(camera);

        // ---- Shadow-casting light ----
        shadowLight = new DirectionalShadowLight(
            4096, 4096,      // shadow map resolution
            30f, 30f,        // viewport size
            .1f, 90f         // near / far
        );

        sceneManager.environment.set(
            new ColorAttribute(ColorAttribute.AmbientLight, 0.2f, 0.2f, 0.2f, 1f)
        );

        shadowLight.set(1f, 1f, 1f, -1f, -2f, -1f);
        sceneManager.environment.add(shadowLight);
        sceneManager.environment.shadowMap = shadowLight;

        // ---- Asset Manager + GLTF Loader ----
        assetManager = new AssetManager();
        assetManager.setLoader(SceneAsset.class, ".gltf", new GLTFAssetLoader());
        assetManager.setLoader(SceneAsset.class, ".glb", new GLBAssetLoader());

        assetManager.load("models/BasicUpgrader.glb", SceneAsset.class);


        cameraController = new IsometricCameraController(camera);
    }

    @Override
    public void render(float delta) {
        cameraController.update(delta);
        camera.update();
        shadowLight.set(1, 1, 1,  -1, -1.5f, -1f);


        if (scene == null) {
            if (assetManager.update()) {
                SceneAsset asset = assetManager.get("models/BasicUpgrader.glb", SceneAsset.class);
                scene = new Scene(asset.scene);
                scene.modelInstance.transform.setToTranslation(0f, 0f, 0f);
                scene.modelInstance.calculateTransforms();
                sceneManager.addScene(scene);
                scene.modelInstance.materials.forEach(mat -> {
                    mat.set(ColorAttribute.createDiffuse(1, 1, 1, 1));
                });
                scene.modelInstance.nodes.forEach(node -> node.parts.forEach(part -> {
                    part.enabled = true;
                }));
            } else {
                return;
            }
        }


//        ModelBatch shadowBatch = new ModelBatch(new DepthShaderProvider());
//        shadowLight.begin(Vector3.Zero, camera.direction);
//        shadowBatch.begin(shadowLight.getCamera());
//        for (RenderableProvider provider: sceneManager.getRenderableProviders()) {
//            shadowBatch.render(provider);
//        }
//        shadowBatch.end();
//        shadowLight.end();


        Gdx.gl.glClearColor(0.0f, 00f, 0.0f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        sceneManager.update(delta);
        sceneManager.render();
    }


    @Override
    public void resize(int w, int h) {
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
        if (sceneManager != null) sceneManager.dispose();
    }

}

