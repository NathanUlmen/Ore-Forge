package ore.forge;

import java.util.Objects;

public class Pair<E> {
    private E first, second;

    public Pair(E first, E second) {
        this.first = first;
        this.second = second;
    }

    public void set(E first, E second) {
        this.first = first;
        this.second = second;
    }

    public E first() {
        return first;
    }

    public E second() {
        return second;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof Pair<?> other) {
            return Objects.equals(first, other.first) && Objects.equals(second, other.second);
        }
        return false;
    }

    public int hashCode() {
        return Objects.hash(first, second);
    }


}
