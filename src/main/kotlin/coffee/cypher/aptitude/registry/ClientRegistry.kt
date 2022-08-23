@file:Environment(EnvType.CLIENT)

package coffee.cypher.aptitude.registry

import coffee.cypher.aptitude.Aptitude
import coffee.cypher.aptitude.abilities.UpgradeEnchantedBookAbilityClient
import coffee.cypher.aptitude.abilities.base.NoOpAbilityClient
import coffee.cypher.aptitude.abilities.base.client.VillagerAbilityClient
import coffee.cypher.aptitude.gui.AptitudeVillagerScreen
import coffee.cypher.aptitude.gui.AptitudeVillagerScreenHandler
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.gui.screen.ingame.HandledScreens
import net.minecraft.util.Identifier

val CLIENT_ABILITY_REGISTRY = mutableMapOf<Identifier, (AptitudeVillagerScreenHandler.Client) -> VillagerAbilityClient>()

fun runClientRegistrations() {
    HandledScreens.register(APTITUDE_VILLAGER_SCREEN_HANDLER, HandledScreens.Provider(::AptitudeVillagerScreen))

    CLIENT_ABILITY_REGISTRY[Aptitude.id("no_ability")] = { NoOpAbilityClient() }
    CLIENT_ABILITY_REGISTRY[Aptitude.id("upgrade_enchanted_book")] = { UpgradeEnchantedBookAbilityClient(it) }
}
