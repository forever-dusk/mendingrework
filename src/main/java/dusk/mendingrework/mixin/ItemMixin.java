package dusk.mendingrework.mixin;

import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public class ItemMixin {
    @Inject(method = "isValidRepairItem", at = @At("HEAD"), cancellable = true)
    public void setDefaultRepairItem(ItemStack toRepair, ItemStack repair, CallbackInfoReturnable<Boolean> cir) {
        if (toRepair.is(ItemTags.DURABILITY_ENCHANTABLE)) {
            if (repair.is(Items.EXPERIENCE_BOTTLE)) {
                cir.setReturnValue(true);
            }
        }
    }
}
