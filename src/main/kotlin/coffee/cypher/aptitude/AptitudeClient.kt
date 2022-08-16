package coffee.cypher.aptitude

import coffee.cypher.aptitude.registry.runClientRegistrations
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import org.quiltmc.loader.api.ModContainer
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer

@Suppress("unused")
@Environment(EnvType.CLIENT)
object AptitudeClient : ClientModInitializer {
    override fun onInitializeClient(mod: ModContainer) {
        runClientRegistrations()
    }
}
