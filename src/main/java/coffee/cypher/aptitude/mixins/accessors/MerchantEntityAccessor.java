package coffee.cypher.aptitude.mixins.accessors;

import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.village.TradeOfferList;
import net.minecraft.village.TradeOffers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MerchantEntity.class)
public interface MerchantEntityAccessor {
    @Invoker("fillRecipesFromPool")
    void aptitude$fillRecipesFromPool(TradeOfferList recipeList, TradeOffers.Factory[] pool, int count);

    @Accessor("offers")
    void aptitude$setOffers(TradeOfferList list);
}
