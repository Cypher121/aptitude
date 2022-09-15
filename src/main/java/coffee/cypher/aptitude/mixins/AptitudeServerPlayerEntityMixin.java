package coffee.cypher.aptitude.mixins;

import coffee.cypher.aptitude.Aptitude;
import coffee.cypher.aptitude.gui.packets.TradeOfferAptitudeLevelS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.village.TradeOfferList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
abstract class AptitudeServerPlayerEntityMixin {
    @Inject(at = @At("RETURN"), method = "sendTradeOffers")
    void aptitude$sendTradeLevelPacket(
        int syncId,
        TradeOfferList offers,
        int levelProgress,
        int experience,
        boolean leveled,
        boolean refreshable,
        CallbackInfo ci
    ) {
        new TradeOfferAptitudeLevelS2CPacket(syncId, offers).send((ServerPlayerEntity) (Object) this);
        Aptitude.INSTANCE.getLogger().info("Sent packet");
    }
}
