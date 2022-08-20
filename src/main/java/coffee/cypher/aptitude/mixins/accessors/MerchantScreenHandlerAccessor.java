package coffee.cypher.aptitude.mixins.accessors;

import net.minecraft.screen.MerchantScreenHandler;
import net.minecraft.village.Merchant;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MerchantScreenHandler.class)
public interface MerchantScreenHandlerAccessor {
    @NotNull
    @Accessor("merchant")
    Merchant aptitude$getMerchant();
}
