package net.conriot.sona.chat;

import lombok.Getter;

import org.bukkit.plugin.Plugin;

class ChatManager {
	private Plugin plugin;
	
	public ChatManager(Plugin plugin) {
		this.plugin = plugin;
	}
}
