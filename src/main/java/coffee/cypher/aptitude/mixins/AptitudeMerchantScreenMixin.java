package coffee.cypher.aptitude.mixins;

import coffee.cypher.aptitude.gui.OpenAptitudeScreenButton;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.MerchantScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.MerchantScreenHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MerchantScreen.class)
@Environment(EnvType.CLIENT)
abstract class AptitudeMerchantScreenMixin extends HandledScreen<MerchantScreenHandler> {
    public AptitudeMerchantScreenMixin(MerchantScreenHandler screenHandler, PlayerInventory playerInventory, Text text) {
        super(screenHandler, playerInventory, text);
        throw new UnsupportedOperationException();
    }

    private OpenAptitudeScreenButton aptitude$button;

    @Inject(at = @At("TAIL"), method = "init")
    private void aptitude$injectButton(CallbackInfo ci) {
        aptitude$button = addDrawableChild(new OpenAptitudeScreenButton((MerchantScreen) (Object) this, this.backgroundWidth, this.backgroundHeight));
        aptitude$button.active = false;
        aptitude$button.visible = false;
    }

    @Inject(at = @At("HEAD"), method = "render")
    private void aptitude$toggleButtonVisibility(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        aptitude$button.visible = handler.isLeveled();
        aptitude$button.active = handler.isLeveled();
    }
}
