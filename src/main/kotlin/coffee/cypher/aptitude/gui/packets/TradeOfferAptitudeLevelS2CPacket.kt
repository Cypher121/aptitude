package coffee.cypher.aptitude.gui.packets

import coffee.cypher.aptitude.Aptitude
import coffee.cypher.aptitude.datamodel.AptitudeLevel
import coffee.cypher.aptitude.mixinaccessors.offeredByAptitudeLevel
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.ClientPlayNetworkHandler
import net.minecraft.network.PacketByteBuf
import net.minecraft.screen.MerchantScreenHandler
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.village.TradeOffer
import net.minecraft.village.TradeOfferList
import org.quiltmc.qsl.networking.api.PacketByteBufs
import org.quiltmc.qsl.networking.api.PacketSender
import org.quiltmc.qsl.networking.api.ServerPlayNetworking
import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking.ChannelReceiver

class TradeOfferAptitudeLevelS2CPacket(val syncId: Int, val levels: List<AptitudeLevel?>) {
    constructor(syncId: Int, tradeOffers: TradeOfferList) : this(
        syncId,
        tradeOffers.map(TradeOffer::offeredByAptitudeLevel)
    )

    fun send(player: ServerPlayerEntity) {
        val buf = PacketByteBufs.create().apply {
            writeVarInt(syncId)

            writeVarInt(levels.size)
            levels.forEach {
                writeVarInt(it?.ordinal ?: -1)
            }
        }

        ServerPlayNetworking.send(player, CHANNEL, buf)
    }

    companion object {
        val CHANNEL = Aptitude.id("trade_offer_aptitude_level")
    }

    object Receiver : ChannelReceiver {
        override fun receive(
            client: MinecraftClient,
            handler: ClientPlayNetworkHandler,
            buf: PacketByteBuf,
            responseSender: PacketSender
        ) {
            val packet = TradeOfferAptitudeLevelS2CPacket(buf.readVarInt(), List(buf.readVarInt()) {
                val idx = buf.readVarInt()

                if (idx < 0) {
                    null
                } else {
                    enumValues<AptitudeLevel>()[idx]
                }
            })

            client.execute {
                val screenHandler = client.player?.currentScreenHandler

                if (packet.syncId == screenHandler?.syncId && screenHandler is MerchantScreenHandler) {
                    screenHandler.recipes.zip(packet.levels).forEach { (trade, level) ->
                        trade.offeredByAptitudeLevel = level
                    }
                } else {
                    Aptitude.logger.info("Failed! SyncId ${packet.syncId} vs ${screenHandler?.syncId}, screenHandler is $screenHandler")
                }
            }
        }

    }
}
