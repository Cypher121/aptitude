package coffee.cypher.aptitude.gui.packets

import coffee.cypher.aptitude.Aptitude
import coffee.cypher.aptitude.gui.AptitudeVillagerScreenHandler
import coffee.cypher.aptitude.mixinaccessors.beginTradeWith
import coffee.cypher.aptitude.mixinaccessors.merchant
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory
import net.minecraft.entity.passive.VillagerEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.network.PacketByteBuf
import net.minecraft.screen.MerchantScreenHandler
import net.minecraft.screen.ScreenHandler
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayNetworkHandler
import net.minecraft.server.network.ServerPlayerEntity
import org.quiltmc.qsl.networking.api.PacketByteBufs
import org.quiltmc.qsl.networking.api.PacketSender
import org.quiltmc.qsl.networking.api.ServerPlayNetworking.ChannelReceiver
import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking

class AptitudeToggleVillagerScreenC2SPacket(val syncId: Int, val enableAptitude: Boolean) {
    @Environment(EnvType.CLIENT)
    fun send() {
        ClientPlayNetworking.send(CHANNEL, PacketByteBufs.create().also {
            it.writeInt(syncId)
            it.writeBoolean(enableAptitude)
        })
    }

    companion object {
        val CHANNEL = Aptitude.id("toggle_aptitude_screen")
    }

    object Receiver : ChannelReceiver {
        override fun receive(
            server: MinecraftServer,
            player: ServerPlayerEntity,
            handler: ServerPlayNetworkHandler,
            buf: PacketByteBuf,
            responseSender: PacketSender
        ) {
            val packet = AptitudeToggleVillagerScreenC2SPacket(buf.readInt(), buf.readBoolean())

            if (player.currentScreenHandler?.syncId != packet.syncId) {
                return
            }

            if (packet.enableAptitude) {
                val screenHandler = player.currentScreenHandler as? MerchantScreenHandler ?: return

                val villager = screenHandler.merchant as? VillagerEntity ?: return

                server.execute {
                    player.closeHandledScreen()

                    player.openHandledScreen(object : ExtendedScreenHandlerFactory {
                        override fun createMenu(
                            syncId: Int,
                            playerInventory: PlayerInventory,
                            playerEntity: PlayerEntity
                        ): ScreenHandler {
                            return AptitudeVillagerScreenHandler.Server(
                                syncId,
                                playerInventory,
                                villager
                            )
                        }

                        override fun getDisplayName() = villager.displayName

                        override fun writeScreenOpeningData(player: ServerPlayerEntity, buf: PacketByteBuf) {
                            AptitudeVillagerScreenHandler.writeBuffer(buf, villager)
                        }
                    })

                    villager.currentCustomer = player
                }
            } else {
                val screenHandler = player.currentScreenHandler as? AptitudeVillagerScreenHandler.Server ?: return

                server.execute {
                    player.closeHandledScreen()

                    screenHandler.villager.beginTradeWith(player)
                }
            }
        }
    }
}
