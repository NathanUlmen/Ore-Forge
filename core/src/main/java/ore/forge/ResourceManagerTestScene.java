package ore.forge;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Vector3;
import ore.forge.engine.Sizeable;
import ore.forge.engine.definitions.AssetType;
import ore.forge.engine.resources.AssetID;
import ore.forge.engine.resources.CpuAssetData;
import ore.forge.engine.resources.GpuResource;
import ore.forge.engine.resources.ResourceHandle;
import ore.forge.engine.resources.ResourceManager;
import ore.forge.engine.render.RenderPart;
import ore.forge.engine.render.Renderer;
import ore.forge.engine.render.passes.BasicRenderPass;
import ore.forge.game.input.FreeCamController;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Fuzz tests the {@link ResourceManager} by randomly acquiring and releasing
 * assets from both the CPU and GPU. Will occasionally flush the CPU cache.
 */
public final class ResourceManagerTestScene implements Screen {
    private static final String LOG_TAG = ResourceManagerTestScene.class.getSimpleName();
    private static final String SEED_PROPERTY = "ore.forge.resourceManagerTestSeed";
    private static final float OPERATIONS_PER_SECOND = 100f;
    private static final int MAX_OPERATIONS_PER_FRAME = 8;
    private static final float LOG_INTERVAL_SECONDS = 2f;
    private static final float CACHE_LIMIT_CYCLE_SECONDS = 12;
    private static final float GPU_RELOAD_INTERVAL_SECONDS = 8;
    private static final float GPU_RELOAD_GAP_SECONDS = 0.5f;
    private static final float TIME_SCALE = 1f;
    private static final long NORMAL_CPU_CACHE_BYTES = 100 * Sizeable.MB;
    private static final int DEFAULT_SEED = 0x524D5453;

    private static final List<String> GLTF_FIXTURES = List.of(
        "models/Cube.gltf",
        "models/Wedge.gltf",
        "models/Emerald.gltf",
        "models/Sphere.gltf",
        "models/texture_test.glb",
        "models/BoomBox.glb"
    );

    private final ResourceManager resourceManager;
    private final Random random;
    private final ArrayList<AssetID> stressAssetIds = new ArrayList<>();
    private final ArrayList<AssetID> meshAssetIds = new ArrayList<>();
    private final ArrayList<AssetID> textureAssetIds = new ArrayList<>();
    private final ArrayList<ResourceHandle<CpuAssetData>> activeHandles = new ArrayList<>();
    private final ArrayList<ResourceHandle<CpuAssetData>> pendingHandles = new ArrayList<>();
    private final ArrayList<RenderableAsset> renderableAssets = new ArrayList<>();

    private final Camera camera;
    private final FreeCamController cameraController;
    private final Renderer renderer;
    private final BasicRenderPass basicRenderPass;
    private float operationAccumulator;
    private float logAccumulator;
    private float cacheLimitAccumulator;
    private float gpuReloadAccumulator;
    private float gpuReloadGapRemaining;
    private float rotation;
    private long operationCount;
    private long acquireCount;
    private long releaseCount;
    private long readyAcquireCount;
    private long largestObservedAssetBytes;
    private long cacheEvictionCycleCount;
    private long gpuReloadCycleCount;
    private long gpuAcquireCount;
    private long gpuReleaseCount;
    private long frameSampleCount;
    private double frameTimeTotalMs;
    private double frameTimeMaxMs;
    private double frameDurationTotalSeconds;
    private long currentCacheLimitBytes = NORMAL_CPU_CACHE_BYTES;
    private boolean evictionMode;
    private final long seed;

    public ResourceManagerTestScene(ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
        this.seed = readSeed();
        this.random = new Random(seed);

        importGltfFixtures();
        collectStressAssets();
        if (meshAssetIds.isEmpty() || textureAssetIds.isEmpty()) {
            throw new IllegalStateException("ResourceManagerTestScene requires mesh and texture assets.");
        }

        camera = new PerspectiveCamera(67f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(0f, 5f, 24f);
        camera.lookAt(0f, 1f, 0f);
        camera.near = 0.1f;
        camera.far = 2000f;
        camera.up.set(Vector3.Y);
        camera.update(true);
        cameraController = new FreeCamController((PerspectiveCamera) camera);

        renderer = new Renderer(resourceManager);
        basicRenderPass = new BasicRenderPass();
        renderer.addRenderPass(basicRenderPass);
        createRenderables();

        Gdx.app.log(LOG_TAG, "Starting resource stress test with seed=" + seed
            + ", assets=" + stressAssetIds.size());
    }

    @Override
    public void render(float delta) {
        long frameStartNanos = System.nanoTime();
        float realDelta = delta;
        delta *= TIME_SCALE;
        resourceManager.synchronize();
        promoteReadyHandles();
        cameraController.update(realDelta);
        camera.update();
        updateRenderTransforms(delta);
        updateCacheLimit(delta);
        updateGpuResources(delta);

        Gdx.gl.glClearColor(0.04f, 0.05f, 0.08f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
        renderer.render(renderableParts(), camera);
        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);

        operationAccumulator += delta * OPERATIONS_PER_SECOND;
        int operations = Math.min(MAX_OPERATIONS_PER_FRAME, (int) operationAccumulator);
        operationAccumulator -= operations;

        for (int i = 0; i < operations; i++) {
            runRandomOperation();
        }

        recordFrameTiming(realDelta, frameStartNanos);

        logAccumulator += realDelta;
        if (logAccumulator >= LOG_INTERVAL_SECONDS) {
            logAccumulator = 0f;
            logStatus();
        }
    }

    private void runRandomOperation() {
        operationCount++;

        if (!activeHandles.isEmpty() && random.nextFloat() < 0.55f) {
            releaseRandomHandle();
        } else {
            acquireRandomAsset();
        }
    }

    private void acquireRandomAsset() {
        AssetID id = stressAssetIds.get(random.nextInt(stressAssetIds.size()));
        ResourceHandle<CpuAssetData> handle = resourceManager.acquireCpuDataAsync(id);
        acquireCount++;

        if (handle.isReady()) {
            observeAssetSize(handle);
            readyAcquireCount++;
            activeHandles.add(handle);
        } else {
            pendingHandles.add(handle);
        }
    }

    private void releaseRandomHandle() {
        int index = random.nextInt(activeHandles.size());
        ResourceHandle<CpuAssetData> handle = activeHandles.remove(index);
        observeAssetSize(handle);
        ensureCacheCanStore(handle);
        resourceManager.releaseCpuAsset(handle);
        releaseCount++;
    }

    private void promoteReadyHandles() {
        for (int i = pendingHandles.size() - 1; i >= 0; i--) {
            ResourceHandle<CpuAssetData> handle = pendingHandles.get(i);
            if (handle.isReady()) {
                pendingHandles.remove(i);
                observeAssetSize(handle);
                activeHandles.add(handle);
            }
        }
    }

    private void updateCacheLimit(float delta) {
        cacheLimitAccumulator += delta;
        if (cacheLimitAccumulator < CACHE_LIMIT_CYCLE_SECONDS) {
            return;
        }

        cacheLimitAccumulator = 0f;
        evictionMode = !evictionMode;
        if (evictionMode) {
            currentCacheLimitBytes = Math.max(1L, largestObservedAssetBytes);
            cacheEvictionCycleCount++;
            resourceManager.setCpuCacheMaxBytes(currentCacheLimitBytes);
            resourceManager.synchronize();
            Gdx.app.log(LOG_TAG, "Enabling CPU cache eviction mode at "
                + currentCacheLimitBytes + " bytes");
        } else {
            currentCacheLimitBytes = NORMAL_CPU_CACHE_BYTES;
            resourceManager.setCpuCacheMaxBytes(NORMAL_CPU_CACHE_BYTES);
            resourceManager.synchronize();
            Gdx.app.log(LOG_TAG, "Restoring CPU cache limit to "
                + NORMAL_CPU_CACHE_BYTES + " bytes");
        }
    }

    private void observeAssetSize(ResourceHandle<CpuAssetData> handle) {
        CpuAssetData data = resourceManager.getCpuAsset(handle.handle());
        if (data != null) {
            largestObservedAssetBytes = Math.max(largestObservedAssetBytes, data.sizeInBytes());
        }
    }

    private void ensureCacheCanStore(ResourceHandle<CpuAssetData> handle) {
        if (!evictionMode) {
            return;
        }

        CpuAssetData data = resourceManager.getCpuAsset(handle.handle());
        if (data != null && data.sizeInBytes() > currentCacheLimitBytes) {
            currentCacheLimitBytes = data.sizeInBytes();
            resourceManager.setCpuCacheMaxBytes(currentCacheLimitBytes);
            resourceManager.synchronize();
        }
    }

    private void importGltfFixtures() {
        for (String fixture : GLTF_FIXTURES) {
            Path path = Gdx.files.internal(fixture).file().toPath();
            resourceManager.importGltf(path);
        }
    }

    private void collectStressAssets() {
        for (AssetID id : resourceManager.getAssetIDs()) {
            AssetType type = resourceManager.getAssetType(id);
            if (type == AssetType.MESH || type == AssetType.TEXTURE) {
                stressAssetIds.add(id);
                if (type == AssetType.MESH) {
                    meshAssetIds.add(id);
                } else {
                    textureAssetIds.add(id);
                }
            }
        }
    }

    private void createRenderables() {
        AssetID textureId = textureAssetIds.getFirst();
        int columns = Math.max(1, (int) Math.ceil(Math.sqrt(meshAssetIds.size())));
        float spacing = 5f;
        float center = (columns - 1) * spacing * 0.5f;

        for (int i = 0; i < meshAssetIds.size(); i++) {
            int row = i / columns;
            int column = i % columns;
            float x = column * spacing - center;
            float z = row * spacing - center;

            ResourceHandle<GpuResource> meshHandle = resourceManager.acquireGpuResourceAsync(meshAssetIds.get(i));
            ResourceHandle<GpuResource> textureHandle = resourceManager.acquireGpuResourceAsync(textureId);
            RenderPart part = RenderPart.defaultRenderPart(meshHandle);
            part.material.baseColorTexture = textureHandle;
            renderableAssets.add(new RenderableAsset(
                part,
                meshAssetIds.get(i),
                textureId,
                x,
                z,
                i
            ));
        }
    }

    private void updateGpuResources(float delta) {
        if (gpuReloadGapRemaining > 0f) {
            gpuReloadGapRemaining -= delta;
            if (gpuReloadGapRemaining <= 0f) {
                reacquireGpuResources();
            }
            return;
        }

        gpuReloadAccumulator += delta;
        if (gpuReloadAccumulator < GPU_RELOAD_INTERVAL_SECONDS) {
            return;
        }

        gpuReloadAccumulator = 0f;
        releaseGpuResourcesForReload();
        gpuReloadGapRemaining = GPU_RELOAD_GAP_SECONDS;
        gpuReloadCycleCount++;
    }

    private void releaseGpuResourcesForReload() {
        for (RenderableAsset renderable : renderableAssets) {
            if (renderable.part.meshHandle != null) {
                resourceManager.releaseGpuResource(renderable.part.meshHandle);
                gpuReleaseCount++;
            }
            if (renderable.part.material.baseColorTexture != null) {
                resourceManager.releaseGpuResource(renderable.part.material.baseColorTexture);
                gpuReleaseCount++;
            }
            renderable.part.meshHandle = null;
            renderable.part.material.baseColorTexture = null;
        }
        resourceManager.synchronize();
        Gdx.app.log(LOG_TAG, "Released GPU render handles; rendering gap started");
    }

    private void reacquireGpuResources() {
        for (RenderableAsset renderable : renderableAssets) {
            renderable.part.meshHandle = resourceManager.acquireGpuResourceAsync(renderable.meshId);
            renderable.part.material.baseColorTexture = resourceManager.acquireGpuResourceAsync(renderable.textureId);
            gpuAcquireCount += 2;
        }
        Gdx.app.log(LOG_TAG, "Reacquired GPU render handles; waiting for uploads");
    }

    private void updateRenderTransforms(float delta) {
        rotation += delta * 20f;
        for (RenderableAsset renderable : renderableAssets) {
            renderable.part.transform.idt()
                .setToTranslation(renderable.x, 1f, renderable.z)
                .rotate(Vector3.Y, rotation + renderable.index * 15f)
                .scale(1.8f, 1.8f, 1.8f);
        }
    }

    private List<RenderPart> renderableParts() {
        ArrayList<RenderPart> parts = new ArrayList<>(renderableAssets.size());
        for (RenderableAsset renderable : renderableAssets) {
            if (renderable.part.meshHandle != null
                && renderable.part.material.baseColorTexture != null
                && renderable.part.meshHandle.isReady()
                && renderable.part.material.baseColorTexture.isReady()) {
                parts.add(renderable.part);
            }
        }
        return parts;
    }

    private void logStatus() {
        double averageFrameTimeMs = frameSampleCount == 0
            ? 0d
            : frameTimeTotalMs / frameSampleCount;
        double observedFps = frameDurationTotalSeconds <= 0d
            ? 0d
            : frameSampleCount / frameDurationTotalSeconds;

        Gdx.app.log(LOG_TAG,
            "ops=" + operationCount
                + ", acquires=" + acquireCount
                + ", releases=" + releaseCount
                + ", ready acquisitions=" + readyAcquireCount
                + ", active handles=" + activeHandles.size()
                + ", pending handles=" + pendingHandles.size()
                + ", queued tasks=" + resourceManager.queuedTaskCount()
                + ", manager active CPU resources=" + resourceManager.activeCpuResources()
                + ", cache limit=" + currentCacheLimitBytes
                + ", eviction cycles=" + cacheEvictionCycleCount
                + ", GPU reload cycles=" + gpuReloadCycleCount
                + ", GPU acquires=" + gpuAcquireCount
                + ", GPU releases=" + gpuReleaseCount
                + String.format(
                    ", frame avg=%.2fms, frame max=%.2fms, fps=%.2f, samples=%d",
                    averageFrameTimeMs,
                    frameTimeMaxMs,
                    observedFps,
                    frameSampleCount
                )
                + ", seed=" + seed);

        frameSampleCount = 0;
        frameTimeTotalMs = 0d;
        frameTimeMaxMs = 0d;
        frameDurationTotalSeconds = 0d;
    }

    private void recordFrameTiming(float delta, long frameStartNanos) {
        double frameTimeMs = (System.nanoTime() - frameStartNanos) / 1_000_000d;
        frameSampleCount++;
        frameTimeTotalMs += frameTimeMs;
        frameTimeMaxMs = Math.max(frameTimeMaxMs, frameTimeMs);
        frameDurationTotalSeconds += Math.max(0f, delta);
    }

    private long readSeed() {
        String configuredSeed = System.getProperty(SEED_PROPERTY);
        if (configuredSeed == null || configuredSeed.isBlank()) {
            return DEFAULT_SEED;
        }

        try {
            return Long.decode(configuredSeed);
        } catch (NumberFormatException exception) {
            Gdx.app.error(LOG_TAG,
                "Invalid " + SEED_PROPERTY + " value '" + configuredSeed
                    + "'; using default seed " + DEFAULT_SEED,
                exception);
            return DEFAULT_SEED;
        }
    }

    @Override
    public void dispose() {
        resourceManager.synchronize();
        promoteReadyHandles();

        for (ResourceHandle<CpuAssetData> handle : activeHandles) {
            resourceManager.releaseCpuAsset(handle);
        }
        activeHandles.clear();

        for (ResourceHandle<CpuAssetData> handle : pendingHandles) {
            if (handle.isReady()) {
                resourceManager.releaseCpuAsset(handle);
            }
        }
        pendingHandles.clear();

        for (RenderableAsset renderable : renderableAssets) {
            if (renderable.part.meshHandle != null) {
                resourceManager.releaseGpuResource(renderable.part.meshHandle);
            }
            if (renderable.part.material.baseColorTexture != null) {
                resourceManager.releaseGpuResource(renderable.part.material.baseColorTexture);
            }
        }
        renderableAssets.clear();
        resourceManager.synchronize();
    }

    private static final class RenderableAsset {
        private final RenderPart part;
        private final AssetID meshId;
        private final AssetID textureId;
        private final float x;
        private final float z;
        private final int index;

        private RenderableAsset(
            RenderPart part,
            AssetID meshId,
            AssetID textureId,
            float x,
            float z,
            int index
        ) {
            this.part = part;
            this.meshId = meshId;
            this.textureId = textureId;
            this.x = x;
            this.z = z;
            this.index = index;
        }
    }

    @Override
    public void show() {
    }

    @Override
    public void resize(int width, int height) {
        if (camera instanceof PerspectiveCamera perspectiveCamera) {
            perspectiveCamera.viewportWidth = width;
            perspectiveCamera.viewportHeight = height;
            perspectiveCamera.update(true);
        }
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
}
