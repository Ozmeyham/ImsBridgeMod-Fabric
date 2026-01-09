package ozmeyham.imsbridge.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ozmeyham.imsbridge.IMSBridge;

@Pseudo
@Mixin(targets = "at.hannibal2.skyhanni.features.chat.CurrentChatDisplay")
public class MixinSkyhanniChatDisplay {
	@Inject(at = @At("HEAD"), method = "drawDisplay", cancellable = true)
	private void init(CallbackInfoReturnable<String> cir) {
		if (IMSBridge.combinedBridgeChatEnabled) cir.setReturnValue("§aChat: §6CBridge");
	}
}