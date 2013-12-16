package net.conriot.sona.chat;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

class Commands implements CommandExecutor {
	private ChatManager chat;
	private MessageManager message;
	
	public Commands(ChatManager chat, MessageManager message) {
		this.chat = chat;
		this.message = message;
	}
	
	private void message(Player sender, String[] args) {
		// Verify that there are enough args
		if(args.length > 1) {
			// Build the message string
			String message = "";
			for(int i = 1; i < args.length; i++) {
				if(i == 1)
					message += args[i];
				else
					message += " " + args[i];
			}
			
			// Attempt to send the message
			this.chat.message(sender, message, args[0]);
		}
	}
	
	private void reply(Player sender, String[] args) {
		// Verify that there are enough args
		if(args.length > 0) {
			// Build the message string
			String message = "";
			for(int i = 0; i < args.length; i++) {
				if(i == 0)
					message += args[i];
				else
					message += " " + args[i];
			}
			
			// Attempt to send the message
			this.chat.reply(sender, message);
		}
	}
	
	private void block(Player sender, String[] args) {
		// Verify that there are enough args
		if(args.length > 0)
			this.chat.block(sender, args[0]);
		else
			this.message.send(ColorScheme.RED_DARKRED, "{1}You must specify a {2}player name{1}!", sender);
	}
	
	private void unblock(Player sender, String[] args) {
		// Verify that there are enough args
		if(args.length > 0)
			this.chat.unblock(sender, args[0]);
		else
			this.message.send(ColorScheme.RED_DARKRED, "{1}You must specify a {2}player name{1}!", sender);
	}
	
	private void mute(Player sender, String[] args) {
		// Check if the sender is an op
		if(sender.isOp()) {
			// Verify that there are enough args
			if(args.length > 1) {
				try {
					// Convert minute duration to milliseconds
					long duration = Integer.parseInt(args[1]) * 60 * 1000;
					// Attempt to perform the mute
					if(this.chat.mute(args[0], duration))
						this.message.send(ColorScheme.GREEN_DARKGREEN, "{1}You have muted '{2}" + args[0] + "{1}' for {2}" + args[1] + "{1} minutes!", sender);
					else
						this.message.send(ColorScheme.RED_DARKRED, "{1}The player '{2}" + args[0] + "{1}' is not online!", sender);
				} catch(Exception e) {
					// Notify of malformed input
					this.message.send(ColorScheme.RED_DARKRED, "{1}You must specify a {2}player name{1} and a {2}duration in minutes{1}!", sender);
				}
			} else
				this.message.send(ColorScheme.RED_DARKRED, "{1}You must specify a {2}player name{1} and a {2}duration in minutes{1}!", sender);
		}
	}
	
	private void unmute(Player sender, String[] args) {
		// Check if the sender is an op
		if(sender.isOp()) {
			// Verify that there are enough args
			if(args.length > 0) {
				if(this.chat.unmute(args[0]))
					this.message.send(ColorScheme.GREEN_DARKGREEN, "{1}You have unmuted '{2}" + args[0] + "{1}'!", sender);
				else
					this.message.send(ColorScheme.RED_DARKRED, "{1}The player '{2}" + args[0] + "{1}' is not online!", sender);
			} else
				this.message.send(ColorScheme.RED_DARKRED, "{1}You must specify a {2}player name{1}!", sender);
		}
	}
	
	private void silence(Player sender) {
		this.chat.silence(sender);
	}
	
	private void unsilence(Player sender) {
		this.chat.unsilence(sender);
	}
	
	private void broadcast(Player sender, String[] args) {
		// Check is the sender is an op
		if(sender.isOp()) {
			// Rebuild the message from the args
			String message = "";
			for(String sub : args)
				message += " " + sub;
			
			// Announce the message to everyone
			this.message.sendAll(ColorScheme.GRAY_WHITE_AQUA, "{1}*{2}*{3}" + ChatColor.BOLD + message + " {2}*{1}*");
		}
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(sender instanceof Player) {
			Player player = (Player)sender;
			switch(label.toLowerCase()) {
			case "msg":
				message(player, args);
				break;
			case "r":
				reply(player, args);
				break;
			case "block":
				block(player, args);
				break;
			case "unblock":
				unblock(player, args);
				break;
			case "mute":
				mute(player, args);
				break;
			case "unmute":
				unmute(player, args);
				break;
			case "silence":
				silence(player);
				break;
			case "unsilence":
				unsilence(player);
				break;
			case "broadcast":
				broadcast(player, args);
				break;
			}
		}
		
		// Always return true to prevent default Bukkit messages
		return true;
	}
}

/*
msg:
description: Send a private message to a player
r:
description: Reply to the last person you got a message from
block:
description: Hide all private and public messages from a player
unblock:
description: Show all private and public messages from a player
mute:
description: Mute a player for a given time
unmute:
description: Unmute a player if they are muted
silence:
description: Disable all non-op chat
unsilence:
description: Enable all non-op chat
broadcast:
description: Announce a message to the server
*/