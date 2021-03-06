package de.bananaco.permissions;

import java.io.File;
import java.util.List;
import java.util.Set;

import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import de.bananaco.bpermissions.api.Group;
import de.bananaco.bpermissions.api.User;
import de.bananaco.bpermissions.api.WorldManager;
import de.bananaco.bpermissions.api.util.Permission;

public class ImportManager {

	private WorldManager wm = WorldManager.getInstance();
	private final JavaPlugin plugin;
	
	protected ImportManager(JavaPlugin plugin) {
		this.plugin = plugin;
	}
	
	public void importYML() throws Exception {
		for (World world : plugin.getServer().getWorlds()) {
			de.bananaco.bpermissions.api.World wd = wm.getWorld(world.getName());
			File perms = new File("plugins/bPermissions/worlds/"
					+ world.getName() + ".yml");
			if(perms.exists()) {
			System.out.println("Importing world: "+world.getName());
			YamlConfiguration pConfig = new YamlConfiguration();//new Configuration(perms);
			pConfig.load(perms);
			// Here we grab the different bits and bobs
			ConfigurationSection users = pConfig.getConfigurationSection("players");
			ConfigurationSection groups = pConfig.getConfigurationSection("groups");
			
			// Load users
			if(users != null && users.getKeys(false) != null && users.getKeys(false).size() > 0) {
				Set<String> u = users.getKeys(false);
				for(String usr : u) {
					System.out.println("Importing user: "+usr);
					List<String> g = users.getStringList(usr);
					// Clear the groups in their list firstly
					wd.getUser(usr).getGroupsAsString().clear();
					// Another NPE Fix
					if(g != null && g.size() > 0)
					for(String group : g)
						wd.getUser(usr).addGroup(group);
				}
			}
			// Load groups
			if(groups != null && groups.getKeys(false) != null && groups.getKeys(false).size() > 0) {
				Set<String> g = groups.getKeys(false);
				for(String grp : g) {
					System.out.println("Importing group: "+grp);
					List<String> p = groups.getStringList(grp);
					if(p != null && p.size() > 0)
						for(String perm : p)
							wd.getGroup(grp).getPermissions().add(Permission.loadFromString(perm));
				}
			}
			}
			// Forgot to save after importing!
			wd.save();
		}
		wm.cleanup();
	}
	
	public void importPermissions3() throws Exception {
		for (World world : plugin.getServer().getWorlds()) {
			de.bananaco.bpermissions.api.World wd = wm.getWorld(world.getName());
			
			File users = new File("plugins/Permissions/" + world.getName()
					+ "/users.yml");
			File groups = new File("plugins/Permissions/" + world.getName()
					+ "/groups.yml");
			
			if(users.exists() && groups.exists()) {
			System.out.println("Importing world: "+world.getName());

			YamlConfiguration uConfig = new YamlConfiguration();
			YamlConfiguration gConfig = new YamlConfiguration();
			try {
			uConfig.load(users);
			gConfig.load(groups);
			} catch (Exception e) {
				e.printStackTrace();
			}
			ConfigurationSection usConfig = uConfig.getConfigurationSection("users");
			ConfigurationSection grConfig = gConfig.getConfigurationSection("groups");
			
			Set<String> usersList = null;
			if(usConfig != null)
				usersList = usConfig.getKeys(false);
			Set<String> groupsList = null;
			if(grConfig != null)
				groupsList = grConfig.getKeys(false);
			
			if (usersList != null)
				for (String player : usersList) {
					System.out.println("Importing user: "+player);
					User user = wd.getUser(player);
					try {
					List<String> p = uConfig.getStringList("users."+player+".permissions");
					List<String> i = uConfig.getStringList("users."+player+".groups");
					
					if(p != null)
						user.getPermissions().addAll(Permission.loadFromString(p));
					if(i != null) {
						user.getGroupsAsString().clear();
						user.getGroupsAsString().addAll(i);
					}
					} catch (Exception e) {
						System.err.println("Error importing user: "+player);
					}
				}
			
			if (groupsList != null)
				for (String group : groupsList) {
					System.out.println("Importing group: "+group);
					Group gr = wd.getGroup(group);
					try {
					List<String> p = gConfig.getStringList("groups."+group+".permissions");
					List<String> i = gConfig.getStringList("groups."+group+".inheritance");
					
					if(p != null)
						gr.getPermissions().addAll(Permission.loadFromString(p));
					if(i != null)
						gr.getGroupsAsString().addAll(i);
					} catch (Exception e) {
						System.err.println("Error importing group: "+group);
					}
				}
			wd.save();
			}
		}
		wm.cleanup();
	}
	
}
