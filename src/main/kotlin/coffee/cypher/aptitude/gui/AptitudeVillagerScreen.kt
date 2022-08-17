@file:Environment(EnvType.CLIENT)

package coffee.cypher.aptitude.gui

import coffee.cypher.aptitude.Aptitude
import coffee.cypher.aptitude.datamodel.AptitudeLevel
import coffee.cypher.aptitude.gui.packets.AptitudeToggleVillagerScreenC2SPacket
import com.mojang.blaze3d.systems.RenderSystem
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.gui.Drawable
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.gui.widget.ButtonWidget.PressAction
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.client.gui.widget.EntryListWidget
import net.minecraft.client.gui.widget.TexturedButtonWidget
import net.minecraft.client.render.GameRenderer
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.text.ScreenTexts
import net.minecraft.text.Text
import net.minecraft.util.Language
import net.minecraft.village.VillagerProfession
import java.util.function.Consumer
import org.quiltmc.qkl.wrapper.qsl.client.screen.client as clientNotNull

private const val TEXTURE_PATH = "textures/gui/aptitude_villager_bg.png"

@Environment(EnvType.CLIENT)
class AptitudeVillagerScreen(
    screenHandler: AptitudeVillagerScreenHandler.Client,
    playerInventory: PlayerInventory,
    text: Text
) : HandledScreen<AptitudeVillagerScreenHandler.Client>(
    screenHandler,
    playerInventory,
    text
) {
    val list = handler.aptitudes.toList()
        .sortedByDescending { it.second.second.ordinal }

    override fun drawBackground(
        matrices: MatrixStack,
        delta: Float,
        mouseX: Int,
        mouseY: Int
    ) {
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

        backgroundWidth = 276
        backgroundHeight = 196
        playerInventoryTitleX = 107
        playerInventoryTitleY += 31
        titleX =
            (backgroundWidth - (client?.textRenderer?.getWidth(
                Language.getInstance().reorder(title)
            ) ?: 0)) / 2

        addDrawableChild(ReturnToMerchantButton())
        list.forEach {
            addDrawable(Entry(5, 26, 88, 20, it))
        }
    }

    @Environment(EnvType.CLIENT)
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
            AptitudeToggleVillagerScreenC2SPacket(
                screenHandler.syncId,
                false
            ).send()
        },
        object : TooltipSupplier {
            override fun onTooltip(
                buttonWidget: ButtonWidget,
                matrixStack: MatrixStack,
                i: Int,
                j: Int
            ) {
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

    @Environment(EnvType.CLIENT)
    inner class Entry(
        val entryX: Int, val entryY: Int,
        val entryWidth: Int, val entryHeight: Int,
        val entry: Pair<VillagerProfession, Pair<AptitudeLevel, AptitudeLevel>>
    ) : Drawable {
        override fun render(
            matrices: MatrixStack,
            mouseX: Int,
            mouseY: Int,
            tickDelta: Float
        ) {
            val mid = entryY + 10 + (backgroundHeight - height) / 2
            val left = entryX + 5 + (backgroundWidth - width) / 2
            textRenderer.draw(
                matrices,
                Text.literal(entry.second.first.name),
                left.toFloat(),
                mid.toFloat(),
                4210752
            )
        }
    }
}
