package coffee.cypher.aptitude.gui

import coffee.cypher.aptitude.Aptitude
import coffee.cypher.aptitude.mixinaccessors.merchant
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.gui.screen.ingame.MerchantScreen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.gui.widget.ButtonWidget.PressAction
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.passive.VillagerEntity
import net.minecraft.screen.SimpleNamedScreenHandlerFactory
import net.minecraft.text.Text
import org.quiltmc.qkl.wrapper.qsl.client.screen.client
import java.util.function.Consumer

@Environment(EnvType.CLIENT)
class OpenAptitudeScreenButton(
    screen: MerchantScreen,
    backgroundWidth: Int,
    backgroundHeight: Int
) : ButtonWidget(
    (screen.width + backgroundWidth) / 2 - 25,
    (screen.height - backgroundHeight) / 2 + 5,
    20,
    20,
    Text.literal("A"),
    PressAction {
        val villager = screen.screenHandler.merchant as VillagerEntity

        screen.client.player?.openHandledScreen(
            SimpleNamedScreenHandlerFactory({ i, playerInventory, _ ->
                AptitudeVillagerScreenHandler(
                    i,
                    playerInventory,
                    villager
                )
            }, villager.displayName)
        )
    },
    object : TooltipSupplier {
        override fun onTooltip(buttonWidget: ButtonWidget, matrixStack: MatrixStack, i: Int, j: Int) {
            screen.renderOrderedTooltip(
                matrixStack,
                screen.client.textRenderer.wrapLines(
                    Text.translatable("aptitude.gui.open_screen"),
                    100
                ),
                i, j
            )
        }

        override fun supply(consumer: Consumer<Text>) {
            consumer.accept(Text.translatable("aptitude.gui.open_screen"))
        }
    }
) {
    init {
        Aptitude.logger.info(":AAAAAAAAAAAAAAAAAAAAAAAAA:")
    }
}
