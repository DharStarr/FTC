package net.forthecrown.cosmetics.effects.death;

import net.forthecrown.core.inventory.CrownItems;
import net.forthecrown.core.user.CrownUser;
import net.forthecrown.cosmetics.custominvs.CustomInv;
import net.forthecrown.cosmetics.custominvs.CustomInvBuilder;
import net.forthecrown.cosmetics.custominvs.borders.GenericBorder;
import net.forthecrown.cosmetics.custominvs.options.ClickableOption;
import net.forthecrown.cosmetics.custominvs.options.Option;
import net.forthecrown.cosmetics.effects.CosmeticMenu;
import net.forthecrown.cosmetics.effects.Vault;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;

import java.util.Map;

public class DeathParticleMenu implements CosmeticMenu {

    private final CustomInv inv;
    private final Map<Integer, Option> deathEffectSlots;

    public DeathParticleMenu(CrownUser user) {
        deathEffectSlots = Map.of(
                10, Vault.soul.getClickableOption(user),
                11, Vault.totem.getClickableOption(user),
                12, Vault.explosion.getClickableOption(user),
                13, Vault.enderRing.getClickableOption(user),
                31, Vault.none.getClickableOption(user));
        this.inv = buildInventory(user);
    }


    private Option getReturnItem() {
        ClickableOption returnOption = new ClickableOption();
        returnOption.setCooldown(0);
        returnOption.setActionOnClick(() -> {
            // TODO: go back to main cosmetic menu
        });
        returnOption.setItem(CrownItems.makeItem(Material.PAPER, 1, true, ChatColor.YELLOW + "< Go Back"));
        return returnOption;
    }

    private CustomInv buildInventory(CrownUser user) {
        CustomInvBuilder invBuilder = new CustomInvBuilder();

        CustomInv result = invBuilder
                .setUser(user)
                .setSize(this.getSize())
                .setTitle(this.getTitle())
                .setInvBorder(new GenericBorder())
                .addOptions(deathEffectSlots)
                .addOption(4, getReturnItem())
                .build();

        return result;
    }

    @Override
    public CustomInv getCustomInv() { return this.inv; }

    @Override
    public int getSize() { return 36; }

    @Override
    public TextComponent getTitle() { return Component.text("Death Particles"); }

}
