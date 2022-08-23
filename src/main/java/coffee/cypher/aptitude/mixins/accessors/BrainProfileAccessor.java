package coffee.cypher.aptitude.mixins.accessors;

import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Collection;

@Mixin(Brain.Profile.class)
public interface BrainProfileAccessor {
    @Accessor("memoryModules")
    Collection<MemoryModuleType<?>> aptitude$getMemoryModules();

    @Accessor("sensors")
    Collection<SensorType<?>> aptitude$getSensors();
}
