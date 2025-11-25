package dusk.mendingrework.mixin;

import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Shadow
    public abstract boolean is(TagKey<Item> tagKey);

    @Shadow
    public abstract DataComponentMap getComponents();

    @Shadow
    public abstract boolean is(Item item);

    @Inject(method = "isValidRepairItem", at = @At("HEAD"), cancellable = true)
    public void addDefaultRepairItem(ItemStack itemStack, CallbackInfoReturnable<Boolean> cir) {
        if (this.is(Items.BOW)) {
            if (itemStack.is(Items.STRING)) {
                cir.setReturnValue(true);
            }
        } else if (this.is(Items.FLINT_AND_STEEL)) {
            if (itemStack.is(Items.FLINT)) {
                cir.setReturnValue(true);
            }
        } else if (this.is(ItemTags.DURABILITY_ENCHANTABLE) && !this.getComponents().has(DataComponents.REPAIRABLE)
                && itemStack.is(Items.EXPERIENCE_BOTTLE)) {
            cir.setReturnValue(true);
        }
    }
}
