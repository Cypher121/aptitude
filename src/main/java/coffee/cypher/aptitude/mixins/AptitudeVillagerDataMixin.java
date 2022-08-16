package coffee.cypher.aptitude.mixins;

import coffee.cypher.aptitude.datamodel.AptitudeVillagerData;
import coffee.cypher.aptitude.datamodel.AptitudeVillagerDataUtil;
import com.mojang.serialization.DataResult;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VillagerEntity.class)
abstract class AptitudeVillagerDataMixin {
    @Inject(at = @At("TAIL"), method = "initDataTracker")
    private void aptitude$trackAptitudeData(CallbackInfo ci) {
        AptitudeVillagerDataUtil.startTrackingAptitude((VillagerEntity) (Object) this);
    }

    @Inject(at = @At("TAIL"), method = "readCustomDataFromNbt")
    public void aptitude$loadAptitudesFromNbt(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains("AptitudeData")) {
            DataResult<AptitudeVillagerData> dataResult =
                AptitudeVillagerData.CODEC.parse(NbtOps.INSTANCE, nbt.get("AptitudeData"));

            dataResult.resultOrPartial((error) -> {
            }).ifPresent(avd -> AptitudeVillagerDataUtil.setAptitudeData((VillagerEntity) (Object) this, avd));
        }
    }

    @Inject(at = @At("TAIL"), method = "writeCustomDataToNbt")
    private void aptitude$saveAptitudesToNbt(NbtCompound nbt, CallbackInfo ci) {
        AptitudeVillagerData.CODEC
            .encodeStart(
                NbtOps.INSTANCE,
                AptitudeVillagerDataUtil.getAptitudeData((VillagerEntity) (Object) this)
            )
            .resultOrPartial(error -> {
            })
            .ifPresent(nbtElement -> nbt.put("AptitudeData", nbtElement));
    }
}
