package com.willfp.stattrackers.tracker;

import com.willfp.eco.core.EcoPlugin;
import com.willfp.eco.core.display.Display;
import com.willfp.eco.core.items.CustomItem;
import com.willfp.eco.core.items.Items;
import com.willfp.eco.core.recipe.recipes.ShapedCraftingRecipe;
import com.willfp.eco.util.StringUtils;
import com.willfp.stattrackers.StatTrackersPlugin;
import com.willfp.stattrackers.stats.Stat;
import com.willfp.stattrackers.tracker.util.TrackerUtils;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class StatTracker {
    /**
     * Instance of StatTrackers to create keys for.
     */
    @Getter
    private final EcoPlugin plugin = StatTrackersPlugin.getInstance();

    /**
     * The stat to track.
     */
    @Getter
    private final Stat stat;

    /**
     * The ItemStack of the tracker.
     */
    @Getter
    private ItemStack itemStack;

    /**
     * The crafting recipe to make the tracker.
     */
    @Getter
    private ShapedCraftingRecipe recipe;

    /**
     * If the recipe is enabled.
     */
    @Getter
    private boolean enabled;

    /**
     * Create a new Stat tracker.
     *
     * @param stat The stat to track.
     */
    public StatTracker(@NotNull final Stat stat) {
        this.stat = stat;
        this.update();
    }

    /**
     * Update the tracker's crafting recipe.
     */
    public void update() {
        enabled = this.getPlugin().getConfigYml().getBool("stat." + stat.getKey().getKey() + ".crafting-enabled");

        NamespacedKey key = this.getPlugin().getNamespacedKeyFactory().create("stat_tracker");

        ItemStack out = Items.lookup(plugin.getConfigYml().getString("stat." + stat.getKey().getKey() + ".tracker-material")).getItem();
        if (out.getType() == Material.AIR) {
            out.setType(Material.COMPASS);
        }
        out.setAmount(1);
        TrackerUtils.registerMaterial(out.getType());
        ItemMeta outMeta = out.getItemMeta();
        assert outMeta != null;
        PersistentDataContainer container = outMeta.getPersistentDataContainer();
        container.set(key, PersistentDataType.STRING, stat.getKey().getKey());
        List<String> lore = new ArrayList<>(this.getPlugin().getLangYml().getFormattedStrings("tracker-description"));
        lore.replaceAll(string -> Display.PREFIX + StringUtils.format(string));
        outMeta.setLore(lore);
        out.setItemMeta(outMeta);
        this.itemStack = out;

        new CustomItem(
                this.getStat().getKey(),
                test -> Objects.equals(TrackerUtils.getTrackedStat(test), this.stat),
                this.itemStack
        ).register();

        if (this.isEnabled()) {
            ShapedCraftingRecipe.Builder builder = ShapedCraftingRecipe.builder(this.getPlugin(), stat.getKey().getKey())
                    .setOutput(out);

            List<String> recipeStrings = plugin.getConfigYml().getStrings("stat." + stat.getKey().getKey() + ".tracker-recipe");

            for (int i = 0; i < 9; i++) {
                builder.setRecipePart(i, Items.lookup(recipeStrings.get(i)));
            }

            this.recipe = builder.build();
            this.recipe.register();
        }
    }
}
