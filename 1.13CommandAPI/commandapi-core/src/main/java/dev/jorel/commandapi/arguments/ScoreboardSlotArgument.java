package dev.jorel.commandapi.arguments;

import dev.jorel.commandapi.CommandAPIHandler;
import dev.jorel.commandapi.wrappers.ScoreboardSlot;

/**
 * An argument that represents the Bukkit ScoreboardSlot object
 */
public class ScoreboardSlotArgument extends Argument {

	/**
	 * A Display slot argument. Represents scoreboard slots
	 */
	public ScoreboardSlotArgument() {
		super(CommandAPIHandler.getNMS()._ArgumentScoreboardSlot());
	}

	@Override
	public Class<?> getPrimitiveType() {
		return ScoreboardSlot.class;
	}
	
	@Override
	public CommandAPIArgumentType getArgumentType() {
		return CommandAPIArgumentType.SCOREBOARD_SLOT;
	}
}
