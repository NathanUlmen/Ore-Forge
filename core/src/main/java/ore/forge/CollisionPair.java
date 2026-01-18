package ore.forge;

import java.util.Objects;

public class CollisionPair {
    private float timeTouching;
    private final PhysicsBodyData a, b;

    public CollisionPair(PhysicsBodyData left, PhysicsBodyData right) {
        this.timeTouching = 0;
        this.a = left;
        this.b = right;
    }

    public void updateTouchingTime(float delta) {
        timeTouching += delta;
    }

    public float getTimeTouching() {
        return timeTouching;
    }

    public PhysicsBodyData a() {
        return a;
    }

    public PhysicsBodyData b() {
        return b;
    }

    public boolean contains(EntityInstance entity) {
        return a.parentEntityInstance == entity ||  b.parentEntityInstance == entity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof CollisionPair other) {
            return Objects.equals(a, other.a) && Objects.equals(b, other.b) ||
                Objects.equals(a, other.b) && Objects.equals(b, other.a);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int h1 = a.hashCode();
        int h2 = b.hashCode();

        if (h1 > h2) {
            int tmp = h1;
            h1 = h2;
            h2 = tmp;
        }

        int result = 17;
        result = 31 * result + h1;
        result = 31 * result + h2;
        return result;
    }


}
