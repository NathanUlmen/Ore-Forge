package ore.forge.serialization;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import ore.forge.engine.Dispatcher;
import ore.forge.engine.definitions.AssetType;
import ore.forge.engine.resources.AssetID;
import ore.forge.engine.resources.GpuResource;
import ore.forge.engine.resources.ResourceHandle;
import ore.forge.engine.resources.ResourceManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class ResourceManagerGpuIntegrationTest {
    private static GpuTestContext gpuTestContext;

    @TempDir
    Path tmpDir;

    @BeforeAll
    static void startGpuTestContext() {
        gpuTestContext = GpuTestContext.start();
    }

    @AfterAll
    static void stopGpuTestContext() {
        if (gpuTestContext != null) {
            gpuTestContext.close();
        }
    }

    @Test
    void simpleLoadReturnsLoadedResource() throws Exception {
        runOnGpu(() -> {
            ResourceManager manager = newManager();
            ResourceHandle<GpuResource> resource = manager.acquireGpuResourceAsync(meshId(manager), null, null);
            await(manager, resource);
            assertTrue(resource.isReady());
            assertNotNull(manager.getGpuResource(resource.handle()));
            releaseOnGpu(manager, resource);
        });
    }

    @Test
    void spacedLoadUsesAlreadyLoadedResource() throws Exception {
        runOnGpu(() -> {
            ResourceManager manager = newManager();
            AssetID id = meshId(manager);
            ResourceHandle<GpuResource> first = manager.acquireGpuResourceAsync(id, null, null);
            await(manager, first);
            ResourceHandle<GpuResource> second = manager.acquireGpuResourceAsync(id, null, null);
            assertTrue(second.isReady());
            assertEquals(first.handle().identity(), second.handle().identity());
            assertSame(manager.getGpuResource(first.handle()), manager.getGpuResource(second.handle()));
            releaseOnGpu(manager, first);
            releaseOnGpu(manager, second);
        });
    }

    @Test
    void multipleRequestsShareInProgressLoad() throws Exception {
        runOnGpu(() -> {
            ResourceManager manager = newManager();
            AssetID id = meshId(manager);
            ResourceHandle<GpuResource> first = manager.acquireGpuResourceAsync(id, null, null);
            ResourceHandle<GpuResource> second = manager.acquireGpuResourceAsync(id, null, null);
            assertNotSame(first, second);
            assertEquals(first.handle().identity(), second.handle().identity());
            await(manager, first);
            await(manager, second);
            assertSame(manager.getGpuResource(first.handle()), manager.getGpuResource(second.handle()));
            releaseOnGpu(manager, first);
            releaseOnGpu(manager, second);
        });
    }

    @Test
    void loadReleaseLoadCreatesNewResource() throws Exception {
        runOnGpu(() -> {
            ResourceManager manager = newManager();
            AssetID id = meshId(manager);
            ResourceHandle<GpuResource> first = manager.acquireGpuResourceAsync(id, null, null);
            await(manager, first);
            long firstIdentity = first.handle().identity();
            releaseOnGpu(manager, first);
            assertEquals(0, manager.activeGpuResources());
            ResourceHandle<GpuResource> second = manager.acquireGpuResourceAsync(id, null, null);
            await(manager, second);
            assertTrue(second.handle().identity() != firstIdentity);
            assertNotNull(manager.getGpuResource(second.handle()));
            releaseOnGpu(manager, second);
        });
    }

    @Test
    void releasingGpuHandleMoreThanOnceDoesNotCorruptManager() throws Exception {
        runOnGpu(() -> {
            ResourceManager manager = newManager();
            ResourceHandle<GpuResource> resource = manager.acquireGpuResourceAsync(meshId(manager), null, null);
            await(manager, resource);

            assertDoesNotThrow(() -> {
                releaseOnGpu(manager, resource);
                releaseOnGpu(manager, resource);
            });
            assertEquals(0, manager.activeGpuResources());
        });
    }

    @Test
    void nullCallbackMayUseNullDispatcher() throws Exception {
        runOnGpu(() -> {
            ResourceManager manager = newManager();
            ResourceHandle<GpuResource> resource = manager.acquireGpuResourceAsync(meshId(manager), null, null);
            await(manager, resource);
            releaseOnGpu(manager, resource);
        });
    }

    @Test
    void callbackRequiresDispatcher() throws Exception {
        runOnGpu(() -> {
            ResourceManager manager = newManager();
            assertThrows(IllegalArgumentException.class, () ->
                manager.acquireGpuResourceAsync(meshId(manager), ignored -> { }, null));
        });
    }

    @Test
    void inProgressCallbacksRunInRequestOrder() throws Exception {
        runOnGpu(() -> {
            ResourceManager manager = newManager();
            AssetID id = meshId(manager);
            RecordingDispatcher callbackDispatcher = new RecordingDispatcher();
            List<Integer> callbackOrder = new ArrayList<>();
            ResourceHandle<GpuResource> first = manager.acquireGpuResourceAsync(
                id, ignored -> callbackOrder.add(1), callbackDispatcher);
            ResourceHandle<GpuResource> second = manager.acquireGpuResourceAsync(
                id, ignored -> callbackOrder.add(2), callbackDispatcher);
            await(manager, first);
            callbackDispatcher.runAll();
            assertTrue(second.isReady());
            assertIterableEquals(List.of(1, 2), callbackOrder);
            releaseOnGpu(manager, first);
            releaseOnGpu(manager, second);
        });
    }

    @Test
    void loadedCallbacksRunInRequestOrder() throws Exception {
        runOnGpu(() -> {
            ResourceManager manager = newManager();
            AssetID id = meshId(manager);
            RecordingDispatcher callbackDispatcher = new RecordingDispatcher();
            List<Integer> callbackOrder = new ArrayList<>();
            ResourceHandle<GpuResource> initial = manager.acquireGpuResourceAsync(id, null, null);
            await(manager, initial);
            manager.acquireGpuResourceAsync(id, ignored -> callbackOrder.add(1), callbackDispatcher);
            manager.acquireGpuResourceAsync(id, ignored -> callbackOrder.add(2), callbackDispatcher);
            callbackDispatcher.runAll();
            assertIterableEquals(List.of(1, 2), callbackOrder);
            releaseOnGpu(manager, initial);
        });
    }

    private void runOnGpu(ThrowingRunnable runnable) throws Exception {
        assumeTrue(gpuTestContext != null && gpuTestContext.isAvailable(),
            "GPU integration tests require an active LibGDX GL20/GL30 context");
        gpuTestContext.run(runnable);
    }

    private ResourceManager newManager() {
        ResourceManager manager = new ResourceManager(tmpDir.toString(), gpuTestContext);
        manager.importGltf(modelFixture("Cube.gltf"));
        return manager;
    }

    private void releaseOnGpu(ResourceManager manager, ResourceHandle<GpuResource> resource) throws Exception {
        gpuTestContext.execute(() -> manager.releaseGpuResource(resource));
        synchronizeGpu();
    }

    private void synchronizeGpu() throws Exception {
        gpuTestContext.execute(() -> { });
        Throwable dispatchFailure = gpuTestContext.dispatchFailure();
        if (dispatchFailure != null) {
            throw new AssertionError("GPU dispatcher task failed", dispatchFailure);
        }
    }

    private AssetID meshId(ResourceManager manager) {
        for (AssetID id : manager.getAssetIDs()) {
            if (manager.getAssetType(id) == AssetType.MESH) {
                return id;
            }
        }
        throw new AssertionError("Test fixture did not produce a mesh asset");
    }

    private static void await(ResourceManager manager, ResourceHandle<GpuResource> resource) throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(2);
        while (!resource.isReady() && System.nanoTime() < deadline) {
            Throwable dispatchFailure = gpuTestContext.dispatchFailure();
            if (dispatchFailure != null) {
                throw new AssertionError("GPU dispatcher task failed", dispatchFailure);
            }
            if (!resource.isReady()) {
                Thread.sleep(1);
            }
        }
        assertTrue(resource.isReady(), "Timed out waiting for resource");
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }

    private static final class RecordingDispatcher implements Dispatcher {
        private final Queue<Runnable> tasks = new ArrayDeque<>();

        @Override
        public void post(Runnable runnable) {
            tasks.add(runnable);
        }

        void runAll() {
            Runnable task;
            while ((task = tasks.poll()) != null) {
                task.run();
            }
        }
    }

    private static final class GpuTestContext implements Dispatcher {
        private final Queue<Runnable> tasks = new ArrayDeque<>();
        private final CountDownLatch created = new CountDownLatch(1);
        private final AtomicReference<Throwable> dispatchFailure = new AtomicReference<>();
        private volatile Throwable startupFailure;
        private Application application;
        private Thread applicationThread;

        static GpuTestContext start() {
            GpuTestContext context = new GpuTestContext();
            Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
            configuration.setWindowedMode(1, 1);
            configuration.setInitialVisible(false);
            configuration.setForegroundFPS(60);
            configuration.setTitle("Ore Forge Resource Tests");
            configuration.setOpenGLEmulation(
                Lwjgl3ApplicationConfiguration.GLEmulation.GL30, 3, 3);
            context.applicationThread = new Thread(() -> {
                try {
                    new Lwjgl3Application(new ApplicationAdapter() {
                        @Override
                        public void create() {
                            context.application = Gdx.app;
                            context.created.countDown();
                        }

                        @Override
                        public void render() {
                            Runnable task;
                            while ((task = context.pollTask()) != null) {
                                try {
                                    task.run();
                                } catch (Throwable throwable) {
                                    context.dispatchFailure.compareAndSet(null, throwable);
                                }
                            }
                        }
                    }, configuration);
                } catch (Throwable throwable) {
                    context.startupFailure = throwable;
                    context.created.countDown();
                }
            }, "ore-forge-gpu-test-context");
            context.applicationThread.setDaemon(true);
            context.applicationThread.start();
            try {
                if (!context.created.await(10, TimeUnit.SECONDS)) {
                    context.startupFailure = new IllegalStateException("Timed out creating GPU test context");
                }
            } catch (InterruptedException throwable) {
                Thread.currentThread().interrupt();
                context.startupFailure = throwable;
            }
            return context;
        }

        boolean isAvailable() {
            return startupFailure == null && created.getCount() == 0 && application != null;
        }

        void run(ThrowingRunnable runnable) throws Exception {
            runnable.run();
        }

        void execute(ThrowingRunnable runnable) throws Exception {
            AtomicReference<Throwable> failure = new AtomicReference<>();
            CountDownLatch completed = new CountDownLatch(1);
            post(() -> {
                try {
                    runnable.run();
                } catch (Throwable throwable) {
                    failure.set(throwable);
                } finally {
                    completed.countDown();
                }
            });
            if (!completed.await(30, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Timed out running GPU operation");
            }
            Throwable throwable = failure.get();
            if (throwable instanceof Exception exception) throw exception;
            if (throwable instanceof Error error) throw error;
            if (throwable != null) throw new RuntimeException(throwable);
        }

        @Override
        public void post(Runnable runnable) {
            synchronized (tasks) {
                tasks.add(runnable);
            }
        }

        private Runnable pollTask() {
            synchronized (tasks) {
                return tasks.poll();
            }
        }

        Throwable dispatchFailure() {
            return dispatchFailure.get();
        }

        void close() {
            if (application != null) {
                application.exit();
            }
            if (applicationThread != null) {
                try {
                    applicationThread.join(5000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    private Path modelFixture(String fileName) {
        try {
            return Path.of(getClass().getResource("/models/" + fileName).toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException("Failed to resolve fixture: " + fileName, e);
        }
    }
}
