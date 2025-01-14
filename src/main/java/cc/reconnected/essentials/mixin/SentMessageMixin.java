package cc.reconnected.essentials.mixin;

import cc.reconnected.essentials.RccEssentials;
import cc.reconnected.essentials.core.customChat.CustomSentMessage;
import net.minecraft.network.message.SentMessage;
import net.minecraft.network.message.SignedMessage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SentMessage.class)
public interface SentMessageMixin {
    @Inject(method = "of", at = @At("HEAD"), cancellable = true)
    private static void rccServer$of(SignedMessage message, CallbackInfoReturnable<SentMessage> cir) {

        if (message.isSenderMissing()) {
            RccEssentials.warnUnsignedMessages();
            cir.setReturnValue(new SentMessage.Profileless(message.getContent()));
        } else {
            cir.setReturnValue(new CustomSentMessage(message));
        }
    }
}
