package com.gmail.necnionch.myplugin.mcsmcommand.bukkit.command;

import com.gmail.necnionch.myplugin.mcsmcommand.bukkit.mcsm.MCSMInstance;
import com.gmail.necnionch.myplugin.mcsmcommand.bukkit.mcsm.MCSMRequest;
import com.gmail.necnionch.myplugin.mcsmcommand.bukkit.mcsm.MCSMRequestExecutor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class MainCommand extends Command {

    private final MCSMRequestExecutor mcsm;

    public MainCommand(MCSMRequestExecutor mcsm) {
        super("mcsmcommand", null);
        this.mcsm = mcsm;

        messagePrefix(Component.text()
                .append(Component.text("[", NamedTextColor.GRAY))
                .append(Component.text("MCSM", NamedTextColor.GREEN))
                .append(Component.text("C", NamedTextColor.WHITE))
                .append(Component.text("] ", NamedTextColor.GRAY)), false);

        addChild("list", this::listCommand);
        addChild("start", this::startCommand)
                .argument("name", instanceArgOffline);
        addChild("stop", this::stopCommand)
                .argument("name", instanceArgOnline);
        addChild("restart", this::restartCommand)
                .argument("name", instanceArgOnline);
        addChild("kill", this::killCommand)
                .argument("name", instanceArgOnline);
    }

    private void restartCommand(Context ctx) {
        String nicknameOrUuid = ctx.get(instanceArgOnline);

        CompletableFuture<UUID> future;
        try {
            future = CompletableFuture.completedFuture(MCSMRequest.uuidFromString(nicknameOrUuid));
        } catch (IllegalArgumentException e) {
            future = mcsm.getInstanceWithCacheAsync(nicknameOrUuid).thenApply(getUuidOrError());
        }

        future.thenCompose(mcsm::restartInstanceAsync).whenComplete((uuid, ex) -> {
            if (ex != null) {
                ex = ex.getCause();
                if (ex instanceof NotFoundInstance) {
                    ctx.send(Component.text("指定されたインスタンスが見つかりませんでした", NamedTextColor.RED));
                } else {
                    ctx.send(Component.text()
                            .append(Component.text("再起動をリクエストできませんでした: ", NamedTextColor.RED))
                            .append(Component.text(ex.getMessage(), NamedTextColor.DARK_RED)));
                }
            } else {
                ctx.send(Component.text(getInstanceLabel(uuid) + "の再起動をリクエストしました", NamedTextColor.GOLD));
            }
        });
    }

    private void startCommand(Context ctx) {
        String nicknameOrUuid = ctx.get(instanceArgOffline);

        CompletableFuture<UUID> future;
        try {
            future = CompletableFuture.completedFuture(MCSMRequest.uuidFromString(nicknameOrUuid));
        } catch (IllegalArgumentException e) {
            future = mcsm.getInstanceWithCacheAsync(nicknameOrUuid).thenApply(getUuidOrError());
        }

        future.thenCompose(mcsm::openInstanceAsync).whenComplete((uuid, ex) -> {
            if (ex != null) {
                ex = ex.getCause();
                if (ex instanceof NotFoundInstance) {
                    ctx.send(Component.text("指定されたインスタンスが見つかりませんでした", NamedTextColor.RED));
                } else {
                    ctx.send(Component.text()
                            .append(Component.text("起動リクエストに失敗しました: ", NamedTextColor.RED))
                            .append(Component.text(ex.getMessage(), NamedTextColor.DARK_RED)));
                }
            } else {
                ctx.send(Component.text(getInstanceLabel(uuid) + "を起動します", NamedTextColor.GREEN));
            }
        });
    }

    private void stopCommand(Context ctx) {
        String nicknameOrUuid = ctx.get(instanceArgOnline);

        CompletableFuture<UUID> future;
        try {
            future = CompletableFuture.completedFuture(MCSMRequest.uuidFromString(nicknameOrUuid));
        } catch (IllegalArgumentException e) {
            future = mcsm.getInstanceWithCacheAsync(nicknameOrUuid).thenApply(getUuidOrError());
        }

        future.thenCompose(mcsm::stopInstanceAsync).whenComplete((uuid, ex) -> {
            if (ex != null) {
                ex = ex.getCause();
                if (ex instanceof NotFoundInstance) {
                    ctx.send(Component.text("指定されたインスタンスが見つかりませんでした", NamedTextColor.RED));
                } else {
                    ctx.send(Component.text()
                            .append(Component.text("停止リクエストに失敗しました: ", NamedTextColor.RED))
                            .append(Component.text(ex.getMessage(), NamedTextColor.DARK_RED)));
                }
            } else {
                ctx.send(Component.text(getInstanceLabel(uuid) + "を停止します", NamedTextColor.GOLD));
            }
        });
    }

    private void killCommand(Context ctx) {
        String nicknameOrUuid = ctx.get(instanceArgOnline);

        CompletableFuture<UUID> future;
        try {
            future = CompletableFuture.completedFuture(MCSMRequest.uuidFromString(nicknameOrUuid));
        } catch (IllegalArgumentException e) {
            future = mcsm.getInstanceWithCacheAsync(nicknameOrUuid).thenApply(getUuidOrError());
        }

        future.thenCompose(mcsm::killInstanceAsync).whenComplete((uuid, ex) -> {
            if (ex != null) {
                ex = ex.getCause();
                if (ex instanceof NotFoundInstance) {
                    ctx.send(Component.text("指定されたインスタンスが見つかりませんでした", NamedTextColor.RED));
                } else {
                    ctx.send(Component.text()
                            .append(Component.text("強制終了をリクエストできませんでした: ", NamedTextColor.RED))
                            .append(Component.text(ex.getMessage(), NamedTextColor.DARK_RED)));
                }
            } else {
                ctx.send(Component.text(getInstanceLabel(uuid) + "を強制終了します", NamedTextColor.YELLOW));
            }
        });
    }

    private void listCommand(Context ctx) {
        mcsm.getInstancesAsync().whenComplete((instances, ex) -> {
            if (ex != null) {
                ctx.send(Component.text()
                        .append(Component.text("インスタンス一覧を取得できません: ", NamedTextColor.RED))
                        .append(Component.text(ex.getMessage(), NamedTextColor.DARK_RED)));
                return;
            } else if (instances == null || instances.isEmpty()) {
                ctx.send(Component.text("インスタンスが１つもありません", NamedTextColor.RED));
                return;
            }

            TextComponent.Builder b = Component.text();
            b.append(Component.text("インスタンス一覧: ", NamedTextColor.DARK_AQUA));
            b.appendNewline();

            for (int i = 0; i < instances.size(); i++) {
                if (0 < i)
                    b.appendNewline();

                MCSMInstance instance = instances.get(i);

                TextComponent.Builder bb = Component.text();
                bb.append(Component.text("- ", NamedTextColor.WHITE));

                TextComponent statusText = Component.text(MCSMInstance.Status.UNKNOWN.equals(instance.getStatus()) ? "Status " + instance.getStatusValue() : instance.getStatus().name(), getStatusColor(instance));
                if (MCSMInstance.Status.RUNNING.equals(instance.getStatus()) || MCSMInstance.Status.STARTING.equals(instance.getStatus())) {
                    String command = "/" + getExecuteName(ctx) + " stop " + instance.getNickname();
                    statusText = statusText.clickEvent(ClickEvent.suggestCommand(command));
                    statusText = statusText.hoverEvent(HoverEvent.showText(Component.text()
                            .append(Component.text("クリックして停止コマンドを補完"))
                            .appendNewline()
                            .append(Component.text(command, NamedTextColor.GRAY))));
                } else if (MCSMInstance.Status.STOPPED.equals(instance.getStatus()) || MCSMInstance.Status.STOPPING.equals(instance.getStatus())) {
                    String command = "/" + getExecuteName(ctx) + " start " + instance.getNickname();
                    statusText = statusText.clickEvent(ClickEvent.suggestCommand(command));
                    statusText = statusText.hoverEvent(HoverEvent.showText(Component.text()
                            .append(Component.text("クリックして起動コマンドを補完"))
                            .appendNewline()
                            .append(Component.text(command, NamedTextColor.GRAY))));
                }
                bb.append(statusText);

                bb.append(Component.text(" | " , NamedTextColor.GRAY));

                String uuidShort = MCSMRequest.uuidToShortString(instance.getUuid());
                if (instance.getNickname() != null) {
                    bb.append(Component.text(instance.getNickname(), NamedTextColor.GOLD)
                            .clickEvent(ClickEvent.copyToClipboard(uuidShort))
                            .hoverEvent(HoverEvent.showText(Component.text()
                                    .append(Component.text("クリックしてUUIDをコピー"))
                                    .appendNewline()
                                    .append(Component.text("UUID: " + uuidShort, NamedTextColor.GRAY)))));
                } else {
                    bb.append(Component.text(uuidShort, NamedTextColor.GRAY, TextDecoration.ITALIC)
                            .hoverEvent(HoverEvent.showText(Component.text()
                                    .append(Component.text("クリックしてUUIDをコピー"))
                                    .appendNewline()
                                    .append(Component.text("UUID: " + uuidShort, NamedTextColor.GRAY)))));
                }

                b.append(bb);
            }

            ctx.send(b);
        });
    }


    private String getInstanceLabel(UUID instanceId) {
        MCSMInstance instance = mcsm.cachedInstances().get(instanceId);
        return instance != null && instance.getNickname() != null ? instance.getNickname() + "インスタンス" : "インスタンス " + MCSMRequest.uuidToShortString(instanceId) + " ";
    }

    private Argument<String> createInstanceArgument(Predicate<MCSMInstance> filter) {
        return new Argument<String>() {
            @Override
            public String execute(Context context, String input) {
                return input;
            }

            @Override
            public Stream<String> completeEntries(Context context, String input) {
                if (mcsm.isOldCache())
                    mcsm.getInstancesAsync();  // caching request
                return mcsm.cachedInstances().values().stream().filter(filter).map(MCSMInstance::getNickname);
            }
        };
    }

    private final Argument<String> instanceArgOnline = createInstanceArgument(i -> MCSMInstance.Status.STARTING.equals(i.getStatus()) || MCSMInstance.Status.RUNNING.equals(i.getStatus()));
    private final Argument<String> instanceArgOffline = createInstanceArgument(i -> MCSMInstance.Status.STOPPING.equals(i.getStatus()) || MCSMInstance.Status.STOPPED.equals(i.getStatus()));

    private static NamedTextColor getStatusColor(MCSMInstance instance) {
        switch (instance.getStatus()) {
            case BUSY: {
                return NamedTextColor.LIGHT_PURPLE;
            }
            case RUNNING: {
                return NamedTextColor.GREEN;
            }
            case STOPPED: {
                return NamedTextColor.GRAY;
            }
            case STARTING:
            case STOPPING: {
                return NamedTextColor.AQUA;
            }
            default:
                return NamedTextColor.GRAY;
        }
    }

    private static Function<MCSMInstance, UUID> getUuidOrError() {
        return instance -> {
            if (instance == null)
                throw new NotFoundInstance();
            return instance.getUuid();
        };
    }

    private static class NotFoundInstance extends Error {}

}
