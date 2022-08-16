package coffee.cypher.aptitude.mixins;

import coffee.cypher.aptitude.mixinaccessors.MerchantAccessor;
import net.minecraft.screen.MerchantScreenHandler;
import net.minecraft.village.Merchant;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MerchantScreenHandler.class)
interface AptitudeMerchantScreenHandlerMixin extends MerchantAccessor {
    @NotNull
    @Override
    @Accessor
    Merchant getMerchant();
}
