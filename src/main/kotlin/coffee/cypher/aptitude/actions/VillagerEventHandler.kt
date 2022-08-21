@file:JvmName("VillagerEventHandler")

package coffee.cypher.aptitude.actions

import coffee.cypher.aptitude.datamodel.AptitudeLevel
import coffee.cypher.aptitude.datamodel.aptitudeData
import coffee.cypher.aptitude.mixinaccessors.fillRecipesFromPool
import coffee.cypher.aptitude.mixinaccessors.offerList
import coffee.cypher.aptitude.mixinaccessors.offeredByAptitudeLevel
import coffee.cypher.kettle.item.equals
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import net.minecraft.entity.passive.VillagerEntity
import net.minecraft.item.ItemStack
import net.minecraft.village.TradeOffer
import net.minecraft.village.TradeOfferList
import net.minecraft.village.TradeOffers.*
import java.util.*

fun increaseAptitude(villagerEntity: VillagerEntity) {
    val profession = villagerEntity.villagerData.profession
    val profLevel = villagerEntity.villagerData.level

    val (current, max) = villagerEntity.aptitudeData.getAptitudeLevels(profession)

    if (max <= current || profLevel < current.next.professionLevel) {
        return
    }

    villagerEntity.aptitudeData = villagerEntity.aptitudeData.withCurrentAptitude(profession, current.next)

    for (i in 1..profLevel) {
        addTradesForAptitudeAndProfessionLevel(villagerEntity, current.next, i)
    }
}

fun onVillagerLevelUp(villagerEntity: VillagerEntity) {
    val profession = villagerEntity.villagerData.profession
    val aptLevel = villagerEntity.aptitudeData.getCurrentAptitude(profession)
    val profLevel = villagerEntity.villagerData.level

    for (i in 1..aptLevel.ordinal) {
        addTradesForAptitudeAndProfessionLevel(villagerEntity, enumValues<AptitudeLevel>()[i], profLevel)
    }
}

val REPEATABLE_FACTORIES = listOf(
    EnchantBookFactory::class.java,
    SellDyedArmorFactory::class.java,
    SellEnchantedToolFactory::class.java,
    SellPotionHoldingItemFactory::class.java
)

class TradeOfferDescriptor(
    val first: ItemStack,
    val second: ItemStack,
    val result: ItemStack
) {
    constructor(tradeOffer: TradeOffer) : this(
        tradeOffer.originalFirstBuyItem,
        tradeOffer.secondBuyItem,
        tradeOffer.sellItem
    )

    override fun equals(other: Any?): Boolean {
        if (other !is TradeOfferDescriptor) {
            return false
        }

        return first.equals(other.first, ignoreDurability = true, ignoreSize = true) &&
                second.equals(other.second, ignoreDurability = true, ignoreSize = true) &&
                result.equals(other.result, ignoreDurability = true, ignoreSize = true)
    }

    override fun hashCode(): Int {
        return Objects.hash(
            first.item,
            second.item,
            result.item
        )
    }
}

/*
 * TODO find a way to determine what trades come from what factory
 *      and use same factory multiple times, e.g. multiple enchant books
 */
fun addTradesForAptitudeAndProfessionLevel(
    villagerEntity: VillagerEntity,
    aptitudeLevel: AptitudeLevel,
    professionLevel: Int
) {
    val profession = villagerEntity.villagerData.profession

    val originalUniqueTrades =
        villagerEntity.offers.distinctBy { TradeOfferDescriptor(it) }.size
    val originalTrades = PROFESSION_TO_LEVELED_TRADE.getOrDefault(profession, Int2ObjectOpenHashMap())
        .getOrDefault(professionLevel, emptyArray())

    val attempts = List(20) { i ->
        val newOffers = TradeOfferList()
        newOffers += villagerEntity.offers

        val duplicatedTrades = originalTrades.flatMap { factory ->
            if (REPEATABLE_FACTORIES.any { it.isAssignableFrom(factory.javaClass) })
                List(1 + i / 5) { factory }
            else {
                listOf(factory)
            }
        }.map { factory ->
            Factory { entity, random ->
                factory.create(entity, random).also { it?.offeredByAptitudeLevel = aptitudeLevel }
            }
        }

        villagerEntity.fillRecipesFromPool(
            newOffers,
            duplicatedTrades,
            1
        )

        val uniqueCount = newOffers.distinctBy { TradeOfferDescriptor(it) }.size

        newOffers to uniqueCount
    }

    val (bestAttempt, bestAttemptUniques) = attempts.maxBy { it.second }

    if (bestAttemptUniques > originalUniqueTrades) {
        villagerEntity.offerList = bestAttempt
    }
}
