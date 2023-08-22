package wtf.casper.lightdupefix;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;

public final class LightDupeFix extends JavaPlugin implements Listener {

    private final Set<Material> scannedMaterials = new HashSet<>();
    private final Set<Block> blocks = new HashSet<>();

    @Override
    public void onEnable() {
        // Added all materials that can be multi-blocks in-case the bug applies to them too
        for (Material value : Material.values()) {
            if (value.toString().endsWith("_BED")) {
                scannedMaterials.add(value);
                continue;
            }

            if (value.toString().endsWith("_BANNER")) {
                scannedMaterials.add(value);
                continue;
            }

            if (value.toString().endsWith("_WALL_BANNER")) {
                scannedMaterials.add(value);
                continue;
            }

            if (value.toString().endsWith("_DOOR")) {
                scannedMaterials.add(value);
                continue;
            }

            switch (value) {
                case LARGE_FERN, TALL_GRASS, LILAC, ROSE_BUSH, PEONY -> scannedMaterials.add(value);
                default -> {
                }
            }
        }

        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        final Block block = event.getClickedBlock();
        if (event.getItem() == null || block == null) return;

        if (!scannedMaterials.contains(event.getItem().getType())) return;

        Block downBlock = block.getRelative(BlockFace.DOWN);

        if (downBlock.getType().equals(Material.FARMLAND) && downBlock.getLightLevel() == 0) {
            blocks.add(block);
        }

    }

    @EventHandler
    public void onPhysicsEvent(BlockPhysicsEvent event) {
        final Block block = event.getBlock();
        if (blocks.contains(block)) {
            event.setCancelled(true);
            blocks.remove(block);
        }
    }

}
