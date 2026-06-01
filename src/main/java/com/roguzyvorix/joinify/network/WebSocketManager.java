package com.roguzyvorix.joinify.network;

import com.roguzyvorix.joinify.Joinify;
import com.google.gson.JsonParser;
import com.google.gson.JsonObject;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.BiConsumer;

public class WebSocketManager {
    private static WebSocket webSocket;
    private static final String SERVER_URL = "ws://bore.pub:58585"; 
    
    // Sistem Heartbeat untuk menjaga koneksi
    private static final ScheduledExecutorService heartbeatScheduler = Executors.newSingleThreadScheduledExecutor();

    private static Consumer<String> friendRequestListener; 
    private static Consumer<String> statusListener;        
    private static BiConsumer<String, String> inviteListener; 
    private static BiConsumer<String, Boolean> friendStatusListener; 
    
    private static final List<String> friendsList = new ArrayList<>();
    private static final List<String> requestsList = new ArrayList<>();
    private static final List<InviteData> invitesList = new ArrayList<>();
    private static final Map<String, Boolean> onlineFriendsMap = new ConcurrentHashMap<>();
    private static String currentE4mcLink = "";

    public static List<String> getFriendsList() { return friendsList; }
    public static List<String> getRequestsList() { return requestsList; }
    public static List<InviteData> getInvitesList() { return invitesList; }
    public static void setCurrentE4mcLink(String link) { currentE4mcLink = link; }
    public static String getCurrentE4mcLink() { return currentE4mcLink; }

    public static class InviteData {
        public String fromUsername;
        public String serverUrl;
        public InviteData(String fromUsername, String serverUrl) {
            this.fromUsername = fromUsername;
            this.serverUrl = serverUrl;
        }
    }

    public static void setFriendRequestListener(Consumer<String> listener) { friendRequestListener = listener; }
    public static void setStatusListener(Consumer<String> listener) { statusListener = listener; }
    public static void setInviteListener(BiConsumer<String, String> listener) { inviteListener = listener; } 
    public static void setFriendStatusListener(BiConsumer<String, Boolean> listener) { friendStatusListener = listener; }

    // Method untuk memulai ping berkala
    public static void startHeartbeat() {
        heartbeatScheduler.scheduleAtFixedRate(() -> {
            if (webSocket != null && !webSocket.isOutputClosed()) {
                webSocket.sendText("{\"type\":\"PING\"}", true);
            }
        }, 0, 50, TimeUnit.SECONDS);
    }

    public static void connectToServer(String username, String uuid, String accessToken) {
        HttpClient.newHttpClient().newWebSocketBuilder()
            .buildAsync(URI.create(SERVER_URL), new WebSocket.Listener() {
                @Override
                public void onOpen(WebSocket ws) {
                    webSocket = ws;
                    String authPayload = String.format("{\"type\":\"AUTH\",\"username\":\"%s\",\"uuid\":\"%s\"}", username, uuid);
                    ws.sendText(authPayload, true);
                    
                    // Memulai Heartbeat
                    startHeartbeat();
                    
                    ws.request(1);
                }

                @Override
                public CompletionStage<?> onText(WebSocket ws, CharSequence data, boolean last) {
                    try {
                        JsonObject json = JsonParser.parseString(data.toString()).getAsJsonObject();
                        String type = json.get("type").getAsString();

                        switch (type) {
                            case "FRIEND_REQUEST":
                                if (friendRequestListener != null) friendRequestListener.accept(json.get("sender").getAsString());
                                break;
                            case "WORLD_INVITE":
                                String inviter = json.get("inviter").getAsString();
                                String url = json.get("serverUrl").getAsString();
                                invitesList.removeIf(i -> i.fromUsername.equals(inviter));
                                invitesList.add(new InviteData(inviter, url));
                                if (inviteListener != null) inviteListener.accept(inviter, url);
                                break;
                            case "friend-status":
                                String fName = json.get("username").getAsString();
                                boolean isOnline = json.get("isOnline").getAsBoolean();
                                
                                // Debug Log Console
                                System.out.println("[DEBUG] Menerima Status dari Server: " + fName + " adalah " + (isOnline ? "ONLINE" : "OFFLINE"));
                                
                                onlineFriendsMap.put(fName, isOnline);
                                Joinify.LOGGER.info("[Joinify] Debug: Status update " + fName + " -> " + isOnline);
                                
                                if (friendStatusListener != null) friendStatusListener.accept(fName, isOnline);
                                break;
                            case "FRIENDS_LIST":
                                friendsList.clear();
                                json.getAsJsonArray("friends").forEach(e -> friendsList.add(e.getAsString()));
                                break;
                            case "REQUESTS_LIST":
                                requestsList.clear();
                                json.getAsJsonArray("requests").forEach(e -> requestsList.add(e.getAsString()));
                                break;
                            case "ERROR":
                                if (statusListener != null) statusListener.accept("§c" + json.get("message").getAsString());
                                break;
                            case "NOTIF":
                                if (statusListener != null) statusListener.accept("§a" + json.get("message").getAsString());
                                break;
                        }
                    } catch (Exception e) { Joinify.LOGGER.error("[Joinify] JSON Error: " + e.getMessage()); }
                    ws.request(1);
                    return null;
                }
            });
    }

    public static void requestFriendsList() { if (webSocket != null) webSocket.sendText("{\"type\":\"GET_FRIENDS\"}", true); }
    public static void sendFriendRequest(String t) { if (webSocket != null) webSocket.sendText("{\"type\":\"ADD_FRIEND\",\"targetUsername\":\""+t+"\"}", true); }
    public static void acceptFriendRequest(String t) { if (webSocket != null) webSocket.sendText("{\"type\":\"ACCEPT_FRIEND\",\"targetUsername\":\""+t+"\"}", true); }
    public static void removeFriend(String t) { if (webSocket != null) webSocket.sendText("{\"type\":\"REMOVE_FRIEND\",\"targetUsername\":\""+t+"\"}", true); }
    public static void sendWorldInvite(String t, String u) { if (webSocket != null) webSocket.sendText("{\"type\":\"SEND_INVITE\",\"targetUsername\":\""+t+"\",\"serverUrl\":\""+u+"\"}", true); }
    public static boolean isFriendOnline(String u) { return onlineFriendsMap.getOrDefault(u, false); }
}