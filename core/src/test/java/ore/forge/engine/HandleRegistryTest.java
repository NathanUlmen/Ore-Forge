package ore.forge.engine;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.ApplicationLogger;
import com.badlogic.gdx.Audio;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.LifecycleListener;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.utils.Clipboard;
import com.badlogic.gdx.utils.Disposable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HandleRegistryTest {
    @BeforeEach
    void setUpGdxApp() {
        Gdx.app = new TestApplication();
    }

    @AfterEach
    void tearDownGdxApp() {
        Gdx.app = null;
    }

    @Test
    void addResourceCreatesValidHandleAndStoresResource() {
        HandleRegistry<TestDisposable> registry = new HandleRegistry<>();
        TestDisposable resource = new TestDisposable();

        Handle<TestDisposable> handle = registry.addResource(resource);

        assertTrue(handle.isValid());
        assertEquals(1, registry.size());
        assertSame(resource, registry.getResource(handle));
    }

    @Test
    void releaseHandleDisposesResourceWhenInitialReferenceIsReleased() {
        HandleRegistry<TestDisposable> registry = new HandleRegistry<>();
        TestDisposable resource = new TestDisposable();
        Handle<TestDisposable> handle = registry.addResource(resource);

        registry.releaseHandle(handle);

        assertTrue(resource.disposed);
        assertEquals(0, registry.size());
    }

    @Test
    void acquireHandleKeepsResourceAliveUntilFinalRelease() {
        HandleRegistry<TestDisposable> registry = new HandleRegistry<>();
        TestDisposable resource = new TestDisposable();
        Handle<TestDisposable> handle = registry.addResource(resource);

        registry.accquireHandle(handle);
        registry.releaseHandle(handle);

        assertEquals(1, registry.size());
        assertTrue(!resource.disposed);
        assertSame(resource, registry.getResource(handle));

        registry.releaseHandle(handle);

        assertTrue(resource.disposed);
        assertEquals(0, registry.size());
    }

    @Test
    void addResourceReusesFreedIndexWithNewVersion() {
        HandleRegistry<TestDisposable> registry = new HandleRegistry<>();
        Handle<TestDisposable> first = registry.addResource(new TestDisposable());
        registry.releaseHandle(first);

        Handle<TestDisposable> second = registry.addResource(new TestDisposable());

        assertEquals(first.index(), second.index());
        assertNotEquals(first.version(), second.version());
    }

    private static final class TestDisposable implements Disposable {
        private boolean disposed;

        @Override
        public void dispose() {
            disposed = true;
        }
    }

    private static final class TestApplication implements Application {
        private int logLevel = LOG_INFO;
        private ApplicationLogger logger;

        @Override
        public ApplicationListener getApplicationListener() {
            return null;
        }

        @Override
        public Graphics getGraphics() {
            return null;
        }

        @Override
        public Audio getAudio() {
            return null;
        }

        @Override
        public Input getInput() {
            return null;
        }

        @Override
        public Files getFiles() {
            return null;
        }

        @Override
        public Net getNet() {
            return null;
        }

        @Override
        public void log(String tag, String message) {
        }

        @Override
        public void log(String tag, String message, Throwable exception) {
        }

        @Override
        public void error(String tag, String message) {
        }

        @Override
        public void error(String tag, String message, Throwable exception) {
        }

        @Override
        public void debug(String tag, String message) {
        }

        @Override
        public void debug(String tag, String message, Throwable exception) {
        }

        @Override
        public void setLogLevel(int logLevel) {
            this.logLevel = logLevel;
        }

        @Override
        public int getLogLevel() {
            return logLevel;
        }

        @Override
        public void setApplicationLogger(ApplicationLogger applicationLogger) {
            this.logger = applicationLogger;
        }

        @Override
        public ApplicationLogger getApplicationLogger() {
            return logger;
        }

        @Override
        public ApplicationType getType() {
            return null;
        }

        @Override
        public int getVersion() {
            return 0;
        }

        @Override
        public long getJavaHeap() {
            return 0;
        }

        @Override
        public long getNativeHeap() {
            return 0;
        }

        @Override
        public Preferences getPreferences(String name) {
            return null;
        }

        @Override
        public Clipboard getClipboard() {
            return null;
        }

        @Override
        public void postRunnable(Runnable runnable) {
            runnable.run();
        }

        @Override
        public void exit() {
        }

        @Override
        public void addLifecycleListener(LifecycleListener listener) {
        }

        @Override
        public void removeLifecycleListener(LifecycleListener listener) {
        }
    }
}
