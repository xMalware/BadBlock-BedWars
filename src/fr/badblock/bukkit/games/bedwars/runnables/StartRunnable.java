package fr.badblock.bukkit.games.bedwars.runnables;

import fr.badblock.bukkit.games.bedwars.PluginBedWars;
import fr.badblock.bukkit.games.bedwars.configuration.BedWarsMapConfiguration;
import fr.badblock.bukkit.games.bedwars.listeners.JoinListener;
import fr.badblock.bukkit.games.bedwars.players.BedWarsScoreboard;
import fr.badblock.gameapi.GameAPI;
import fr.badblock.gameapi.players.BadblockPlayer;
import fr.badblock.gameapi.utils.BukkitUtils;
import fr.badblock.gameapi.utils.i18n.TranslatableString;
import fr.badblock.gameapi.utils.i18n.messages.GameMessages;
import lombok.AllArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;

@AllArgsConstructor
public class StartRunnable extends BukkitRunnable {
	
	public    static final int 		     TIME_BEFORE_START = 20;
	protected static 	   StartRunnable task 		       = null;
	public 	  static 	   GameRunnable  gameTask		   = null;
	
	public static int time = TIME_BEFORE_START;

	@Override
	public void run() {
		GameAPI.setJoinable(time > 10);
		if(time == 0){
			for(Player player : Bukkit.getOnlinePlayers()){
				BadblockPlayer bPlayer = (BadblockPlayer) player;
				bPlayer.playSound(Sound.ORB_PICKUP);
			}
			for(BadblockPlayer player : BukkitUtils.getPlayers()) if (player.getCustomObjective() == null) new BedWarsScoreboard(player);
			for(Sheep sheep : JoinListener.sheeps){
				sheep.eject();
				sheep.remove();
			}
			JoinListener.sheeps.clear();
			String winner = GameAPI.getAPI().getBadblockScoreboard().getWinner().getInternalName();
			File file = new File(PluginBedWars.MAP, winner + ".json");
			BedWarsMapConfiguration config = new BedWarsMapConfiguration(GameAPI.getAPI().loadConfiguration(file));
			config.save(file);
			PluginBedWars.getInstance().setMapConfiguration(config);
			GameAPI.getAPI().balanceTeams(true);
			gameTask = new GameRunnable(config);
			gameTask.runTaskTimer(GameAPI.getAPI(), 0, 20L);
			cancel();
		} else if(time % 10 == 0 || time <= 5){
			sendTime(time);
		}

		if (time == 10)
		{
			
		} else if(time % 10 == 0 || time <= 5) sendTime(time);
		if(time == 5){
			String winner = GameAPI.getAPI().getBadblockScoreboard().getWinner().getInternalName();
			
			File file = new File(PluginBedWars.MAP, winner + ".json");
			BedWarsMapConfiguration config = new BedWarsMapConfiguration(GameAPI.getAPI().loadConfiguration(file));
			config.save(file);
			GameAPI.getAPI().getBadblockScoreboard().endVote();

			for(Player player : Bukkit.getOnlinePlayers()){
				new BedWarsScoreboard((BadblockPlayer) player);
			}

		}
		
		sendTimeHidden(time);
		time--;
	}

	private void start(){
		sendTime(time);
		runTaskTimer(GameAPI.getAPI(), 0, 20L);
	}

	private void sendTime(int time){
		ChatColor color = getColor(time);
		TranslatableString title = GameMessages.startIn(time, color);
		for(Player player : Bukkit.getOnlinePlayers()){
			BadblockPlayer bPlayer = (BadblockPlayer) player;
			bPlayer.playSound(Sound.NOTE_PLING);
			bPlayer.sendTranslatedTitle(title.getKey(), title.getObjects());
			bPlayer.sendTimings(2, 30, 2);
		}
	}

	private void sendTimeHidden(int time){
		ChatColor color = getColor(time);
		TranslatableString actionbar = GameMessages.startInActionBar(time, color);
		for(Player player : Bukkit.getOnlinePlayers()){
			BadblockPlayer bPlayer = (BadblockPlayer) player;
			if(time > 0) bPlayer.sendTranslatedActionBar(actionbar.getKey(), actionbar.getObjects());
			bPlayer.setLevel(time);
			bPlayer.setExp(0.0f);
		}
	}

	private ChatColor getColor(int time){
		if(time == 1) return ChatColor.DARK_RED;
		else if(time <= 5) return ChatColor.RED;
		else return ChatColor.AQUA;
	}

	public static void joinNotify(int currentPlayers, int maxPlayers){
		if ((!GameAPI.getAPI().isHostedGame() && currentPlayers + 1 < PluginBedWars.getInstance().getMinPlayers())
				|| (GameAPI.getAPI().isHostedGame() && currentPlayers + 1 < PluginBedWars.getInstance().getMaxPlayers())) return;
		
		startGame();
		if (time >= 5 && Bukkit.getOnlinePlayers().size() >= Bukkit.getMaxPlayers()) time = 5;
	}

	public static void startGame(){
		if(task == null){
			task = new StartRunnable();
			task.start();
		}
	}

	public static void stopGame(){
		if(gameTask != null){
			gameTask.forceEnd = true;
			time = TIME_BEFORE_START;
		} else if(task != null){
			task.cancel();
			time = time > 5 ? time : 5;
			GameAPI.setJoinable(true);
		}
		task = null;
		gameTask = null;
	}

	public static boolean started(){
		return task != null;
	}
}
