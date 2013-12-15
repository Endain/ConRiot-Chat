package net.conriot.sona.chat;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Chat extends JavaPlugin {
	private ChatManager chat;
	private MessageManager message;
	
	@Override
	public void onEnable() {
		// Register the chat manager which will handle all chat
		this.chat = new ChatManager(this);
		// Register the message manager which will handle all custom messages
		this.message = new MessageManager();
		// Register all basic economy commands for this manager
		Commands c = new Commands(this.chat, this.message);
		getCommand("msg").setExecutor(c);
		getCommand("r").setExecutor(c);
		getCommand("mute").setExecutor(c);
		getCommand("unmute").setExecutor(c);
		getCommand("block").setExecutor(c);
		getCommand("unblock").setExecutor(c);
		getCommand("silence").setExecutor(c);
		getCommand("unsilence").setExecutor(c);
		getCommand("broadcast").setExecutor(c);
	}
	
	@Override
	public void onDisable() {
		// Nothing to do here
	}
	
	public String style(ColorScheme scheme, String input) {
		return this.message.style(scheme, input);
	}
	
	public void send(ColorScheme scheme, String input, Player player) {
		this.message.send(scheme, input, player);
	}
	
	public void sendAll(ColorScheme scheme, String input) {
		this.message.sendAll(scheme, input);
	}
	
	public void sendLocal(ColorScheme scheme, String input, Location loc, double range) {
		this.message.sendLocal(scheme, input, loc, range);
	}
	
	public boolean mute(String target, long duration) {
		return this.mute(target, duration);
	}
	
	public boolean unmute(String target) {
		return this.chat.unmute(target);
	}
	
	public void sendChannel(Player sender, String message, String channel) {
		this.chat.sendChannel(sender, message, channel);
	}
	
	public void sendLocal(Player sender, String message, Location loc, double range) {
		this.chat.sendLocal(sender, message, loc, range);
	}
	
	public void setChannel(Player target, String channel) {
		this.chat.setChannel(target, channel);
	}
	
	public void addChannel(Player target, String channel) {
		this.chat.addChannel(target, channel);
	}
	
	public void removeChannel(Player target, String channel) {
		this.chat.removeChannel(target, channel);
	}
	
	public void clearChannel(Player target, String channel) {
		this.chat.clearChannel(target, channel);
	}
}
