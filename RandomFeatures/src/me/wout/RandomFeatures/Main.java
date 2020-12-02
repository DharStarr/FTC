package me.wout.RandomFeatures;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.EntityEffect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerEggThrowEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.wout.RandomFeatures.commands.ChristmasGift;
import me.wout.RandomFeatures.commands.Crowntop;
import me.wout.RandomFeatures.commands.Deathtop;
import me.wout.RandomFeatures.commands.Grave;
import me.wout.RandomFeatures.commands.wild;

public class Main extends JavaPlugin implements Listener {
	
	public static Main plugin;
	
	//public BossBar capturePointHealth;
	public int state = 0;
	//public Capture cap;
	//public Capturing capturing;
	public String playername;
	public Boolean busy = false;
	//public Set<LivingEntity> raidwave = new HashSet<LivingEntity>();
	//public Set<LivingEntity> raidwave2 = new HashSet<LivingEntity>();
	//public Set<LivingEntity> raidwave3 = new HashSet<LivingEntity>();
	private boolean hasFrozenAll = false;
	
	public File graves;
	public YamlConfiguration gravesyaml;
	
	public void onEnable() {
		plugin = this;
		
		getConfig().options().copyDefaults(true);
		saveDefaultConfig();
		
		// Commands + Events
		//plugin.getCommand("capture").setExecutor(this);
		//plugin.getCommand("capturing").setExecutor(this);
		getServer().getPluginCommand("crowntop").setExecutor(new Crowntop());
		getServer().getPluginCommand("deathtop").setExecutor(new Deathtop());
		getServer().getPluginCommand("grave").setExecutor(new Grave());
		//getServer().getPluginCommand("aahelp").setExecutor(this);
		getServer().getPluginCommand("wild").setExecutor(new wild());
		getServer().getPluginCommand("christmasgift").setExecutor(new ChristmasGift());
		
		getServer().getPluginManager().registerEvents(this, this);
		
		// Check datafolder.
		File dir = getDataFolder();
		if (!dir.exists())
			if (!dir.mkdir())
				System.out.println("Could not create directory for plugin: " + getDescription().getName());
		
	}
	
	// -----
	
	
	public void onDisable() {
		loadFiles();
		saveyaml(gravesyaml, graves);
		unloadFiles();
	}
	
	public void loadFiles() {
		graves = new File(getDataFolder(), "GravesData.yml");
		if(!graves.exists()){
			try {
				graves.createNewFile();
				gravesyaml = YamlConfiguration.loadConfiguration(graves);
				saveyaml(gravesyaml, graves);
			} catch (IOException e) {
				e.printStackTrace();
			}
        } else {
        	gravesyaml = YamlConfiguration.loadConfiguration(graves);
        }
	}
	
	public void unloadFiles() {
		saveyaml(gravesyaml, graves);
		gravesyaml = null;
		graves = null;
	}
	
	private void saveyaml(YamlConfiguration yaml, File file) {
		try {
			yaml.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// -----
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("aaconfig")) {
			if (sender.isOp()) {
				reloadConfig();
				sender.sendMessage(ChatColor.GRAY + "AutoAnnouncer config reloaded.");
			} else {
				sender.sendMessage(ChatColor.RED + "You don't have permission to do this!");
			}
		}
		
		return true;
	}
	
	/*public String getRegionWorld() {
		return getConfig().getString("RegionWorld");
	}*/
	
	/*public List<String> getOtherWorlds() {
		return (List<String>) getConfig().getStringList("OtherWorlds");
	}*/
	
	
	// MOTHERBAT
	/*@EventHandler
	public void onEntityDeath(EntityDeathEvent event)
	{             
		if(event.getEntity() instanceof Bat) {
	    	Bat mother = (Bat) event.getEntity();
	    	if (mother.isCustomNameVisible()) {
	    		for (int i = 0;i<5;i++)
	    			mother.getWorld().spawnEntity(mother.getLocation(), EntityType.BAT);
	    	}
	     }
	}
	*/
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		loadFiles();
		if (gravesyaml.getList(event.getPlayer().getUniqueId().toString()) != null) {
			event.getPlayer().sendMessage(ChatColor.GRAY + "[FTC] You have royal items in your " + ChatColor.YELLOW + "/grave" + ChatColor.GRAY + ".");
		}
		unloadFiles();
		
		if (!hasFrozenAll)
		{
			for (World world : Bukkit.getServer().getWorlds()) 
			{
				for (Entity e : world.getEntities())
					if (e.getType() == EntityType.VILLAGER && e.isInvulnerable()) ((Villager) e).setCollidable(false);
			}
			hasFrozenAll = true;
			Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.YELLOW + "!" + ChatColor.RESET + " Tried freezing all villagers");
		}
	}
	
	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		loadFiles();
		if (gravesyaml.getList(event.getPlayer().getUniqueId().toString()) != null) {
			event.getPlayer().sendMessage(ChatColor.GRAY + "[FTC] You have royal items in your " + ChatColor.YELLOW + "/grave" + ChatColor.GRAY + ".");
		}
		unloadFiles();
	}
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		Player player = (Player) event.getEntity();
		
		// Find royal tools
		List<ItemStack> graveitems = new ArrayList<ItemStack>();
		for (ItemStack item : player.getInventory().getContents()) {
			if (item != null && item.getType() != Material.AIR) {
				if (item.getItemMeta().hasLore() && (item.getType() == Material.GOLDEN_HELMET || item.getType() == Material.GOLDEN_SWORD || item.getType() == Material.NETHERITE_SWORD)) {
					graveitems.add(item.clone());
					item.setAmount(0);
				}
			}
		}
		
		// Add to file or existing grave if found.
		if (!graveitems.isEmpty()) {
			loadFiles();
			if (gravesyaml.getList(player.getUniqueId().toString()) == null) {
				gravesyaml.createSection(player.getUniqueId().toString());
				gravesyaml.set(player.getUniqueId().toString(), graveitems);
				
			}
			else {
				@SuppressWarnings("unchecked")
				List<ItemStack> existingGrave = (List<ItemStack>) gravesyaml.getList(player.getUniqueId().toString());
				for (ItemStack item : graveitems) {
					existingGrave.add(item);
				}
				gravesyaml.set(player.getUniqueId().toString(), existingGrave);
			}
			unloadFiles();
		}
		
		
	}

	
	
	// ITEM-RIGHTCLICK ADDS SCORE
	/*@EventHandler
	public void onPlayerClick(PlayerInteractEvent event) {
		Player player = (Player) event.getPlayer();
		
		if (player.getInventory().getItemInMainHand().getType() == Material.getMaterial(getConfig().getString("Item"))) {
			ItemStack handItem = player.getInventory().getItemInMainHand();
			int amount = handItem.getAmount();
			
			if (handItem.getEnchantments().containsKey(Enchantment.CHANNELING)) {
				Objective scoreboardobj = player.getScoreboard().getObjective(DisplaySlot.PLAYER_LIST);
				if (!scoreboardobj.getName().equalsIgnoreCase(getConfig().getString("Scoreboard"))) {
					player.sendMessage(ChatColor.RED + "[" + ChatColor.GOLD + "FTC" + ChatColor.RED + "]" + ChatColor.RESET + " You can't do this at this moment!");
					return;
				}
				
				@SuppressWarnings("deprecation")
				Score score = scoreboardobj.getScore(player);
				score.setScore(score.getScore() + amount);
				
				handItem.setAmount(0);
				if (amount == 1) player.sendMessage(ChatColor.GOLD + "[FTC]" + ChatColor.RESET + " Added " + ChatColor.YELLOW + "1" + ChatColor.RESET + " point to your score!");
				else player.sendMessage(ChatColor.GOLD + "[FTC]" + ChatColor.RESET + " Added " + ChatColor.YELLOW + amount + ChatColor.RESET + " points to your score!");
			}
		}
	}*/
	
	
	// PINATA
	/*private boolean canDrop = true;
	@EventHandler
	public void onPlayerHitPinata(EntityDamageByEntityEvent event) {
		if (event.getEntity() instanceof Husk && event.getDamager() instanceof Player) {
            Husk husk = (Husk) event.getEntity();
            Player player = (Player) event.getDamager();
            
            if (husk.getCustomName() != null && husk.getCustomName().contains(ChatColor.YELLOW + "$$$")) {
            	Objective scoreboardobj = player.getScoreboard().getObjective(DisplaySlot.PLAYER_LIST);
				if (!scoreboardobj.getName().equalsIgnoreCase(getConfig().getString("Scoreboard"))) {
					//player.sendMessage(ChatColor.RED + "[" + ChatColor.GOLD + "FTC" + ChatColor.RED + "]" + ChatColor.RESET + " You can't do this at this moment!");
					return;
				}
				@SuppressWarnings("deprecation")
				Score score = scoreboardobj.getScore(player);
				score.setScore(score.getScore() + 1);
				player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.2f, 1.0f);
				
				if (canDrop == false) return;
				
				double random = Math.random();
				if (random < 0.33) {
					Item gold = player.getWorld().dropItem(husk.getLocation(), new ItemStack(Material.GOLD_INGOT));
					gold.setVelocity(new Vector(0, 0.2, 0));
				}
				else if (random > 0.85) {
					Item diamond = player.getWorld().dropItem(husk.getLocation(), new ItemStack(Material.DIAMOND));
					diamond.setVelocity(new Vector(0, 0.2, 0));
				}
				else return;
				
				this.canDrop = false;
				Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			        @Override
			        public void run() {
			        	canDrop = true;
			        }
			    }, 100L);
            }
        }
	}*/
	
	
	
	// PINATA HAROLD TP
	/*private Set<Player> teleportingPlayers = new HashSet<Player>();
	
	@EventHandler
	public void onPlayerClick(PlayerInteractEntityEvent event) {
		if(!event.getHand().equals(EquipmentSlot.HAND))
			return;
		Player player = (Player) event.getPlayer();
		if (event.getRightClicked().getType() == EntityType.VILLAGER) {
			if (event.getRightClicked().getName().contains(ChatColor.GOLD + "Harold")) {
				if (teleportingPlayers.contains(player)) return;
				teleportingPlayers.add(player);
				player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 200, 4));
				Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			        @Override
			        public void run() {
			        	player.teleport(new Location(Bukkit.getWorld("world"), -66.5d, 75d, 900.5d, 90f, 0f));
			        	player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.5f, 1f);
			        	teleportingPlayers.remove(player);
			        }
			    }, 120L);
			}
		}
	}*/
	
	// ENDER WEEK
	/*private Set<UUID> endermenCD = new HashSet<UUID>();
	
	@EventHandler
	public void onEntityDeath(EntityDeathEvent event)
	{       
		if (event.getEntity().getKiller() == null) return;
		
		Objective scoreboardobj = event.getEntity().getKiller().getScoreboard().getObjective(DisplaySlot.PLAYER_LIST);
		if (!scoreboardobj.getName().equalsIgnoreCase(getConfig().getString("Scoreboard"))) {
			return;
		}
		@SuppressWarnings("deprecation")
		Score score = scoreboardobj.getScore(event.getEntity().getKiller());
		
		if(event.getEntity().getType() == EntityType.ENDERMAN) {
			event.getEntity().setCustomNameVisible(true);
			event.getEntity().setCustomName(ChatColor.LIGHT_PURPLE + "+1 Point");
			score.setScore(score.getScore() + 1);
			endermenCD.remove(event.getEntity().getUniqueId());
	    }
		else if (event.getEntity().getType() == EntityType.ENDERMITE) {
			event.getEntity().setCustomNameVisible(true);
			event.getEntity().setCustomName(ChatColor.LIGHT_PURPLE + "+5 Points");
			score.setScore(score.getScore() + 5);
		}
		else if (event.getEntity().getType() == EntityType.SHULKER) {
			event.getEntity().setCustomNameVisible(true);
			event.getEntity().setCustomName(ChatColor.LIGHT_PURPLE + "+15 Points");
			score.setScore(score.getScore() + 15);	
		}
		else if (event.getEntity().getType() == EntityType.ENDER_DRAGON) {
			score.setScore(score.getScore() + 500);
		}
	}
		
	@EventHandler
	public void onEndermanTeleport(EntityTeleportEvent event) {
		if (event.getEntityType() == EntityType.ENDERMAN && !endermenCD.contains(event.getEntity().getUniqueId())) {
			event.getEntity().getWorld().spawn(event.getFrom(), Endermite.class);
			endermenCD.add(event.getEntity().getUniqueId());
		}
	}*/

	// HAROLD PROTECTING
	/*int killcounter = 0;
	
	@EventHandler
	public void onEntityDeath(EntityDeathEvent event)
	{       
		// HAROLD DIED
		if (event.getEntity().getType() == EntityType.VILLAGER && event.getEntity().isGlowing()) {
			killcounter = 0;
		}
		
		if (event.getEntity().getKiller() == null) return;
		
		// ENTITY IS ZOMBIE / HUSK WITH BUTTON ON HEAD
		if (event.getEntity().getType() == EntityType.ZOMBIE || event.getEntity().getType() == EntityType.HUSK) {
			if (event.getEntity().getEquipment().getHelmet().getType() != Material.STONE_BUTTON) {
				return;
			}
			Objective scoreboardobj = event.getEntity().getKiller().getScoreboard().getObjective(DisplaySlot.PLAYER_LIST);
			if (!scoreboardobj.getName().equalsIgnoreCase(getConfig().getString("Scoreboard"))) {
				return;
			}
			@SuppressWarnings("deprecation")
			Score score = scoreboardobj.getScore(event.getEntity().getKiller());
			
			event.getEntity().setCustomNameVisible(true);
			event.getEntity().setCustomName(ChatColor.GOLD + "+1 Point");
			killcounter++;
			
			if (killcounter % 10 == 0) {
				event.getEntity().getKiller().sendMessage(ChatColor.GRAY + "You've killed " + killcounter + " enemies in this run.");
				double chance = Math.random();
				if (chance > 0.8) {
					Bukkit.dispatchCommand(this.getServer().getConsoleSender(), "crate givekey " + event.getEntity().getKiller().getName() + " crate1 1");
				}
			}
			
			if (killcounter > score.getScore()) {
				score.setScore(killcounter);
			}
			
	    }
		
	}*/
	
	// Harold old-heads to new-heads
	/*@SuppressWarnings("deprecation")
	@EventHandler
	public void onPlayerClick(PlayerInteractEntityEvent event) {
		if(!event.getHand().equals(EquipmentSlot.HAND))
			return;

		if (event.getRightClicked().getType() == EntityType.VILLAGER) 
		{
			if (event.getRightClicked().getName().contains(ChatColor.GOLD + "Harold")) 
			{
				Player player = (Player) event.getPlayer();
				ItemStack itemInHand = player.getInventory().getItemInMainHand();
				if (itemInHand != null && itemInHand.getType() == Material.PLAYER_HEAD && itemInHand.hasItemMeta() && itemInHand.getItemMeta().getDisplayName().contains(ChatColor.YELLOW + "" + ChatColor.BOLD)) 
				{
					String itemInHandName = itemInHand.getItemMeta().getDisplayName().split(" Head")[0];
					ItemStack bestMatchingItem = null;
					int minDistance = 999;
					Set<Location> headChestLocs = new HashSet<Location>();
					for (int i = 0; i < 4; i++) 
					{
						Location chestloc = new Location(Bukkit.getWorld("world"), 208, 61, 1001+i);
						headChestLocs.add(chestloc);
					}
					
					
					for (Location loc : headChestLocs) 
					{
						for (int slot = 0; slot < 27; slot++)
						{
							ItemStack chosenItem = ((Chest) loc.getWorld().getBlockAt(loc).getState()).getInventory().getContents()[slot];
							
							if (chosenItem != null && chosenItem.hasItemMeta() && chosenItem.getType() == Material.PLAYER_HEAD) 
							{
								if (StringUtils.getLevenshteinDistance(itemInHandName, ((SkullMeta) chosenItem.getItemMeta()).getOwner()) < minDistance) 
								{
									//Bukkit.broadcastMessage(((SkullMeta) chosenItem.getItemMeta()).getOwner() + " - " + StringUtils.getLevenshteinDistance(itemInHandName, ((SkullMeta) chosenItem.getItemMeta()).getOwner()));
									bestMatchingItem = chosenItem.clone();
									minDistance = StringUtils.getLevenshteinDistance(itemInHandName, ((SkullMeta) chosenItem.getItemMeta()).getOwner());
								}
							}
						}
					}
					if (minDistance > 4)
					{
						player.sendMessage(ChatColor.GRAY + "No matching head found :(");
						return;
					}
					int amount = itemInHand.getAmount();
					itemInHand.setAmount(0);
					for (int i = amount; i != 0; i--)
					{
						player.getInventory().addItem(bestMatchingItem);
					}
					player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1f, 1f);
					this.getServer().getConsoleSender().sendMessage(player.getName() + " gave " + amount + " " + itemInHandName + ChatColor.RESET + " to Harold, and received " + bestMatchingItem.getItemMeta().getDisplayName() + ChatColor.RESET + ".");
				}
			}
		}
	}*/
	
	//199 66 958
	
	@EventHandler
	public void onPlayerClick(PlayerInteractEntityEvent event) {
		if(!event.getHand().equals(EquipmentSlot.HAND))
			return;
		if (event.getRightClicked().getType() == EntityType.WITCH) {
			if (event.getRightClicked().getName().contains(ChatColor.GRAY + "Definitely")) {
				ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
				if (item != null
						&& item.hasItemMeta()
						&& item.getItemMeta().hasLore()
						&& item.getItemMeta().getLore().get(0).contains("Right click harold with this item in your hand...")) 
				{
					Chest chest = (Chest) event.getRightClicked().getWorld().getBlockAt(new Location(event.getRightClicked().getWorld(), 199, 66, 958)).getState();
					event.getRightClicked().getWorld().dropItem(event.getPlayer().getLocation(), chest.getInventory().getItem(0));
					event.getRightClicked().getWorld().playSound(event.getRightClicked().getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 2f, 1f);
					System.out.println(event.getPlayer().getName() + " has been given the halloween shulker");
					item.setAmount(item.getAmount()-1);
				}
				else {
					event.getPlayer().sendMessage(ChatColor.GRAY + "You need to hold a Halloween Ticket.");
				}
				
			}
		}
	}
	
	
	@EventHandler
	public void test(EntityPortalEnterEvent event)
	{
		if (event.getEntity().getWorld().getName().contains("world_the_end"))
		{
			//Bukkit.broadcastMessage("happened! (2)");
			if (event.getEntity() instanceof Player)
			{
				try {
					event.getEntity().teleport(((Player) event.getEntity()).getBedSpawnLocation());
				}
				catch (Exception e) {
					event.getEntity().teleport(new Location(Bukkit.getWorld("world"), 200.5, 70, 1000.5));
				}
			}
			else 
			{
				event.getEntity().teleport(new Location(Bukkit.getWorld("world"), 282.0, 72, 948.0));
			}
		}
		else 
		{
			// Wildportals
			Location locs[] = {new Location(Bukkit.getWorld("world"), 181, 67, 1000), new Location(Bukkit.getWorld("world"), 269, 62, 1014)};
			
			for (Location loc : locs) 
			{
				try {
					if (loc.distance(event.getEntity().getLocation()) > 3) continue;
				} catch (Exception e) {
					continue;
				}
				
				if (!(event.getEntity() instanceof Player))
					return;
				
				Player player = (Player) event.getEntity();
				 
				if(player != null) {
					int x = 0;
					if (Math.random() < 0.5)
						x = getRandomNumberInRange(200, 4800);
					else 
						x = getRandomNumberInRange(-4800, -200);
					
					int z = 0;
					if (Math.random() < 0.5)
						z = getRandomNumberInRange(200, 4800);
					else 
						z = getRandomNumberInRange(-4800, -200);
					
					player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 400, 1));
					player.teleport(new Location(player.getWorld(), x, 150, z));
					
					player.setBedSpawnLocation(new Location(player.getWorld(), 200, 70, 1000), true);
					player.sendMessage(ChatColor.GRAY + "You've been teleported. To get back to spawn, you can do " + ChatColor.YELLOW + "/findpost" + ChatColor.GRAY + " and then " + ChatColor.YELLOW + "/visit hazelguard" + ChatColor.GRAY + ".");
					break;
				} 
			}
		}
		
	}
	
	private static int getRandomNumberInRange(int min, int max) {
		if (min >= max) {
			return 0;
		}
		Random r = new Random();
		return r.nextInt((max - min) + 1) + min;
	}
	
	
	/*@EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) 
	{
		if (event.getPlayer().hasPermission("ftc.donator3"))
		{
			String message = event.getMessage();
			message = message.replaceAll(":shrug:", "¯\\\\_(ツ)_/¯");
			message = message.replaceAll(":ughcry:", "(ಥ�?ಥ)");
			message = message.replaceAll(":gimme:", "༼ �?� ◕_◕ ༽�?�");
			message = message.replaceAll(":gimmecry:", "༼ �?� ಥ_ಥ ༽�?�");
			message = message.replaceAll(":bear:", "ʕ• ᴥ •ʔ");
			message = message.replaceAll(":smooch:", "( ^ 3^) ♥");
			message = message.replaceAll(":why:", "ლ(ಠ益ಠლ)");
			message = message.replaceAll(":tableflip:", "(ノಠ益ಠ)ノ彡┻�?┻");
			message = message.replaceAll(":tableput:", " ┬──┬ ノ( ゜-゜ノ)");
			message = message.replaceAll(":pretty:", "(◕‿◕ ✿)");
			message = message.replaceAll(":sparkle:", "(ﾉ◕ヮ◕)ﾉ*:･ﾟ✧");
			message = message.replaceAll(":blush:", "(▰˘◡˘▰)");
			message = message.replaceAll(":sad:", "(._. )");
			event.setMessage(message);
			return;
		}
	}*/
	
	Set<UUID> eggs = new HashSet<>();
	
	@EventHandler
	public void smokeBomb(PlayerInteractEvent event) {
		if (event.getHand() != EquipmentSlot.HAND) return;
		if (event.getPlayer().getInventory().getItemInMainHand() == null) return;
		
		if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) 
		{
			ItemStack itemInMainHand = event.getPlayer().getInventory().getItemInMainHand();
			if (itemInMainHand.getType() == Material.FIREWORK_STAR && itemInMainHand.getItemMeta().getDisplayName().contains(ChatColor.GRAY + "Smoke Bomb"))
			{
				if (event.getPlayer().hasCooldown(Material.FIREWORK_STAR)) return;
				
				if (event.getPlayer().getGameMode() != GameMode.CREATIVE && event.getPlayer().getGameMode() != GameMode.SPECTATOR) itemInMainHand.setAmount(itemInMainHand.getAmount()-1);
				Egg egg = (Egg) event.getPlayer().launchProjectile(Egg.class);
				egg.playEffect(EntityEffect.TOTEM_RESURRECT);
				egg.setCustomName(ChatColor.GRAY + "Boom!");
				eggs.add(egg.getUniqueId());
				eggSmoke();
				
				event.getPlayer().setCooldown(Material.FIREWORK_STAR, 200);
			}
		}
	}
	
	private void eggSmoke() {
		Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
	        @Override
	        public void run() {
	        	Set<UUID> toRemove = new HashSet<UUID>();
	        	for (UUID eggID : eggs) {
	        		try {
	        			Location loc = Bukkit.getEntity(eggID).getLocation();
	        			loc.getWorld().spawnParticle(Particle.SMOKE_NORMAL, loc.getX(), loc.getY(), loc.getZ(), 0, 0, 0, 0, 1);
	        		}
		        	catch (Exception ignored) {
		        		toRemove.add(eggID);
		        	}
	        	}
	        	for (UUID uuid : toRemove) eggs.remove(uuid);
	        	if (!eggs.isEmpty()) eggSmoke();
	        }
	    }, 3);
	}
	
	@EventHandler
	public void smokeBomb(PlayerEggThrowEvent event) {
		if (event.getEgg().getCustomName() != null && event.getEgg().getCustomName().contains(ChatColor.GRAY + "Boom!")) {
			event.setHatching(false);
		}
	}
	
	@EventHandler
	public void smokeBomb(ProjectileHitEvent event) {
		if (event.getEntity() instanceof Egg && event.getEntity().getCustomName() != null && event.getEntity().getCustomName().contains(ChatColor.GRAY + "Boom!")) 
		{
			eggs.remove(event.getEntity().getUniqueId());
			//execute at Wout run particle minecraft:campfire_signal_smoke ~ ~1.6 ~ 0.15 0.05 0.15 0 10 force
			Location loc = event.getEntity().getLocation();
			loc.getWorld().playSound(loc, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1, 1);
			loc.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, loc.getX(), loc.getY()+1, loc.getZ(), 10, 1, 1, 1, 0);
			
			double x = loc.getBlockX();
			double y = loc.getBlockY();
			double z = loc.getBlockZ();
			for (int i = -1; i <= 1; i++) {
				for (int j = 0; j <= 2; j++) {
					for (int k = -1; k <= 1; k++) {
						loc.getWorld().spawnParticle(Particle.CAMPFIRE_SIGNAL_SMOKE, x+i, y+j, z+k, 50, 0.5, 0.5, 0.5, 0.01);
					}
				}
			}
			
		}
	}
	
}
