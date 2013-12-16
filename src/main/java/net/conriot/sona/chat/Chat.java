package net.conriot.sona.chat;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Chat extends JavaPlugin {
	private static ChatManager chat;
	private static MessageManager message;
	
	@Override
	public void onEnable() {
		// Register the chat manager which will handle all chat
		chat = new ChatManager(this);
		// Register the message manager which will handle all custom messages
		message = new MessageManager();
		// Register all basic economy commands for this manager
		Commands c = new Commands(chat, message);
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
	
	public static String style(ColorScheme scheme, String input) {
		return message.style(scheme, input);
	}
	
	public static void send(ColorScheme scheme, String input, Player player) {
		message.send(scheme, input, player);
	}
	
	public static void sendAll(ColorScheme scheme, String input) {
		message.sendAll(scheme, input);
	}
	
	public static void sendLocal(ColorScheme scheme, String input, Location loc, double range) {
		message.sendLocal(scheme, input, loc, range);
	}
	
	public static boolean mute(String target, long duration) {
		return mute(target, duration);
	}
	
	public static boolean unmute(String target) {
		return chat.unmute(target);
	}
	
	public static void sendChannel(Player sender, String message, String channel) {
		chat.sendChannel(sender, message, channel);
	}
	
	public static void sendLocal(Player sender, String message, Location loc, double range) {
		chat.sendLocal(sender, message, loc, range);
	}
	
	public static void setChannel(Player target, String channel) {
		chat.setChannel(target, channel);
	}
	
	public static void addChannel(Player target, String channel) {
		chat.addChannel(target, channel);
	}
	
	public static void removeChannel(Player target, String channel) {
		chat.removeChannel(target, channel);
	}
	
	public static void clearChannel(Player target, String channel) {
		chat.clearChannel(target, channel);
	}
}
