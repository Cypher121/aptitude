@file:Environment(EnvType.CLIENT)

package coffee.cypher.aptitude.gui

import coffee.cypher.aptitude.Aptitude
import coffee.cypher.aptitude.gui.packets.AptitudeToggleVillagerScreenC2SPacket
import com.mojang.blaze3d.systems.RenderSystem
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.gui.widget.ButtonWidget.PressAction
import net.minecraft.client.gui.widget.TexturedButtonWidget
import net.minecraft.client.render.GameRenderer
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.text.Text
import java.util.function.Consumer
import org.quiltmc.qkl.wrapper.qsl.client.screen.client as clientNotNull

private const val TEXTURE_PATH = "textures/gui/aptitude_villager_bg.png"

@Environment(EnvType.CLIENT)
class AptitudeVillagerScreen(
    screenHandler: AptitudeVillagerScreenHandler,
    playerInventory: PlayerInventory,
    text: Text
) : HandledScreen<AptitudeVillagerScreenHandler>(screenHandler, playerInventory, text) {
    init {
        backgroundWidth = 276
        backgroundHeight = 196
        playerInventoryTitleX = 107
        playerInventoryTitleY += 31
    }

    override fun drawBackground(matrices: MatrixStack, delta: Float, mouseX: Int, mouseY: Int) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader)
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f)
        RenderSystem.setShaderTexture(0, Aptitude.id(TEXTURE_PATH))
        val i = (width - backgroundWidth) / 2
        val j = (height - backgroundHeight) / 2
        drawTexture(
            matrices, i, j,
            zOffset, 0.0f, 0.0f, backgroundWidth, backgroundHeight, 512, 256
        )
    }

    override fun init() {
        super.init()
        addDrawableChild(ReturnToMerchantButton())
    }

    inner class ReturnToMerchantButton : TexturedButtonWidget(
        (width + backgroundWidth) / 2 - 24,
        (height - backgroundHeight) / 2 + 4,
        20,
        20,
        279,
        3,
        20,
        Aptitude.id(TEXTURE_PATH),
        512,
        256,
        PressAction {
            AptitudeToggleVillagerScreenC2SPacket(screenHandler.syncId, false).send()
        },
        object : TooltipSupplier {
            override fun onTooltip(buttonWidget: ButtonWidget, matrixStack: MatrixStack, i: Int, j: Int) {
                renderOrderedTooltip(
                    matrixStack,
                    clientNotNull.textRenderer.wrapLines(
                        Text.translatable("aptitude.gui.close_screen"),
                        100
                    ),
                    i, j
                )
            }

            override fun supply(consumer: Consumer<Text>) {
                consumer.accept(Text.translatable("aptitude.gui.close_screen"))
            }
        },
        Text.empty()
    )

    inner class AptitudesListWidget
}
