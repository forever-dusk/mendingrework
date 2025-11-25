package dusk.mendingrework;

import net.minecraft.core.component.DataComponents;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.AnvilRepairEvent;

import java.util.Objects;

@Mod(MendingRework.MODID)
public class MendingRework {
    public static final String MODID = "mendingrework";

    public MendingRework(IEventBus ignoredModEventBus, ModContainer ignoredModContainer) {
        NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void modifyAnvilDamage(AnvilRepairEvent event) {
        event.setBreakChance(0.06f);
        if (Objects.requireNonNull(event.getLeft()
                .get(DataComponents.ENCHANTMENTS)).toString().contains("Mending")) {
            if (event.getLeft().getItem().isValidRepairItem(event.getLeft(), event.getRight())) {
                event.setBreakChance(0);
            } else if (!event.getLeft().getHoverName().equals(event.getOutput().getHoverName())
                    && event.getRight().isEmpty()) {
                event.setBreakChance(0);
            }
        }
    }
}
