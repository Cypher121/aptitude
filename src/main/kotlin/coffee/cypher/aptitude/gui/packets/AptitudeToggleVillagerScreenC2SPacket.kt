package coffee.cypher.aptitude.gui.packets

import coffee.cypher.aptitude.Aptitude
import net.minecraft.client.network.ClientPlayNetworkHandler
import net.minecraft.network.ClientConnection
import net.minecraft.network.Packet
import net.minecraft.network.PacketByteBuf
import net.minecraft.network.listener.PacketListener
import net.minecraft.network.listener.ServerPlayPacketListener
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayNetworkHandler
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import org.quiltmc.qsl.networking.api.PacketSender
import org.quiltmc.qsl.networking.api.ServerPlayNetworking
import org.quiltmc.qsl.networking.api.ServerPlayNetworking.ChannelReceiver

class AptitudeToggleVillagerScreenC2SPacket(enableAptitude: Boolean) {
    fun send() {
        ClientPlayNetworkHandler.
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

        }
    }
}
