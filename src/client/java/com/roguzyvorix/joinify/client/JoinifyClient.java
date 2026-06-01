package com.roguzyvorix.joinify.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.roguzyvorix.joinify.Joinify;
import com.roguzyvorix.joinify.client.screen.JoinifyScreen;
import com.roguzyvorix.joinify.client.screen.JoinifyNotificationToast;
import com.roguzyvorix.joinify.network.WebSocketManager;

public class JoinifyClient implements ClientModInitializer {
    private static KeyMapping socialMenuKey;

    @Override
    public void onInitializeClient() {
        socialMenuKey = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.joinify.social", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_O, "category.joinify"));

        ClientTickEvents.END_CLIENT_TICK.register(c -> { while(socialMenuKey.consumeClick()) if(c.screen == null) c.setScreen(new JoinifyScreen()); });

        ClientReceiveMessageEvents.CHAT.register((m, s, se, p, o) -> handleMessage(m.getString()));
        
        // --- CALLBACKS (TOAST + CHAT + SOUND) ---
        WebSocketManager.setFriendRequestListener(f -> runClient(() -> {
            JoinifyNotificationToast.send("§b§lFRIEND REQUEST", "§e" + f + " §7ingin berteman.");
            net.minecraft.client.Minecraft.getInstance().gui.getChat().addMessage(Component.literal("§d[Joinify] §f🔔 §b" + f + " §fmengirimimu permintaan pertemanan!"));
        }));

        WebSocketManager.setInviteListener((f, s) -> runClient(() -> {
            JoinifyNotificationToast.send("§d§lWORLD INVITE", "§e" + f + " §7mengundangmu mabar!");
            net.minecraft.client.Minecraft.getInstance().player.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F);
        }));

        WebSocketManager.setFriendStatusListener((u, o) -> { if(o) runClient(() -> JoinifyNotificationToast.send("§a🟢 PLAYER ONLINE", "§e" + u + " §7sekarang aktif.")); });

        ClientLifecycleEvents.CLIENT_STARTED.register(c -> { if(c.getUser() != null) WebSocketManager.connectToServer(c.getUser().getName(), c.getUser().getProfileId().toString(), c.getUser().getAccessToken()); });
    }

    private void runClient(Runnable r) { net.minecraft.client.Minecraft.getInstance().execute(r); }

    public static void handleMessage(String text) {
        if (text == null || text.contains("[Joinify]")) return;
        Matcher m = Pattern.compile("([a-zA-Z0-9.-]+\\.e4mc\\.link)").matcher(text);
        if (m.find()) WebSocketManager.setCurrentE4mcLink(m.group(1));
    }
}