package dev.jorel.commandapi.arguments;

import dev.jorel.commandapi.CommandAPIHandler;
import dev.jorel.commandapi.wrappers.Location2D;

/**
 * An argument that represents the Bukkit Location object in x and z directions
 */
public class Location2DArgument extends Argument {

	/**
	 * A Location argument. Represents Minecraft locations in 2D space. Defaults to LocationType.PRECISE_POSITION
	 */
	public Location2DArgument() {
		this(LocationType.PRECISE_POSITION);
	}

	/**
	 * A Location argument. Represents Minecraft locations in 2D space
	 * @param type the location type of this location, either LocationType.BLOCK_POSITION or LocationType.PRECISE_POSITION
	 */
	public Location2DArgument(LocationType type) {
		super(type == LocationType.BLOCK_POSITION ? CommandAPIHandler.getNMS()._ArgumentPosition2D()
				: CommandAPIHandler.getNMS()._ArgumentVec2());
		locationType = type;
	}

	private final LocationType locationType;

	/**
	 * Returns whether this argument is LocationType.BLOCK_POSITION or LocationType.PRECISE_POSITION 
	 * @return the location type of this argument
	 */
	public LocationType getLocationType() {
		return locationType;
	}
	
	@Override
	public Class<?> getPrimitiveType() {
		return Location2D.class;
	}

	@Override
	public CommandAPIArgumentType getArgumentType() {
		return CommandAPIArgumentType.LOCATION_2D;
	}
}
