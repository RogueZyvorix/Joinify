package com.roguzyvorix.joinify.client.screen;

import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.client.Minecraft;

public class JoinifyNotificationToast implements Toast {
    private final Component title;
    private final Component description;
    private long lastTimeElapsed = 0L;

    public JoinifyNotificationToast(Component title, Component description) {
        this.title = title;
        this.description = description;
    }

    // 1. Method Update: Terus merekam waktu berjalan dari Minecraft Toast Manager
    @Override
    public void update(ToastManager toastManager, long timeElapsed) {
        this.lastTimeElapsed = timeElapsed;
    }

    // 2. Method Visibility: Menentukan apakah toast harus tetap tampil atau menghilang
    @Override
    public Toast.Visibility getWantedVisibility() {
        // Jika waktu berjalan kurang dari 7000ms (7 detik), tetap tampilkan (SHOW)
        // Jika sudah lewat atau sama dengan 7 detik, picu animasi keluar (HIDE)
        return this.lastTimeElapsed < 7000L ? Toast.Visibility.SHOW : Toast.Visibility.HIDE;
    }

    // 3. Method Render: Menggambar box hitam transparan & border ungu khas Joinify
    @Override
    public void render(GuiGraphics guiGraphics, Font font, long timeElapsed) {
        int width = this.width();
        int height = this.height();

        // Background hitam transparan (0xEE111111)
        guiGraphics.fill(0, 0, width, height, 0xEE111111);
        // Border ungu menyala khas §d (0xFFD665E0)
        guiGraphics.renderOutline(0, 0, width, height, 0xFFD665E0);

        // Cetak teks judul dan deskripsi notifikasi
        guiGraphics.drawString(font, this.title, 10, 6, 0xFFFFFF, false);
        guiGraphics.drawString(font, this.description, 10, 18, 0xAAAAAA, false);
    }

    @Override
    public int width() {
        return 180;
    }

    @Override
    public int height() {
        return 32;
    }

    @Override
    public Object getToken() {
        return Toast.NO_TOKEN;
    }

    /**
     * Helper static untuk memicu dan memasukkan toast ke antrean manager Minecraft.
     */
    public static void send(String titleText, String descText) {
        Minecraft.getInstance().execute(() -> {
            Minecraft.getInstance().getToastManager().addToast(
                new JoinifyNotificationToast(Component.literal(titleText), Component.literal(descText))
            );
        });
    }
}