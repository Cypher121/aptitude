@file:JvmName("VillagerTradeOfferExtras")
@file:Environment(EnvType.CLIENT)

package coffee.cypher.aptitude.gui

import coffee.cypher.aptitude.Aptitude
import coffee.cypher.aptitude.mixinaccessors.client.indexStartOffset
import coffee.cypher.aptitude.mixinaccessors.offeredByAptitudeLevel
import com.mojang.blaze3d.systems.RenderSystem
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.gui.DrawableHelper
import net.minecraft.client.gui.screen.ingame.MerchantScreen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.village.TradeOffer
import org.quiltmc.qkl.wrapper.qsl.client.screen.client

private const val INDICATOR_X = 2
private const val INDICATOR_Y = 1

private const val INDICATOR_WIDTH = 8
private const val INDICATOR_HEIGHT = 8

private const val INDICATOR_U = 117f
private const val INDICATOR_V = 206f

private const val WIDGET_OFFSET_X = 5
private const val WIDGET_OFFSET_Y = -1

fun drawAptitudeTradeIndicator(matrices: MatrixStack, tradeOffer: TradeOffer, x: Int, y: Int, z: Int) {
    if (tradeOffer.offeredByAptitudeLevel == null) {
        return
    }

    RenderSystem.setShaderTexture(0, Aptitude.id(AptitudeVillagerScreen.TEXTURE_PATH))
    DrawableHelper.drawTexture(
        matrices,
        x + INDICATOR_X, y + INDICATOR_Y, z,
        INDICATOR_U, INDICATOR_V,
        INDICATOR_WIDTH, INDICATOR_HEIGHT,
        AptitudeVillagerScreen.TEX_WIDTH, AptitudeVillagerScreen.TEX_HEIGHT
    )
    RenderSystem.setShaderTexture(0, Identifier("textures/gui/container/villager2.png"))
}

fun createTooltip(
    screen: MerchantScreen,
    index: Int,
    widget: ButtonWidget,
    matrices: MatrixStack,
    mouseX: Int,
    mouseY: Int
): Boolean {
    val offer = screen.screenHandler.recipes[index + screen.indexStartOffset]
    val level = offer.offeredByAptitudeLevel ?: return false

    if ((mouseX - (widget.x - WIDGET_OFFSET_X)) in INDICATOR_X until (INDICATOR_X + INDICATOR_WIDTH) &&
        (mouseY - (widget.y - WIDGET_OFFSET_Y)) in INDICATOR_Y until (INDICATOR_Y + INDICATOR_HEIGHT)
    ) {
        screen.renderOrderedTooltip(
            matrices,
            screen.client.textRenderer.wrapLines(
                Text.translatable("aptitude.gui.special_trade", Text.translatable("aptitude.level.${level.ordinal}")),
                150
            ),
            mouseX, mouseY
        )

        return true
    }

    return false
}
