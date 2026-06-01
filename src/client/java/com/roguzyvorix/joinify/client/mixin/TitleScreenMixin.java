package com.roguzyvorix.joinify.client.mixin; // <-- PASTIKAN PACKAGE-NYA SUDAH DIUBAH KE CLIENT

import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.roguzyvorix.joinify.client.screen.JoinifyScreen;

@Mixin(TitleScreen.class)
public class TitleScreenMixin extends Screen {

    protected TitleScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        this.addRenderableWidget(Button.builder(Component.literal("§dJoinify Social"), button -> {
            if (this.minecraft != null) {
                this.minecraft.setScreen(new JoinifyScreen());
            }
        }).bounds(this.width - 110, 10, 100, 20).build());
    }
}