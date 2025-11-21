package dusk.mendingrework;

import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Repairable;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;

@Mod(MendingRework.MODID)
public class MendingRework {
    public static final String MODID = "mendingrework";

    public MendingRework(IEventBus modEventBus, ModContainer ignoredModContainer) {
        modEventBus.addListener(this::alterRepairMaterials);
    }

    @SuppressWarnings("deprecation")
    public void alterRepairMaterials(ModifyDefaultComponentsEvent event) {
        event.modify(Items.BOW, builder -> builder.set(
                DataComponents.REPAIRABLE,
                new Repairable(HolderSet.direct(Items.STRING.builtInRegistryHolder()))));
        event.modify(Items.FISHING_ROD, builder -> builder.set(
                DataComponents.REPAIRABLE,
                new Repairable(HolderSet.direct(Items.STRING.builtInRegistryHolder()))));
        event.modify(Items.BRUSH, builder -> builder.set(
                DataComponents.REPAIRABLE,
                new Repairable(HolderSet.direct(Items.FEATHER.builtInRegistryHolder()))));
        event.modify(Items.FLINT_AND_STEEL, builder -> builder.set(
                DataComponents.REPAIRABLE,
                new Repairable(HolderSet.direct(Items.FLINT.builtInRegistryHolder()))));
        event.modify(Items.CROSSBOW, builder -> builder.set(
                DataComponents.REPAIRABLE,
                new Repairable(HolderSet.direct(Items.IRON_INGOT.builtInRegistryHolder()))));
        event.modify(Items.SHEARS, builder -> builder.set(
                DataComponents.REPAIRABLE,
                new Repairable(HolderSet.direct(Items.IRON_INGOT.builtInRegistryHolder()))));
        event.modify(Items.TRIDENT, builder -> builder.set(
                DataComponents.REPAIRABLE,
                new Repairable(HolderSet.direct(Items.DIAMOND.builtInRegistryHolder()))));
        event.modify(Items.CARROT_ON_A_STICK, builder -> builder.set(
                DataComponents.REPAIRABLE,
                new Repairable(HolderSet.direct(Items.CARROT.builtInRegistryHolder()))));
        event.modify(Items.WARPED_FUNGUS_ON_A_STICK, builder -> builder.set(
                DataComponents.REPAIRABLE,
                new Repairable(HolderSet.direct(Items.WARPED_FUNGUS.builtInRegistryHolder()))));

        event.modifyMatching(item -> item.getDefaultInstance().isCombineRepairable() &&
                !item.getDefaultInstance().has(DataComponents.REPAIRABLE), builder -> builder.set(
                DataComponents.REPAIRABLE,
                new Repairable(HolderSet.direct(Items.EXPERIENCE_BOTTLE.builtInRegistryHolder()))));
    }
}
