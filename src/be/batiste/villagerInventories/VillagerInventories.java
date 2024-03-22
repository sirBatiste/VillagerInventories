package be.batiste.villagerInventories;



import java.awt.Color;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.TradeSelectEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;


public class VillagerInventories extends JavaPlugin implements Listener{
	Dictionary<Player, Villager> activeInspections= new Hashtable<>();
	
	@Override
	public void onEnable() {
		 int pluginId = 21368; // <-- Replace with the id of your plugin!
	     Metrics metrics = new Metrics(this, pluginId);
		checkVersion();
		getServer().getPluginManager().registerEvents(this, this);	
	}
	
	public void checkVersion(){
	    HttpClient client = HttpClient.newHttpClient();
	    HttpRequest request = HttpRequest.newBuilder()
	          .uri(URI.create("https://batiste.be/api/checkVersion/villagerinventories/0.1"))
	          .build();

	    try {
			HttpResponse<String> response=client.send(request, BodyHandlers.ofString());
			String[] parts = response.body().split("\\|");
			getLogger().info( response.body());
			if(parts[0].equals("ok")) {
				getLogger().info(parts[1]);
			}else if(parts[0].equals("error")) {
				getLogger().warning(parts[1]);
			}
			
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	@EventHandler
	public void InventoryOpenEvent(org.bukkit.event.inventory.InventoryOpenEvent event) {
		if(event.getInventory().getType()!= InventoryType.MERCHANT)
			return;
		Villager villager=(Villager) event.getInventory().getHolder();
		List<MerchantRecipe> villagerRecipes = new ArrayList<>(villager.getRecipes());
		if(inventoryButtonExists(villagerRecipes))
			return;
		addInventoryButton(villager);
		
	}
	@EventHandler
	public void TradeSelectEvent(TradeSelectEvent event) {
		if(event.getIndex()!=0)
			return;
		Villager villager=(Villager) event.getInventory().getHolder();
		Player player=(Player) event.getWhoClicked();
		ItemStack red=namedItem(Material.RED_STAINED_GLASS_PANE," ");
		Inventory myInventory=cloneVillagerInventory(villager);
		myInventory.setItem(8,red);
		player.openInventory(myInventory);
		activeInspections.put(player, villager);
	}
	@EventHandler
	public void closeVillagerInventory(InventoryCloseEvent event) {		
		Player player=(Player) event.getPlayer();
		if(activeInspections.get(player)==null)
			return;
		updateVillagerInventory(activeInspections.get(player),event.getInventory());
		activeInspections.remove(player);
	}
	@EventHandler
	public void cancelRedPanelPick(org.bukkit.event.inventory.InventoryClickEvent event) {
		ItemStack item = event.getCurrentItem();
		if(item.getType()==Material.RED_STAINED_GLASS_PANE && item.getItemMeta().getDisplayName().equals(" ")) {
			event.setCancelled(true);
		}
	}
	
	private boolean inventoryButtonExists(List<MerchantRecipe> recipes) {
		if(recipes.get(0).getIngredients().get(0).getType()==Material.CHEST) {
			return true; 
		}
		return false;
	}
	
	private void addInventoryButton(Villager villager) {
		List<MerchantRecipe> recipes = new ArrayList<>(villager.getRecipes());
		MerchantRecipe recipe = new MerchantRecipe(new ItemStack(Material.AIR), 1);
		recipe.addIngredient(namedItem(Material.CHEST,"Villager Inventory"));
		recipes.add(0, recipe);
		
		villager.setRecipes(recipes);
	}
	
	
	private Inventory cloneVillagerInventory(Villager villager) {
		Inventory villagerInventory = villager.getInventory();
		Inventory myInventory = Bukkit.createInventory(null, 9, villager.getName());
		for(int x = 0;x<=7;x++) {
			myInventory.setItem(x,villagerInventory.getItem(x));
		}
		return myInventory;
	}
	
	
	private void updateVillagerInventory(Villager villager,Inventory inventory) {
		Inventory villagerInventory = villager.getInventory();
		for(int x = 0;x<=7;x++) {
			villagerInventory.setItem(x,inventory.getItem(x));
		}
		
	}
	
	
	private ItemStack namedItem(Material material,String name) {
		ItemStack red=new ItemStack(material);
		ItemMeta meta=red.getItemMeta();
		meta.setDisplayName(name);
		red.setItemMeta(meta);
		return red;
	}
}



	
	
	
