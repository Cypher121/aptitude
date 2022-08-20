package coffee.cypher.aptitude.mixins;

import coffee.cypher.aptitude.gui.VillagerTradeOfferExtras;
import coffee.cypher.aptitude.mixinaccessors.client.AptitudeMerchantScreenAccessor;
import net.minecraft.client.gui.screen.ingame.MerchantScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.client.gui.screen.ingame.MerchantScreen$WidgetButtonPage")
abstract class AptitudeTradeOfferWidgetMixin extends ButtonWidget {
    AptitudeTradeOfferWidgetMixin(int i, int j, int k, int l, Text text, PressAction pressAction) {
        super(i, j, k, l, text, pressAction);
        throw new UnsupportedOperationException();
    }

    @Final
    @Shadow
    int index;

    private MerchantScreen aptitude$screen;

    @Inject(at = @At("TAIL"), method = "<init>")
    void aptitude$getContainingScreen(MerchantScreen merchantScreen, int i, int j, int k, PressAction pressAction, CallbackInfo ci) {
        aptitude$screen = merchantScreen;
    }


    @Inject(at = @At(value = "HEAD"), method = "renderTooltip", cancellable = true)
    void aptitude$renderExtraRecipeTooltip(MatrixStack matrices, int mouseX, int mouseY, CallbackInfo ci) {
        if (aptitude$screen.getScreenHandler().getRecipes().size() > this.index +
            ((AptitudeMerchantScreenAccessor) aptitude$screen).getAptitude$indexStartOffset()) {

            if (VillagerTradeOfferExtras.createTooltip(aptitude$screen, index, this, matrices, mouseX, mouseY)) {
                ci.cancel();
            }
        }
    }
}
