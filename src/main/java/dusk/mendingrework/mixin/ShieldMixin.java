package dusk.mendingrework.mixin;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ShieldItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShieldItem.class)
public class ShieldMixin {
    @Inject(method = "isValidRepairItem", at = @At("HEAD"), cancellable = true)
    public void changeRepairItem(ItemStack toRepair, ItemStack repair, CallbackInfoReturnable<Boolean> cir) {
        if (repair.is(Items.EXPERIENCE_BOTTLE)) {
            cir.setReturnValue(false);
        }
    }
}
