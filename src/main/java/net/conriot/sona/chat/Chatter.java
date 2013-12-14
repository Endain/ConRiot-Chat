package net.conriot.sona.chat;

import java.util.HashSet;

import lombok.Getter;

import net.conriot.sona.mysql.IOCallback;
import net.conriot.sona.mysql.MySQL;
import net.conriot.sona.mysql.Query;
import net.conriot.sona.mysql.Result;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

class Chatter implements IOCallback {
	private Plugin plugin;
	private Player player;
	@Getter private boolean loaded;
	@Getter private boolean muted;
	private BukkitTask unmute;
	@Getter private Player lastPM;
	private long lastSent;
	private HashSet<String> channels;
	private HashSet<String> blocked;
	
	public Chatter(Plugin plugin, Player player) {
		this.plugin = plugin;
		this.player = player;
		this.loaded = false;
		this.muted = false;
		this.unmute = null;
		this.lastPM = null;
		this.lastSent = System.currentTimeMillis();
		this.channels = new HashSet<String>();
		this.blocked = new HashSet<String>();
		
		// Load persisted player chat data
		load();
	}
	
	public void mute(long duration) {
		// Flag the player as muted
		this.muted = true;
		
		// Cancel any existing bukkit task if needed
		if(this.unmute != null)
			Bukkit.getScheduler().cancelTask(this.unmute.getTaskId());
		this.unmute = null;
		
		// Create a new unmute bukkit task
		this.unmute = Bukkit.getScheduler().runTaskLater(this.plugin, new Runnable() {
			@Override
			public void run() {
				unmute();
			}
		}, (duration / 50) + 1);
		
		// Persist the mute to the database
		Query q = MySQL.makeQuery();
		q.setQuery("INSERT ITO chat_mutes VALUES (name=?, finish=?)");
		q.add(this.player.getName());
		q.add(System.currentTimeMillis() + duration);
		// Execute query asynchronously
		MySQL.execute(this, "mute", q);
	}
	
	public void unmute() {
		// Cancel and clear the unmute bukkit task if there is one
		if(this.unmute != null)
			Bukkit.getScheduler().cancelTask(this.unmute.getTaskId());
		this.unmute = null;
		
		// Set the player as unmuted
		this.muted = false;
		
		// Update the database accordingly
		Query q = MySQL.makeQuery();
		q.setQuery("DELETE FROM chat_mutes WHERE name=?");
		q.add(this.player.getName());
		// Execute query asynchronously
		MySQL.execute(this, "unmute", q);
	}
	
	public String say(String message) {
		// check if the player's chat data is loaded yet
		if(!this.loaded) {
			return null;
		}
		
		// Check if the player is muted
		if(this.muted) {
			return null;
		}
		
		// Check if player sent a message recently
		if(System.currentTimeMillis() - this.lastSent < 2000) {
			return null;
		}
		
		// Create a prefix accordingly
		String prefix = "";
		// TODO
		
		// Update the time of the last message sent
		this.lastSent = System.currentTimeMillis();
		
		// Return the created prefix
		return prefix;
	}
	
	public void message(Player sender, String message) {
		// Check if the sender is blocked by this player
		if(this.blocked.contains(sender.getName()))
			return;
		
		// Update the last person interacted with
		this.lastPM = sender;
		
		// Create a prefix for this message
		String prefix = ChatColor.DARK_GRAY + " [ " + ChatColor.GRAY + sender.getName() + ChatColor.DARK_PURPLE + ChatColor.BOLD + " -> " + ChatColor.WHITE + "You" + ChatColor.DARK_GRAY + " ] ";
		
		// Send the private message to the player
		this.player.sendMessage(prefix + ChatColor.LIGHT_PURPLE + ChatColor.ITALIC + message);
	}
	
	public void send(Player sender, String prefix, String message) {
		// Check if the sender is blocked by this player
		if(this.blocked.contains(sender.getName()))
			return;
				
		// Send a message with the defined prefix
		this.player.sendMessage(prefix + message);
	}
	
	public void sendChannel(Player sender, String prefix, String message, String channel) {
		// Check if the sender is blocked by this player
		if(this.blocked.contains(sender.getName()))
			return;
				
		// Check if the user is not in the channel being broadcast to
		if(!inChannel(channel))
			return;
		
		// Send a message styled for this channel
		String channelPrefix = ChatColor.DARK_GRAY + " [" + ChatColor.GRAY + ChatColor.ITALIC + channel + ChatColor.DARK_GRAY + "] " + ChatColor.RESET;
		this.player.sendMessage(channelPrefix + prefix + ChatColor.GRAY + message);
	}
	
	public void sendLocal(Player sender, String prefix, String message, Location loc, double range) {
		// Check if the sender is blocked by this player
		if(this.blocked.contains(sender.getName()))
			return;
		
		// Check if the play is outside of the receiving range
		if(loc.getWorld() != this.player.getLocation().getWorld() || loc.distanceSquared(this.player.getLocation()) > range)
			return;
		
		// Send the message as normal if in range
		send(sender, prefix, message);
	}
	
	public boolean inChannel(String channel) {
		// Return is the user is in the given channel
		return channels.contains(channel);
	}
	
	public void setChannel(String channel) {
		// Place the user into the specified channel and remove all others
		channels.clear();
		channels.add(channel);
	}
	
	public void addChannel(String channel) {
		// Place the user into the specified channel, keep all others
		channels.add(channel);
	}
	
	public void removeChannel(String channel) {
		// Remove the user from the specified channel
		channels.remove(channel);
	}
	
	public void clearChannel(String channel) {
		// Remove the user from all channels
		channels.clear();
	}
	
	private void load() {
		// Create a query to get any existing chat bans
		Query q = MySQL.makeQuery();
		q.setQuery("SELECT * FROM chat_mutes WHERE name=?");
		q.add(this.player.getName());
		// Execute query asynchronously
		MySQL.execute(this, "load", q);
	}

	@Override
	public void complete(boolean success, Object tag, Result result) {
		// Handle all database returns
		if(tag instanceof String && ((String)tag).equalsIgnoreCase("load")) {
			// Set up the player's mute if there is one
			if(result.next()) {
				long duration = (long)result.get(1) - System.currentTimeMillis();
				if(duration > 0)
					mute(duration);
				else
					unmute();
			}
			
			// Proceed with step 2 of loading the players chat data
			Query q = MySQL.makeQuery();
			q.setQuery("SELECT * FROM chat_blocks WHERE name=?");
			q.add(this.player.getName());
			// Execute query asynchronously
			MySQL.execute(this, "load2", q);
		} else if(tag instanceof String && ((String)tag).equalsIgnoreCase("load2")) {
			// If there are any blocked players, add them
			while(result.next())
				this.blocked.add((String)result.get(2));
			
			// Flag this user as loaded and allow all chat
			this.loaded = true;
		}
	}
}
