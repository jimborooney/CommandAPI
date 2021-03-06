package dev.jorel.commandapi.arguments;

import org.bukkit.Sound;

import dev.jorel.commandapi.CommandAPIHandler;

/**
 * An argument that represents the Bukkit Sound object
 */
public class SoundArgument extends Argument implements ICustomProvidedArgument {
	
	public SoundArgument() {
		super(CommandAPIHandler.getNMS()._ArgumentMinecraftKeyRegistered());
	}

	@Override
	public Class<?> getPrimitiveType() {
		return Sound.class;
	}

	@Override
	public SuggestionProviders getSuggestionProvider() {
		return SuggestionProviders.SOUNDS;
	}
	
	@Override
	public CommandAPIArgumentType getArgumentType() {
		return CommandAPIArgumentType.SOUND;
	}
}
