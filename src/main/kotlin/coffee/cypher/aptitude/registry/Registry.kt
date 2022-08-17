package coffee.cypher.aptitude.registry

import coffee.cypher.aptitude.Aptitude
import coffee.cypher.aptitude.datamodel.ProfessionExtension
import coffee.cypher.aptitude.gui.AptitudeVillagerScreenHandler
import coffee.cypher.aptitude.gui.packets.AptitudeToggleVillagerScreenC2SPacket
import coffee.cypher.aptitude.util.register
import coffee.cypher.aptitude.util.toGenericRegistry
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry
import net.minecraft.screen.ScreenHandlerType
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

val APTITUDE_VILLAGER_SCREEN_HANDLER: ScreenHandlerType<AptitudeVillagerScreenHandler> by register {
    ExtendedScreenHandlerType<AptitudeVillagerScreenHandler>(
        AptitudeVillagerScreenHandler::Client
    ) withPath "aptitude_villager_screen" toGenericRegistry Registry.SCREEN_HANDLER
}

fun runRegistrations() {
    PROFESSION_EXTENSION_ATTACHMENT
    APTITUDE_VILLAGER_SCREEN_HANDLER

    ServerPlayNetworking.registerGlobalReceiver(
        AptitudeToggleVillagerScreenC2SPacket.CHANNEL,
        AptitudeToggleVillagerScreenC2SPacket.Receiver
    )
}
