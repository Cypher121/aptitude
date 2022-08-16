package coffee.cypher.aptitude.mixins;

import coffee.cypher.aptitude.gui.OpenAptitudeScreenButton;
import coffee.cypher.aptitude.mixinaccessors.MerchantAccessor;
import coffee.cypher.aptitude.mixinaccessors.MerchantAccessorKt;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.MerchantScreen;
import net.minecraft.entity.passive.VillagerEntity;
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

    @Inject(at = @At("TAIL"), method = "init")
    private void aptitude$injectButton(CallbackInfo ci) {
        if (MerchantAccessorKt.getMerchant(handler) instanceof VillagerEntity) {
            addDrawableChild(new OpenAptitudeScreenButton((MerchantScreen) (Object) this, this.backgroundWidth, this.backgroundHeight));
        }
    }
}
