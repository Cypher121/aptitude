package coffee.cypher.aptitude.mixins;

import coffee.cypher.aptitude.actions.VillagerEventHandler;
import coffee.cypher.aptitude.datamodel.AptitudeVillagerData;
import coffee.cypher.aptitude.items.AptitudeIncreaseItem;
import coffee.cypher.aptitude.mixinaccessors.AptitudeVillagerDataAccessor;
import com.mojang.serialization.DataResult;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VillagerEntity.class)
abstract class AptitudeVillagerEntityMixin implements AptitudeVillagerDataAccessor {
    private AptitudeVillagerData aptitude$aptitudeVillagerData = AptitudeVillagerData.createRandom();

    @Inject(at = @At("TAIL"), method = "readCustomDataFromNbt")
    public void aptitude$loadAptitudesFromNbt(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains("AptitudeData")) {
            DataResult<AptitudeVillagerData> dataResult =
                AptitudeVillagerData.CODEC.parse(NbtOps.INSTANCE, nbt.get("AptitudeData"));

            dataResult.resultOrPartial((error) -> {
            }).ifPresent(avd -> aptitude$aptitudeVillagerData = avd);
        }
    }

    @Inject(at = @At("TAIL"), method = "writeCustomDataToNbt")
    private void aptitude$saveAptitudesToNbt(NbtCompound nbt, CallbackInfo ci) {
        AptitudeVillagerData.CODEC
            .encodeStart(
                NbtOps.INSTANCE,
                aptitude$aptitudeVillagerData
            )
            .resultOrPartial(error -> {
            })
            .ifPresent(nbtElement -> nbt.put("AptitudeData", nbtElement));
    }

    @Inject(at = @At("TAIL"), method = "levelUp")
    private void aptitude$onLevelUp(CallbackInfo ci) {
        VillagerEventHandler.onVillagerLevelUp((VillagerEntity) (Object) this);
    }

    @Inject(at = @At("HEAD"), method = "interactMob", cancellable = true)
    private void aptitude$interceptItemUse(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        if (player.getStackInHand(hand).isOf(AptitudeIncreaseItem.INSTANCE)) {
            cir.setReturnValue(ActionResult.PASS);
            cir.cancel();
        }
    }

    @NotNull
    @Override
    public AptitudeVillagerData getAptitude$aptitudeVillagerData() {
        return aptitude$aptitudeVillagerData;
    }

    @Override
    public void setAptitude$aptitudeVillagerData(@NotNull AptitudeVillagerData avd) {
        this.aptitude$aptitudeVillagerData = avd;
    }
}
