package net.conriot.sona.chat;

import java.util.HashMap;
import java.util.Iterator;

import net.conriot.sona.mysql.IOCallback;
import net.conriot.sona.mysql.MySQL;
import net.conriot.sona.mysql.Query;
import net.conriot.sona.mysql.Result;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

class ChatManager implements Listener, IOCallback {
	private Chat plugin;
	private HashMap<String, Chatter> chatters;
	private boolean silenced;
	
	public ChatManager(Chat plugin) {
		this.plugin = plugin;
		this.chatters = new HashMap<String, Chatter>();
		this.silenced = false;
	}
	
	public boolean mute(String target, long duration) {
		// Perform the channel set if the chatter is not null
		Chatter chatter = this.chatters.get(target);
		if(chatter != null)
			// Mute the player for the given duration in milliseconds
			chatter.mute(duration);
		else
			return false;
		return true;
	}
	
	public boolean unmute(String target) {
		// Perform the channel set if the chatter is not null
		Chatter chatter = this.chatters.get(target);
		if(chatter != null)
			// Unmute the given player is they are muted
			chatter.unmute();
		else
			return false;
		return true;
	}
	
	public void message(Player sender, String message, String target) {
		// Perform the channel set if the chatter is not null
		Chatter to = this.chatters.get(target);
		if(to != null) {
			// Log this chat message to the database
			log(sender.getName(), target, message);
			
			// Send the private message
			to.message(sender, message);
		} else {
			// Notify that the target of the message is offline
			this.plugin.send(ColorScheme.RED_DARKRED, "{1}The player '{2}" + target + "{1}' is not online!", sender);
		}
	}
	
	public void reply(Player sender, String message) {
		// Check if the player has somebody to reply to
		Chatter from = this.chatters.get(sender.getName());
		Player player = from.getLastPM();
		if(player != null) {
			// Perform a check to see if the target chatter is null
			Chatter to = this.chatters.get(player.getName());
			if(to != null) {
				// Log this chat message to the database
				log(sender.getName(), player.getName(), message);
				
				// Send the private message
				to.message(sender, message);
			} else {
				// Notify that the target of the message is offline
				this.plugin.send(ColorScheme.RED_DARKRED, "{1}The player '{2}" + player.getName() + "{1}' is not online!", sender);
			}
		} else {
			// Notify that there is nobody to reply to
			this.plugin.send(ColorScheme.RED_DARKRED, "{1}There is nobody to reply to!", sender);
		}
	}
	
	public void sendChannel(Player sender, String message, String channel) {
		// Get the chatter object of the player sending the message
		Chatter chatter = this.chatters.get(sender.getName());
		if(chatter != null) {
			// Verify that the chat is not currently silenced
			if(!sender.isOp() && this.silenced) {
				this.plugin.send(ColorScheme.RED_DARKRED, "{1}An {2}admin{1} has put chat into {2}silent mode{1}! You cannot talk right now!", sender);
				return;
			}
			
			// Get a styled prefix string based on the sender
			String prefix = chatter.say(message);
			
			// Only continue if a valid prefix was returned
			if(prefix != null) {
				// Log this chat message to the database
				log(sender.getName(), "*" + channel, message);
				
				// Try to send to all loaded players
				Iterator<Chatter> iter = this.chatters.values().iterator();
				while(iter.hasNext()) {
					Chatter to = iter.next();
					if(to.isLoaded())
						to.sendChannel(sender, prefix, message, channel);
				}
			}
		}
	}
	
	public void sendLocal(Player sender, String message, Location loc, double range) {
		// Get the chatter object of the player sending the message
		Chatter chatter = this.chatters.get(sender.getName());
		if(chatter != null) {
			// Verify that the chat is not currently silenced
			if(!sender.isOp() && this.silenced) {
				this.plugin.send(ColorScheme.RED_DARKRED, "{1}An {2}admin{1} has put chat into {2}silent mode{1}! You cannot talk right now!", sender);
				return;
			}
			
			// Get a styled prefix string based on the sender
			String prefix = chatter.say(message);
			
			// Only continue if a valid prefix was returned
			if(prefix != null) {
				// Log this chat message to the database
				log(sender.getName(), "**", message);
				
				// Try to send to all loaded players
				Iterator<Chatter> iter = this.chatters.values().iterator();
				while(iter.hasNext()) {
					Chatter to = iter.next();
					if(to.isLoaded())
						to.sendLocal(sender, prefix, message, loc, range);
				}
			}
		}
	}
	
	public void setChannel(Player target, String channel) {
		// Perform the channel set if the chatter is not null
		Chatter chatter = this.chatters.get(target.getName());
		if(chatter != null)
			chatter.setChannel(channel);
	}
	
	public void addChannel(Player target, String channel) {
		// Perform the channel set if the chatter is not null
		Chatter chatter = this.chatters.get(target.getName());
		if(chatter != null)
			chatter.addChannel(channel);
	}
	
	public void removeChannel(Player target, String channel) {
		// Perform the channel set if the chatter is not null
		Chatter chatter = this.chatters.get(target.getName());
		if(chatter != null)
			chatter.removeChannel(channel);
	}
	
	public void clearChannel(Player target, String channel) {
		// Perform the channel set if the chatter is not null
		Chatter chatter = this.chatters.get(target.getName());
		if(chatter != null)
			chatter.clearChannel(channel);
	}
	
	private void handleChat(Player player, String message) {
		// Get the chatter object of the player sending the message
		Chatter chatter = this.chatters.get(player.getName());
		if(chatter != null) {
			// Verify that the chat is not currently silenced
			if(!player.isOp() && this.silenced) {
				this.plugin.send(ColorScheme.RED_DARKRED, "{1}An {2}admin{1} has put chat into {2}silent mode{1}! You cannot talk right now!", player);
				return;
			}
			
			// Get a styled prefix string based on the sender
			String prefix = chatter.say(message);
			
			// Only continue if a valid prefix was returned
			if(prefix != null) {
				// Log this chat message to the database
				log(player.getName(), "**", message);
				
				// Try to send to all loaded players
				Iterator<Chatter> iter = this.chatters.values().iterator();
				while(iter.hasNext()) {
					Chatter to = iter.next();
					if(to.isLoaded())
						to.send(player, prefix, message);
				}
			}
		}
	}
	
	private void log(String from, String to, String message) {
		// Log the given message to the chat log
		Query q = MySQL.makeQuery();
		q.setQuery("INSERT INTO chat_log VALUES (time=?, to=?, from=? message=?)");
		q.add(System.currentTimeMillis());
		q.add(from);
		q.add(to);
		q.add(message);
		// Execute query asynchronously
		MySQL.execute(this, "log", q);
	}
	
	@EventHandler
	public void onChat(AsyncPlayerChatEvent event) {
		// Verify that a valid chat event occured
		if(!event.isCancelled()) {
			// Cancel the event and handle it ourselves
			event.setCancelled(true);
			handleChat(event.getPlayer(), event.getMessage());
		}
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		// Add the joining player to the list of chatters
		this.chatters.put(event.getPlayer().getName(), new Chatter(this.plugin, event.getPlayer()));
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		// Remove the leaving player from the list of chatters
		this.chatters.remove(event.getPlayer().getName());
	}

	@Override
	public void complete(boolean success, Object tag, Result result) {
		// No need to do anything, only logging is performed
	}
}
