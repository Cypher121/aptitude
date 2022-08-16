package coffee.cypher.aptitude.mixinaccessors

import net.minecraft.screen.MerchantScreenHandler
import net.minecraft.village.Merchant

interface MerchantAccessor {
    val merchant: Merchant
}

val MerchantScreenHandler.merchant
    get() = (this as MerchantAccessor).merchant
