@file:Environment(EnvType.CLIENT)

package coffee.cypher.aptitude.mixinaccessors.client

import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.gui.screen.ingame.MerchantScreen

val MerchantScreen.indexStartOffset
    get() = (this as AptitudeMerchantScreenAccessor).`aptitude$indexStartOffset`
