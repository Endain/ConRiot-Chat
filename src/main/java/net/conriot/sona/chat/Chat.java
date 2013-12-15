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
}
