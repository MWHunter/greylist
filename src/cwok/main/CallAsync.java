package cwok.main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;
import net.coreprotect.CoreProtectAPI.ParseResult;

public class CallAsync {
	boolean returnValue = false;

	public void callAsync(Location locationCalled, Material blockTypeCall, Block blockCalled,
			org.bukkit.block.data.BlockData blockData, int radiusCall, Player playerCall, boolean removeBlock,
			String errorMessage) {

		new BukkitRunnable() {

			@Override
			public void run() {

				CoreProtectAPI coapi = getCoreProtect();
				
				List<String> excludeList = new ArrayList<>();
				excludeList.add(playerCall.getName());
				excludeList.add("#water");
				excludeList.add("#lava");
				excludeList.add("#piston");

				List<String[]> lookup = coapi.performLookup(315576000, null, excludeList, null,
						null, Arrays.asList(1), radiusCall, locationCalled);

				if (lookup != null) {
					for (String[] result : lookup) {
						ParseResult parseResult = coapi.parseResult(result);
						if (parseResult.getActionId() == 1) {
							returnValue = true;
						}
					}

					new BukkitRunnable() {
						@Override
						public void run() {
							// This is someone else's area
							if (returnValue == true) {
								// Placing a block
								if (removeBlock == false) {
									blockCalled.setType(blockTypeCall);
									blockCalled.setBlockData(blockData);
								} // Breaking a block
								else {
									blockCalled.breakNaturally();
									blockCalled.setType(Material.AIR);
								}
								// Not an actual error
								playerCall.sendMessage(errorMessage);
								// This is the player's land and they placed a block
							} else if (removeBlock == true) {
								blockCalled.setType(blockTypeCall);
								blockCalled.setBlockData(blockData);
							} else // This is the player's land and they removed a block
							{
								blockCalled.setType(blockTypeCall);
								blockCalled.setBlockData(blockData);
								blockCalled.breakNaturally();
								blockCalled.setType(Material.AIR);
							}
							/*
							 * else if (removeBlock == false) {
							 * //locationCalled.getWorld().dropItemNaturally(locationCalled, new
							 * ItemStack(blockTypeCall, 1)); blockCalled.breakNaturally(); }
							 */

						}
					}.runTask(Greylist.plugin);
				}
			}
		}.runTaskAsynchronously(Greylist.plugin);
	}

	private CoreProtectAPI getCoreProtect() {
		Plugin plugin = Greylist.plugin.getServer().getPluginManager().getPlugin("CoreProtect");

		// Check that CoreProtect is loaded
		if (plugin == null || !(plugin instanceof CoreProtect)) {
			return null;
		}

		// Check that the API is enabled
		CoreProtectAPI CoreProtect = ((CoreProtect) plugin).getAPI();
		if (CoreProtect.isEnabled() == false) {
			return null;
		}

		// Check that a compatible version of the API is loaded
		if (CoreProtect.APIVersion() < 6) {
			return null;
		}

		return CoreProtect;
	}
}
