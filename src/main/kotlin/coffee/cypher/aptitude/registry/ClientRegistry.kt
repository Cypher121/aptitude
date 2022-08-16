@file:Environment(EnvType.CLIENT)

package coffee.cypher.aptitude.registry

import coffee.cypher.aptitude.gui.AptitudeVillagerScreen
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.gui.screen.ingame.HandledScreens

fun runClientRegistrations() {
    HandledScreens.register(APTITUDE_VILLAGER_SCREEN_HANDLER, HandledScreens.Provider(::AptitudeVillagerScreen))
}
