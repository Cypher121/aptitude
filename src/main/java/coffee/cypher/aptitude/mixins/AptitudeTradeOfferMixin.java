package coffee.cypher.aptitude.mixins;

import coffee.cypher.aptitude.datamodel.AptitudeLevel;
import coffee.cypher.aptitude.mixinaccessors.AptitudeTradeOfferAccessor;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.village.TradeOffer;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TradeOffer.class)
abstract class AptitudeTradeOfferMixin implements AptitudeTradeOfferAccessor {
    private AptitudeLevel aptitude$offeredByAptitudeLevel = null;

    @Nullable
    @Override
    public AptitudeLevel getAptitude$offeredByAptitudeLevel() {
        return aptitude$offeredByAptitudeLevel;
    }

    @Override
    public void setAptitude$offeredByAptitudeLevel(AptitudeLevel level) {
        this.aptitude$offeredByAptitudeLevel = level;
    }

    @Inject(at = @At("RETURN"), method = "toNbt")
    void aptitude$saveToNbt(CallbackInfoReturnable<NbtCompound> cir) {
        cir.getReturnValue().putInt(
            "AptitudeIsAptitudeTradeOffer",
            aptitude$offeredByAptitudeLevel == null ? -1 :
                aptitude$offeredByAptitudeLevel.ordinal()
        );
    }

    @Inject(at = @At("RETURN"), method = "<init>(Lnet/minecraft/nbt/NbtCompound;)V")
    void aptitude$readFromNbt(NbtCompound nbtCompound, CallbackInfo ci) {
        if (nbtCompound.contains("AptitudeIsAptitudeTradeOffer")) {
            var level = nbtCompound.getInt("AptitudeIsAptitudeTradeOffer");
            aptitude$offeredByAptitudeLevel = level < 0 ? null : AptitudeLevel.values()[level];
        }
    }
}
