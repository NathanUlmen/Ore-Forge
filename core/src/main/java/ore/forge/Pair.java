package ore.forge;

import com.badlogic.gdx.utils.Pool;

import java.util.Objects;

public record Pair<E>(E first, E second) {

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof Pair<?> other) {
            return Objects.equals(first, other.first) && Objects.equals(second, other.second);
        }
        return false;
    }

}
