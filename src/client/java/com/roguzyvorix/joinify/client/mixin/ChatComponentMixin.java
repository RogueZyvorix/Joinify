package com.roguzyvorix.joinify.client.mixin; // 🟢 Ubah package-nya jadi ini

import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.roguzyvorix.joinify.client.JoinifyClient; // 🟢 Sekarang import ini bakal aman tanpa eror!

@Mixin(ChatComponent.class)
public class ChatComponentMixin {
    
    @Inject(method = "addMessage(Lnet/minecraft/network/chat/Component;)V", at = @At("HEAD"))
    private void onAddMessage(Component message, CallbackInfo ci) {
        if (message != null) {
            JoinifyClient.handleMessage(message.getString());
        }
    }
}