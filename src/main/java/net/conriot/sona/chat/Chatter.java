package net.conriot.sona.chat;

import java.util.HashSet;

import lombok.Getter;
import lombok.Setter;

import net.conriot.sona.mysql.IOCallback;
import net.conriot.sona.mysql.MySQL;
import net.conriot.sona.mysql.Query;
import net.conriot.sona.mysql.Result;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

class Chatter implements IOCallback {
	private Chat plugin;
	private Player player;
	@Getter private boolean loaded;
	@Getter private boolean muted;
	private BukkitTask unmute;
	@Getter @Setter private Player lastPM;
	private long lastSent;
	private HashSet<String> channels;
	private HashSet<String> blocked;
	
	public Chatter(Chat plugin, Player player) {
		this.plugin = plugin;
		this.player = player;
		this.loaded = false;
		this.muted = false;
		this.unmute = null;
		this.lastPM = null;
		this.lastSent = System.currentTimeMillis();
		this.channels = new HashSet<String>();
		this.blocked = new HashSet<String>();
		
		Bukkit.getLogger().info(player.getName() + " was added as a Chatter!"); // DEBUG
		
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
		q.setQuery("INSERT INTO chat_mutes (chat_mutes.name, chat_mutes.finish) VALUES (?, ?)");
		q.add(this.player.getName());
		q.add(System.currentTimeMillis() + duration);
		// Execute query asynchronously
		MySQL.execute(this, "mute", q);
		
		// Notify the player they have been muted
		this.plugin.send(ColorScheme.RED_DARKRED, "{1}You have been muted for {2}" + (duration / 60000) + "{1} minutes!", this.player);
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
		q.setQuery("DELETE FROM chat_mutes WHERE chat_mutes.name=?");
		q.add(this.player.getName());
		// Execute query asynchronously
		MySQL.execute(this, "unmute", q);
		
		// Notify the player they have been muted
		this.plugin.send(ColorScheme.GREEN_DARKGREEN, "{1}You have been unmuted!", this.player);
	}
	
	public void block(String name) {
		// Convert to lowercase
		String lower = name.toLowerCase();
		
		// Check if the player is already blocked or not
		if(this.blocked.contains(lower)) {
			// Notify that they are already blocked
			this.plugin.send(ColorScheme.RED_DARKRED, "{1}You have already blocked '{2}" + name + "{1}'!", this.player);
		} else {
			// Block the player name
			this.blocked.add(lower);
			
			// Persist the data to the database
			Query q = MySQL.makeQuery();
			q.setQuery("INSERT INTO chat_blocks (chat_blocks.name, chat_blocks.blocked) VALUES (?, ?)");
			q.add(this.player.getName());
			q.add(lower);
			// Execute query asynchronously
			MySQL.execute(this, "block", q);
			
			// Notify that the player is now blocked
			this.plugin.send(ColorScheme.GRAY_DARKDRAY, "{1}You have blocked '{2}" + name + "{1}'!", this.player);
		}
	}
	
	public void unblock(String name) {
		// Convert to lowercase
		String lower = name.toLowerCase();
		
		// Check if the player is already blocked or not
		if(!this.blocked.contains(lower)) {
			// Notify that they are not blocked
			this.plugin.send(ColorScheme.RED_DARKRED, "{1}You have not yet blocked '{2}" + name + "{1}'!", this.player);
		} else {
			// Unblock the player name
			this.blocked.remove(lower);
			
			// Persist the data to the database
			Query q = MySQL.makeQuery();
			q.setQuery("DELETE FROM chat_blocks WHERE chat_blocks.name=? AND chat_blocks.blocked=?");
			q.add(this.player.getName());
			q.add(lower);
			// Execute query asynchronously
			MySQL.execute(this, "block", q);
			
			// Notify that the player is now unblocked
			this.plugin.send(ColorScheme.GRAY_DARKDRAY, "{1}You have unblocked '{2}" + name + "{1}'!", this.player);
		}
	}
	
	public String say(String message) {
		// check if the player's chat data is loaded yet
		if(!this.loaded) {
			// Notify the player they cannot speak yet
			this.plugin.send(ColorScheme.RED_DARKRED, "{1}You cannot speak at this time, please wait!", this.player);
			return null;
		}
		
		// Check if the player is muted
		if(this.muted) {
			// Notify the player they are currently muted
			this.plugin.send(ColorScheme.RED_DARKRED, "{1}You have been {2}muted{1} and cannot speak right now!", this.player);
			return null;
		}
		
		// Check if player sent a message recently
		if(System.currentTimeMillis() - this.lastSent < 2000 && !this.player.isOp()) {
			// Notify the player they cannot chat that often
			this.plugin.send(ColorScheme.RED_DARKRED, "{1}You cannot send messages that quickly!", this.player);
			return null;
		}
		
		// Create a prefix accordingly
		String prefix = buildPrefix();
		
		// Update the time of the last message sent
		this.lastSent = System.currentTimeMillis();
		
		// Return the created prefix
		return prefix;
	}
	
	public void message(Player sender, String message) {
		// Create a prefix for the copy of this message given to the sender
		String senderprefix = ChatColor.DARK_GRAY + " [ " + ChatColor.GRAY + "You" + ChatColor.DARK_PURPLE + ChatColor.BOLD + " -> " + ChatColor.WHITE + this.player.getName() + ChatColor.DARK_GRAY + " ] ";
		
		// Send the copy of the private message to the sender
		sender.sendMessage(senderprefix + ChatColor.LIGHT_PURPLE + ChatColor.ITALIC + message);
		
		// Check if the sender is blocked by this player
		if(this.blocked.contains(sender.getName()))
			return;
		
		// Update the last person interacted with
		this.lastPM = sender;
		
		// Create a prefix for this message
		String prefix = ChatColor.DARK_GRAY + " [ " + ChatColor.WHITE + sender.getName() + ChatColor.DARK_PURPLE + ChatColor.BOLD + " -> " + ChatColor.GRAY + "You" + ChatColor.DARK_GRAY + " ] ";
		
		// Send the private message to the player
		this.player.sendMessage(prefix + ChatColor.LIGHT_PURPLE + ChatColor.ITALIC + message);
	}
	
	public void send(Player sender, String prefix, String message) {
		// Check if the sender is blocked by this player
		if(!sender.isOp() && this.blocked.contains(sender.getName()))
			return;
				
		// Send a message with the defined prefix
		this.player.sendMessage(prefix + message);
	}
	
	public void sendChannel(Player sender, String prefix, String message, String channel) {
		// Check if the sender is blocked by this player
		if(!sender.isOp() && this.blocked.contains(sender.getName()))
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
		if(!sender.isOp() && this.blocked.contains(sender.getName()))
			return;
		
		// Check if the play is outside of the receiving range
		if(loc.getWorld() != this.player.getLocation().getWorld() || loc.distanceSquared(this.player.getLocation()) > range * range)
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
	
	private String buildPrefix() {
		// Build a prefix based on rank
		String prefix = "";
		
		// Check if the play is op
		if(this.player.isOp()) {
			prefix = ChatColor.GRAY + "[" + ChatColor.DARK_RED + ChatColor.BOLD + "Owner" + ChatColor.GRAY + "] ";
			prefix += ChatColor.DARK_RED + this.player.getName() + ChatColor.DARK_GRAY + " : " + ChatColor.RED;
		} else {
			prefix = ChatColor.GRAY + "[" + ChatColor.BLUE + "Inmate" + ChatColor.GRAY + "] ";
			prefix += ChatColor.YELLOW + this.player.getName() + ChatColor.DARK_GRAY + " : " + ChatColor.GRAY;
		}
		
		// Return the prefix
		return prefix;
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
