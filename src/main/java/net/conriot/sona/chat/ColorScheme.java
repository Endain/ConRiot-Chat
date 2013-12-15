package net.conriot.sona.chat;

import org.bukkit.ChatColor;

public enum ColorScheme {
	RED_DARKRED(ChatColor.RED, ChatColor.DARK_RED, null),
	GREEN_DARKGREEN(ChatColor.GREEN, ChatColor.DARK_GREEN, null),
	BLUE_DARBLUE(ChatColor.BLUE, ChatColor.DARK_BLUE, null),
	AQUA_DARKAQUA(ChatColor.AQUA, ChatColor.DARK_AQUA, null),
	PURLE_DARKPURPLE(ChatColor.LIGHT_PURPLE, ChatColor.DARK_PURPLE, null),
	YELLOW_GOLD(ChatColor.YELLOW, ChatColor.GOLD, null),
	GRAY_DARKDRAY(ChatColor.GRAY, ChatColor.DARK_GRAY, null);

	private ChatColor primary;
	private ChatColor secondary;
	private ChatColor tertiary;

	private ColorScheme(ChatColor primary, ChatColor secondary, ChatColor tertiary) {
		this.primary = primary;
    	this.secondary = secondary;
    	this.tertiary = tertiary;
    }
	
	public ChatColor primary() {
		if(this.primary == null)
			return ChatColor.WHITE;
		return this.primary;
	}
	
	public ChatColor secondary() {
		if(this.secondary == null)
			return primary();
		return this.secondary;
	}
	
	public ChatColor tertiary() {
		if(this.tertiary == null)
			return secondary();
		return this.tertiary;
	}

	@Override
	public String toString() {
		return this.primary + ", " + this.secondary + ", " + this.tertiary;
	}
}
