package coffee.cypher.aptitude

import coffee.cypher.aptitude.registry.PROFESSION_EXTENSION_ATTACHMENT
import net.minecraft.util.Identifier
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.quiltmc.loader.api.ModContainer
import org.quiltmc.qkl.wrapper.qsl.registerEvents
import org.quiltmc.qkl.wrapper.qsl.resource.onDataPackReloadFinish
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer

@Suppress("unused")
object Aptitude : ModInitializer {
    lateinit var id: String
        private set

    val logger: Logger = LogManager.getLogger("Aptitude")

    fun id(name: String): Identifier =
        Identifier(id, name)

    override fun onInitialize(mod: ModContainer) {
        id = mod.metadata().id()
        logger.info("Loading Aptitude!")
        PROFESSION_EXTENSION_ATTACHMENT

        registerEvents {
            onDataPackReloadFinish { _, _, _ ->
                logger.info("Professions have ${PROFESSION_EXTENSION_ATTACHMENT.keySet().size} attachments")
                PROFESSION_EXTENSION_ATTACHMENT.entryIterator().forEach {
                    logger.info("${it.entry.name} associated with ${it.value}")
                }
            }
        }
    }
}
