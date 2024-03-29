package fr.badblock.bukkit.games.bedwars.result;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;

import com.google.common.collect.Maps;

import fr.badblock.bukkit.games.bedwars.players.BedWarsData;
import fr.badblock.gameapi.GameAPI;
import fr.badblock.gameapi.players.BadblockPlayer;
import fr.badblock.gameapi.players.BadblockPlayerData;
import fr.badblock.gameapi.players.BadblockTeam;

public class BedWarsResults {
	public BedWarsResults(String time, BadblockTeam winner){

		Collection<BadblockPlayerData> data  = GameAPI.getAPI().getGameServer().getSavedPlayers();
		Collection<BadblockTeam>	   teams = GameAPI.getAPI().getGameServer().getSavedTeams();
	
		List<BadblockPlayerData> inOrderPlayers = new ArrayList<>();
		Map<BadblockTeam, Integer> scoredTeams = Maps.newConcurrentMap();
		
		data.stream().sorted((a, b) -> {
			return a.inGameData(BedWarsData.class).getScore() < b.inGameData(BedWarsData.class).getScore() ? 1 : -1;
		}).forEach(player -> inOrderPlayers.add(player));
		
		for(BadblockTeam team : teams){
			int score = 0;
			
			for(UUID uniqueId : team.getPlayersAtStart()){
				score += getScore(uniqueId, team, inOrderPlayers);	
			}
			
			scoredTeams.put(team, score);
		}
		
		Map<BadblockTeam, Integer> result = new LinkedHashMap<>();
		
		scoredTeams.entrySet().stream().sorted((a, b) -> {
			return a.getValue() < b.getValue() ? 1 : -1;
		}).forEach(team -> result.put(team.getKey(), team.getValue()));

		for(BadblockPlayerData playerData : inOrderPlayers){
			BadblockPlayer player = (BadblockPlayer) Bukkit.getPlayer(playerData.getUniqueId());
			
			if(player != null){
				BedWarsResult bedwarsResult = new BedWarsResult(player);
				bedwarsResult.doPlayersTop(inOrderPlayers);
				bedwarsResult.doTeamTop(result, winner);
				bedwarsResult.doGeneral(time, teams.size(), inOrderPlayers.size());
				
				player.postResult(bedwarsResult);
			}
		}
		
	}
	
	public int getScore(UUID player, BadblockTeam team, Collection<BadblockPlayerData> in){
		for(BadblockPlayerData p : in)
			if(p.getUniqueId().equals(player)){
				if(p.getTeam() == null)
					p.setTeam(team);
				return p.inGameData(BedWarsData.class).getScore();
			}
		
		return 0;
	}
}