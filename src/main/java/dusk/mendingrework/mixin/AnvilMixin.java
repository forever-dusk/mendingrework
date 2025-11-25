package dusk.mendingrework.mixin;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(AnvilMenu.class)
public abstract class AnvilMixin extends ItemCombinerMenu {
    public AnvilMixin(@Nullable MenuType<?> menuType, int containerId, Inventory inventory, ContainerLevelAccess access) {
        super(menuType, containerId, inventory, access);
    }

    @Shadow
    public int repairItemCountCost;

    @Shadow
    @javax.annotation.Nullable
    private String itemName;

    @Shadow
    public abstract int getCost();

    @Shadow
    @Final
    private DataSlot cost;

    @Unique
    public boolean mendingrework_1_21_10_neo$onlyRepairing = false;

    @Inject(method = "mayPickup", at = @At("TAIL"), cancellable = true)
    public void allowZeroCost(Player player, boolean hasItem, CallbackInfoReturnable<Boolean> cir) {
        if (mendingrework_1_21_10_neo$onlyRepairing) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "createResult", at = @At("HEAD"))
    public void resetRepairingStatus(CallbackInfo ci) {
        mendingrework_1_21_10_neo$onlyRepairing = false;
    }

    @Inject(method = "createResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/AnvilMenu;broadcastChanges()V"))
    public void removeRepairCost(CallbackInfo ci) {
        if (this.repairItemCountCost > 0) {
            ItemStack inputItem = this.inputSlots.getItem(0).copy();
            ItemStack outputItem = this.inputSlots.getItem(0).copy();

            if (Objects.requireNonNull(inputItem.get(DataComponents.ENCHANTMENTS)).toString().contains("Mending")) {
                int repairAmount = Math.min(inputItem.getDamageValue(), (int) (inputItem.getMaxDamage() * 0.35));
                int timesRepaired;

                for (timesRepaired = 0; repairAmount > 0 && timesRepaired < this.repairItemCountCost; timesRepaired++) {
                    int damageLeft = outputItem.getDamageValue() - repairAmount;
                    outputItem.setDamageValue(damageLeft);
                    repairAmount = Math.min(outputItem.getDamageValue(), repairAmount);
                }

                this.repairItemCountCost = timesRepaired;
            } else {
                int repairAmount = Math.min(inputItem.getDamageValue(), inputItem.getMaxDamage() / 4);
                int timesRepaired;

                for (timesRepaired = 0; repairAmount > 0 && timesRepaired < this.repairItemCountCost; timesRepaired++) {
                    int damageLeft = outputItem.getDamageValue() - repairAmount;
                    outputItem.setDamageValue(damageLeft);
                    repairAmount = Math.min(outputItem.getDamageValue(), repairAmount);
                }

                this.repairItemCountCost = timesRepaired;
            }

            mendingrework_1_21_10_neo$onlyRepairing = true;
            if (this.itemName != null && !StringUtil.isBlank(this.itemName)) {
                if (!this.itemName.equals(inputItem.getHoverName().getString())) {
                    outputItem.set(DataComponents.CUSTOM_NAME, Component.literal(this.itemName));
                    mendingrework_1_21_10_neo$onlyRepairing = false;
                }
            } else if (inputItem.has(DataComponents.CUSTOM_NAME)) {
                outputItem.remove(DataComponents.CUSTOM_NAME);
                mendingrework_1_21_10_neo$onlyRepairing = false;
            }

            if (mendingrework_1_21_10_neo$onlyRepairing) {
                this.cost.set(0);
            } else {
                this.cost.set(Math.min(39, this.getCost() - this.repairItemCountCost));
            }
            this.resultSlots.setItem(0, outputItem);
        }
    }
}
