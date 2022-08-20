package coffee.cypher.aptitude.mixins.accessors;

import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ScreenHandler.class)
public interface ScreenHandlerAccessor {
    @Invoker("insertItem")
    boolean aptitude$insertItem(ItemStack stack, int startIndex, int endIndex, boolean fromLast);
}
