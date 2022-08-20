package coffee.cypher.aptitude.mixinaccessors.client

import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment

@Environment(EnvType.CLIENT)
interface AptitudeMerchantScreenAccessor {
    val `aptitude$indexStartOffset`: Int
}
