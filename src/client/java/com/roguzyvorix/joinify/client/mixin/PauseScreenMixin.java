package com.roguzyvorix.joinify.client.mixin;

import com.roguzyvorix.joinify.client.screen.JoinifyScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PauseScreen.class)
public abstract class PauseScreenMixin extends Screen {
    protected PauseScreenMixin(Component title) { super(title); }

    @Inject(method = "init", at = @At("TAIL"))
    private void addJoinifyButton(CallbackInfo ci) {
        // Menambahkan tombol "Joinify" di bawah tombol-tombol standar
        this.addRenderableWidget(Button.builder(Component.literal("Joinify Social"), button -> {
            this.minecraft.setScreen(new JoinifyScreen());
        }).bounds(this.width / 2 - 102, this.height / 4 + 144 + -16, 204, 20).build());
    }
}