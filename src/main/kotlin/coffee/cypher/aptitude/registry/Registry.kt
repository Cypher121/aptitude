package coffee.cypher.aptitude.registry

import coffee.cypher.aptitude.Aptitude
import coffee.cypher.aptitude.abilities.UpgradeEnchantedBookAbility
import coffee.cypher.aptitude.abilities.base.NoOpAbility
import coffee.cypher.aptitude.abilities.base.VillagerAbility
import coffee.cypher.aptitude.datamodel.ProfessionExtension
import coffee.cypher.aptitude.gui.AptitudeVillagerScreenHandler
import coffee.cypher.aptitude.gui.packets.AptitudeToggleVillagerScreenC2SPacket
import coffee.cypher.aptitude.items.AptitudeIncreaseItem
import coffee.cypher.aptitude.util.register
import coffee.cypher.aptitude.util.toGenericRegistry
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType
import net.minecraft.entity.passive.VillagerEntity
import net.minecraft.item.Item
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import net.minecraft.village.VillagerProfession
import org.quiltmc.qsl.networking.api.ServerPlayNetworking
import org.quiltmc.qsl.registry.attachment.api.RegistryEntryAttachment

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

//TODO make an actual registry or entrypoints?
val ABILITY_REGISTRY = mutableMapOf<Identifier, (VillagerEntity) -> VillagerAbility>()

fun runRegistrations() {
    PROFESSION_EXTENSION_ATTACHMENT
    APTITUDE_VILLAGER_SCREEN_HANDLER
    APTITUDE_INCREASE_ITEM

    ServerPlayNetworking.registerGlobalReceiver(
        AptitudeToggleVillagerScreenC2SPacket.CHANNEL,
        AptitudeToggleVillagerScreenC2SPacket.Receiver
    )

    ABILITY_REGISTRY[Aptitude.id("no_ability")] = { NoOpAbility() }
    ABILITY_REGISTRY[Aptitude.id("upgrade_enchanted_book")] = { UpgradeEnchantedBookAbility() }
}
