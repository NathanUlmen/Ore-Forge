package ore.forge;//The OreRealm is a stack of max Length 500(Ore limit) and when its created it will be full of 500 basic ore objects.
//Droppers will "pop"/pull their ore from the OreRealm rather than making a new ore object each time they drop an ore.
//When a furnace sells an Ore it will reset it and return the ore to the OreRealm.
//This allows for ore objects to be recycled and limits the number of ore objects in that can be present in the tycoon.
//The oreRealm also keeps track of the ore that are active in the tycoon, and is used to set ore state to moveable at the end of each tick.


import ore.forge.game.Ore;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;

//@author Nathan Ulmen
public class OreRealm {
    private static OreRealm oreRealmInstance = new OreRealm();
    private final Deque<Ore> stackOfOre, removalStack, additionStack;
    private final ArrayList<Ore> activeOre;

    private OreRealm() {
        stackOfOre = new ArrayDeque<Ore>();
        activeOre = new ArrayList<>(200);
        removalStack = new ArrayDeque<>();
        additionStack = new ArrayDeque<>();
    }

    public static OreRealm getSingleton() {
        if (oreRealmInstance == null) {
            oreRealmInstance = new OreRealm();
        }
        return oreRealmInstance;
    }

    public Ore queueOre() {
        additionStack.add(stackOfOre.peek());
        return stackOfOre.pop();
    }

    public Ore giveOre() {
//        stackOfOre.peek().setIsActive(true);
        activeOre.add(stackOfOre.peek());
        return stackOfOre.pop();
    }

    public Ore peek() {
        return stackOfOre.peek();
    }

    public void populate() {
//        for (int i = 0; i < Constants.ORE_LIMIT; i++) {
//            stackOfOre.push(new Ore());
//        }
    }

    public void depopulate() {
        stackOfOre.clear();
    }

    public void takeOre(Ore ore) {
        if (!removalStack.contains(ore)) {
            removalStack.add(ore);
            stackOfOre.push(ore);
        }
    }

    public void updateActiveOre() {
        while (!removalStack.isEmpty()) {
            removalStack.peek().deepReset();
            activeOre.remove(removalStack.pop());
        }
        while (!additionStack.isEmpty()) {
//            additionStack.peek().setIsActive(true);
            activeOre.add(additionStack.pop());
        }
    }

    public boolean containsOre(String ID) {
        for (Ore ore : activeOre) {
            if (ore.getID().equals(ID)) {
                return true;
            }
        }
        return false;
    }

//    Could Implement caching for this so We dont recalculate it all the time.
    public int getOreCount(String ID) {
//        Stopwatch stopwatch = new Stopwatch(TimeUnit.MICROSECONDS);
//        stopwatch.start();
        int count = 0;
        for (Ore ore : activeOre) {
            if (ore.getID().equals(ID)) {
                count++;
            }
        }
//        Gdx.app.log("ORE REALM", stopwatch.toString());
        return count;
    }

    public ArrayList<Ore> getActiveOre() {
        return activeOre;
    }

    public ArrayList<Ore> getUniqueActiveOre() {
        return new ArrayList<>(activeOre);
    }

    public Deque<Ore> getStackOfOre() {
        return stackOfOre;
    }

    public void resetAllOre() {
        for (Ore ore : activeOre) {
            takeOre(ore);
        }
        updateActiveOre();
    }

    public String toString() {
        StringBuilder result = new StringBuilder();
        for (Ore o : stackOfOre) {
            result.append("\n[").append(o.toString()).append("]");
        }
        return result.toString();
    }

}


