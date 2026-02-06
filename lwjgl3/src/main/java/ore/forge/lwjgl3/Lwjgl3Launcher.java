package ore.forge.lwjgl3;

import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import ore.forge.GLTFQuickStartExample;
import ore.forge.OreForge;
import org.lwjgl.opengl.ARBInstancedArrays;

/**
 * Launches the desktop (LWJGL3) application.
 */
public class Lwjgl3Launcher {

    public static void main(String[] args) {
        if (StartupHelper.startNewJvmIfRequired()) return; // This handles macOS support and helps on Windows.
        createApplication();
    }

    private static void createApplication() {
        new Lwjgl3Application(new OreForge(), getDefaultConfiguration());
//        new Lwjgl3Application(new GLTFQuickStartExample(), getDefaultConfiguration());
    }

    private static Lwjgl3ApplicationConfiguration getDefaultConfiguration() {
        System.out.println(System.getProperty("java.version"));
        System.out.println(System.getProperty("java.vendor"));
        System.out.println(System.getProperty("java.vm.name"));

        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Ore Forge");

        config.setOpenGLEmulation(Lwjgl3ApplicationConfiguration.GLEmulation.GL30, 3, 3);
        config.setForegroundFPS(10_000);
        config.useVsync(true);
        Graphics.DisplayMode mode = Lwjgl3ApplicationConfiguration.getDisplayMode();
        config.setWindowedMode(mode.width, mode.height);
//        config.setFullscreenMode(mode);
        config.setResizable(true);
        config.setDecorated(false);
        config.setBackBufferConfig(8, 8, 8, 8, 16, 0, 2);


        //// If you remove the above line and set Vsync to false, you can get unlimited FPS, which can be
        //// useful for testing performance, but can also be very stressful to some hardware.
        //// You may also need to configure GPU drivers to fully disable Vsync; this can cause screen tearing.
        config.setWindowIcon("libgdx128.png", "libgdx64.png", "libgdx32.png", "libgdx16.png");
        return config;
    }
}
