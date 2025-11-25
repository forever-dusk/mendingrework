package dusk.mendingrework.mixin;

import net.minecraft.core.Holder;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ArmorItem.class)
public class NetheriteArmorMixin {
    @Shadow
    @Final
    protected Holder<ArmorMaterial> material;

    @Inject(method = "isValidRepairItem", at = @At("HEAD"), cancellable = true)
    public void changeRepairItem(ItemStack toRepair, ItemStack repair, CallbackInfoReturnable<Boolean> cir) {
        if (this.material.value().repairIngredient().get().test(new ItemStack(Items.NETHERITE_INGOT))) {
            if (repair.is(Items.DIAMOND) || repair.is(Items.NETHERITE_SCRAP)) {
                cir.setReturnValue(true);
            } else if (repair.is(Items.NETHERITE_INGOT)) {
                cir.setReturnValue(false);
            }
        }

        if (repair.is(Items.EXPERIENCE_BOTTLE)) {
            cir.setReturnValue(false);
        }
    }
}
