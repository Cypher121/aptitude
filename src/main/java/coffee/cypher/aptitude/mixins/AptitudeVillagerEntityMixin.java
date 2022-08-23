package coffee.cypher.aptitude.mixins;

import coffee.cypher.aptitude.actions.VillagerEventHandler;
import coffee.cypher.aptitude.datamodel.AptitudeVillagerData;
import coffee.cypher.aptitude.items.AptitudeIncreaseItem;
import coffee.cypher.aptitude.mixinaccessors.AptitudeVillagerDataAccessor;
import coffee.cypher.aptitude.mixinaccessors.MixinUtilKt;
import coffee.cypher.aptitude.registry.RegistryKt;
import com.mojang.serialization.DataResult;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.ai.brain.sensor.SensorType;
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

import java.util.HashSet;

@Mixin(VillagerEntity.class)
abstract class AptitudeVillagerEntityMixin implements AptitudeVillagerDataAccessor {
    AptitudeVillagerData aptitude$aptitudeVillagerData = AptitudeVillagerData.createRandom();

    @Inject(at = @At("TAIL"), method = "readCustomDataFromNbt")
    void aptitude$loadAptitudesFromNbt(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains("AptitudeData")) {
            DataResult<AptitudeVillagerData> dataResult =
                AptitudeVillagerData.CODEC.parse(NbtOps.INSTANCE, nbt.get("AptitudeData"));

            dataResult.resultOrPartial((error) -> {
            }).ifPresent(avd -> aptitude$aptitudeVillagerData = avd);
        }
    }

    @Inject(at = @At("TAIL"), method = "writeCustomDataToNbt")
    void aptitude$saveAptitudesToNbt(NbtCompound nbt, CallbackInfo ci) {
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
    void aptitude$onLevelUp(CallbackInfo ci) {
        VillagerEventHandler.onVillagerLevelUp((VillagerEntity) (Object) this);
    }

    @Inject(at = @At("HEAD"), method = "interactMob", cancellable = true)
    void aptitude$interceptItemUse(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
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

    //TODO this is bad?
    @Inject(at = @At("RETURN"), method = "createBrainProfile", cancellable = true)
    void aptitude$addBrainComponents(CallbackInfoReturnable<Brain.Profile<VillagerEntity>> cir) {
        var currentProfile = cir.getReturnValue();

        var newMemories = new HashSet<>(MixinUtilKt.getMemoryModuleTypes(currentProfile));
        newMemories.add(RegistryKt.getTRACKED_UPGRADES_MEMORY_MODULE());

        var newSensors = new HashSet<>(MixinUtilKt.getSensors(currentProfile));
        newSensors.add((SensorType<? extends Sensor<? super VillagerEntity>>) RegistryKt.getWORKSTATION_UPGRADES_SENSOR_TYPE());

        cir.setReturnValue(Brain.createProfile(newMemories, newSensors));
    }
}
