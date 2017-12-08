package fr.badblock.bukkit.games.bedwars.players;

import fr.badblock.bukkit.games.bedwars.PluginBedWars;
import fr.badblock.bukkit.games.bedwars.entities.BedWarsTeamData;
import fr.badblock.bukkit.games.bedwars.runnables.GameRunnable;
import fr.badblock.gameapi.GameAPI;
import fr.badblock.gameapi.players.BadblockPlayer;
import fr.badblock.gameapi.players.BadblockPlayer.BadblockMode;
import fr.badblock.gameapi.players.BadblockTeam;
import fr.badblock.gameapi.players.scoreboard.BadblockScoreboardGenerator;
import fr.badblock.gameapi.players.scoreboard.CustomObjective;

public class BedWarsScoreboard extends BadblockScoreboardGenerator {
	public static final String WINS = "wins", KILLS = "kills", DEATHS = "deaths", LOOSES = "looses", BROKENBEDS = "brokenbeds";

	private CustomObjective objective;
	private BadblockPlayer  player;

	public BedWarsScoreboard(BadblockPlayer player){
		this.objective = GameAPI.getAPI().buildCustomObjective("bedwars");
		this.player = player;
		objective.showObjective(player);
		objective.setDisplayName("&b&o" + GameAPI.getGameName());
		objective.setGenerator(this);
		objective.generate();
		doBadblockFooter(objective);
	}

	@Override
	public void generate(){
		objective.changeLine(15, "&8&m----------------------");
		int i = 14;
		objective.changeLine(i--,  i18n("bedwars.scoreboard.time-desc"));
		objective.changeLine(i--,  i18n("bedwars.scoreboard.time", time(GameRunnable.time)));
		if (PluginBedWars.getInstance().getMapConfiguration() != null) {
			objective.changeLine(i,  ""); i--;
			if (!PluginBedWars.getInstance().getMapConfiguration().getAllowBows()) objective.changeLine(i--,  i18n("bedwars.scoreboard.nobows"));
			else objective.changeLine(i--,  i18n("bedwars.scoreboard.withbows"));
		}
		objective.changeLine(i--, "");
		for(BadblockTeam team : GameAPI.getAPI().getTeams()){
			BedWarsTeamData data = team.teamData(BedWarsTeamData.class);
			if(!data.hasBed()) objective.changeLine(i, team.getChatName().getAsLine(player) + " > &c✘");
			else objective.changeLine(i, team.getChatName().getAsLine(player) + " > &a✔");
			i--;
		}
		if(player.getBadblockMode() != BadblockMode.SPECTATOR){
			objective.changeLine(i,  ""); i--;
			objective.changeLine(i,  i18n("bedwars.scoreboard.wins", stat(WINS))); i--;
			objective.changeLine(i,  i18n("bedwars.scoreboard.kills", stat(KILLS))); i--;
			objective.changeLine(i,  i18n("bedwars.scoreboard.deaths", stat(DEATHS))); i--;
			objective.changeLine(i,  i18n("bedwars.scoreboard.brokenbeds", stat(BROKENBEDS))); i--;
		}
		for(int a=3;a<=i;a++) objective.removeLine(a);
		objective.changeLine(2,  "&8&m----------------------");
	}

	private String time(int time){
		String res = "m";
		int sec = time % 60;
		res = (time / 60) + res;
		if(sec < 10) res += "0";
		return res + sec + "s";
	}

	private int stat(String name){
		return (int) player.getPlayerData().getStatistics("bedwars", name);
	}

	private String i18n(String key, Object... args){
		return GameAPI.i18n().get(player.getPlayerData().getLocale(), key, args)[0];
	}
}
