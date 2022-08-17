package coffee.cypher.aptitude.mixins;

import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(VillagerEntity.class)
public interface AptitudeVillagerAccessorMixin {
    @Invoker
    void callBeginTradeWith(PlayerEntity player);
}
