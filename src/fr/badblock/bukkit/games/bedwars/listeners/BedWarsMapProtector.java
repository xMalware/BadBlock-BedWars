package fr.badblock.bukkit.games.bedwars.listeners;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.event.block.Action;
import org.bukkit.potion.PotionEffectType;

import fr.badblock.bukkit.games.bedwars.entities.BedWarsTeamData;
import fr.badblock.bukkit.games.bedwars.runnables.GameRunnable;
import fr.badblock.gameapi.GameAPI;
import fr.badblock.gameapi.game.GameState;
import fr.badblock.gameapi.players.BadblockPlayer;
import fr.badblock.gameapi.players.BadblockTeam;
import fr.badblock.gameapi.servers.MapProtector;

public class BedWarsMapProtector implements MapProtector {

	public static List<Location> breakableBlocks = new ArrayList<>();
	public static List<Location> glassBlocks = new ArrayList<>();

	private boolean inGame(){
		return GameAPI.getAPI().getGameServer().getGameState() == GameState.RUNNING;
	}

	@Override
	public boolean blockPlace(BadblockPlayer player, Block block) {

		boolean can = inGame() || player.hasAdminMode();

		if (can)
		{
			breakableBlocks.add(block.getLocation());

			if (block.getType().equals(Material.GLASS))
			{
				glassBlocks.add(block.getLocation());
			}
		}

		return can;
	}

	@Override
	public boolean blockBreak(BadblockPlayer player, Block block) {
		if(!inGame()){
			return player.hasAdminMode();
		}

		boolean can = false;

		if (BedListenerUtils.onBreakBed(player, block, false))
		{
			can = true;
		}

		if (breakableBlocks.contains(block.getLocation()))
		{
			can = true;
			breakableBlocks.remove(block.getLocation());

			if (block.getType().equals(Material.GLASS))
			{
				glassBlocks.remove(block.getLocation());
			}
		}

		return can || player.hasAdminMode();
	}

	@Override
	public boolean modifyItemFrame(BadblockPlayer player, Entity itemFrame) {
		return player.hasAdminMode();
	}

	@Override
	public boolean canLostFood(BadblockPlayer player) {
		return false;
	}

	@Override
	public boolean canUseBed(BadblockPlayer player, Block bed) {
		return false;
	}

	@Override
	public boolean canUsePortal(BadblockPlayer player) {
		return false;
	}

	@Override
	public boolean canDrop(BadblockPlayer player) {
		return inGame() || player.hasAdminMode();
	}

	@Override
	public boolean canPickup(BadblockPlayer player) {
		return inGame() || player.hasAdminMode();
	}

	@Override
	public boolean canFillBucket(BadblockPlayer player) {
		return inGame() || player.hasAdminMode();
	}

	@Override
	public boolean canEmptyBucket(BadblockPlayer player) {
		return inGame() || player.hasAdminMode();
	}

	@Override
	public boolean canInteract(BadblockPlayer player, Action action, Block block) {
		if(inGame() && action == Action.RIGHT_CLICK_BLOCK){
			boolean cancel = false;

			switch(block.getType()){
			case CHEST: case TRAPPED_CHEST: case ENDER_CHEST: case FURNACE: case ENCHANTMENT_TABLE: case WORKBENCH:
				cancel = true;
				break;
			case BARRIER:
				cancel = true;
				player.sendTranslatedMessage("bedwars.youcantplaceablockthere");
				return false;
			default: break;
			}

			if(cancel)
				for(BadblockTeam team : GameAPI.getAPI().getTeams()){
					if(!team.equals(player.getTeam())){
						if(team.teamData(BedWarsTeamData.class).getSpawnSelection().isInSelection(block)){
							return false;
						}
					}
				}
		}

		if(block != null && block.getType() == Material.BED_BLOCK && inGame() && action == Action.RIGHT_CLICK_BLOCK){
			BadblockTeam team = BedListenerUtils.parseBedTeam(block);

			if(team == null || !team.equals(player.getTeam())){
				player.sendTranslatedTitle("bedwars.noyourebed");
			} else {
				player.sendTranslatedTitle("bedwars.sleeping", player.getName());
			}

			return false;
		}

		if(block != null && block.getType() == Material.CHEST && inGame() && action == Action.RIGHT_CLICK_BLOCK){

			for(BadblockTeam team : GameAPI.getAPI().getTeams()){
				Location loc = team.teamData(BedWarsTeamData.class).getRespawnLocation();

				if(loc.getWorld().equals(block.getWorld()) && loc.distance(block.getLocation()) < 10.0d){
					return team.equals(player.getTeam());
				}
			}

		}

		return inGame() || player.hasAdminMode();
	}

	@Override
	public boolean canInteractArmorStand(BadblockPlayer player, ArmorStand entity) {
		return false; // sait on jamais :o
	}

	@Override
	public boolean canInteractEntity(BadblockPlayer player, Entity entity) {
		return true; // � priori rien � bloquer ... :o
	}

	@Override
	public boolean canEnchant(BadblockPlayer player, Block table) {
		return false; // � prioris pas d'enchant � faire :3
	}

	@Override
	public boolean canBeingDamaged(BadblockPlayer player) {
		if (player.getActivePotionEffects() != null)
		{
			if (player.getActivePotionEffects().parallelStream().filter(effect ->
			effect.getType().equals(PotionEffectType.INVISIBILITY)).count() > 0)
			{
				player.removePotionEffect(PotionEffectType.INVISIBILITY);
				player.playSound(Sound.ENDERMAN_HIT);
				player.sendTranslatedMessage("bedwars.invisibilitystop");
			}
		}
		return GameRunnable.damage;
	}

	@Override
	public boolean healOnJoin(BadblockPlayer player) {
		return !inGame();
	}

	@Override
	public boolean canBlockDamage(Block block) {
		return true;
	}

	@Override
	public boolean allowFire(Block block) {
		return false;
	}

	@Override
	public boolean allowMelting(Block block) {
		return false;
	}

	@Override
	public boolean allowBlockFormChange(Block block) {
		return true; //TODO test
	}

	@Override
	public boolean allowPistonMove(Block block) {
		return false;
	}

	@Override
	public boolean allowBlockPhysics(Block block) {
		return true;
	}

	@Override
	public boolean allowLeavesDecay(Block block) {
		return false;
	}

	@Override
	public boolean allowRaining() {
		return false;
	}

	@Override
	public boolean modifyItemFrame(Entity itemframe) {
		return false;
	}

	@Override
	public boolean canSoilChange(Block soil) {
		return false;
	}

	@Override
	public boolean canSpawn(Entity entity) {
		return true;
	}

	@Override
	public boolean canCreatureSpawn(Entity creature, boolean isPlugin) {
		if (creature != null && creature.getType().equals(EntityType.ARMOR_STAND))
		{
			return true;
		}
		System.out.println("Creature spawn : " + creature.getType());
		return isPlugin || creature.getType().equals(EntityType.IRON_GOLEM);
	}

	@Override
	public boolean canItemSpawn(Item item) {
		return true;
	}

	@Override
	public boolean canItemDespawn(Item item) {
		return true;
	}

	@Override
	public boolean allowExplosion(Location location) {
		return inGame();
	}

	@Override
	public boolean allowInteract(Entity entity) {
		return false;
	}

	@Override
	public boolean canCombust(Entity entity) {
		return true;
	}

	@Override
	public boolean canEntityBeingDamaged(Entity entity) {
		return !inGame();
	}

	@Override
	public boolean destroyArrow() {
		return true;
	}

	@Override
	public boolean canEntityBeingDamaged(Entity entity, BadblockPlayer badblockPlayer) {
		return false;
	}

}
