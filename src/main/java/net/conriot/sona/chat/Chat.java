package net.conriot.sona.chat;

import org.bukkit.plugin.java.JavaPlugin;

public class Chat extends JavaPlugin {
	private ChatManager chat;
	private MessageManager message;
	
	@Override
	public void onEnable() {
		// Register the chat manager which will handle all chat
		this.chat = new ChatManager();
		// Register the message manager which will handle all custom messages
		this.message = new MessageManager();
	}
	
	@Override
	public void onDisable() {
		// Nothing to do here
	}
}
