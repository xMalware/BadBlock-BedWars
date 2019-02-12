package fr.badblock.bukkit.games.bedwars.commands;

import org.bukkit.command.CommandSender;

import fr.badblock.bukkit.games.bedwars.PluginBedWars;
import fr.badblock.bukkit.games.bedwars.runnables.StartRunnable;
import fr.badblock.gameapi.GameAPI;
import fr.badblock.gameapi.command.AbstractCommand;
import fr.badblock.gameapi.players.BadblockPlayer;
import fr.badblock.gameapi.players.BadblockTeam;
import fr.badblock.gameapi.players.BadblockPlayer.GamePermission;
import fr.badblock.gameapi.utils.BukkitUtils;
import fr.badblock.gameapi.utils.i18n.TranslatableString;

public class GameCommand extends AbstractCommand {
	public GameCommand() {
		super("game", new TranslatableString("commands.gbedwars.usage"), GamePermission.PLAYER);
		allowConsole(false);
	}

	@Override
	public boolean executeCommand(CommandSender sender, String[] args) {
		if(args.length == 0) {
			return false;
		}

		BadblockPlayer player = (BadblockPlayer) sender;
		PluginBedWars plug = PluginBedWars.getInstance();

		switch(args[0].toLowerCase()) {
		case "start":
			if (GameAPI.getAPI().isHoster(player))
			{
				String msg = "commands.grush.start";

				if(!StartRunnable.started()){
					StartRunnable.startGame(true);
				} else msg += "-fail";

				player.sendTranslatedMessage(msg);
			}
			else
			{
				player.sendMessage("§cVous n'avez pas la permission de faire cela.");
			}
			break;
		case "stop":
			if (player.hasPermission(GamePermission.ADMIN))
			{
				String msg = "commands.grush.stop";

				if(StartRunnable.started()){
					StartRunnable.stopGame();
				} else msg += "-fail";

				player.sendTranslatedMessage(msg);
			}
			else
			{
				player.sendMessage("§cVous n'avez pas la permission de faire cela.");
			}
			break;
		default: return false;
		}

		return true;
	}
}