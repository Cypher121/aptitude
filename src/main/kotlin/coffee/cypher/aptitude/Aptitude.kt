package coffee.cypher.aptitude

import coffee.cypher.aptitude.registry.PROFESSION_EXTENSION_ATTACHMENT
import coffee.cypher.aptitude.registry.runRegistrations
import coffee.cypher.aptitude.util.registerDebugUtils
import net.minecraft.util.Identifier
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.quiltmc.loader.api.ModContainer
import org.quiltmc.qkl.wrapper.qsl.registerEvents
import org.quiltmc.qkl.wrapper.qsl.resource.onDataPackReloadFinish
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer

//TODO button on trading screen, goes to aptitude screen
//TODO hook into level up/refresh trades for adding extra trades
//TODO add memories/poi/tasks for advanced job sites
//TODO(maybe) hook into refreshing trades, refresh advanced trades on advanced job site
//TODO actually come up with effects
//TODO item for nitwit replacement. aptitude 3 in random job, locked to that job (nbt hook into job loss/swap?)

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
        runRegistrations()

        registerEvents {
            onDataPackReloadFinish { _, _, _ ->
                logger.info("Professions have ${PROFESSION_EXTENSION_ATTACHMENT.keySet().size} attachments")
                PROFESSION_EXTENSION_ATTACHMENT.entryIterator().forEach {
                    logger.info("${it.entry.name} associated with ${it.value}")
                }
            }
        }

        registerDebugUtils()
    }
}
