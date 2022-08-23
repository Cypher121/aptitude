package coffee.cypher.aptitude.registry

import coffee.cypher.aptitude.Aptitude
import coffee.cypher.aptitude.abilities.UpgradeEnchantedBookAbility
import coffee.cypher.aptitude.abilities.base.NoOpAbility
import coffee.cypher.aptitude.abilities.base.VillagerAbility
import coffee.cypher.aptitude.brain.WorkstationUpgradeSensor
import coffee.cypher.aptitude.datamodel.ProfessionExtension
import coffee.cypher.aptitude.gui.AptitudeVillagerScreenHandler
import coffee.cypher.aptitude.gui.packets.AptitudeToggleVillagerScreenC2SPacket
import coffee.cypher.aptitude.items.AptitudeIncreaseItem
import coffee.cypher.aptitude.util.register
import coffee.cypher.aptitude.util.toGenericRegistry
import com.mojang.serialization.Codec
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType
import net.minecraft.entity.ai.brain.MemoryModuleType
import net.minecraft.entity.ai.brain.sensor.SensorType
import net.minecraft.item.Item
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import net.minecraft.village.VillagerProfession
import org.quiltmc.qsl.networking.api.ServerPlayNetworking
import org.quiltmc.qsl.registry.attachment.api.RegistryEntryAttachment
import java.util.*

val PROFESSION_EXTENSION_ATTACHMENT: RegistryEntryAttachment<VillagerProfession, ProfessionExtension> by lazy {
    RegistryEntryAttachment.builder(
        Registry.VILLAGER_PROFESSION,
        Aptitude.id("profession_extensions"),
        ProfessionExtension::class.java,
        ProfessionExtension.CODEC
    ).build()
}

val APTITUDE_VILLAGER_SCREEN_HANDLER: ScreenHandlerType<AptitudeVillagerScreenHandler.Client> by register {
    ExtendedScreenHandlerType(
        AptitudeVillagerScreenHandler::Client
    ) withPath "aptitude_villager_screen" toGenericRegistry Registry.SCREEN_HANDLER
}

val APTITUDE_INCREASE_ITEM: Item by register {
    AptitudeIncreaseItem withPath "aptitude_increase_item" toGenericRegistry Registry.ITEM
}

val TRACKED_UPGRADES_MEMORY_MODULE: MemoryModuleType<Int> by register {
    MemoryModuleType(Optional.of(Codec.INT)) withPath "tracked_upgrades" toGenericRegistry Registry.MEMORY_MODULE_TYPE
}

val WORKSTATION_UPGRADES_SENSOR_TYPE: SensorType<*> by register {
    SensorType(::WorkstationUpgradeSensor) withPath "workstation_upgrades" toGenericRegistry Registry.SENSOR_TYPE
}

//TODO make an actual registry or entrypoints?
val ABILITY_REGISTRY = mutableMapOf<Identifier, (AptitudeVillagerScreenHandler.Server) -> VillagerAbility>()

fun runRegistrations() {
    PROFESSION_EXTENSION_ATTACHMENT
    APTITUDE_VILLAGER_SCREEN_HANDLER
    APTITUDE_INCREASE_ITEM
    TRACKED_UPGRADES_MEMORY_MODULE
    WORKSTATION_UPGRADES_SENSOR_TYPE

    ServerPlayNetworking.registerGlobalReceiver(
        AptitudeToggleVillagerScreenC2SPacket.CHANNEL,
        AptitudeToggleVillagerScreenC2SPacket.Receiver
    )

    ABILITY_REGISTRY[Aptitude.id("no_ability")] = { NoOpAbility() }
    ABILITY_REGISTRY[Aptitude.id("upgrade_enchanted_book")] = { UpgradeEnchantedBookAbility(it) }
}
