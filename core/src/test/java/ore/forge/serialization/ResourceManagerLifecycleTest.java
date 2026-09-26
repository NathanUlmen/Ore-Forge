package ore.forge.serialization;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import ore.forge.engine.Dispatcher;
import ore.forge.engine.definitions.AssetType;
import ore.forge.engine.resources.AssetID;
import ore.forge.engine.resources.CpuAssetData;
import ore.forge.engine.resources.ResourceHandle;
import ore.forge.engine.resources.ResourceManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.lang.reflect.Proxy;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResourceManagerLifecycleTest {
    @TempDir
    Path tmpDir;

    @BeforeEach
    void setUpGdxApp() {
        Gdx.app = (Application) Proxy.newProxyInstance(
            Application.class.getClassLoader(),
            new Class<?>[] {Application.class},
            (proxy, method, args) -> {
                if (method.getName().equals("postRunnable")) {
                    ((Runnable) args[0]).run();
                    return null;
                }
                Class<?> returnType = method.getReturnType();
                if (!returnType.isPrimitive()) return null;
                if (returnType == boolean.class) return false;
                if (returnType == long.class) return 0L;
                if (returnType == float.class) return 0f;
                if (returnType == double.class) return 0d;
                if (returnType == char.class) return '\0';
                return 0;
            }
        );
    }

    @AfterEach
    void tearDownGdxApp() {
        Gdx.app = null;
    }

    @Test
    void simpleLoadReturnsLoadedResource() throws Exception {
        ResourceManager manager = newManager();
        ResourceHandle<CpuAssetData> resource = manager.acquireCpuDataAsync(meshId(manager), null, null);
        await(manager, resource);
        assertTrue(resource.isReady());
        assertNotNull(manager.getCpuAsset(resource.handle()));
    }

    @Test
    void batchLoadLoadsCpuResourcesAndDispatchesOneCallback() throws Exception {
        ResourceManager manager = newManager();
        AssetID meshId = assetId(manager, AssetType.MESH);
        AssetID secondMeshId = secondAssetId(manager, meshId);
        RecordingDispatcher callbackDispatcher = new RecordingDispatcher();
        List<Collection<ResourceHandle<?>>> callbacks = new ArrayList<>();

        manager.batchLoad(
            List.of(
                new AssetID.BatchRequest(meshId, AssetID.ResolveType.CPU_DATA),
                new AssetID.BatchRequest(secondMeshId, AssetID.ResolveType.CPU_DATA)
            ),
            callbacks::add,
            callbackDispatcher
        );

        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (callbacks.isEmpty() && System.nanoTime() < deadline) {
            manager.synchronize();
            callbackDispatcher.runAll();
            if (callbacks.isEmpty()) Thread.sleep(1);
        }

        assertEquals(1, callbacks.size());
        List<ResourceHandle<?>> resources = new ArrayList<>(callbacks.get(0));
        assertEquals(2, resources.size());
        assertTrue(resources.stream().allMatch(ResourceHandle::isReady));

        manager.releaseCpuAsset(castCpuHandle(resources.get(0)));
        manager.releaseCpuAsset(castCpuHandle(resources.get(1)));
    }

    @Test
    void emptyBatchStillDispatchesCallback() {
        ResourceManager manager = newManager();
        RecordingDispatcher callbackDispatcher = new RecordingDispatcher();
        List<Collection<ResourceHandle<?>>> callbacks = new ArrayList<>();

        manager.batchLoad(List.of(), callbacks::add, callbackDispatcher);

        assertTrue(callbacks.isEmpty());
        callbackDispatcher.runAll();
        assertEquals(1, callbacks.size());
        assertTrue(callbacks.get(0).isEmpty());
    }

    @Test
    void batchCallbackRequiresDispatcher() {
        ResourceManager manager = newManager();
        assertThrows(NullPointerException.class, () ->
            manager.batchLoad(List.of(), ignored -> { }, null));
    }

    @Test
    void spacedLoadUsesAlreadyLoadedResource() throws Exception {
        ResourceManager manager = newManager();
        AssetID id = meshId(manager);
        ResourceHandle<CpuAssetData> first = manager.acquireCpuDataAsync(id, null, null);
        await(manager, first);
        ResourceHandle<CpuAssetData> second = manager.acquireCpuDataAsync(id, null, null);
        assertTrue(second.isReady());
        assertEquals(first.handle().identity(), second.handle().identity());
        assertSame(manager.getCpuAsset(first.handle()), manager.getCpuAsset(second.handle()));
        manager.releaseCpuAsset(first);
        manager.releaseCpuAsset(second);
    }

    @Test
    void multipleRequestsShareInProgressLoad() throws Exception {
        ResourceManager manager = newManager();
        AssetID id = meshId(manager);
        ResourceHandle<CpuAssetData> first = manager.acquireCpuDataAsync(id, null, null);
        ResourceHandle<CpuAssetData> second = manager.acquireCpuDataAsync(id, null, null);
        assertNotSame(first, second);
        assertEquals(first.handle().identity(), second.handle().identity());
        await(manager, first);
        await(manager, second);
        assertSame(manager.getCpuAsset(first.handle()), manager.getCpuAsset(second.handle()));
        manager.releaseCpuAsset(first);
        manager.releaseCpuAsset(second);
    }

    @Test
    void releasedCpuAssetIsReusedFromCache() throws Exception {
        ResourceManager manager = newManager();
        AssetID id = meshId(manager);

        ResourceHandle<CpuAssetData> first = manager.acquireCpuDataAsync(id, null, null);
        await(manager, first);
        CpuAssetData data = manager.getCpuAsset(first.handle());
        long firstHandleIdentity = first.handle().identity();

        manager.releaseCpuAsset(first);
        assertEquals(0, manager.activeCpuResources());

        ResourceHandle<CpuAssetData> cached = manager.acquireCpuDataAsync(id, null, null);

        assertTrue(cached.isReady());
        assertNotEquals(firstHandleIdentity, cached.handle().identity());
        assertSame(data, manager.getCpuAsset(cached.handle()));
        assertEquals(1, manager.activeCpuResources());

        manager.releaseCpuAsset(cached);
    }

    @Test
    void cacheRetainsMultipleReleasedCpuAssets() throws Exception {
        ResourceManager manager = newManager();
        AssetID firstId = meshId(manager);
        AssetID secondId = secondAssetId(manager, firstId);

        ResourceHandle<CpuAssetData> first = manager.acquireCpuDataAsync(firstId, null, null);
        ResourceHandle<CpuAssetData> second = manager.acquireCpuDataAsync(secondId, null, null);
        await(manager, first);
        await(manager, second);
        CpuAssetData firstData = manager.getCpuAsset(first.handle());
        CpuAssetData secondData = manager.getCpuAsset(second.handle());

        manager.releaseCpuAsset(first);
        manager.releaseCpuAsset(second);
        assertEquals(0, manager.activeCpuResources());

        ResourceHandle<CpuAssetData> cachedFirst = manager.acquireCpuDataAsync(firstId, null, null);
        ResourceHandle<CpuAssetData> cachedSecond = manager.acquireCpuDataAsync(secondId, null, null);

        assertTrue(cachedFirst.isReady());
        assertTrue(cachedSecond.isReady());
        assertSame(firstData, manager.getCpuAsset(cachedFirst.handle()));
        assertSame(secondData, manager.getCpuAsset(cachedSecond.handle()));

        manager.releaseCpuAsset(cachedFirst);
        manager.releaseCpuAsset(cachedSecond);
    }

    @Test
    void cacheStoresCpuAssetOnlyAfterLastHandleIsReleased() throws Exception {
        ResourceManager manager = newManager();
        AssetID id = meshId(manager);

        ResourceHandle<CpuAssetData> first = manager.acquireCpuDataAsync(id, null, null);
        await(manager, first);
        ResourceHandle<CpuAssetData> second = manager.acquireCpuDataAsync(id, null, null);

        manager.releaseCpuAsset(first);
        assertEquals(1, manager.activeCpuResources());

        ResourceHandle<CpuAssetData> stillActive = manager.acquireCpuDataAsync(id, null, null);
        assertTrue(stillActive.isReady());
        assertEquals(second.handle().identity(), stillActive.handle().identity());
        assertEquals(1, manager.activeCpuResources());

        manager.releaseCpuAsset(second);
        manager.releaseCpuAsset(stillActive);
        assertEquals(0, manager.activeCpuResources());
    }

    @Test
    void releasingCpuHandleMoreThanOnceDoesNotCorruptManager() throws Exception {
        ResourceManager manager = newManager();
        ResourceHandle<CpuAssetData> resource = manager.acquireCpuDataAsync(meshId(manager), null, null);
        await(manager, resource);

        assertDoesNotThrow(() -> {
            manager.releaseCpuAsset(resource);
        });
        assertThrows(Exception.class, () -> manager.releaseCpuAsset(resource));
        assertEquals(0, manager.activeCpuResources());
    }

    @Test
    void nullCallbackMayUseNullDispatcher() throws Exception {
        ResourceManager manager = newManager();
        ResourceHandle<CpuAssetData> resource = manager.acquireCpuDataAsync(meshId(manager), null, null);
        await(manager, resource);
    }

    @Test
    void callbackRequiresDispatcher() {
        ResourceManager manager = newManager();
        assertThrows(IllegalArgumentException.class, () ->
            manager.acquireCpuDataAsync(meshId(manager), ignored -> { }, null));
    }

    @Test
    void inProgressCallbacksRunInRequestOrder() throws Exception {
        ResourceManager manager = newManager();
        AssetID id = meshId(manager);
        RecordingDispatcher callbackDispatcher = new RecordingDispatcher();
        List<Integer> callbackOrder = new ArrayList<>();
        ResourceHandle<CpuAssetData> first = manager.acquireCpuDataAsync(
            id, ignored -> callbackOrder.add(1), callbackDispatcher);
        ResourceHandle<CpuAssetData> second = manager.acquireCpuDataAsync(
            id, ignored -> callbackOrder.add(2), callbackDispatcher);
        await(manager, first);
        callbackDispatcher.runAll();
        assertTrue(second.isReady());
        assertIterableEquals(List.of(1, 2), callbackOrder);
    }

    @Test
    void loadedCallbacksRunInRequestOrder() throws Exception {
        ResourceManager manager = newManager();
        AssetID id = meshId(manager);
        RecordingDispatcher callbackDispatcher = new RecordingDispatcher();
        List<Integer> callbackOrder = new ArrayList<>();
        ResourceHandle<CpuAssetData> initial = manager.acquireCpuDataAsync(id, null, null);
        await(manager, initial);
        manager.acquireCpuDataAsync(id, ignored -> callbackOrder.add(1), callbackDispatcher);
        manager.acquireCpuDataAsync(id, ignored -> callbackOrder.add(2), callbackDispatcher);
        callbackDispatcher.runAll();
        assertIterableEquals(List.of(1, 2), callbackOrder);
    }

    private ResourceManager newManager() {
        ResourceManager manager = new ResourceManager(tmpDir.toString(), null);
        manager.importGltf(modelFixture("Cube.gltf"));
        manager.importGltf(modelFixture("Wedge.gltf"));
        return manager;
    }

    private AssetID meshId(ResourceManager manager) {
        return assetId(manager, AssetType.MESH);
    }

    private AssetID assetId(ResourceManager manager, AssetType type) {
        for (AssetID id : manager.getAssetIDs()) {
            if (manager.getAssetType(id) == type) return id;
        }
        throw new AssertionError("Test fixture did not produce an asset of type " + type);
    }

    private AssetID secondAssetId(ResourceManager manager, AssetID first) {
        for (AssetID id : manager.getAssetIDs()) {
            if (!id.equals(first) && manager.getAssetType(id) == AssetType.MESH) return id;
        }
        throw new AssertionError("Test fixture did not produce a second asset of type " + AssetType.MESH);
    }

    @SuppressWarnings("unchecked")
    private static ResourceHandle<CpuAssetData> castCpuHandle(ResourceHandle<?> handle) {
        return (ResourceHandle<CpuAssetData>) handle;
    }

    private static void await(ResourceManager manager, ResourceHandle<?> resource) throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (!resource.isReady() && System.nanoTime() < deadline) {
            manager.synchronize();
            if (!resource.isReady()) Thread.sleep(1);
        }
        assertTrue(resource.isReady(), "Timed out waiting for resource");
    }

    private static final class RecordingDispatcher implements Dispatcher {
        private final Queue<Runnable> tasks = new ArrayDeque<>();

        @Override
        public void post(Runnable runnable) {
            tasks.add(runnable);
        }

        void runAll() {
            Runnable task;
            while ((task = tasks.poll()) != null) task.run();
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
