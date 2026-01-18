package ore.forge;

import ore.forge.Strategies.DropperStrategies.BurstDrop;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class BurstDropTest {
    @Test
    void testBurstDrop() {
        int minute = 60;
        int steps = 121;
        float stepSize = (float) minute / steps;
        BurstDrop burstDrop = new BurstDrop(450, 3);
        int dropped = 0;
        for (int i = 0; i < steps; i++) {
            dropped += burstDrop.drop(stepSize);
        }
        assertEquals(450, dropped, 1);
    }

    @Test
    void testFastDrop() {
        int minute = 60;
        int steps = 60 * 10;
        float stepSize = (float) minute / steps;
        int expectedOre = 9999;
        BurstDrop burstDrop = new BurstDrop(expectedOre, 1);
        int dropped = 0;
        for (int i = 0; i < steps; i++) {
            dropped += burstDrop.drop(stepSize);
        }
        assertEquals(expectedOre, dropped, 1);
    }

    @Test
    void testSingleLargeStep() {
        BurstDrop burstDrop = new BurstDrop(100, 2);
        int dropped = burstDrop.drop(60f);
        assertEquals(100, dropped, 1);
    }

    @Test
    void testDeterminism() {
        BurstDrop a = new BurstDrop(200, 2);
        BurstDrop b = new BurstDrop(200, 2);

        for (int i = 0; i < 120; i++) {
            assertEquals(a.drop(0.5f), b.drop(0.5f));
        }
    }

    @Test
    void testPrecisionDrift() {
        BurstDrop burstDrop = new BurstDrop(1000, 5);
        int dropped = 0;

        for (int i = 0; i < 1_000_000; i++) {
            dropped += burstDrop.drop(0.00006f);
        }

        assertEquals(1000, dropped, 2);
    }

}
