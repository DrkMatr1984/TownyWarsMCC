package main.java.com.danielrharris.townywars;

import com.palmergames.bukkit.towny.exceptions.EconomyException;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownyUniverse;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginManager;


class WarExecutor implements CommandExecutor
{
  private TownyWars plugin;
 
   public WarExecutor(TownyWars aThis)
  {
    this.plugin = aThis;
  }
  
  public boolean onCommand(CommandSender cs, Command cmnd, String string, String[] strings)
  {
    if (strings.length == 0)
    {
      cs.sendMessage(ChatColor.GREEN + "Towny Wars Configuration Information:");
      cs.sendMessage(ChatColor.BLUE + "Defense Points Calculation: " + ChatColor.AQUA + "Per Player: " + ChatColor.YELLOW + TownyWars.pPlayer + ChatColor.AQUA + " || Per Chunk: " + ChatColor.YELLOW + TownyWars.pPlayer);
      cs.sendMessage(ChatColor.BLUE + "Costs: " + ChatColor.AQUA + "Per Death: " + ChatColor.YELLOW + TownyWars.pKill + ChatColor.AQUA + " || Declare Cost: " + ChatColor.YELLOW + TownyWars.declareCost + ChatColor.AQUA + " || End Cost: " + ChatColor.YELLOW + TownyWars.endCost);
      cs.sendMessage(ChatColor.GREEN + "For help with TownyWars, type /twar help");
      return true;
    }
    String farg = strings[0];
    if (farg.equals("reload"))
    {
      if (!cs.hasPermission("townywars.admin")) {
        return false;
      }
      cs.sendMessage(ChatColor.GREEN + "Reloading plugin...");
      PluginManager pm = Bukkit.getServer().getPluginManager();
      pm.disablePlugin(this.plugin);
      pm.enablePlugin(this.plugin);
      cs.sendMessage(ChatColor.GREEN + "Plugin reloaded!");
    }
    if (farg.equals("help"))
    {
      cs.sendMessage(ChatColor.GREEN + "Towny Wars Help:");
      cs.sendMessage(ChatColor.AQUA + "/twar" + ChatColor.YELLOW + "Displays the TownyWars configuration information");
      cs.sendMessage(ChatColor.AQUA + "/twar help - " + ChatColor.YELLOW + "Displays the TownyWars help page");
      cs.sendMessage(ChatColor.AQUA + "/twar status - " + ChatColor.YELLOW + "Displays a list of on-going wars");
      cs.sendMessage(ChatColor.AQUA + "/twar status [nation] - " + ChatColor.YELLOW + "Displays a list of the nation's towns and their defense points");
      cs.sendMessage(ChatColor.AQUA + "/twar showtowndp - " + ChatColor.YELLOW + "Shows your towns current defense points.");
      cs.sendMessage(ChatColor.AQUA + "/twar declare [nation] - " + ChatColor.YELLOW + "Starts a war with another nation (REQUIRES YOU TO BE A KING/ASSISTANT)");
      cs.sendMessage(ChatColor.AQUA + "/twar end - " + ChatColor.YELLOW + "Request from enemy nations king to end the ongoing war. (REQUIRES YOU TO BE A KING/ASSISTANT)");
      cs.sendMessage(ChatColor.AQUA + "/twar createrebellion [name] - " + ChatColor.YELLOW + "Creates a (secret) rebellion within your nation.");
      cs.sendMessage(ChatColor.AQUA + "/twar joinrebellion [name] - " + ChatColor.YELLOW + "Joins a rebellion within your nation using the name.");
      cs.sendMessage(ChatColor.AQUA + "/twar leaverebellion - " + ChatColor.YELLOW + "Leaves your current rebellion.");
      cs.sendMessage(ChatColor.AQUA + "/twar showrebellion - " + ChatColor.YELLOW + "Shows your current rebellion and its members.");
      cs.sendMessage(ChatColor.AQUA + "/twar executerebellion - " + ChatColor.YELLOW + "Executes your rebellion and you go to war with your nation (requires to be leader of rebellion).");
      if (cs.hasPermission("townywars.admin"))
      {
        cs.sendMessage(ChatColor.AQUA + "/twar reload - " + ChatColor.YELLOW + "Reload the plugin");
        cs.sendMessage(ChatColor.AQUA + "/twar astart [nation] [nation] - " + ChatColor.YELLOW + "Forces two nations to go to war");
        cs.sendMessage(ChatColor.AQUA + "/twar aend [nation] [nation] - " + ChatColor.YELLOW + "Forces two nations to stop a war");
      }
      return true;
    }
    War w;
    if (farg.equals("status"))
    {
      if (strings.length == 1)
      {
        cs.sendMessage(ChatColor.GREEN + "List of on-going wars:");
        for (War war : WarManager.getWars())
        {
          Nation first = null;
          Nation second = null;
          for (Nation st : war.getNationsInWar()) {
            if (first == null) {
              first = st;
            } else {
              second = st;
            }
          }
          try {
			cs.sendMessage(ChatColor.GREEN + first.getName() + " " + war.getNationPoints(first) + " vs. " + second.getName() + " " + war.getNationPoints(second));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        }
        return true;
      }
      String onation = strings[1];
      Nation t;
      try
      {
        t = TownyUniverse.getDataSource().getNation(onation);
      }
      catch (NotRegisteredException ex)
      {
        cs.sendMessage(ChatColor.GOLD + "No nation called " + onation + " could be found!");
        return true;
      }
      w = WarManager.getWarForNation(t);
      if (w == null)
      {
        cs.sendMessage(ChatColor.RED + "That nation isn't in a war!");
        return true;
      }
      cs.sendMessage(t.getName() + " war info:");
      for (Town tt : t.getTowns()) {
        try {
			cs.sendMessage(ChatColor.GREEN + tt.getName() + ": " + w.getTownPoints(tt) + " points");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
      }
      return true;
    }
    if (farg.equals("showtowndp")){
    	Town town = null;
    	try {
			town = TownyUniverse.getDataSource().getResident(cs.getName()).getTown();
		} catch (NotRegisteredException e) {
			cs.sendMessage(ChatColor.RED + "You are not in a Town!");
			return true;
		}
    	Double points = town.getNumResidents() * TownyWars.pPlayer + (60-60*Math.pow(Math.E, (-0.00203*town.getTownBlocks().size())));
    	
    	cs.sendMessage(ChatColor.YELLOW + "Your towns defense value is currently " +  points.floatValue() + " which result in " + points.intValue() + " defense points!");
    	return true;
    }
    if (farg.equals("neutral"))
    {
      if (!cs.hasPermission("townywars.neutral"))
      {
        cs.sendMessage(ChatColor.RED + "You are not allowed to do this!");
        return true;
      }
      Nation csNation;
      try
      {
        Town csTown = TownyUniverse.getDataSource().getResident(cs.getName()).getTown();
        csNation = TownyUniverse.getDataSource().getTown(csTown.toString()).getNation();
      }
      catch (NotRegisteredException ex)
      {
        cs.sendMessage(ChatColor.RED + "You are not not part of a town, or your town is not part of a nation!");
        Logger.getLogger(WarExecutor.class.getName()).log(Level.SEVERE, null, ex);
        return true;
      }
      if ((!cs.isOp()) && (!csNation.toString().equals(strings[1])))
      {
        cs.sendMessage(ChatColor.RED + "You may only set your own nation to neutral, not others.");
        return true;
      }
      if (strings.length == 0) {
        cs.sendMessage(ChatColor.RED + "You must specify a nation to toggle neutrality for (eg. /twar neutral [nation]");
      }
      if (strings.length == 1)
      {
        String onation = strings[1];
        Nation t;
        try
        {
          t = TownyUniverse.getDataSource().getNation(onation);
        }
        catch (NotRegisteredException ex)
        {
          cs.sendMessage(ChatColor.GOLD + "The nation called " + onation + " could be found!");
          return true;
        }
        War.MutableInteger mi = new War.MutableInteger(0);
        WarManager.neutral.put(t.toString(), mi);
      }
    }
    if (farg.equals("astart"))
    {
      if (!cs.hasPermission("townywars.admin"))
      {
        cs.sendMessage(ChatColor.RED + "You are not allowed to do this!");
        return true;
      }
      return declareWar(cs, strings, true);
    }
    if (farg.equals("declare")) {
      return declareWar(cs, strings, false);
    }
    if (farg.equals("end")) {
      return declareEnd(cs, strings, false);
    }
    if (farg.equals("createrebellion")) {
        return createRebellion(cs,strings, false);
      }
    if (farg.equals("joinrebellion")) {
    	return joinRebellion(cs,strings, false);
    }
    if (farg.equals("leaverebellion")) {
    	return leaveRebellion(cs, strings, false);
    }
    if(farg.equals("executerebellion")) {
      return executeRebellion(cs, strings, false);
    }
    if(farg.equals("showrebellion")) {
    	return showRebellion(cs, strings, false);
    }
    if (farg.equals("aend"))
    {
      if (!cs.hasPermission("warexecutor.admin"))
      {
        cs.sendMessage(ChatColor.RED + "You are not allowed to do this!");
        return true;
      }
      return declareEnd(cs, strings, true);
    }
    cs.sendMessage(ChatColor.RED + "Unknown twar command.");
    return true;
  }
  
  private boolean showRebellion(CommandSender cs, String[] strings, boolean admin) {
	  
	  Resident res = null;
	  try {
		res = TownyUniverse.getDataSource().getResident(cs.getName());
	  } catch (NotRegisteredException e3) {
		// TODO Auto-generated catch block
		e3.printStackTrace();
		}
	  
	  try {
			if ((!admin) && (!res.getTown().isMayor(res)))
			  {
			      cs.sendMessage(ChatColor.RED + "You are not powerful enough in your town to do that!");
			      return true;
			  }
		} catch (NotRegisteredException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
	  
	  for(Rebellion r : Rebellion.getAllRebellions()){
			try {
				if(r.isRebelTown(res.getTown()) || r.isRebelLeader(res.getTown())){
						cs.sendMessage(ChatColor.YELLOW + ".oOo.___________.[ " + r.getName() + " (Rebellion) ].___________.oOo.");
						cs.sendMessage(ChatColor.GREEN + "Nation: " + r.getMotherNation().getName());
						cs.sendMessage(ChatColor.GREEN + "Leader: " + r.getLeader().getName());
						String members = new String("");
						for(Town town : r.getRebels())
							members = members + ", " + town.getName();
						if(!members.isEmpty())
							members = members.substring(1);
						cs.sendMessage(ChatColor.GREEN + "Members: " + members);
						return true;
				}
			} catch (NotRegisteredException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	  }
	  
	  cs.sendMessage(ChatColor.RED + "You are not in a rebellion!");
	  return true;
  }

//Author: Noxer
  private boolean createRebellion(CommandSender cs, String[] strings, boolean admin){
	  
	  Resident res = null;
	try {
		res = TownyUniverse.getDataSource().getResident(cs.getName());
	} catch (NotRegisteredException e2) {
		// TODO Auto-generated catch block
		e2.printStackTrace();
	}
	  
	  if(strings.length != 2){
	  	cs.sendMessage(ChatColor.RED + "You need to give your rebellion a name!");
	  	return true;
	  }
	  
	  try {
		if((!admin) && (!res.getTown().hasNation())){
			cs.sendMessage(ChatColor.RED + "You are not in a nation!");
			return true;
		  }
	} catch (NotRegisteredException e3) {
		// TODO Auto-generated catch block
		e3.printStackTrace();
	}
	  
	  try {
		if ((!admin) && (!res.getTown().isMayor(res)))
		  {
			  cs.sendMessage(ChatColor.RED + "You are not powerful enough in your town to do that!");
			  return true;
		  }
	} catch (NotRegisteredException e2) {
		// TODO Auto-generated catch block
		e2.printStackTrace();
	}
	  
	  try {
		if (res.getTown().getNation().getCapital() == res.getTown())
		  {
			  cs.sendMessage(ChatColor.RED + "You cannot create a rebellion (towards yourself) when you are the capital!");
			  return true;
		  }
	} catch (NotRegisteredException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	}
	  
	  for(Rebellion r : Rebellion.getAllRebellions()){
		  try {
			if(r.isRebelTown(res.getTown()) || r.isRebelLeader(res.getTown())){
			  		cs.sendMessage(ChatColor.RED + "You are already in a rebellion!");
			      	return true;
			  }
		} catch (NotRegisteredException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	  }
	  for(Rebellion r : Rebellion.getAllRebellions())
			if(r.getName() == strings[1]){
				  cs.sendMessage(ChatColor.RED + "Rebellion with that name already exists!");
				  return true;
			  }
	  if(strings[1].length() > 13){
		  cs.sendMessage(ChatColor.RED + "Rebellion name too long (max 13)!");
		  return true;
	  }
	  try {
		new Rebellion(res.getTown().getNation(), strings[1], res.getTown());
		cs.sendMessage(ChatColor.YELLOW + "You created the rebellion " + strings[1] + " in your nation!");
	} catch (NotRegisteredException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	  return true;
  }
  
  //Author: Noxer
  private boolean joinRebellion(CommandSender cs, String[] strings, boolean admin)
  {
	  Resident res = null;
	  try {
		res = TownyUniverse.getDataSource().getResident(cs.getName());
	} catch (NotRegisteredException e3) {
		// TODO Auto-generated catch block
		e3.printStackTrace();
	}
	  
	  if(strings.length != 2){
		  	cs.sendMessage(ChatColor.RED + "You need to specify which rebellion to join!");
		  	return true;
	 }
	  
	  try {
		if ((!admin) && (!res.getTown().isMayor(res)))
		  {
		      cs.sendMessage(ChatColor.RED + "You are not powerful enough in your town to do that!");
		      return true;
		  }
	} catch (NotRegisteredException e2) {
		// TODO Auto-generated catch block
		e2.printStackTrace();
	}
	  
	  try {
		if (res.getTown().getNation().getCapital() == res.getTown())
		  {
			  cs.sendMessage(ChatColor.RED + "You cannot join a rebellion (towards yourself) when you are the capital!");
			  return true;
		  }
	} catch (NotRegisteredException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	}
	  
	  for(Rebellion r : Rebellion.getAllRebellions()){
		  try {
			if(r.isRebelTown(res.getTown()) || r.isRebelLeader(res.getTown())){
			  		cs.sendMessage(ChatColor.RED + "You are already in a rebellion!");
			      	return true;
			  }
		} catch (NotRegisteredException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	  }
	  for(Rebellion r : Rebellion.getAllRebellions()){
	  	try {
			if(r.getName().equals(strings[1]) && res.getTown().getNation() == r.getMotherNation()){
				try {
					r.addRebell(res.getTown());
					cs.sendMessage(ChatColor.YELLOW + "You join the rebellion " + r.getName() + "!");
					Bukkit.getPlayer(r.getLeader().getMayor().getName()).sendMessage(ChatColor.YELLOW + r.getName() + " joined your rebellion!");
					return true;
				} catch (NotRegisteredException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} catch (NotRegisteredException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	  }
	cs.sendMessage(ChatColor.YELLOW + "No rebellion with that name!");
	return true;
  }
  
  //Author: Noxer
  private boolean leaveRebellion(CommandSender cs, String[] strings, boolean admin){
	  
	Resident res = null;
	
	try {
		res = TownyUniverse.getDataSource().getResident(cs.getName());
	} catch (NotRegisteredException e2) {
		// TODO Auto-generated catch block
		e2.printStackTrace();
	}
	
	try {
		if ((!admin) && (!res.getTown().isMayor(res)))
		  {
		      cs.sendMessage(ChatColor.RED + "You are not powerful enough in your town to do that!");
		      return true;
		  }
	} catch (NotRegisteredException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	}
	  
	  for(Rebellion r : Rebellion.getAllRebellions())
		try {
			if(r.isRebelLeader(res.getTown())){
				Rebellion.getAllRebellions().remove(r);
				cs.sendMessage(ChatColor.RED + "You disbanded your rebellion in your nation!");
				return true;
			}
			else if(r.isRebelTown(res.getTown())){
				r.removeRebell(res.getTown());
				cs.sendMessage(ChatColor.RED + "You left the rebellion in your nation!");
				return true;
			}
		} catch (NotRegisteredException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	  
	  cs.sendMessage(ChatColor.RED + "You are not in a rebellion!");
	  return true;
  }
  
  //Author: Noxer
  private boolean executeRebellion(CommandSender cs, String[] strings, boolean admin){

	  Resident res = null;
	  
	  try {
			res = TownyUniverse.getDataSource().getResident(cs.getName());
		} catch (NotRegisteredException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
	  
	  try {
		if ((!admin) && (!res.getTown().isMayor(res)))
		  {
		      cs.sendMessage(ChatColor.RED + "You are not powerful enough in your town to do that!");
		      return true;
		  }
	} catch (NotRegisteredException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	  
	  try {
		if (WarManager.getWarForNation(res.getTown().getNation()) != null)
		  {
		      cs.sendMessage(ChatColor.RED + "You can't rebel while your nation is at war!");
		      return true;
		  }
	} catch (NotRegisteredException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	  
	  for(Rebellion r : Rebellion.getAllRebellions())
		try {
			if(res.getTown().getNation() == r.getMotherNation() && r.isRebelLeader(res.getTown())){
				  r.Execute(cs);
				  return true;
			}
		} catch (NotRegisteredException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	  
	  cs.sendMessage(ChatColor.RED + "You are not in a rebellion!");
      return true;
  }
  
  private boolean declareEnd(CommandSender cs, String[] strings, boolean admin)
  {
    if ((admin) && (strings.length <= 2))
    {
      cs.sendMessage(ChatColor.RED + "You need to specify two nations!");
      return true;
    }
    String sonat = "";
    if (admin) {
      sonat = strings[1];
    }
    Resident res = null;
    Nation nat;
    try
    {
      if (admin)
      {
        nat = TownyUniverse.getDataSource().getNation(strings[2]);
      }
      else
      {
        res = TownyUniverse.getDataSource().getResident(cs.getName());
        nat = res.getTown().getNation();
      }
    }
    catch (Exception ex)
    {
      cs.sendMessage(ChatColor.RED + "You are not in a town, or your town isn't part of a nation!");
      return true;
    }
    if (!admin && !res.isKing() && !nat.hasAssistant(res))
    {
      cs.sendMessage(ChatColor.RED + "You are not powerful enough in your nation to do that!");
      return true;
    }
    if (!admin)
    {
      War w = WarManager.getWarForNation(nat);
      if (w == null)
      {
        cs.sendMessage(ChatColor.RED + nat.getName() + " is not at war!");
        return true;
      }
      try {
		sonat = w.getEnemy(nat).getName();
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    }
    Nation onat;
    try
    {
      onat = TownyUniverse.getDataSource().getNation(sonat);
    }
    catch (NotRegisteredException ex)
    {
      cs.sendMessage(ChatColor.RED + "That nation doesn't exist!");
      return true;
    }
    if (WarManager.requestPeace(nat, onat, admin)) {
      return true;
    }
    if (admin) {
      cs.sendMessage(ChatColor.GREEN + "Forced peace!");
    } else {
      cs.sendMessage(ChatColor.GREEN + "Requested peace!");
    }
    return true;
  }
  
  private boolean declareWar(CommandSender cs, String[] strings, boolean admin)
  {
    if ((strings.length == 2) && (admin))
    {
      cs.sendMessage(ChatColor.RED + "You need to specify two nations!");
      return true;
    }
    if (strings.length == 1)
    {
      cs.sendMessage(ChatColor.RED + "You need to specify a nation!");
      return true;
    }
    String sonat = strings[1];
    Resident res;
    Nation nat;
    try
    {
      if (admin)
      {
        res = null;
        nat = TownyUniverse.getDataSource().getNation(strings[2]);
      }
      else
      {
        res = TownyUniverse.getDataSource().getResident(cs.getName());
        nat = res.getTown().getNation();
      }
    }
    catch (Exception ex)
    {
      cs.sendMessage(ChatColor.RED + "You are not in a town, or your town isn't part of a nation!");
      return true;
    }
    if (WarManager.getWarForNation(nat) != null)
    {
      cs.sendMessage(ChatColor.RED + "Your nation is already at war!");
      return true;
    }
    if ((!admin) && (!nat.isKing(res)) && (!nat.hasAssistant(res)))
    {
      cs.sendMessage(ChatColor.RED + "You are not powerful enough in your nation to do that!");
      return true;
    }
    Nation onat;
    try
    {
      onat = TownyUniverse.getDataSource().getNation(sonat);
    }
    catch (NotRegisteredException ex)
    {
      cs.sendMessage(ChatColor.RED + "That nation doesn't exist!");
      return true;
    }
    if (WarManager.neutral.containsKey(onat))
    {
      cs.sendMessage(ChatColor.RED + "That nation is neutral and cannot enter in a war!");
      return true;
    }
    if (WarManager.neutral.containsKey(nat))
    {
      cs.sendMessage(ChatColor.RED + "You are in a neutral nation and cannot declare war on others!");
      return true;
    }
    if (WarManager.getWarForNation(onat) != null)
    {
      cs.sendMessage(ChatColor.RED + "That nation is already at war!");
      return true;
    }
    if (nat.getName().equals(onat.getName()))
    {
      cs.sendMessage(ChatColor.RED + "A nation can't be at war with itself!");
      return true;
    }
    WarManager.createWar(nat, onat, cs);
    try
    {
      nat.collect(TownyWars.declareCost);
    }
    catch (EconomyException ex)
    {
      Logger.getLogger(WarExecutor.class.getName()).log(Level.SEVERE, null, ex);
    }
    cs.sendMessage(ChatColor.GREEN + "Declared war on " + onat.getName() + "!");
    return true;
  }
}