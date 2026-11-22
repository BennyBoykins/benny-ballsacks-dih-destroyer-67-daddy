package dev.benny.tools;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Tracks armor durability and totem count; shows action-bar warnings. */
public final class ArmorMonitor {
    public static final EquipmentSlot[] SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };
    public static final String[] NAMES = {"Helmet", "Chest", "Legs", "Boots"};

    private static final boolean[] warned = new boolean[4];
    private static int lastTotems = -1;

    private ArmorMonitor() {}

    /** Durability percent 0-100, or -1 if the slot is empty or the item has no durability. */
    public static int percent(Player p, EquipmentSlot slot) {
        ItemStack s = p.getItemBySlot(slot);
        if (s.isEmpty() || !s.isDamageableItem() || s.getMaxDamage() <= 0) return -1;
        return Math.round(100f * (s.getMaxDamage() - s.getDamageValue()) / s.getMaxDamage());
    }

    public static int totems(Player p) {
        int n = 0;
        for (int i = 0; i < p.getInventory().getContainerSize(); i++) {
            ItemStack s = p.getInventory().getItem(i);
            if (s.is(Items.TOTEM_OF_UNDYING)) n += s.getCount();
        }
        return n;
    }

    public static void tick(Minecraft mc) {
        Player p = mc.player;
        if (p == null) { lastTotems = -1; return; }

        int total = totems(p);
        if (Config.armorWarnings && lastTotems > 0 && total == 0) {
            p.displayClientMessage(Component.literal("\u00a7cOut of totems!"), true);
        }
        lastTotems = total;

        if (!Config.armorWarnings) return;
        for (int i = 0; i < SLOTS.length; i++) {
            int pc = percent(p, SLOTS[i]);
            if (pc < 0) { warned[i] = false; continue; }
            if (pc <= 10 && !warned[i]) {
                warned[i] = true;
                p.displayClientMessage(Component.literal("\u00a7c" + NAMES[i] + " durability low: " + pc + "%"), true);
            } else if (pc > 15) {
                warned[i] = false;
            }
        }
    }
}
