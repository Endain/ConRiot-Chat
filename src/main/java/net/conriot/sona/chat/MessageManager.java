package net.conriot.sona.chat;

import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

class MessageManager {
	public String style(ColorScheme scheme, String input) {
		// Replace all markers with primary/secondary/ tertiary colors
		input = input.replaceAll("\\{1\\}", scheme.primary() + "");
		input = input.replaceAll("\\{2\\}", scheme.secondary() + "");
		input = input.replaceAll("\\{3\\}", scheme.tertiary() + "");
		
		// Return the colored string
		return input;
	}
	
	public void send(ColorScheme scheme, String input, Player player) {
		// Color the input string
		String colored = style(scheme, input);
		
		// Send the message to the given player
		player.sendMessage(colored);
	}
	
	public void sendAll(ColorScheme scheme, String input) {
		// Color the input string
		String colored = style(scheme, input);
		
		// Send the message to every player on the server
		for(Player player : Bukkit.getOnlinePlayers())
			player.sendMessage(colored);
	}
	
	public void sendLocal(ColorScheme scheme, String input, Location loc, double range) {
		// Color the input string
		String colored = style(scheme, input);
		
		// Send the message to all players in the given range
		Iterator<Player> iter = loc.getWorld().getPlayers().iterator();
		while(iter.hasNext()) {
			Player player = iter.next();
			if(loc.distanceSquared(player.getLocation()) <= range * range)
				player.sendMessage(colored);
		}
	}
}
