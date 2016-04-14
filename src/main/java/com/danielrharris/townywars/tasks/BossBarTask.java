package com.danielrharris.townywars.tasks;

import java.text.DecimalFormat;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.inventivetalent.bossbar.BossBar;
import org.inventivetalent.bossbar.BossBarAPI;

import com.danielrharris.townywars.TownyWars;
import com.danielrharris.townywars.War;
import com.danielrharris.townywars.WarManager;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;

import mkremins.fanciful.FancyMessage;
import net.md_5.bungee.api.chat.TextComponent;

public class BossBarTask extends BukkitRunnable{
	
	private float percent;
	private Town town;
	private Nation nation;
	private TownyWars plugin;
	private DecimalFormat d = new DecimalFormat("#.00");
	
	public BossBarTask(Town town, TownyWars plugin){
		this.town = town;
		try {
			this.nation = town.getNation();
		} catch (NotRegisteredException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.plugin = plugin;
	}
	
	@SuppressWarnings({ "deprecation", "unused" })
	@Override
	public void run() {
		War wwar = null;
		if(nation!=null){
			for(Resident r : nation.getResidents()){
				if(r.getName()!=null){
					final Player player = Bukkit.getServer().getPlayer(r.getName());
					if(player!=null){								
						percent = 1.0F;
						wwar = WarManager.getWarForNation(nation);
						if(wwar != null && !((Double)(War.getTownMaxPoints(town))).equals(null)){
							try {
								percent = (float)((wwar.getTownPoints(town)/((Double)War.getTownMaxPoints(town)).intValue()));
								if(TownyWars.isBossBar){
									if(percent!=0f){
										if(BossBarAPI.hasBar(player)){
											BossBarAPI.removeAllBars(player);
										}
										String barMessage = "&c&l" + town.getName() + " &r&4&ois Under Attack! &r&4(&fBar is Actual TPs&4)";
										final BossBar bossBar = BossBarAPI.addBar(player,
												new TextComponent(net.md_5.bungee.api.ChatColor.translateAlternateColorCodes('&', barMessage)), // Displayed message
										   BossBarAPI.Color.RED,
										   BossBarAPI.Style.PROGRESS, 
										   percent,
										   2000,
										   5000);
										new BukkitRunnable(){
											@Override
											public void run() {
												if(BossBarAPI.hasBar(player)){
													BossBarAPI.removeAllBars(player);
												}											
											}				
										}.runTaskLater(plugin, 140L);
									}
								}else{
									sendAttackMessage(player, wwar, town);
								}
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}				
					}
				}
			}
		}	
	}
	
	public void sendAttackMessage(Player player, War wwar, Town town){
		String points = "";
		try {
			points = ChatColor.YELLOW + d.format(((Double)wwar.getTownPoints(town))) + ChatColor.WHITE + " Town Points Left";
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		new FancyMessage("                 ")
		.then("g")
			.color(ChatColor.WHITE)
			.style(ChatColor.MAGIC)
		.then("  ")
		.then(town.getName())
			.color(ChatColor.RED)
			.style(ChatColor.BOLD)
			.tooltip("Click to Travel to " + ChatColor.GREEN + town.getName())
			.command("/t spawn " + town.getName())
		.then(" is Under Attack!")
		    .color(ChatColor.DARK_RED)
		    .style(ChatColor.ITALIC)
		    .tooltip(points)
		    .command("/twar showtowndp")
		.then("  ")
		.then("g")
			.color(ChatColor.WHITE)
			.style(ChatColor.MAGIC)
		.send(player);
	  }
}