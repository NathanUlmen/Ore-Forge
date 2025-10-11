package ore.forge;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import ore.forge.Items.Experimental.ItemBlueprint;
import ore.forge.Items.Experimental.ItemUserData;
import ore.forge.Strategies.Move;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GameWorld {
    private static final GameWorld instance = getInstance();
    private final World world;
    private final HashMap<Fixture, BodyContactCounter> touchingBodies;
    private final ArrayList<UpdatePair> updateList;

    private GameWorld() {
        world = new World(new Vector2(0, 0), true);
        touchingBodies = new HashMap<>();
        configureWorld(world);
        updateList = new ArrayList<>();
    }

    public static GameWorld getInstance() {
        return instance != null ? instance : new GameWorld();
    }

    private static class BodyContactCounter {
        private final Fixture fixture;
        private int numContacts;

        public BodyContactCounter(Fixture body) {
            this.fixture = body;
            this.numContacts = 0;
        }
    }

    private record UpdatePair(Fixture fixtureA, Fixture fixtureB) {
        public void update() {
            var userA = fixtureA.getUserData();
            var userB = fixtureB.getUserData();

            if (userB instanceof ItemUserData conveyor) {
                var trigger = conveyor.behavior();
                if (trigger != null) {
                    trigger.interact(fixtureA, conveyor);
                }
            }
            if (userA instanceof ItemUserData conveyor) {
                var trigger = conveyor.behavior();
                if (trigger != null) {
                    trigger.interact(fixtureB, conveyor);
                }
            }
        }

    }

    private void configureWorld(World world) {
        world.setContactListener(new ContactListener() {
            @Override
            public void beginContact(Contact contact) {
                var fixtureA = contact.getFixtureA();
                var fixtureB = contact.getFixtureB();
                //update numContacts
                touchingBodies.compute(fixtureA, (k, v) -> {
                    if (v == null) {
                        v = new BodyContactCounter(fixtureA);
                    }
                    v.numContacts++;
                    return v;
                });
                touchingBodies.compute(fixtureB, (k, v) -> {
                    if (v == null) {
                        v = new BodyContactCounter(fixtureB);
                    }
                    v.numContacts++;
                    return v;
                });

                var userA = fixtureA.getUserData();
                var userB = fixtureB.getUserData();

                //only add item to list if it moves.
                if (userB instanceof ItemUserData item) {
                    if (item.behavior() instanceof Move) {
                        updateList.add(new UpdatePair(fixtureA, fixtureB));
                    }
                } else if (userA instanceof ItemUserData item) {
                    if (item.behavior() instanceof Move) {
                        updateList.add(new UpdatePair(fixtureA, fixtureB));
                    }
                }

                if (userB instanceof ItemUserData item && !(userA instanceof ItemUserData)) {
                    if (item.behavior() != null) {
                        item.behavior().interact(fixtureA, item);
                    }
                } else if (userA instanceof ItemUserData item && !(userB instanceof ItemUserData)) {
                    if (item.behavior() != null) {
                        item.behavior().interact(fixtureB, item);
                    }
                }
            }

            @Override
            public void endContact(Contact contact) {
                var fixtureA = contact.getFixtureA();
                var fixtureB = contact.getFixtureB();
                System.out.println(fixtureA);
                System.out.println(fixtureB);

                touchingBodies.compute(fixtureA, (k, v) -> {
                    assert v != null;
                    v.numContacts--;
                    if (v.numContacts == 0) {
                        return null; //remove from map
                    }
                    return v;
                });
                touchingBodies.compute(fixtureB, (k, v) -> {
                    assert v != null;
                    v.numContacts--;
                    if (v.numContacts == 0) {
                        return null;
                    }
                    return v;
                });

                for (UpdatePair pair : updateList) {
                    if (pair.fixtureA == fixtureA && pair.fixtureB == fixtureB) {
                        updateList.remove(pair);
                        break;
                    }
                }


            }

            @Override
            public void preSolve(Contact contact, Manifold oldManifold) {

            }

            @Override
            public void postSolve(Contact contact, ContactImpulse impulse) {

            }
        });
    }

    public World physicsWorld() {
        return world;
    }

    public List<Fixture> getContactFixtures() {
        List<Fixture> contactBodies = new ArrayList<>();
        for (BodyContactCounter contactCounter : touchingBodies.values()) {
            contactBodies.add(contactCounter.fixture);
        }
        return contactBodies;
    }

    public void updateTouchingPairs() {
        for (UpdatePair updatePair : updateList) {
            updatePair.update();
        }
    }

}
