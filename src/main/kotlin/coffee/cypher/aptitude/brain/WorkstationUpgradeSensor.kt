package coffee.cypher.aptitude.brain

import coffee.cypher.aptitude.datamodel.AptitudeLevel
import coffee.cypher.aptitude.datamodel.ProfessionExtension
import coffee.cypher.aptitude.datamodel.aptitudeData
import coffee.cypher.aptitude.registry.PROFESSION_EXTENSION_ATTACHMENT
import coffee.cypher.aptitude.registry.TRACKED_UPGRADES_MEMORY_MODULE
import coffee.cypher.kettle.math.getContainedBlockPos
import net.minecraft.block.BlockState
import net.minecraft.entity.ai.brain.MemoryModuleType
import net.minecraft.entity.ai.brain.sensor.Sensor
import net.minecraft.entity.passive.VillagerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.Box

class WorkstationUpgradeSensor : Sensor<VillagerEntity>(100) {
    override fun sense(world: ServerWorld, entity: VillagerEntity) {
        val jobPos = entity.brain.getOptionalMemory(MemoryModuleType.JOB_SITE).orElse(null)

        if (jobPos == null) {
            entity.brain.forget(TRACKED_UPGRADES_MEMORY_MODULE)
            return
        }

        if (world.registryKey != jobPos.dimension) {
            entity.brain.forget(TRACKED_UPGRADES_MEMORY_MODULE)
            return
        }

        if (entity.aptitudeData.getCurrentAptitude(entity.villagerData.profession) < AptitudeLevel.ADVANCED) {
            entity.brain.forget(TRACKED_UPGRADES_MEMORY_MODULE)
            return
        }

        val profExtension = PROFESSION_EXTENSION_ATTACHMENT[entity.villagerData.profession].orElse(null)
                as? ProfessionExtension.Regular

        if (profExtension == null) {
            entity.brain.forget(TRACKED_UPGRADES_MEMORY_MODULE)
            return
        }

        val blockPredicate: (BlockState) -> Boolean = profExtension.workstationUpgrade.station.map(
            { block -> { it.block == block } },
            { tag -> { it.holder.hasTag(tag) } }
        )

        val upgradeCount = Box(jobPos.pos).expand(6.0).getContainedBlockPos().count {
            blockPredicate(world.getBlockState(it))
        }

        entity.brain.remember(TRACKED_UPGRADES_MEMORY_MODULE, upgradeCount)
    }

    override fun getOutputMemoryModules(): Set<MemoryModuleType<*>> {
        return setOf(TRACKED_UPGRADES_MEMORY_MODULE)
    }
}
