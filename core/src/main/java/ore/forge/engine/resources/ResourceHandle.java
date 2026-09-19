package ore.forge.engine.resources;

import java.util.concurrent.CompletableFuture;


import ore.forge.engine.Handle;

public class ResourceHandle<E> {
    private final CompletableFuture<E> handleFuture;
    private final Handle<E> handle;

    ResourceHandle(Handle<E> handle, CompletableFuture<E> future) {
        assert handle != null : "Handle cannot be null";
        assert future != null : "Future should not be null";
        this.handleFuture = future;
        this.handle = java.util.Objects.requireNonNull(handle, "Resource handle must not be null.");
    }

    public Handle<E> handle() {
        return handle;
    }

    public boolean isReady() {
        return handleFuture.isDone();
    }

    CompletableFuture<E> getFuture() {
        return  handleFuture;
    }

}
