@file:Suppress("KotlinConstantConditions", "UNCHECKED_CAST")

package coffee.cypher.aptitude.mixinaccessors

import coffee.cypher.aptitude.mixins.accessors.BrainProfileAccessor
import coffee.cypher.aptitude.mixins.accessors.MerchantEntityAccessor
import coffee.cypher.aptitude.mixins.accessors.MerchantScreenHandlerAccessor
import coffee.cypher.aptitude.mixins.accessors.ScreenHandlerAccessor
import coffee.cypher.aptitude.mixins.accessors.SensorTypeAccessor
import coffee.cypher.aptitude.mixins.accessors.VillagerEntityAccessor
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.ai.brain.Brain
import net.minecraft.entity.ai.brain.MemoryModuleType
import net.minecraft.entity.ai.brain.sensor.Sensor
import net.minecraft.entity.ai.brain.sensor.SensorType
import net.minecraft.entity.passive.MerchantEntity
import net.minecraft.entity.passive.VillagerEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.screen.MerchantScreenHandler
import net.minecraft.screen.ScreenHandler
import net.minecraft.village.TradeOffer
import net.minecraft.village.TradeOfferList
import net.minecraft.village.TradeOffers

val MerchantScreenHandler.merchant
    get() = (this as MerchantScreenHandlerAccessor).`aptitude$getMerchant`()

fun ScreenHandler.insertItem(stack: ItemStack, slotRange: IntRange, fromLast: Boolean = false): Boolean =
    (this as ScreenHandlerAccessor).`aptitude$insertItem`(stack, slotRange.first, slotRange.last, fromLast)

fun MerchantEntity.fillRecipesFromPool(recipeList: TradeOfferList, pool: List<TradeOffers.Factory>, count: Int) {
    (this as MerchantEntityAccessor).`aptitude$fillRecipesFromPool`(recipeList, pool.toTypedArray(), count)
}

var MerchantEntity.offerList: TradeOfferList
    get() = offers
    set(value) { (this as MerchantEntityAccessor).`aptitude$setOffers`(value) }

fun VillagerEntity.beginTradeWith(player: PlayerEntity) {
    (this as VillagerEntityAccessor).`aptitude$beginTradeWith`(player)
}

var TradeOffer.offeredByAptitudeLevel
    get() = (this as AptitudeTradeOfferAccessor).`aptitude$offeredByAptitudeLevel`
    set(value) { (this as AptitudeTradeOfferAccessor).`aptitude$offeredByAptitudeLevel` = value }

val Brain.Profile<*>.memoryModuleTypes: Collection<MemoryModuleType<*>>
    get() = (this as BrainProfileAccessor).`aptitude$getMemoryModules`()

val <E : LivingEntity> Brain.Profile<E>.sensors: Collection<SensorType<out Sensor<in E>>>
    get() = (this as BrainProfileAccessor).`aptitude$getSensors`() as Collection<SensorType<out Sensor<in E>>>

fun <U : Sensor<in Any?>> SensorType(factory: () -> U): SensorType<U> {
    return SensorTypeAccessor.`aptitude$createSensorType`(factory)
}
