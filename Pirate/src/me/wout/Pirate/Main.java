package me.wout.Pirate;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Shulker;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.entity.Villager.Type;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerCommandSendEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;

import me.wout.Pirate.commands.UpdateLB;
import me.wout.Pirate.commands.Leave;
import me.wout.Pirate.commands.ghtarget;
import me.wout.Pirate.commands.ghtargetshowname;
import me.wout.Pirate.commands.parrot;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;


public class Main extends JavaPlugin implements Listener {
	
	//public File wilhelmFile;
	//public YamlConfiguration wilhelmYaml;
	//public List<String> players = new ArrayList<String>();
	//public List<ItemStack> itemsToGet = new ArrayList<ItemStack>();
	public File offlineWithParrots;
	public static Main plugin;
	
	public void onEnable() {
		plugin = this;
		getConfig().options().copyDefaults(true);
		saveDefaultConfig();
		
		// Check datafolder.
		File dir = getDataFolder();
		if (!dir.exists())
			if (!dir.mkdir())
				System.out.println("Could not create directory for plugin: " + getDescription().getName());
				
		// Check yaml
		offlineWithParrots = new File(getDataFolder(), "Offline_With_Parrot_Players.yml");
		if(!offlineWithParrots.exists()){
			try {
				offlineWithParrots.createNewFile();
				YamlConfiguration yaml = YamlConfiguration.loadConfiguration(offlineWithParrots);
				yaml.createSection("Players");
				yaml.set("Players", new ArrayList<String>());
				saveyaml(yaml, offlineWithParrots);
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
		
		getServer().getPluginManager().registerEvents(this, this);
		new ghtarget();
		new ghtargetshowname();
		new parrot();
		new Leave();
		new UpdateLB();
		
		getServer().getPluginManager().registerEvents(new BaseEgg(), this);
		
		updateDate();
	}
	

	@SuppressWarnings("deprecation")
	public void onDisable() {
		List<String> players = new ArrayList<String>();
		for (UUID playeruuid : parrots.values())
		{
			try { // Online while reload
				Bukkit.getPlayer(playeruuid).setShoulderEntityLeft(null);
			} 
			catch (Exception e) { // Offline while reload
				players.add(playeruuid.toString());
			}
		}
		parrots.clear();
		
		YamlConfiguration yaml = YamlConfiguration.loadConfiguration(offlineWithParrots);
		yaml.set("Players", players);
		saveyaml(yaml, offlineWithParrots);
	}

	private void updateDate() {
		Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
	        @SuppressWarnings("deprecation")
			@Override
	        public void run() {
	        	Calendar cal = Calendar.getInstance();
	    		if (cal.get(Calendar.DAY_OF_WEEK) != getConfig().getInt("Day")) 
	    		{
	    			 getConfig().set("Day", cal.get(Calendar.DAY_OF_WEEK));
	    			 ItemStack chosenItem = getRandomHeadFromChest();
	    			 //Bukkit.broadcastMessage(chosenItem + "");
	    			 getConfig().set("ChosenHead", ((SkullMeta) chosenItem.getItemMeta()).getOwner());
	    			 
	    			 List<String> temp = getConfig().getStringList("PlayerWhoSoldHeadAlready");
	    			 temp.clear();
	    			 getConfig().set("PlayerWhoSoldHeadAlready", temp);
	    			 getConfig().set("PlayerWhoFoundTreasureAlready", temp);
	    			 
	    			 killOldTreasure(new Location(Bukkit.getWorld(getConfig().getString("TreasureLoc.world")), getConfig().getInt("TreasureLoc.x"), getConfig().getInt("TreasureLoc.y"), getConfig().getInt("TreasureLoc.z")));
	    			 
	    			 int x = getRandomNumberInRange(-1970, 1970);
	    			 int y = getRandomNumberInRange(40, 50);
	    			 int z = getRandomNumberInRange(-1970, 1970);
	    			 //int x = getRandomNumberInRange(-50, 50);
	    			 //int y = getRandomNumberInRange(40, 50);
	    			 //int z = getRandomNumberInRange(-50, 50);
	    			 getConfig().set("TreasureLoc.x", x);
	    			 getConfig().set("TreasureLoc.y", y);
	    			 getConfig().set("TreasureLoc.z", z);
	    			 
	    			 spawnTreasureShulker(new Location(Bukkit.getWorld(getConfig().getString("TreasureLoc.world")), getConfig().getInt("TreasureLoc.x"), getConfig().getInt("TreasureLoc.y"), getConfig().getInt("TreasureLoc.z")));
	    			 
	    			 saveConfig();
	    		}
	    		
	    		if (cal.get(Calendar.WEEK_OF_MONTH) != getConfig().getInt("Week")) 
	    		{
	    			// Picks a day for the trader to spawn.
	    			getConfig().set("Week", cal.get(Calendar.WEEK_OF_MONTH));
	    			getConfig().set("ChosenDayForTrader", getRandomNumberInRange(2, 6));
	    			saveConfig();
	    		}
	    		Location loc = new Location(Bukkit.getWorld("world"), -629.5, 44.2, 3839.5, 0, 0);
	    		killRareEnchantTraders(loc);
	    		if (getConfig().getInt("ChosenDayForTrader") == Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) spawnRareEnchantTrader(loc);
	        }
	    }, 20L);
	}
	
	
	protected void killRareEnchantTraders(Location loc) {
		for (Entity ent : loc.getWorld().getNearbyEntities(loc, 1, 1, 1)) {
			if (ent.getType() != EntityType.PLAYER && ent instanceof LivingEntity) {
				Bukkit.getConsoleSender().sendMessage("Killed " + ent.getType().toString().toLowerCase() + " at enchantvillager spot.");
				ent.remove();
			}
		}
		
	}


	private void spawnRareEnchantTrader(Location loc) {
		Villager villager = loc.getWorld().spawn(loc, Villager.class);
		villager.setAdult();
		villager.setVillagerType(Type.SAVANNA);
		villager.setProfession(Profession.LIBRARIAN);
		villager.setVillagerLevel(5);
		villager.setCustomName(ChatColor.YELLOW + "Edward");
		villager.setCustomNameVisible(true);
		villager.setPersistent(true);
		villager.setRemoveWhenFarAway(false);
		villager.setRecipes(new ArrayList<>());
		villager.setCollidable(false);
		villager.setInvulnerable(true);
		villager.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0);
	}
	
	

	protected void killOldTreasure(Location location) {
		for (Entity nearbyEntity : location.getWorld().getNearbyEntities(location, 1, 1, 1))
		{
			if (nearbyEntity.getType() == EntityType.SHULKER)
			{
				nearbyEntity.remove();
			}
		}
	}

	protected void spawnTreasureShulker(Location spawnLoc) {
		spawnLoc.getBlock().setType(Material.AIR);
		Shulker treasureShulker = spawnLoc.getWorld().spawn(spawnLoc, Shulker.class);
		treasureShulker.setAI(false);
		treasureShulker.setInvulnerable(true);
		treasureShulker.setColor(DyeColor.GRAY);
		treasureShulker.setRemoveWhenFarAway(false);
		treasureShulker.setPersistent(true);
		
	}
	
	Map<String, Boolean> map = new HashMap<>();
	Set<String> cd = new HashSet<String>();
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onPlayerClick(PlayerInteractEntityEvent event) {
		if(!event.getHand().equals(EquipmentSlot.HAND))
			return;
		
		Player player = (Player) event.getPlayer();
		if (event.getRightClicked().getType() == EntityType.VILLAGER) {
			if (event.getRightClicked().getName().contains(ChatColor.GOLD + "Wilhelm")) 
			{
				event.setCancelled(true);
			
				if (getConfig().getStringList("PlayerWhoSoldHeadAlready").contains(player.getUniqueId().toString()))
				{
					player.sendMessage(ChatColor.GRAY + "You've already sold a " + getConfig().getString("ChosenHead") + ChatColor.GRAY + " head today.");
					return;
				}
				if (checkIfInvContainsHead(event.getPlayer().getInventory()) == true) {
					//player.getWorld().playEffect(event.getRightClicked().getLocation(), Effect.VILLAGER_PLANT_GROW, 1);
					player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.5f);
					giveReward(player);
					List<String> temp = getConfig().getStringList("PlayerWhoSoldHeadAlready");
					temp.add(player.getUniqueId().toString());
					getConfig().set("PlayerWhoSoldHeadAlready", temp);
					saveConfig();
				}
				else {
					player.sendMessage(ChatColor.GOLD + "{FTC} " + ChatColor.RESET + "Bring Wilhelm a " + getConfig().getString("ChosenHead") + ChatColor.RESET + " head for a reward.");
				}
			}
			
			else if (event.getRightClicked().getName().contains(ChatColor.YELLOW + "Smith")) 
			{
				event.setCancelled(true);

				if (cd.contains(player.getUniqueId().toString())) return; 
				cd.add(player.getUniqueId().toString());
				Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
					
					@Override
					public void run() {
						cd.remove(player.getUniqueId().toString());
					}
				}, 40L);
				
				map.put(player.getName(), true);
				
				TextComponent message1 = new TextComponent("[Info about Pirates]");
				message1.setColor(net.md_5.bungee.api.ChatColor.YELLOW);
				message1.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/commanduno"));
				message1.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click me!").color(net.md_5.bungee.api.ChatColor.GRAY).italic(true).create()));
			
				TextComponent message2 = new TextComponent("[Join Pirates]");
				message2.setColor(net.md_5.bungee.api.ChatColor.YELLOW);
				message2.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/commandduo"));
				message2.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click me!").color(net.md_5.bungee.api.ChatColor.GRAY).italic(true).create()));
				
				TextComponent message3 = new TextComponent("[Captain's Cutlass]");
				message3.setColor(net.md_5.bungee.api.ChatColor.GOLD);
				message3.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/commandtres"));
				message3.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click me!").color(net.md_5.bungee.api.ChatColor.GRAY).italic(true).create()));
				
				player.sendMessage(ChatColor.GOLD + "--" + ChatColor.WHITE + " Aye mate, what can I do for ya? " + ChatColor.GOLD + "--");
				player.spigot().sendMessage(message1);
				player.spigot().sendMessage(message2);
				player.spigot().sendMessage(message3);
				
				event.setCancelled(true);
					
			}
			
			else if (event.getRightClicked().getName().contains(ChatColor.YELLOW + "Ramun")) 
			{
				event.setCancelled(true);
				
				// Open parrots shop inventory
				if (getServer().getPluginManager().getPlugin("DataPlugin").getConfig().getString("players." + player.getUniqueId().toString() + ".ActiveBranch").contains("Pirate"))
				{
					Inventory invToOpen = Bukkit.createInventory(null, 27, "Parrot Shop");
					ItemStack pane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE, 1);
					ItemMeta meta = pane.getItemMeta();
					meta.setDisplayName(ChatColor.GRAY + " ");
					pane.setItemMeta(meta);
					
					for (int i = 0; i < 10; i++) {
						invToOpen.setItem(i, pane);
					}
					for (int i = 17; i < 27; i++) {
						invToOpen.setItem(i, pane);
					}
					
					ItemStack wool = new ItemStack(Material.GRAY_WOOL, 1);
					meta = wool.getItemMeta();
					meta.setDisplayName(ChatColor.GRAY + "Gray Parrot");
					List<String> lore = new ArrayList<>();
					lore.add(ChatColor.YELLOW + "Costs 50,000 Rhines.");
					lore.add(ChatColor.DARK_GRAY + "Do /parrot gray to summon it.");
					meta.setLore(lore);
					wool.setItemMeta(meta);
					invToOpen.setItem(11, wool);
					
					wool = new ItemStack(Material.GREEN_WOOL, 1);
					meta = wool.getItemMeta();
					meta.setDisplayName(ChatColor.GREEN + "Green Parrot");
					lore.clear();
					lore.add(ChatColor.YELLOW + "Costs 50,000 Rhines.");
					lore.add(ChatColor.DARK_GRAY + "Do /parrot green to summon it.");
					meta.setLore(lore);
					wool.setItemMeta(meta);
					invToOpen.setItem(12, wool);
					
					wool = new ItemStack(Material.BLUE_WOOL, 1);
					meta = wool.getItemMeta();
					meta.setDisplayName(ChatColor.BLUE + "Blue Parrot");
					lore.clear();
					lore.add(ChatColor.YELLOW + "Costs 100,000 Rhines.");
					lore.add(ChatColor.DARK_GRAY + "Do /parrot blue to summon it.");
					meta.setLore(lore);
					wool.setItemMeta(meta);
					invToOpen.setItem(13, wool);
					
					wool = new ItemStack(Material.RED_WOOL, 1);
					meta = wool.getItemMeta();
					meta.setDisplayName(ChatColor.RED + "Red Parrot");
					lore.clear();
					lore.add(ChatColor.YELLOW + "Is available for Captains.");
					lore.add(ChatColor.DARK_GRAY + "Do /parrot red to summon it.");
					meta.setLore(lore);
					wool.setItemMeta(meta);
					invToOpen.setItem(14, wool);
					
					wool = new ItemStack(Material.LIGHT_BLUE_WOOL, 1);
					meta = wool.getItemMeta();
					meta.setDisplayName(ChatColor.AQUA + "Aqua Parrot");
					lore.clear();
					lore.add(ChatColor.YELLOW + "Is available for Admirals.");
					lore.add(ChatColor.DARK_GRAY + "Do /parrot aqua to summon it.");
					meta.setLore(lore);
					wool.setItemMeta(meta);
					invToOpen.setItem(15, wool);
					
					player.openInventory(invToOpen);
				}
				else 
				{
					player.sendMessage(ChatColor.GRAY + "Only pirates can buy from Ramun.");
				}
			}
			
			else if (event.getRightClicked().getName().contains(ChatColor.YELLOW + "Jack"))
			{
				event.setCancelled(true);
				openLevelSelector(player);
			}
			
		}
		else if (event.getRightClicked().getType() == EntityType.SHULKER) {
			Shulker treasureShulker = (Shulker) event.getRightClicked();
			if ((!treasureShulker.hasAI()) && treasureShulker.getColor() == DyeColor.GRAY)
			{
				if (getConfig().getStringList("PlayerWhoFoundTreasureAlready").contains(player.getUniqueId().toString()))
				{
					player.sendMessage(ChatColor.GRAY + "You've already opened this treasure today.");
					return;
				}
				else
				{
					giveTreasure(player);
					List<String> temp = getConfig().getStringList("PlayerWhoFoundTreasureAlready");
					temp.add(player.getUniqueId().toString());
					getConfig().set("PlayerWhoFoundTreasureAlready", temp);
					saveConfig();
				}
			}
		}
	}
	

	private void giveTreasure(Player player) {
		player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.5f);
		
		double moneyDecider = Math.random();
		if (moneyDecider <= 0.6) 
		{
			Bukkit.dispatchCommand(getServer().getConsoleSender(), "givecash " + player.getName() + " 5000");
			player.sendMessage(ChatColor.GRAY + "You've found a treasure with " + ChatColor.YELLOW + "5,000 rhines" + ChatColor.GRAY + " inside.");
			Bukkit.dispatchCommand(getServer().getConsoleSender(), "crate givekey " + player.getName() + " lootbox1 1");
		}
		else if (moneyDecider > 0.6 && moneyDecider <= 0.9)
		{
			Bukkit.dispatchCommand(getServer().getConsoleSender(), "givecash " + player.getName() + " 10000");
			player.sendMessage(ChatColor.GRAY + "You've found a treasure with " + ChatColor.YELLOW + "10,000 rhines" + ChatColor.GRAY + " inside.");
			Bukkit.dispatchCommand(getServer().getConsoleSender(), "crate givekey " + player.getName() + " lootbox1 2");
		}
		else
		{
			Bukkit.dispatchCommand(getServer().getConsoleSender(), "givecash " + player.getName() + " 20000");
			player.sendMessage(ChatColor.GRAY + "You've found a treasure with " + ChatColor.YELLOW + "20,000 rhines" + ChatColor.GRAY + " inside.");
			Bukkit.dispatchCommand(getServer().getConsoleSender(), "crate givekey " + player.getName() + " lootbox1 3");
		}
		
		List<ItemStack> commonItems = getItems(((Chest) Bukkit.getWorld("world").getBlockAt(
				new Location(Bukkit.getWorld(getConfig().getString("TreasureCommonLoot.world")), getConfig().getInt("TreasureCommonLoot.x"), getConfig().getInt("TreasureCommonLoot.y"), getConfig().getInt("TreasureCommonLoot.z"))).getState()));
		List<ItemStack> rareItems = getItems(((Chest) Bukkit.getWorld("world").getBlockAt(
				new Location(Bukkit.getWorld(getConfig().getString("TreasureRareLoot.world")), getConfig().getInt("TreasureRareLoot.x"), getConfig().getInt("TreasureRareLoot.y"), getConfig().getInt("TreasureRareLoot.z"))).getState()));
		List<ItemStack> specialItems = getItems(((Chest) Bukkit.getWorld("world").getBlockAt(
				new Location(Bukkit.getWorld(getConfig().getString("TreasureSpecialLoot.world")), getConfig().getInt("TreasureSpecialLoot.x"), getConfig().getInt("TreasureSpecialLoot.y"), getConfig().getInt("TreasureSpecialLoot.z"))).getState()));
		
		for (int i = 0; i < 6; i++) 
		{
			double random = Math.random();
			ItemStack chosenItem;
			
			if (random <= 0.6) 
			{
				chosenItem = getItemFromList(commonItems);
				//Bukkit.broadcastMessage("Common: " + chosenItem.getType().toString().toLowerCase());
			}
			else if (random > 0.6 && random <= 0.9)
			{
				chosenItem = getItemFromList(rareItems);
				//Bukkit.broadcastMessage("Rare: " + chosenItem.getType().toString().toLowerCase());
			}
			else
			{
				chosenItem = getItemFromList(specialItems);
				//Bukkit.broadcastMessage("Special: " + chosenItem.getType().toString().toLowerCase());
			}
			
			player.getInventory().addItem(chosenItem);
		}
		
		givePP(player, 1);
		
	}
	
	
	@SuppressWarnings("deprecation")
	public void givePP(Player player, int toadd) {
		Objective pp = Bukkit.getServer().getScoreboardManager().getMainScoreboard().getObjective("PiratePoints");
		Score ppp = pp.getScore(player);
		ppp.setScore(ppp.getScore() + toadd);
		if (ppp.getScore() == 1) player.sendMessage(ChatColor.GRAY + "[FTC] You now have " + ChatColor.YELLOW + ppp.getScore() + ChatColor.GRAY + " Pirate point.");
		else 
		{
			player.sendMessage(ChatColor.GRAY+ "You now have " + ChatColor.YELLOW + ppp.getScore() + ChatColor.GRAY + " Pirate points.");
			
			// Check for sailor / pirate
			List<String> ranks = getServer().getPluginManager().getPlugin("DataPlugin").getConfig().getStringList("players." + player.getUniqueId().toString() + ".PirateRanks");
			if (ppp.getScore() >= 10 && (!ranks.contains("sailor")))
			{
				Bukkit.dispatchCommand(getServer().getConsoleSender(), "addrank " + player.getName() + " sailor");
				Bukkit.dispatchCommand(getServer().getConsoleSender(), "lp user " + player.getName() + " parent add free-rank");
				player.playSound(player.getLocation(), Sound.ITEM_TOTEM_USE, 0.5f, 1.2f);
				player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.7f, 1.2f);
				for (int i = 0; i <= 5; i++) {
					Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
				        @Override
				        public void run() {
				        	player.getWorld().spawnParticle(Particle.TOTEM, player.getLocation().getX(), player.getLocation().getY()+2, player.getLocation().getZ(), 30, 0.2d, 0.1d, 0.2d, 0.275d);
				        }
				    }, i*5L);
				}
				player.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "[FTC] " + ChatColor.WHITE + "You've been promoted to " + ChatColor.DARK_GRAY + ChatColor.BOLD + "{" + ChatColor.GRAY + "Sailor" + ChatColor.DARK_GRAY + ChatColor.BOLD + "}" + ChatColor.WHITE + " !");
				player.sendMessage(ChatColor.WHITE + "You can now select the tag in " + ChatColor.YELLOW + "/rank" + ChatColor.WHITE + " now.");
			}
			else if (ppp.getScore() >= 50 && (!ranks.contains("pirate")))
			{
				Bukkit.dispatchCommand(getServer().getConsoleSender(), "addrank " + player.getName() + " pirate");
				player.playSound(player.getLocation(), Sound.ITEM_TOTEM_USE, 0.5f, 1.2f);
				player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.7f, 1.2f);
				for (int i = 0; i <= 5; i++) {
					Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
				        @Override
				        public void run() {
				        	player.getWorld().spawnParticle(Particle.TOTEM, player.getLocation().getX(), player.getLocation().getY()+2, player.getLocation().getZ(), 30, 0.2d, 0.1d, 0.2d, 0.275d);
				        }
				    }, i*5L);
				}
				player.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "[FTC] " + ChatColor.WHITE + "You've been promoted to " + ChatColor.DARK_GRAY + ChatColor.BOLD + "{" + ChatColor.GRAY + "Pirate" + ChatColor.DARK_GRAY + ChatColor.BOLD + "}" + ChatColor.WHITE + " !");
				player.sendMessage(ChatColor.WHITE + "You can now select the tag in " + ChatColor.YELLOW + "/rank" + ChatColor.WHITE + " now.");
			}
		}
		
		//updateLeaderBoard();
	}
	
	
	private void spawnLeaderboard(int amount)
	{
		removeLeaderboard();
		List<String> top = getTopPlayers(Bukkit.getServer().getScoreboardManager().getMainScoreboard().getObjective("PiratePoints"), amount);
		double distanceBetween = 0.27;
		
		for (int i = 0; i < top.size(); i++)
		{
			spawnArmorStand(getLeaderboardLoc(), distanceBetween*i, top.get(top.size()-i-1), true);
		}
		
		spawnArmorStand(getLeaderboardLoc(), distanceBetween*top.size(), ChatColor.GOLD + "---------=o=O=o=---------", false);
		spawnArmorStand(getLeaderboardLoc(), distanceBetween*(top.size()+1), ChatColor.WHITE + "Pirate Points Leaderboard", false);
		spawnArmorStand(getLeaderboardLoc(), -distanceBetween, ChatColor.GOLD + "---------=o=O=o=---------", false);
	}

	private void spawnArmorStand(Location loc, double d, String text, boolean isScoreStand) {
		ArmorStand armorstand = loc.getWorld().spawn(loc.add(0, d, 0), ArmorStand.class);
		armorstand.setGravity(false);
		armorstand.setVisible(false);
		armorstand.setCustomName(text);
		armorstand.setCustomNameVisible(true);
		getAllLeaderboardArmorstands().add(armorstand);
		if (isScoreStand) getLeaderBoardArmorStands().add(armorstand);
	}
	
	private void removeLeaderboard()
	{
		for (ArmorStand armorstand : getAllLeaderboardArmorstands())
		{
			armorstand.remove();
		}
		allLeaderboardArmorstands.clear();
		for (Entity ent : getLeaderboardLoc().getWorld().getNearbyEntities(getLeaderboardLoc(), 0.1, 5, 0.1))
		{
			if (ent instanceof ArmorStand)
				ent.remove();
		}
	}
	
	
	private List<ArmorStand> leaderboardArmorstands = new ArrayList<>();
	public List<ArmorStand> getLeaderBoardArmorStands() 
	{
		return leaderboardArmorstands;
	}
	private List<ArmorStand> allLeaderboardArmorstands = new ArrayList<>();
	public List<ArmorStand> getAllLeaderboardArmorstands() 
	{
		return allLeaderboardArmorstands;
	}
	
	public Location getLeaderboardLoc() 
	{
		return new Location(Bukkit.getWorld("world"), -639.0, 70, 3830.5, 90, 0); // TODO UPDATE?
	}
	
	public void updateLeaderBoard() 
	{
		removeLeaderboard();
		spawnLeaderboard(5);
		/*List<String> top = getTopPlayers(Bukkit.getServer().getScoreboardManager().getMainScoreboard().getObjective("PiratePoints"), getLeaderBoardArmorStands().size());
		for (int i = 0; i < top.size(); i++)
		{
			getLeaderBoardArmorStands().get(i).setCustomName( top.get(top.size()-i-1));
		}*/
		
	}

	private List<ItemStack> getItems(Chest chest) {
		List<ItemStack> result = new ArrayList<ItemStack>();
		for (ItemStack item : chest.getInventory().getContents()) 
		{
			if (item != null) result.add(item);
		}
		return result;
	}
	
	private ItemStack getItemFromList(List<ItemStack> list) {
		ItemStack result;
		
		int index = getRandomNumberInRange(0, list.size()-1);
		result = list.get(index);
		int count = 0;
		while (list.contains(result) && (count++ != list.size())) {
			result = list.get(++index % list.size());
		}
		
		return result;
	}
	
	
	
	@EventHandler
	public void onPlayerClick(PlayerInteractEvent event) {
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) 
		{
			if (event.getHand().equals(EquipmentSlot.HAND)) 
			{
				if (event.getItem() != null && event.getItem().getType() == Material.COMPASS) 
				{
					if (event.getPlayer().getLocation().getWorld().getName().equalsIgnoreCase(getConfig().getString("TreasureLoc.world"))) {
						Location targetLoc = new Location(Bukkit.getWorld(getConfig().getString("TreasureLoc.world")), getConfig().getInt("TreasureLoc.x"), getConfig().getInt("TreasureLoc.y"), getConfig().getInt("TreasureLoc.z"));
						event.getPlayer().setCompassTarget(targetLoc);
						Location playerloc = event.getPlayer().getLocation();
						playerloc.getWorld().playSound(playerloc, Sound.ITEM_LODESTONE_COMPASS_LOCK, 1, 1);
						//playerloc.getWorld().spawnParticle(Particle.END_ROD, playerloc.getX(), playerloc.getY()+0.5, playerloc.getZ(), 5, 0.7, 0, 0.7, 0.02);
					}
				}
			}
		}
	}

	public ItemStack getRandomHeadFromChest()
	{
		Location chestLoc1 = getloc("HeadChestLocation1");
		Location chestLoc2 = getloc("HeadChestLocation2");
		Location chestLoc3 = getloc("HeadChestLocation3");
		Location chestLoc4 = getloc("HeadChestLocation4");
		
		if (Bukkit.getWorld("world").getBlockAt(chestLoc1).getType() != Material.CHEST 
				|| Bukkit.getWorld("world").getBlockAt(chestLoc2).getType() != Material.CHEST
				|| Bukkit.getWorld("world").getBlockAt(chestLoc3).getType() != Material.CHEST
				|| Bukkit.getWorld("world").getBlockAt(chestLoc4).getType() != Material.CHEST) 
		{
			return null;
		}
		else 
		{
			int chosenChest = getRandomNumberInRange(1, 4);
			int slot = getRandomNumberInRange(0, 26);
			Location chosenLoc;
			switch (chosenChest) { 
				case 1:
					chosenLoc = chestLoc1;
				case 2:
					chosenLoc = chestLoc2;
				case 3:
					chosenLoc = chestLoc3;
				case 4:
					chosenLoc = chestLoc4;
				default:
					chosenLoc = chestLoc1;
			}
			
			ItemStack chosenItem = ((Chest) Bukkit.getWorld("world").getBlockAt(chosenLoc).getState()).getInventory().getContents()[slot];
			int nextSlot = slot+1;
			while (nextSlot % 27 != slot && chosenItem == null)
			{
				nextSlot++;
				chosenItem = ((Chest) Bukkit.getWorld("world").getBlockAt(chosenLoc).getState()).getInventory().getContents()[nextSlot % 27];
			}
			if (chosenItem == null)
				return new ItemStack(Material.STONE);
			else
				return chosenItem;
		}
		
	}
	
	private Location getloc(String section) {
		return new Location(Bukkit.getWorld(getConfig().getString(section + ".world")), getConfig().getInt(section + ".x"), getConfig().getInt(section + ".y"), getConfig().getInt(section + ".z"));
	}
	
	private static int getRandomNumberInRange(int min, int max) {
		if (min >= max) {
			return 0;
		}
		Random r = new Random();
		return r.nextInt((max - min) + 1) + min;
	}
	
	private void giveReward(Player player) {
		Bukkit.dispatchCommand(getServer().getConsoleSender(), "givecash " + player.getName() + " 10000");
		player.sendMessage(ChatColor.GRAY + "You've received " + ChatColor.GOLD + "10,000 rhines" + ChatColor.GRAY + " from " + ChatColor.YELLOW + "Wilhelm" + ChatColor.GRAY + ".");
		givePP(player, 2);
	}


	@SuppressWarnings("deprecation")
	private boolean checkIfInvContainsHead(PlayerInventory inv) {
		int size = 36;
		
		for (int i = 0; i < size; i++) 
		{
    		ItemStack invItem = inv.getItem(i);
    		if (invItem != null) {
    			
	    		if (invItem.getType() == Material.PLAYER_HEAD) {
	    			if (invItem.hasItemMeta() && ((SkullMeta) invItem.getItemMeta()).getOwner().equalsIgnoreCase(getConfig().getString("ChosenHead"))) {
	    				invItem.setAmount(invItem.getAmount()-1);
	    				return true;
	    			}
	    		}
    		}
    	}
    	
		return false;
	}
	
	
	public List<String> getTopPlayers(Objective objective, int top) {
		List<String> unsortedResult = new ArrayList<String>();
	    int score;
	    for(String name : objective.getScoreboard().getEntries()) {
	        if (unsortedResult.size() < top) 
	        {
				unsortedResult.add(name);
			}
	        else 
	        {
	        	score = objective.getScore(name).getScore();
	        	for (String nameInList : unsortedResult)
	        	{
	        		if (score > objective.getScore(nameInList).getScore())
	        		{
	        			String lowestPlayer = nameInList;
	        			for (String temp : unsortedResult) 
	        			{
	        				if (objective.getScore(temp).getScore() < objective.getScore(lowestPlayer).getScore())
	        				{
	        					lowestPlayer = temp;
	        				}
	        			}
	        			unsortedResult.remove(lowestPlayer);
	        			unsortedResult.add(name);
	        			break;
	        		}
	        	}
	        }
	    }
	    
	    List<String> sortedResult = new ArrayList<String>();
	    
	    String playername = null;
	    int size = unsortedResult.size();
		for (int j = 1; j <= size; j++) {
			int max = Integer.MIN_VALUE;
			
			// Zoek max in result
			for (int i = 0; i < unsortedResult.size(); i++) {
				if (objective.getScore(unsortedResult.get(i)).getScore() > max) {
					max = objective.getScore(unsortedResult.get(i)).getScore();
					playername = unsortedResult.get(i);
				}
			}
			
			unsortedResult.remove(playername);
			/*if (objective.getScore(playername).getScore() != 0) */sortedResult.add(j + ". " + ChatColor.YELLOW + playername + ChatColor.WHITE + " - " + objective.getScore(playername).getScore());
		}
		
		return sortedResult;
	}
	
	
	@EventHandler
	public void onPlayerTab(PlayerCommandSendEvent e) {
		List<String> blockedCommands = new ArrayList<>();
		blockedCommands.add("commanduno");
		blockedCommands.add("commandduo");
		blockedCommands.add("commandtres");
		e.getCommands().removeAll(blockedCommands);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("rlpirate")) {
			if (sender.isOp()) {
				this.reloadConfig();
				updateDate();
				sender.sendMessage(ChatColor.GRAY + "Pirate config reloaded.");
			} else {
				sender.sendMessage(ChatColor.RED + "You don't have permission to do this!");
			}
		}
		else 
		{
			if (!(sender instanceof Player)) return false;

			Player player = (Player) sender;
			
			if (map.containsKey(player.getName()) && map.get(player.getName()) == true) 
			{
				switch (label) {
				
				case "commanduno": // Pirate Information
					map.replace(player.getName(), true, false);
					Bukkit.dispatchCommand(getServer().getConsoleSender(), "tellraw " + player.getName() + " [\"\",{\"text\":\"The Pirates\",\"bold\":true,\"color\":\"gold\"},{\"text\":\"\\n The pirates are a new faction, which is all about illegal merchandise!\\n\\n\"},{\"text\":\"Joining the Pirates\",\"color\":\"gold\"},{\"text\":\"\\n To join the pirates you'll need 10 PP, Pirate Points. To get PP you must either finish levels in the Grappling Hook Parkour or find treasure chests in the RW. In addition, you can earn money and PP by selling heads to the dreaded \"},{\"text\":\"Captain Willhelm\",\"color\":\"yellow\"},{\"text\":\".\\n\\n\"},{\"text\":\"Pirate Ranks\",\"color\":\"gold\"},{\"text\":\"\\n The pirates have their own ranks: Sailor, Pirate, Captain and Admiral.\\n Sailor can be gotten with 10 PP, but Pirate will require you to have 50 PP. Captain and Admiral must be bought from the webstore (Tier 2 and Tier 3)\\n\\n\"},{\"text\":\"Black Market\",\"color\":\"gold\"},{\"text\":\"\\n The pirates have a black market. where they can sell goods without a price decline. In addition, you can buy parrots, that never leave you, alongside slaves from \"},{\"text\":\"Ramun the Slave-trader\",\"color\":\"yellow\"},{\"text\":\".\\n\\n However, Hazelguard will not allow people guilty of piracy to own shops in town, so pirates will have their shops seized and they won't be able to open new ones. The pirates do however have their own shops in the Black market.\"}]");
					return true;
				
				case "commandduo": // Swap Branch
					map.replace(player.getName(), true, false);
					if (getServer().getPluginManager().getPlugin("DataPlugin").getConfig().getString("players." + player.getUniqueId().toString() + ".ActiveBranch").contains("Pirate"))
					{
						player.sendMessage("You're already a pirate.");
						return false;
					}
					if (!getServer().getPluginManager().getPlugin("DataPlugin").getConfig().getString("players." + player.getUniqueId().toString() + ".CurrentRank").contains("default"))
					{
						player.sendMessage("You need to be a default rank before you can join the pirates.");
						return false;
					}
					
					if (Bukkit.dispatchCommand(getServer().getConsoleSender(), "setbranch " + player.getName() + " Pirate"))
						player.sendMessage("You're now a pirate!");
					else
						player.sendMessage("You can't become a pirate atm.");
					return true;
					
				case "commandtres": // Captain's cutlass
					map.replace(player.getName(), true, false);
					if (player.hasPermission("ftc.donator2") && getServer().getPluginManager().getPlugin("DataPlugin").getConfig().getString("players." + player.getUniqueId().toString() + ".ActiveBranch").contains("Pirate"))
					{
						ItemStack itemInHand = player.getInventory().getItemInMainHand();
						if (itemInHand == null || player.getInventory().getItemInMainHand().getType() != Material.GOLDEN_SWORD || (!player.getInventory().getItemInMainHand().getItemMeta().hasLore()))
						{
							player.sendMessage(ChatColor.GRAY + "You have to hold a Royal Sword to change it into a Captain's Cutlass.");
							return false;
						}
						
						ItemMeta meta = itemInHand.getItemMeta();
						meta.setDisplayName(ChatColor.RESET + "" + net.md_5.bungee.api.ChatColor.of("#917558") + "-" + net.md_5.bungee.api.ChatColor.of("#D1C8BA") + ChatColor.BOLD + "Captain's Cutlass" + ChatColor.RESET + net.md_5.bungee.api.ChatColor.of("#917558") + "-");
						itemInHand.setType(Material.NETHERITE_SWORD);
						List<String> lore = meta.getLore();						//The bearer of this weapon has proven themselves,
						lore.set(2, net.md_5.bungee.api.ChatColor.of("#917558") + "The brearer of this cutlass bows to no laws, to no king,");
						lore.set(3, net.md_5.bungee.api.ChatColor.of("#917558") + "its wielder leads their crew towards everlasting riches.");
						meta.setLore(lore);
						itemInHand.setItemMeta(meta);
						player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.5f);
					}
					
					else player.sendMessage(ChatColor.GRAY + "Only Captains can get this weapon.");
					return true;
				
				default:
					return false;
				}
			}
			 
			else 
			{
				map.put(player.getName(), false);
				 return false;
			}
			
		}
		
		return true;
	}
	
	// parrot uuid - player uuid
	public Map<UUID, UUID> parrots = new HashMap<UUID, UUID>();
	
	@EventHandler
	public void onParrotDismount(CreatureSpawnEvent event)
	{
		if (parrots.containsKey(event.getEntity().getUniqueId())) 
		{
			
			if (Bukkit.getPlayer(parrots.get(event.getEntity().getUniqueId())).isFlying()) {
				Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
					@Override
			        public void run() {
						event.getEntity().remove();
						Bukkit.getPlayer(parrots.get(event.getEntity().getUniqueId())).sendMessage(ChatColor.GRAY + "Poof! Parrot gone.");
						parrots.remove(event.getEntity().getUniqueId());
			    	}
			    }, 1L);
			}
			else {
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onParrotDeath(EntityDeathEvent event) {
		if (parrots.containsKey(event.getEntity().getUniqueId())) {
			event.getDrops().clear();
			event.setDroppedExp(0);
			parrots.remove(event.getEntity().getUniqueId());
		}
	}
	
	/*@SuppressWarnings("deprecation")
	@EventHandler
	public void parrotCarrierLogsOut(PlayerQuitEvent event) {
		if (parrots.containsValue(event.getPlayer().getUniqueId()))
		{
			parrots.remove(event.getPlayer().getShoulderEntityLeft().getUniqueId());
			event.getPlayer().setShoulderEntityLeft(null);
			
		}
	}*/
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event)
	{
		YamlConfiguration yaml = YamlConfiguration.loadConfiguration(offlineWithParrots);
		List<String> players = yaml.getStringList("Players");
		if (players.contains(event.getPlayer().getUniqueId().toString()))
		{
			event.getPlayer().setShoulderEntityLeft(null);
			players.remove(event.getPlayer().getUniqueId().toString());
			yaml.set("Players", players);
			saveyaml(yaml, offlineWithParrots);
		}
	}
	
	
	// *************************************** Grappling Hook Parkour *************************************** //
	
	public File getPlayerLevelsFile() {
		File file = new File(getDataFolder(), "PlayerLevelProgress.yml");
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return file;
	}
	
	public File getArmorStandFile() {
		File file = new File(getDataFolder(), "TargetStandData.yml");
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return file;
	}
	
	public void saveyaml(YamlConfiguration yaml, File file) {
		try {
			yaml.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	

	@EventHandler
	public void onPlayerArmorStandEvent(PlayerInteractAtEntityEvent event)
	{
		if (event.getHand() != EquipmentSlot.HAND) return;
		Player player = event.getPlayer();
		
		if (event.getRightClicked().getType() == EntityType.ARMOR_STAND && event.getRightClicked().isInvulnerable() && event.getRightClicked().getCustomName() != null && event.getRightClicked().getCustomName().contains("GHTargetStand")) 
		{
			final String ghArmorStandID = "Stand_" + event.getRightClicked().getName().split(" ")[1];
			
			YamlConfiguration yaml = YamlConfiguration.loadConfiguration(getArmorStandFile());
			Location loc = new Location(player.getWorld(), yaml.getDouble(ghArmorStandID + ".XToCords"), yaml.getDouble(ghArmorStandID + ".YToCords"), yaml.getDouble(ghArmorStandID + ".ZToCords"), yaml.getInt(ghArmorStandID + ".YawToCords"), 0);
			
			player.sendMessage(ChatColor.GRAY + "Stand on the glowstone for 2 seconds.");
			
			Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
				@Override
		        public void run() {
					if (player.getLocation().clone().subtract(0, 1, 0).getBlock().getType() == Material.GLOWSTONE) 
					{
						player.getInventory().clear();
						player.teleport(loc);
						player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP , 1.0F, 2.0F);
						player.sendMessage(ChatColor.GOLD + "[FTC] " + ChatColor.GRAY + "You've advanced to the next level!");
						
						if (yaml.getInt(ghArmorStandID + ".StandClass") != 3) 
						{
							Bukkit.dispatchCommand(getServer().getConsoleSender(), "grapplinghook give " + player.getName() + " " + yaml.getInt(ghArmorStandID + ".NextLevelHooks") + " " + yaml.getInt(ghArmorStandID + ".NextLevelDistance"));
						}
						File playerProgressFile = getPlayerLevelsFile();
						YamlConfiguration playerProgressYaml = YamlConfiguration.loadConfiguration(playerProgressFile);
						
						int level = 0;
						try {
							level = Integer.parseInt(event.getRightClicked().getName().split(" ")[1]);
						}
						catch (Exception e) {
							getServer().getConsoleSender().sendMessage(ChatColor.RED + "Wrong target-armorstand found: " + event.getRightClicked().getName().split(" ")[1] + " as id is not valid.");
							return;
						}
						
						
						List<String> levelList = getLevelList();
						
						if (!playerProgressYaml.getStringList(player.getUniqueId().toString()).contains(levelList.get(level))) 
						{
							switch (yaml.getInt(ghArmorStandID + ".StandClass"))
							{
								case 2:
									player.sendMessage(ChatColor.GOLD + "[FTC] " + ChatColor.GRAY + "You have recieved " + ChatColor.GOLD  + "25000 Rhines " + ChatColor.GRAY + "for completing all levels in a biome.");
									Bukkit.dispatchCommand(getServer().getConsoleSender(), "givecash " + player.getName() + " 25000");
									givePP(player, 5);
									break;
								case 3:
									player.sendMessage(ChatColor.GOLD + "[FTC] " + ChatColor.GRAY + "You have recieved " + ChatColor.GOLD + "25000 Rhines " + ChatColor.GRAY + "for completing all the Grappling Hook levels!");
									Bukkit.dispatchCommand(getServer().getConsoleSender(), "givecash " + player.getName() + " 25000");
									givePP(player, 25);
									break;
								default:
									break;
								 
							}
						}
						
						
						if (playerProgressYaml.getStringList(player.getUniqueId().toString()).isEmpty())
						{
							playerProgressYaml.createSection(player.getUniqueId().toString());
							List<String> list = new ArrayList<String>();
							list.add("started");
							list.add(levelList.get(level));
							playerProgressYaml.set(player.getUniqueId().toString(), list);
							saveyaml(playerProgressYaml, playerProgressFile);
						}
						else
						{
							List<String> list = playerProgressYaml.getStringList(player.getUniqueId().toString());
							if (!list.contains(levelList.get(level))) list.add(levelList.get(level));
							playerProgressYaml.set(player.getUniqueId().toString(), list);
							saveyaml(playerProgressYaml, playerProgressFile);
						}
						
						
					}
					
					else {
						player.sendMessage(ChatColor.GRAY + "Cancelled.");
					}
		    	}
		    }, 40L);
			
			
		}
			
	}

	@EventHandler
	public void equiping(PlayerArmorStandManipulateEvent event) {
		if (event.getRightClicked().isInvulnerable() && event.getRightClicked().getCustomName() != null && event.getRightClicked().getCustomName().contains("GHTargetStand")) 
		{
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerClickItemInInv(InventoryClickEvent event) {
		String title = event.getView().getTitle();
		
		if (title.contains("Level Selector")) 
		{
			event.setCancelled(true);
			Player player = (Player) event.getWhoClicked();
			
			if (!player.getWorld().getName().contains("world_void")) return; // extra check
			
			if (!invClear(player)) 
			{
				player.sendMessage(ChatColor.GRAY + "Your inventory has to be completely empty to enter.");
				return; // TODO Make portal check for empty inventory too
			}
			
			if (event.getInventory().getItem(event.getSlot()) == null) return;
			player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);
			
			if (getCompletedLevelIndicators().contains(event.getInventory().getItem(event.getSlot()).getType()) || event.getInventory().getItem(event.getSlot()).getEnchantments().containsKey(Enchantment.CHANNELING))
			{
				
				int slot = event.getSlot() - 1;
				Location loc;
				
				if (slot == 39) slot = 35;				
				if (slot == -1) 
				{
					loc = new Location(player.getWorld(), -1003.5, 21, 3.5, 180, 0); // Level 1 start
					Bukkit.dispatchCommand(getServer().getConsoleSender(), "grapplinghook give " + player.getName());
				}
				else 
				{
					YamlConfiguration yaml = YamlConfiguration.loadConfiguration(getArmorStandFile());
					String ghArmorStandID = "Stand_" + slot;
					loc = new Location(player.getWorld(), yaml.getDouble(ghArmorStandID + ".XToCords"), yaml.getDouble(ghArmorStandID + ".YToCords"), yaml.getDouble(ghArmorStandID + ".ZToCords"), yaml.getInt(ghArmorStandID + ".YawToCords"), 0);
					Bukkit.dispatchCommand(getServer().getConsoleSender(), "grapplinghook give " + player.getName() + " " + yaml.getInt(ghArmorStandID + ".NextLevelHooks") + " " + yaml.getInt(ghArmorStandID + ".NextLevelDistance"));
				}
				
				player.teleport(loc);
				player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT , 1.0F, 1.0F);
				player.sendMessage(ChatColor.GRAY + "You can " + ChatColor.YELLOW + "/leave" + ChatColor.GRAY + " at any time.");
			}
		}
	}
	
	
	private void openLevelSelector(Player player) {
		Inventory inv = createLevelSelectorInv(player);
		inv = personalizeInventory(player, inv);
		player.openInventory(inv);
	}
	
	private List<String> getLevelList() {
		String[] levelnames = {"Journey", "Journey Limited", "Ship", "Ship Limited", "Sky Battle", "Sky Battle Limited", "Floating Islands", "Floating Islands Limited", "Floating Islands Limited Distance", "Big Beans", "Big Beans Limited", "Floating Ruins", "Floating Ruins Temple", "Floating Ruins Temple Limited", "Annoying Islands",
				"Bunk Ships", "Bunk Ships Limited", "Shark Attack", "Sharks Failed", "Watchtower", "Watchtower Limited", "More Towers", "More Towers Limited", "Not Enough Towers", "Not Enough Towers Limited", "The Climb", "The Limited Climb", "Weird Object",
				"Tetrominoes Limited", "Tetris", "Tetris Limited", "Tetris Cannons", "Nightmare", "Angry Tetromino", "Parkour A", "Parkour B", "Temple of the Void"};
		
		List<String> levelList = new ArrayList<>();
		for (String level : levelnames) levelList.add(level);
		
		return levelList;
	}


	
	private Inventory createLevelSelectorInv(Player player) {
		Inventory result = Bukkit.createInventory(player, 54, "Level Selector");
		
		List<String> levelList = getLevelList();
		
		ItemStack item;
		ItemMeta meta;
		for (int i = 0; i < result.getSize(); i++)
		{
			if (i < 15) 
			{
				item = new ItemStack(Material.GRASS_BLOCK);
				meta = item.getItemMeta();
				meta.setDisplayName(ChatColor.AQUA + levelList.get(i) + ChatColor.RESET);
				item.setItemMeta(meta);
				result.setItem(i, item);
			}
			else if (i >= 15 && i < 27) 
			{
				item = new ItemStack(Material.RED_SANDSTONE);
				meta = item.getItemMeta();
				meta.setDisplayName(ChatColor.AQUA + levelList.get(i) + ChatColor.RESET);
				item.setItemMeta(meta);
				result.setItem(i, item);
			}
			else if (i >= 27 && i < 34) 
			{
				item = new ItemStack(Material.PURPLE_STAINED_GLASS);
				meta = item.getItemMeta();
				meta.setDisplayName(ChatColor.AQUA + levelList.get(i) + ChatColor.RESET);
				item.setItemMeta(meta);
				result.setItem(i, item);
			}
			else if (i == 34 || i == 35) 
			{
				item = new ItemStack(Material.OAK_PLANKS);
				meta = item.getItemMeta();
				meta.setDisplayName(ChatColor.AQUA + levelList.get(i) + ChatColor.RESET);
				item.setItemMeta(meta);
				result.setItem(i, item);
			}
			else if (i == 40)
			{
				item = new ItemStack(Material.GILDED_BLACKSTONE);
				meta = item.getItemMeta();
				meta.setDisplayName(ChatColor.AQUA + "Temple of the Void");
				item.setItemMeta(meta);
				result.setItem(i, item);
			}
			else if (i > 40) break;
		}
		return result;
	}
	
	private Inventory personalizeInventory(Player player, Inventory inv) {
		File file = getPlayerLevelsFile();
		YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
		
		if (yaml.getStringList(player.getUniqueId().toString()).isEmpty())
		{
			yaml.createSection(player.getUniqueId().toString());
			List<String> list = new ArrayList<String>();
			list.add("started");
			yaml.set(player.getUniqueId().toString(), list);
			saveyaml(yaml, file);
		}
		else
		{
			ItemStack item;
			for (String completedLevel : yaml.getStringList(player.getUniqueId().toString()))
			{
				if (completedLevel.contains("started")) continue;
				item = getItemWithNameFrom(inv, completedLevel);
				if (item == null) continue;
				if (item.getType() == Material.RED_SANDSTONE) item.setType(Material.ORANGE_TERRACOTTA);
				else if (item.getType() == Material.PURPLE_STAINED_GLASS) item.setType(Material.PURPLE_TERRACOTTA);
				else if (item.getType() == Material.OAK_PLANKS) item.setType(Material.TERRACOTTA);
				else if (item.getType() == Material.GILDED_BLACKSTONE) item.setType(Material.BLACK_TERRACOTTA);
				else item.setType(Material.GREEN_TERRACOTTA);
				
				//inv.setItem(i, item);
			}
		}
		
		
		for (int i = 0; i < 41; i++)
		{
			if (inv.getItem(i) != null && inv.getItem(i).getType() != Material.AIR && (!getCompletedLevelIndicators().contains(inv.getItem(i).getType())))
			{
				inv.getItem(i).addUnsafeEnchantment(Enchantment.CHANNELING, 1);
				break;
			}
		}
		
		return inv;
	}

	private List<Material> getCompletedLevelIndicators() {
		List<Material> result = new ArrayList<Material>();
		result.add(Material.GREEN_TERRACOTTA);
		result.add(Material.ORANGE_TERRACOTTA);
		result.add(Material.PURPLE_TERRACOTTA);
		result.add(Material.TERRACOTTA);
		result.add(Material.BLACK_TERRACOTTA);
		
		return result;
	}
	
	private ItemStack getItemWithNameFrom(Inventory inv, String name) {
		for (ItemStack item : inv.getContents())
		{
			//Bukkit.broadcastMessage(item.getItemMeta().getDisplayName());
			if (item != null && item.getType() != Material.AIR && item.getItemMeta().getDisplayName().contains(name))
				return item;
		}
		return null;
	}
	
	
	public boolean invClear(Player player) {
		PlayerInventory playerInv = player.getInventory();
		for (ItemStack item : playerInv)
		{
			if (item != null) return false;
		}
		return true;
	}
}