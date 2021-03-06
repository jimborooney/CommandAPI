package dev.jorel.commandapi.arguments;

import dev.jorel.commandapi.CommandAPIHandler;
import dev.jorel.commandapi.wrappers.FunctionWrapper;

/**
 * An argument that represents Minecraft functions and tags
 */
public class FunctionArgument extends Argument implements ICustomProvidedArgument {

	/**
	 * A Minecraft 1.12 function. Plugin commands which plan to be used INSIDE
	 * a function MUST be registered in the onLoad() method of your plugin, NOT
	 * in the onEnable() method!
	 */
	public FunctionArgument() {
		super(CommandAPIHandler.getNMS()._ArgumentTag());
	}

	@Override
	public Class<?> getPrimitiveType() {
		return FunctionWrapper[].class;
	}
	
	@Override
	public SuggestionProviders getSuggestionProvider() {
		return SuggestionProviders.FUNCTION;
	}
	
	@Override
	public CommandAPIArgumentType getArgumentType() {
		return CommandAPIArgumentType.FUNCTION;
	}
}
