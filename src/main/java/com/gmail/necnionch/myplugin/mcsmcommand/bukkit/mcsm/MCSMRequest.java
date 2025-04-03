package com.gmail.necnionch.myplugin.mcsmcommand.bukkit.mcsm;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class MCSMRequest {

    private static final JsonParser GSON_PARSER = new JsonParser();
    private static final long CACHE_REFRESH_DELAY = 1000 * 60;
    private final String url;
    private final String apiKey;
    private final String daemonId;
    private final Map<UUID, MCSMInstance> cachedInstances = new HashMap<>();
    private long cacheFetchTime = 0;

    public MCSMRequest(String url, String apiKey, String daemonId) {
        this.url = url;
        this.apiKey = apiKey;
        this.daemonId = daemonId;
    }

    public URL getRequestApi(String path, Map<String, String> params) throws MalformedURLException {
        return new URL(url + path + "?" + params.entrySet().stream().map(i -> i.getKey() + "=" + i.getValue()).collect(Collectors.joining("&")));
    }

    public List<MCSMInstance> getInstances() throws ResponseError {
        cacheFetchTime = System.currentTimeMillis();
        JsonObject response = requestGet("/api/service/remote_service_instances", p -> {
            p.put("page", "1");
            p.put("page_size", "20");
            p.put("status", "");
            p.put("instance_name", "");
        });

        JsonObject data = response.getAsJsonObject("data");
        cachedInstances.clear();
        List<MCSMInstance> instances = new ArrayList<>();
        for (JsonElement element : data.get("data").getAsJsonArray()) {
            JsonObject item = element.getAsJsonObject();
            JsonObject infoItem = item.get("info").getAsJsonObject();
            JsonObject configItem = item.get("config").getAsJsonObject();

            UUID uuid = UUID.fromString(item.get("instanceUuid").getAsString().replaceFirst(
                    "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)", "$1-$2-$3-$4-$5"
            ));

            int statusValue = item.get("status").getAsInt();
            MCSMInstance.Status status = MCSMInstance.Status.valueOf(statusValue);
            MCSMInstance instance = new MCSMInstance(
                    uuid,
                    configItem.get("nickname").getAsString(),
                    infoItem.get("currentPlayers").getAsInt(),
                    infoItem.get("maxPlayers").getAsInt(),
                    infoItem.get("version").getAsString(),
                    status,
                    statusValue
            );
            instances.add(instance);
            cachedInstances.put(uuid, instance);
        }

        return instances;
    }

    public MCSMInstance getInstance(String nickname) throws ResponseError {
        JsonObject response = requestGet("/api/service/remote_service_instances", p -> {
            p.put("page", "1");
            p.put("page_size", "1");
            p.put("status", "");
            p.put("instance_name", nickname);
        });

        JsonObject data = response.getAsJsonObject("data");
        for (JsonElement element : data.get("data").getAsJsonArray()) {
            JsonObject item = element.getAsJsonObject();
            JsonObject infoItem = item.get("info").getAsJsonObject();
            JsonObject configItem = item.get("config").getAsJsonObject();
            UUID uuid = uuidFromString(item.get("instanceUuid").getAsString());
            int statusValue = item.get("status").getAsInt();
            MCSMInstance.Status status = MCSMInstance.Status.valueOf(statusValue);

            MCSMInstance instance = new MCSMInstance(
                    uuid,
                    configItem.get("nickname").getAsString(),
                    infoItem.get("currentPlayers").getAsInt(),
                    infoItem.get("maxPlayers").getAsInt(),
                    infoItem.get("version").getAsString(),
                    status,
                    statusValue
            );
            cachedInstances.put(uuid, instance);
            return instance;
        }

        return null;
    }

    public void openInstance(UUID instanceId) throws ResponseError {
        requestGet("/api/protected_instance/open", p -> {
            p.put("uuid", uuidToShortString(instanceId));
        });
    }

    public void stopInstance(UUID instanceId) throws ResponseError {
        requestGet("/api/protected_instance/stop", p -> {
            p.put("uuid", uuidToShortString(instanceId));
        });
    }

    public void restartInstance(UUID instanceId) throws ResponseError {
        requestGet("/api/protected_instance/restart", p -> {
            p.put("uuid", uuidToShortString(instanceId));
        });
    }

    public void killInstance(UUID instanceId) throws ResponseError {
        requestGet("/api/protected_instance/kill", p -> {
            p.put("uuid", uuidToShortString(instanceId));
        });
    }


    public void clearCaches() {
        cachedInstances.clear();
        cacheFetchTime = 0;
    }

    public Map<UUID, MCSMInstance> cachedInstances() {
        return cachedInstances;
    }

    public boolean isOldCache() {
        return CACHE_REFRESH_DELAY < System.currentTimeMillis() - cacheFetchTime;
    }


    public JsonObject requestGet(String path, Consumer<Map<String, String>> apply) throws ResponseError {
        Map<String, String> params = new HashMap<>();
        apply.accept(params);
        return requestGet(path, params);
    }

    public JsonObject requestGet(String path, Map<String, String> params) throws ResponseError {
        params.put("apikey", apiKey);
        params.put("daemonId", daemonId);

        try {
            URL url = getRequestApi(path, params);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            connection.setDoInput(true);

            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            connection.setRequestProperty("X-Requested-With", "XMLHttpRequest");

            int status = connection.getResponseCode();
            if (status == 400) {
                throw new ResponseError(status, "400: Incorrect request parameters", null);
            } else if (status == 403) {
                throw new ResponseError(status, "403: Insufficient permissions", null);
            } else if (status == 500) {
                throw new ResponseError(status, "500: Program error", null);
            } else if (status != 200) {
                throw new ResponseError(status, status + ": Unknown error", null);
            }

            try (InputStreamReader is = new InputStreamReader(connection.getInputStream())) {
                return GSON_PARSER.parse(is).getAsJsonObject();
            }

        } catch (IOException e) {
            throw new ResponseError(-1, e.getMessage(), e);
        }
    }


    public static class ResponseError extends Exception {

        public final int status;

        public ResponseError(int status, String message, @Nullable Throwable cause) {
            super(message, cause);
            this.status = status;
        }

        public int getStatus() {
            return status;
        }
    }

    public static UUID uuidFromString(String uuid) {
        try {
            return UUID.fromString(uuid);
        } catch (IllegalArgumentException ignored) {
        }
        return UUID.fromString(uuid.replaceFirst(
                "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)", "$1-$2-$3-$4-$5"
        ));
    }

    public static String uuidToShortString(UUID uuid) {
        return uuid.toString().replace("-", "");
    }

}
