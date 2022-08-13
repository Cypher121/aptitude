package coffee.cypher.aptitude.registry

import coffee.cypher.aptitude.Aptitude
import coffee.cypher.aptitude.model.ProfessionExtension
import net.minecraft.util.registry.Registry
import net.minecraft.village.VillagerProfession
import org.quiltmc.qsl.registry.attachment.api.RegistryEntryAttachment

val PROFESSION_EXTENSION_ATTACHMENT: RegistryEntryAttachment<VillagerProfession, ProfessionExtension> =
    RegistryEntryAttachment.builder(
        Registry.VILLAGER_PROFESSION,
        Aptitude.id("profession_extensions"),
        ProfessionExtension::class.java,
        ProfessionExtension.CODEC
    ).build()
