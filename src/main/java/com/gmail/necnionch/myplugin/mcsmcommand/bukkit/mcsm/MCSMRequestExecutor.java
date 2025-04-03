package com.gmail.necnionch.myplugin.mcsmcommand.bukkit.mcsm;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class MCSMRequestExecutor extends MCSMRequest {

    private final Consumer<Runnable> executor;

    public MCSMRequestExecutor(String url, String apiKey, String daemonId, Consumer<Runnable> executor) {
        super(url, apiKey, daemonId);
        this.executor = executor;
    }

    private <T> CompletableFuture<T> execute(ThrowableSupplier<T> task) {
        CompletableFuture<T> f = new CompletableFuture<>();
        executor.accept(() -> {
            try {
                f.complete(task.run());
            } catch (Throwable e) {
                f.completeExceptionally(e);
            }
        });
        return f;
    }

    public CompletableFuture<List<MCSMInstance>> getInstancesAsync() {
        return execute(this::getInstances);
    }

    public CompletableFuture<MCSMInstance> getInstanceAsync(String nickname) {
        return execute(() -> getInstance(nickname));
    }

    public CompletableFuture<Collection<MCSMInstance>> getInstancesWithCacheAsync() {
        Collection<MCSMInstance> caches = cachedInstances().values();

        if (isOldCache()) {
            if (caches.isEmpty())
                return execute(this::getInstances);
            execute(this::getInstances);
        }

        return CompletableFuture.completedFuture(caches);
    }

    public CompletableFuture<MCSMInstance> getInstanceWithCacheAsync(String nickname) {
        Collection<MCSMInstance> caches = cachedInstances().values();

        if (isOldCache()) {
            if (caches.isEmpty())
                return getInstanceAsync(nickname);
            getInstanceAsync(nickname);
        }

        return CompletableFuture.completedFuture(caches.stream()
                .filter(i -> nickname.equalsIgnoreCase(i.getNickname()))
                .findAny()
                .orElse(null));
    }

    public CompletableFuture<UUID> openInstanceAsync(UUID instanceId) {
        return execute(() -> {
            openInstance(instanceId);
            return instanceId;
        });
    }

    public CompletableFuture<UUID> stopInstanceAsync(UUID instanceId) {
        return execute(() -> {
            stopInstance(instanceId);
            return instanceId;
        });
    }

    public CompletableFuture<UUID> restartInstanceAsync(UUID instanceId) {
        return execute(() -> {
            restartInstance(instanceId);
            return instanceId;
        });
    }

    public CompletableFuture<UUID> killInstanceAsync(UUID instanceId) {
        return execute(() -> {
            killInstance(instanceId);
            return instanceId;
        });
    }


    private interface ThrowableSupplier<T> {
        T run() throws Throwable;
    }

}
