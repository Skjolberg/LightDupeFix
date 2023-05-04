package wtf.casper.lightdupefix;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public final class LightDupeFix extends JavaPlugin implements Listener {

    private final Cache<Location, Byte> blockCache = CacheBuilder.newBuilder()
            .expireAfterWrite(3, TimeUnit.SECONDS)
            .build();

    private final BlockFace[] around = new BlockFace[]{
            BlockFace.NORTH,
            BlockFace.EAST,
            BlockFace.SOUTH,
            BlockFace.WEST
    };

    private final Set<Material> scannedMaterials = new HashSet<>();

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
        if (event.getItem() == null) {
            return;
        }
        if (event.getClickedBlock() == null) {
            return;
        }
        if (!scannedMaterials.contains(event.getItem().getType())) {
            return;
        }

        final Block block = event.getClickedBlock().getRelative(event.getBlockFace());

        for (BlockFace face : around) {
            Block relative = block.getRelative(face);
            if (isInvalidCrop(relative)) {
                blockCache.put(relative.getLocation(), (byte) 0);
            }
        }
    }

    @EventHandler
    public void onPhysicsEvent(BlockPhysicsEvent event) {
        if (blockCache.getIfPresent(event.getBlock().getLocation()) != null) {
            event.setCancelled(true);
            blockCache.invalidate(event.getBlock().getLocation());
        }
    }

    private boolean isInvalidCrop(Block block) {
        if (!(block.getBlockData() instanceof Ageable)) {
            return false;
        }
        return !(block.getRelative(BlockFace.DOWN).getType() == Material.FARMLAND
                && block.getRelative(BlockFace.UP).getLightLevel() >= 7);
    }
}
