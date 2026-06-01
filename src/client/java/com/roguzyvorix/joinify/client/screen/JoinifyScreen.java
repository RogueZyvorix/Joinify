package com.roguzyvorix.joinify.client.screen;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.client.multiplayer.ServerData;
import com.roguzyvorix.joinify.network.WebSocketManager;
import java.util.List;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.multiplayer.resolver.ServerAddress;

public class JoinifyScreen extends Screen {
    private EditBox nameInput;
    private static String statusMessage = "";

    public JoinifyScreen() {
        super(Component.literal("Joinify Social Menu"));
    }

    @Override
    protected void init() {
        statusMessage = "";
        WebSocketManager.setStatusListener(msg -> statusMessage = msg);
        WebSocketManager.requestFriendsList();

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // --- Fitur Social ---
        this.nameInput = new EditBox(this.font, centerX + 15, centerY - 30, 140, 20, Component.literal("Username..."));
        this.nameInput.setMaxLength(16);
        this.addRenderableWidget(this.nameInput);

        Button addFriendButton = Button.builder(Component.literal("Add Friend"), button -> {
            String targetName = this.nameInput.getValue().trim();
            if (!targetName.isEmpty()) {
                WebSocketManager.sendFriendRequest(targetName);
                this.nameInput.setValue("");
            }
        }).bounds(centerX + 15, centerY - 5, 140, 20).build();
        this.addRenderableWidget(addFriendButton);

        // --- Tombol Tutup ---
        Button closeButton = Button.builder(Component.literal("Back to Game"), button -> this.onClose())
                .bounds(centerX - 70, this.height - 30, 140, 20).build();
        this.addRenderableWidget(closeButton);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        boolean isHostingLAN = this.minecraft != null && this.minecraft.hasSingleplayerServer() && this.minecraft.getSingleplayerServer().isPublished();

        // 1. Logika Klik Daftar Teman
        int friendYOffset = centerY - 65;
        List<String> friends = WebSocketManager.getFriendsList();
        for (String friendName : friends) {
            // [X] Hapus
            if (mouseX >= centerX - 140 && mouseX <= centerX - 115 && mouseY >= friendYOffset && mouseY <= friendYOffset + 10) {
                WebSocketManager.removeFriend(friendName);
                playClickSound();
                return true;
            }
            // [INVITE]
            if (isHostingLAN && mouseX >= centerX - 110 && mouseX <= centerX - 55 && mouseY >= friendYOffset && mouseY <= friendYOffset + 10) {
                String e4mcLink = WebSocketManager.getCurrentE4mcLink();
                if (!e4mcLink.isEmpty()) {
                    WebSocketManager.sendWorldInvite(friendName, e4mcLink);
                    statusMessage = "§aUndangan terkirim!";
                } else {
                    statusMessage = "§c[!] e4mc belum deteksi IP!";
                }
                playClickSound();
                return true;
            }
            friendYOffset += 14;
        }

        // 2. Logika Klik Permintaan
        int reqYOffset = centerY + 5;
        List<String> requests = WebSocketManager.getRequestsList();
        for (String reqName : requests) {
            if (mouseX >= centerX - 140 && mouseX <= centerX - 80 && mouseY >= reqYOffset && mouseY <= reqYOffset + 10) {
                WebSocketManager.acceptFriendRequest(reqName);
                playClickSound();
                return true;
            }
            reqYOffset += 14;
        }

        // 3. Logika Klik Undangan
        int inviteYOffset = centerY + 70;
        List<WebSocketManager.InviteData> invites = WebSocketManager.getInvitesList();
        for (WebSocketManager.InviteData invite : invites) {
            if (mouseX >= centerX - 140 && mouseX <= centerX - 95 && mouseY >= inviteYOffset && mouseY <= inviteYOffset + 10) {
                playClickSound();
                if (this.minecraft != null && invite.fromUsername.equals(this.minecraft.getUser().getName())) {
                    statusMessage = "§c[!] Kamu adalah host world ini!";
                    return true;
                }

                String serverUrl = invite.serverUrl;
                WebSocketManager.getInvitesList().remove(invite);

                if (this.minecraft != null) {
                    this.minecraft.disconnect();
                    String cleanedUrl = serverUrl.trim();
                    if (cleanedUrl.toLowerCase().startsWith("https://")) { cleanedUrl = cleanedUrl.substring(8); }
                    else if (cleanedUrl.toLowerCase().startsWith("http://")) { cleanedUrl = cleanedUrl.substring(7); }
                    int slashIndex = cleanedUrl.indexOf('/');
                    if (slashIndex != -1) { cleanedUrl = cleanedUrl.substring(0, slashIndex); }
                    final String finalServerUrl = cleanedUrl;

                    new Thread(() -> {
                        try {
                            int attempts = 0;
                            while ((this.minecraft.level != null || this.minecraft.hasSingleplayerServer()) && attempts < 100) {
                                Thread.sleep(50);
                                attempts++;
                            }
                            Thread.sleep(500);
                        } catch (InterruptedException e) { e.printStackTrace(); }

                        this.minecraft.execute(() -> {
                            ServerData serverData = new ServerData("Joinify World", finalServerUrl, ServerData.Type.OTHER);
                            ConnectScreen.startConnecting(new TitleScreen(), this.minecraft, ServerAddress.parseString(finalServerUrl), serverData, false, null);
                        });
                    }).start();
                }
                return true;
            }
            inviteYOffset += 14;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void playClickSound() {
        if (this.minecraft != null && this.minecraft.getSoundManager() != null) {
            this.minecraft.getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(
                net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK, 1.0F));
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        boolean isHostingLAN = this.minecraft != null && this.minecraft.hasSingleplayerServer() && this.minecraft.getSingleplayerServer().isPublished();

        guiGraphics.drawCenteredString(this.font, "§d§lJOINIFY SOCIAL NETWORK", centerX, 15, 0xFFFFFF);
        guiGraphics.drawString(this.font, "Tambah Teman Baru:", centerX + 15, centerY - 45, 0xAAAAAA);

        // --- Render Daftar Teman ---
        guiGraphics.drawString(this.font, "§a§l👥 DAFTAR TEMAN KAMU:", centerX - 140, centerY - 80, 0xFFFFFF);
        List<String> friends = WebSocketManager.getFriendsList();
        int friendYOffset = centerY - 65;
        
        if (friends.isEmpty()) {
            guiGraphics.drawString(this.font, "§7Tidak ada nama player", centerX - 140, friendYOffset, 0x888888);
        } else {
            for (String friendName : friends) {
                // LOGIKA WARNA (Hijau = Online, Merah = Offline)
                boolean isOnline = WebSocketManager.isFriendOnline(friendName);
                String colorCode = isOnline ? "§a" : "§c";
                String statusSymbol = isOnline ? "●" : "○";
                String friendDisplay = colorCode + statusSymbol + " " + friendName;

                if (isHostingLAN) {
                    guiGraphics.drawString(this.font, "§c[ X ] §e[ INVITE ] " + friendDisplay, centerX - 140, friendYOffset, 0xFFFFFF);
                } else {
                    guiGraphics.drawString(this.font, "§c[ X ] " + friendDisplay, centerX - 140, friendYOffset, 0xFFFFFF);
                }
                friendYOffset += 14;
            }
        }

        // Render Permintaan
        guiGraphics.drawString(this.font, "§e§l📩 PERMINTAAN MASUK:", centerX - 140, centerY - 10, 0xFFFFFF);
        List<String> requests = WebSocketManager.getRequestsList();
        int reqYOffset = centerY + 5;
        if (requests.isEmpty()) {
            guiGraphics.drawString(this.font, "§7Tidak ada permintaan", centerX - 140, reqYOffset, 0x888888);
        } else {
            for (String reqName : requests) {
                guiGraphics.drawString(this.font, "§a[ TERIMA ] §f" + reqName, centerX - 140, reqYOffset, 0xFFFFFF);
                reqYOffset += 14;
            }
        }

        // Render Undangan
        guiGraphics.drawString(this.font, "§d§l🎮 UNDANGAN MABAR:", centerX - 140, centerY + 55, 0xFFFFFF);
        List<WebSocketManager.InviteData> invites = WebSocketManager.getInvitesList();
        int inviteYOffset = centerY + 70;
        if (invites.isEmpty()) {
            guiGraphics.drawString(this.font, "§7Tidak ada undangan mabar", centerX - 140, inviteYOffset, 0x888888);
        } else {
            for (WebSocketManager.InviteData invite : invites) {
                guiGraphics.drawString(this.font, "§d[ JOIN ] §fDari: §b" + invite.fromUsername, centerX - 140, inviteYOffset, 0xFFFFFF);
                inviteYOffset += 14;
            }
        }

        // --- Monitoring E4MC ---
        if (isHostingLAN) {
            guiGraphics.drawString(this.font, "§b§l📡 STATUS TUNNEL E4MC:", centerX + 15, centerY + 25, 0xFFFFFF);
            String e4mcLink = WebSocketManager.getCurrentE4mcLink();
            if (!e4mcLink.isEmpty()) {
                guiGraphics.drawString(this.font, "§a✔ Terdeteksi Otomatis", centerX + 15, centerY + 40, 0xFFFFFF);
                guiGraphics.drawString(this.font, "§7IP: §e" + e4mcLink, centerX + 15, centerY + 52, 0xFFFFFF);
            } else {
                guiGraphics.drawString(this.font, "§c⏳ Menunggu Link dari Chat...", centerX + 15, centerY + 40, 0xFFFFFF);
            }
        }

        if (!statusMessage.isEmpty()) {
            guiGraphics.drawString(this.font, statusMessage, centerX + 15, centerY + 80, 0xFFFFFF);
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }
}