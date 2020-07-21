package dev.jorel.commandapi.nms;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.concurrent.CompletableFuture;
import java.util.function.ToIntBiFunction;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.bukkit.Axis;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World.Environment;
import org.bukkit.block.Biome;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.craftbukkit.v1_16_R1.CraftLootTable;
import org.bukkit.craftbukkit.v1_16_R1.CraftParticle;
import org.bukkit.craftbukkit.v1_16_R1.CraftServer;
import org.bukkit.craftbukkit.v1_16_R1.CraftSound;
import org.bukkit.craftbukkit.v1_16_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R1.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_16_R1.command.ProxiedNativeCommandSender;
import org.bukkit.craftbukkit.v1_16_R1.command.VanillaCommandWrapper;
import org.bukkit.craftbukkit.v1_16_R1.enchantments.CraftEnchantment;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_16_R1.potion.CraftPotionEffectType;
import org.bukkit.craftbukkit.v1_16_R1.util.CraftChatMessage;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ComplexRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootTable;
import org.bukkit.potion.PotionEffectType;

import com.google.common.io.Files;
import com.google.gson.GsonBuilder;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;

import de.tr7zw.nbtapi.NBTContainer;
import dev.jorel.commandapi.CommandAPIHandler;
import dev.jorel.commandapi.CommandAPIMain;
import dev.jorel.commandapi.arguments.EntitySelectorArgument.EntitySelector;
import dev.jorel.commandapi.arguments.ICustomProvidedArgument.SuggestionProviders;
import dev.jorel.commandapi.arguments.LocationType;
import dev.jorel.commandapi.wrappers.FloatRange;
import dev.jorel.commandapi.wrappers.FunctionWrapper;
import dev.jorel.commandapi.wrappers.IntegerRange;
import dev.jorel.commandapi.wrappers.Location2D;
import dev.jorel.commandapi.wrappers.MathOperation;
import dev.jorel.commandapi.wrappers.Rotation;
import dev.jorel.commandapi.wrappers.ScoreboardSlot;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.minecraft.server.v1_16_R1.Advancement;
import net.minecraft.server.v1_16_R1.ArgumentChat;
import net.minecraft.server.v1_16_R1.ArgumentChatComponent;
import net.minecraft.server.v1_16_R1.ArgumentChatFormat;
import net.minecraft.server.v1_16_R1.ArgumentCriterionValue;
import net.minecraft.server.v1_16_R1.ArgumentDimension;
import net.minecraft.server.v1_16_R1.ArgumentEnchantment;
import net.minecraft.server.v1_16_R1.ArgumentEntity;
import net.minecraft.server.v1_16_R1.ArgumentEntitySummon;
import net.minecraft.server.v1_16_R1.ArgumentItemStack;
import net.minecraft.server.v1_16_R1.ArgumentMathOperation;
import net.minecraft.server.v1_16_R1.ArgumentMinecraftKeyRegistered;
import net.minecraft.server.v1_16_R1.ArgumentMobEffect;
import net.minecraft.server.v1_16_R1.ArgumentNBTTag;
import net.minecraft.server.v1_16_R1.ArgumentParticle;
import net.minecraft.server.v1_16_R1.ArgumentPosition;
import net.minecraft.server.v1_16_R1.ArgumentProfile;
import net.minecraft.server.v1_16_R1.ArgumentRegistry;
import net.minecraft.server.v1_16_R1.ArgumentRotation;
import net.minecraft.server.v1_16_R1.ArgumentRotationAxis;
import net.minecraft.server.v1_16_R1.ArgumentScoreboardCriteria;
import net.minecraft.server.v1_16_R1.ArgumentScoreboardObjective;
import net.minecraft.server.v1_16_R1.ArgumentScoreboardSlot;
import net.minecraft.server.v1_16_R1.ArgumentScoreboardTeam;
import net.minecraft.server.v1_16_R1.ArgumentScoreholder;
import net.minecraft.server.v1_16_R1.ArgumentTag;
import net.minecraft.server.v1_16_R1.ArgumentTile;
import net.minecraft.server.v1_16_R1.ArgumentTime;
import net.minecraft.server.v1_16_R1.ArgumentVec2;
import net.minecraft.server.v1_16_R1.ArgumentVec2I;
import net.minecraft.server.v1_16_R1.ArgumentVec3;
import net.minecraft.server.v1_16_R1.BlockPosition;
import net.minecraft.server.v1_16_R1.BlockPosition2D;
import net.minecraft.server.v1_16_R1.CommandListenerWrapper;
import net.minecraft.server.v1_16_R1.CompletionProviders;
import net.minecraft.server.v1_16_R1.CustomFunction;
import net.minecraft.server.v1_16_R1.CustomFunctionData;
import net.minecraft.server.v1_16_R1.CustomFunctionManager;
import net.minecraft.server.v1_16_R1.DataPackResources;
import net.minecraft.server.v1_16_R1.DedicatedServer;
import net.minecraft.server.v1_16_R1.Entity;
import net.minecraft.server.v1_16_R1.EnumDirection.EnumAxis;
import net.minecraft.server.v1_16_R1.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_16_R1.ICompletionProvider;
import net.minecraft.server.v1_16_R1.IRecipe;
import net.minecraft.server.v1_16_R1.IRegistry;
import net.minecraft.server.v1_16_R1.IReloadableResourceManager;
import net.minecraft.server.v1_16_R1.IVectorPosition;
import net.minecraft.server.v1_16_R1.LootTableRegistry;
import net.minecraft.server.v1_16_R1.MinecraftKey;
import net.minecraft.server.v1_16_R1.MinecraftServer;
import net.minecraft.server.v1_16_R1.ScoreboardScore;
import net.minecraft.server.v1_16_R1.SystemUtils;
import net.minecraft.server.v1_16_R1.Unit;
import net.minecraft.server.v1_16_R1.Vec2F;
import net.minecraft.server.v1_16_R1.Vec3D;
import net.minecraft.server.v1_16_R1.WorldServer;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class NMS_1_16_R1 implements NMS {

	@Override
	public void reloadDataPacks()
			throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		CommandAPIMain.getLog().info("Reloading datapacks...");

		// Get the NMS server
		DedicatedServer server = ((CraftServer) Bukkit.getServer()).getHandle().getServer();

		// Update the commandDispatcher with the current server's commandDispatcher
		DataPackResources datapackResources = server.dataPackResources;
		datapackResources.commandDispatcher = server.getCommandDispatcher();

		// Reflection doesn't need to be cached because this only executes once at
		// server startup
		Field i = DataPackResources.class.getDeclaredField("i");
		i.setAccessible(true);
		Lookup lookup = MethodHandles.privateLookupIn(Field.class, MethodHandles.lookup());
		VarHandle modifiers = lookup.findVarHandle(Field.class, "modifiers", int.class);
		int mods = i.getModifiers();
		if (Modifier.isFinal(mods)) {
			modifiers.set(i, mods & ~Modifier.FINAL);
		}
//		Field i = DataPackResources.class.getDeclaredField("i");
//		i.setAccessible(true);
//		Field modifiersField = Field.class.getDeclaredField("modifiers");
//		modifiersField.setAccessible(true);
//		modifiersField.setInt(i, i.getModifiers() & ~Modifier.FINAL);

		Field fField = CustomFunctionManager.class.getDeclaredField("f");
		fField.setAccessible(true);
		int f = (int) fField.get(datapackResources.a()); // Related to the permission required to run this function?

		// Update the CustomFunctionManager for the datapackResources which now has the
		// new commandDispatcher
		i.set(datapackResources, new CustomFunctionManager(f, datapackResources.commandDispatcher.a()));

		// Construct the new CompletableFuture that now uses datapackResources
		Field b = DataPackResources.class.getDeclaredField("b");
		b.setAccessible(true);
		IReloadableResourceManager reloadableResourceManager = (IReloadableResourceManager) b.get(datapackResources);
		Field a = DataPackResources.class.getDeclaredField("a");
		a.setAccessible(true);

		CompletableFuture<Unit> unit = (CompletableFuture<Unit>) a.get(null);
		CompletableFuture<Unit> unitCompletableFuture = reloadableResourceManager.a(SystemUtils.f(), Runnable::run,
				server.getResourcePackRepository().f(), unit);

		CompletableFuture<DataPackResources> completablefuture = unitCompletableFuture
				.whenComplete((Unit u, Throwable t) -> {
					if (t != null) {
						datapackResources.close();
					}

				}).thenApply((Unit u) -> {
					return datapackResources;
				});

		// Run the completableFuture and bind tags
		try {
			((DataPackResources) completablefuture.get()).i();
			CommandAPIMain.getLog().info("Finished reloading datapacks");
		} catch (Exception e) {
			CommandAPIMain.getLog().log(Level.WARNING,
					"Failed to load datapacks, can't proceed with server load. You can either fix your datapacks or reset to vanilla with --safeMode",
					e);
		}
	}

	@Override
	public ArgumentType<?> _ArgumentAxis() {
		return ArgumentRotationAxis.a();
	}

	@Override
	public ArgumentType<?> _ArgumentBlockState() {
		return ArgumentTile.a();
	}

	@Override
	public ArgumentType<?> _ArgumentChat() {
		return ArgumentChat.a();
	}

	@Override
	public ArgumentType _ArgumentChatComponent() {
		return ArgumentChatComponent.a();
	}

	@Override
	public ArgumentType _ArgumentChatFormat() {
		return ArgumentChatFormat.a();
	}

	@Override
	public ArgumentType<?> _ArgumentDimension() {
		return ArgumentDimension.a();
	}

	@Override
	public ArgumentType _ArgumentEnchantment() {
		return ArgumentEnchantment.a();
	}

	@Override
	public ArgumentType _ArgumentEntity(EntitySelector selector) {
		switch (selector) {
		case MANY_ENTITIES:
			return ArgumentEntity.multipleEntities();
		case MANY_PLAYERS:
			return ArgumentEntity.d();
		case ONE_ENTITY:
			return ArgumentEntity.a();
		case ONE_PLAYER:
			return ArgumentEntity.c();
		}
		return null;
	}

	@Override
	public ArgumentType _ArgumentEntitySummon() {
		return ArgumentEntitySummon.a();
	}

	@Override
	public ArgumentType<?> _ArgumentFloatRange() {
		return new ArgumentCriterionValue.a();
	}

	@Override
	public ArgumentType<?> _ArgumentIntRange() {
		return new ArgumentCriterionValue.b();
	}

	@Override
	public ArgumentType _ArgumentItemStack() {
		return ArgumentItemStack.a();
	}

	@Override
	public ArgumentType<?> _ArgumentMathOperation() {
		return ArgumentMathOperation.a();
	}

	@Override
	public ArgumentType _ArgumentMinecraftKeyRegistered() {
		return ArgumentMinecraftKeyRegistered.a();
	}

	@Override
	public ArgumentType _ArgumentMobEffect() {
		return ArgumentMobEffect.a();
	}

	@Override
	public ArgumentType<?> _ArgumentNBTCompound() {
		return ArgumentNBTTag.a();
	}

	@Override
	public ArgumentType _ArgumentParticle() {
		return ArgumentParticle.a();
	}

	@Override
	public ArgumentType _ArgumentPosition() {
		return ArgumentPosition.a();
	}

	@Override
	public ArgumentType<?> _ArgumentPosition2D() {
		return ArgumentVec2I.a();
	}

	@Override
	public ArgumentType _ArgumentProfile() {
		return ArgumentProfile.a();
	}

	@Override
	public ArgumentType<?> _ArgumentRotation() {
		return ArgumentRotation.a();
	}

	@Override
	public ArgumentType<?> _ArgumentScoreboardCriteria() {
		return ArgumentScoreboardCriteria.a();
	}

	@Override
	public ArgumentType<?> _ArgumentScoreboardObjective() {
		return ArgumentScoreboardObjective.a();
	}

	@Override
	public ArgumentType<?> _ArgumentScoreboardSlot() {
		return ArgumentScoreboardSlot.a();
	}

	@Override
	public ArgumentType<?> _ArgumentScoreboardTeam() {
		return ArgumentScoreboardTeam.a();
	}

	@Override
	public ArgumentType<?> _ArgumentScoreholder(boolean single) {
		return single ? ArgumentScoreholder.a() : ArgumentScoreholder.b();
	}

	@Override
	public ArgumentType _ArgumentTag() {
		return ArgumentTag.a();
	}

	@Override
	public ArgumentType<?> _ArgumentTime() {
		return ArgumentTime.a();
	}

	@Override
	public ArgumentType<?> _ArgumentVec2() {
		return ArgumentVec2.a();
	}

	@Override
	public ArgumentType _ArgumentVec3() {
		return ArgumentVec3.a();
	}

	@Override
	public String[] compatibleVersions() {
		return new String[] { "1.16.1" };
	}

	@Override
	public void createDispatcherFile(Object server, File file, CommandDispatcher dispatcher) throws IOException {
		Files.write((new GsonBuilder()).setPrettyPrinting().create()
				.toJson(ArgumentRegistry.a(dispatcher, dispatcher.getRoot())), file, StandardCharsets.UTF_8);
	}

	@Override
	public org.bukkit.advancement.Advancement getAdvancement(CommandContext cmdCtx, String key)
			throws CommandSyntaxException {
		return ArgumentMinecraftKeyRegistered.a(cmdCtx, key).bukkit;
	}

	@Override
	public EnumSet<Axis> getAxis(CommandContext cmdCtx, String key) {
		EnumSet<Axis> set = EnumSet.noneOf(Axis.class);
		EnumSet<EnumAxis> parsedEnumSet = ArgumentRotationAxis.a(cmdCtx, key);
		for (EnumAxis element : parsedEnumSet) {
			switch (element) {
			case X:
				set.add(Axis.X);
				break;
			case Y:
				set.add(Axis.Y);
				break;
			case Z:
				set.add(Axis.Z);
				break;
			}
		}
		return set;
	}

	@Override
	public Biome getBiome(CommandContext cmdCtx, String key) {
		MinecraftKey minecraftKey = (MinecraftKey) cmdCtx.getArgument(key, MinecraftKey.class);
		return Biome.valueOf(minecraftKey.getKey().toUpperCase());
	}

	@Override
	public BlockData getBlockState(CommandContext cmdCtx, String key) {
		return CraftBlockData.fromData(ArgumentTile.a(cmdCtx, key).a());
	}

	@Override
	public CommandDispatcher getBrigadierDispatcher(Object server) {
		return ((MinecraftServer) server).getCommandDispatcher().a();
	}

	@Override
	public BaseComponent[] getChat(CommandContext cmdCtx, String key) throws CommandSyntaxException {
		String resultantString = ChatSerializer.a(ArgumentChat.a(cmdCtx, key));
		return ComponentSerializer.parse(resultantString);
	}

	@Override
	public ChatColor getChatColor(CommandContext cmdCtx, String str) {
		return CraftChatMessage.getColor(ArgumentChatFormat.a(cmdCtx, str));
	}

	@Override
	public BaseComponent[] getChatComponent(CommandContext cmdCtx, String str) {
		String resultantString = ChatSerializer.a(ArgumentChatComponent.a(cmdCtx, str));
		return ComponentSerializer.parse((String) resultantString);
	}

	private CommandListenerWrapper getCLW(CommandContext cmdCtx) {
		return (CommandListenerWrapper) cmdCtx.getSource();
	}

	@Override
	public CommandSender getCommandSenderForCLW(Object clw) {
		try {
			return ((CommandListenerWrapper) clw).getBukkitSender();
		} catch (UnsupportedOperationException e) {
			return null;
		}
	}

	@Override
	public Environment getDimension(CommandContext cmdCtx, String key) throws CommandSyntaxException {
		WorldServer worldServer = ArgumentDimension.a(cmdCtx, key);
		return worldServer.getWorld().getEnvironment();
	}

	@Override
	public Enchantment getEnchantment(CommandContext cmdCtx, String str) {
		return new CraftEnchantment(ArgumentEnchantment.a(cmdCtx, str));
	}

	@Override
	public Object getEntitySelector(CommandContext cmdCtx, String str, EntitySelector selector)
			throws CommandSyntaxException {
		switch (selector) {
		case MANY_ENTITIES:
			try {
				return ArgumentEntity.c(cmdCtx, str).stream()
						.map(entity -> (org.bukkit.entity.Entity) ((Entity) entity).getBukkitEntity())
						.collect(Collectors.toList());
			} catch (CommandSyntaxException e) {
				return new ArrayList<org.bukkit.entity.Entity>();
			}
		case MANY_PLAYERS:
			try {
				return ArgumentEntity.d(cmdCtx, str).stream()
						.map(player -> (Player) ((Entity) player).getBukkitEntity()).collect(Collectors.toList());
			} catch (CommandSyntaxException e) {
				return new ArrayList<Player>();
			}
		case ONE_ENTITY:
			return (org.bukkit.entity.Entity) ArgumentEntity.a(cmdCtx, str).getBukkitEntity();
		case ONE_PLAYER:
			return (Player) ArgumentEntity.e(cmdCtx, str).getBukkitEntity();
		}
		return null;
	}

	@Override
	public EntityType getEntityType(CommandContext cmdCtx, String str, CommandSender sender)
			throws CommandSyntaxException {
		Entity entity = IRegistry.ENTITY_TYPE.get(ArgumentEntitySummon.a(cmdCtx, str))
				.a(((CraftWorld) getCommandSenderWorld(sender)).getHandle());
		return entity.getBukkitEntity().getType();
	}

	@Override
	public FloatRange getFloatRange(CommandContext cmdCtx, String key) {
		net.minecraft.server.v1_16_R1.CriterionConditionValue.FloatRange.FloatRange range = (net.minecraft.server.v1_16_R1.CriterionConditionValue.FloatRange.FloatRange) cmdCtx
				.getArgument(key, FloatRange.class);
		float low = range.a() == null ? -Float.MAX_VALUE : range.a();
		float high = range.b() == null ? Float.MAX_VALUE : range.b();
		return new FloatRange(low, high);
	}

	@Override
	public FunctionWrapper[] getFunction(CommandContext cmdCtx, String str) throws CommandSyntaxException {
		Collection<CustomFunction> customFuncList = ArgumentTag.a(cmdCtx, str);

		FunctionWrapper[] result = new FunctionWrapper[customFuncList.size()];

		CustomFunctionData customFunctionData = getCLW(cmdCtx).getServer().getFunctionData();
		CommandListenerWrapper commandListenerWrapper = getCLW(cmdCtx).a().b(2);

		int count = 0;

		for (CustomFunction customFunction : customFuncList) {
			@SuppressWarnings("deprecation")
			NamespacedKey minecraftKey = new NamespacedKey(customFunction.a().getNamespace(),
					customFunction.a().getKey());
			ToIntBiFunction<CustomFunction, CommandListenerWrapper> obj = customFunctionData::a;

			FunctionWrapper wrapper = new FunctionWrapper(minecraftKey, obj, customFunction, commandListenerWrapper,
					e -> {
						return (Object) getCLW(cmdCtx).a(((CraftEntity) e).getHandle());
					});

			result[count] = wrapper;
			count++;
		}

		return result;
	}

	@Override
	public IntegerRange getIntRange(CommandContext cmdCtx, String key) {
		net.minecraft.server.v1_16_R1.CriterionConditionValue.IntegerRange range = ArgumentCriterionValue.b.a(cmdCtx,
				key);
		int low = range.a() == null ? Integer.MIN_VALUE : range.a();
		int high = range.b() == null ? Integer.MAX_VALUE : range.b();
		return new IntegerRange(low, high);
	}

	@Override
	public ItemStack getItemStack(CommandContext cmdCtx, String str) throws CommandSyntaxException {
		return CraftItemStack.asBukkitCopy(ArgumentItemStack.a(cmdCtx, str).a(1, false));
	}

	@Override
	public Location getLocation(CommandContext cmdCtx, String str, LocationType locationType, CommandSender sender)
			throws CommandSyntaxException {
		switch (locationType) {
		case BLOCK_POSITION:
			BlockPosition blockPos = ArgumentPosition.a(cmdCtx, str);
			return new Location(getCommandSenderWorld(sender), blockPos.getX(), blockPos.getY(), blockPos.getZ());
		case PRECISE_POSITION:
			Vec3D vecPos = ArgumentVec3.a(cmdCtx, str);
			return new Location(getCommandSenderWorld(sender), vecPos.x, vecPos.y, vecPos.z);
		}
		return null;
	}

	@Override
	public Location2D getLocation2D(CommandContext cmdCtx, String key, LocationType locationType2d,
			CommandSender sender) throws CommandSyntaxException {
		switch (locationType2d) {
		case BLOCK_POSITION:
			BlockPosition2D blockPos = ArgumentVec2I.a(cmdCtx, key);
			return new Location2D(getCommandSenderWorld(sender), blockPos.a, blockPos.b);
		case PRECISE_POSITION:
			Vec2F vecPos = ArgumentVec2.a(cmdCtx, key);
			return new Location2D(getCommandSenderWorld(sender), vecPos.i, vecPos.j);
		}
		return null;
	}

	@SuppressWarnings("deprecation")
	@Override
	public LootTable getLootTable(CommandContext cmdCtx, String str) {
		MinecraftKey minecraftKey = ArgumentMinecraftKeyRegistered.e(cmdCtx, str);
		String namespace = minecraftKey.getNamespace();
		String key = minecraftKey.getKey();
//		LootItemCondition lootItemCondition = ArgumentMinecraftKeyRegistered.c(cmdCtx, str);
//		lootItemCondition.
//		String namespace = lootItemCondition.b();

		net.minecraft.server.v1_16_R1.LootTable lootTable = getCLW(cmdCtx).getServer().getLootTableRegistry()
				.getLootTable(minecraftKey);

//		CommandListenerWrapper clw = (CommandListenerWrapper) cmdCtx.getSource();
//		new LootTableInfo.Builder(clw.getWorld())
//			.setOptional(LootContextParameters.THIS_ENTITY, clw.getEntity())
//			.set(LootContextParameters.POSITION, var1)
//		
//		private static int a(CommandContext<CommandListenerWrapper> var0, MinecraftKey var1, b var2)
//				throws CommandSyntaxException {
//			CommandListenerWrapper var3 = (CommandListenerWrapper) var0.getSource();
//			LootTableInfo.Builder var4 = new LootTableInfo.Builder(var3.getWorld())
//					.setOptional(LootContextParameters.THIS_ENTITY, (Object) var3.getEntity())
//					.set(LootContextParameters.POSITION, (Object) new BlockPosition(var3.getPosition()));
//			return CommandLoot.a(var0, var1, var4.build(LootContextParameterSets.CHEST), var2);
//		}
//		
//		private static int a(CommandContext<CommandListenerWrapper> var0, MinecraftKey var12, LootTableInfo var2, b var3)
//				throws CommandSyntaxException {
//			CommandListenerWrapper var4 = (CommandListenerWrapper) var0.getSource();
//			LootTable var5 = var4.getServer().getLootTableRegistry().getLootTable(var12);
//			List var6 = var5.populateLoot(var2);
//			return var3.accept(var0, var6, var1 -> CommandLoot.a(var4, var1));
//		}

		return new CraftLootTable(new NamespacedKey(namespace, key), lootTable);
	}

	@Override
	public MathOperation getMathOperation(CommandContext cmdCtx, String key) throws CommandSyntaxException {
		ArgumentMathOperation.a result = ArgumentMathOperation.a(cmdCtx, key);
		net.minecraft.server.v1_16_R1.Scoreboard board = new net.minecraft.server.v1_16_R1.Scoreboard();
		ScoreboardScore tester_left = new ScoreboardScore(board, null, null);
		ScoreboardScore tester_right = new ScoreboardScore(board, null, null);

		tester_left.setScore(6);
		tester_right.setScore(2);
		result.apply(tester_left, tester_right);

		switch (tester_left.getScore()) {
		case 8:
			return MathOperation.ADD;
		case 4:
			return MathOperation.SUBTRACT;
		case 12:
			return MathOperation.MULTIPLY;
		case 3:
			return MathOperation.DIVIDE;
		case 0:
			return MathOperation.MOD;
		case 6:
			return MathOperation.MAX;

		case 2: {
			if (tester_right.getScore() == 6)
				return MathOperation.SWAP;
			tester_left.setScore(2);
			tester_right.setScore(6);
			result.apply(tester_left, tester_right);
			if (tester_left.getScore() == 2)
				return MathOperation.MIN;
			return MathOperation.ASSIGN;
		}
		}
		return null;
	}

	@Override
	public NBTContainer getNBTCompound(CommandContext cmdCtx, String key) {
		return new NBTContainer(ArgumentNBTTag.a(cmdCtx, key));
	}

	@Override
	public String getObjective(CommandContext cmdCtx, String key, CommandSender sender)
			throws IllegalArgumentException, CommandSyntaxException {
		return ArgumentScoreboardObjective.a(cmdCtx, key).getName();
	}

	@Override
	public String getObjectiveCriteria(CommandContext cmdCtx, String key) {
		return ArgumentScoreboardCriteria.a(cmdCtx, key).getName();
	}

	@Override
	public Particle getParticle(CommandContext cmdCtx, String str) {
		return CraftParticle.toBukkit(ArgumentParticle.a(cmdCtx, str));
	}

	@Override
	public Player getPlayer(CommandContext cmdCtx, String str) throws CommandSyntaxException {
		Player target = Bukkit.getPlayer(((GameProfile) ArgumentProfile.a(cmdCtx, str).iterator().next()).getId());
		if (target == null) {
			throw ArgumentProfile.a.create();
		} else {
			return target;
		}
	}

	@Override
	public PotionEffectType getPotionEffect(CommandContext cmdCtx, String str) throws CommandSyntaxException {
		return new CraftPotionEffectType(ArgumentMobEffect.a(cmdCtx, str));
	}

	@Override
	public ComplexRecipe getRecipe(CommandContext cmdCtx, String key) throws CommandSyntaxException {
		IRecipe<?> recipe = ArgumentMinecraftKeyRegistered.b(cmdCtx, key);
		return new ComplexRecipe() {

			@SuppressWarnings("deprecation")
			@Override
			public NamespacedKey getKey() {
				return new NamespacedKey(recipe.getKey().getNamespace(), recipe.getKey().getKey());
			}

			@Override
			public ItemStack getResult() {
				return recipe.toBukkitRecipe().getResult();
			}
		};
	}

	@Override
	public Rotation getRotation(CommandContext cmdCtx, String key) {
		IVectorPosition pos = ArgumentRotation.a(cmdCtx, key);
		Vec2F vec = pos.b(getCLW(cmdCtx));
		return new Rotation(vec.i, vec.j);
	}

	@Override
	public ScoreboardSlot getScoreboardSlot(CommandContext cmdCtx, String key) {
		return new ScoreboardSlot(ArgumentScoreboardSlot.a(cmdCtx, key));
	}

	@Override
	public Collection<String> getScoreHolderMultiple(CommandContext cmdCtx, String key) throws CommandSyntaxException {
		return ArgumentScoreholder.b(cmdCtx, key);
	}

	@Override
	public String getScoreHolderSingle(CommandContext cmdCtx, String key) throws CommandSyntaxException {
		return ArgumentScoreholder.a(cmdCtx, key);
	}

	@Override
	public CommandSender getSenderForCommand(CommandContext cmdCtx) {
		CommandSender sender = getCLW(cmdCtx).getBukkitSender();

		Entity proxyEntity = getCLW(cmdCtx).getEntity();
		if (proxyEntity != null) {
			CommandSender proxy = ((Entity) proxyEntity).getBukkitEntity();

			if (!proxy.equals(sender)) {
				sender = new ProxiedNativeCommandSender(getCLW(cmdCtx), sender, proxy);
			}
		}

		return sender;
	}

	@Override
	public SimpleCommandMap getSimpleCommandMap() {
		return ((CraftServer) Bukkit.getServer()).getCommandMap();
	}

	@Override
	public Sound getSound(CommandContext cmdCtx, String key) {
		MinecraftKey minecraftKey = ArgumentMinecraftKeyRegistered.e(cmdCtx, key);
		for (CraftSound sound : CraftSound.values()) {
			try {
				if (CommandAPIHandler.getField(CraftSound.class, "minecraftKey").get(sound)
						.equals(minecraftKey.getKey())) {
					return Sound.valueOf(sound.name());
				}
			} catch (IllegalArgumentException | IllegalAccessException e1) {
				e1.printStackTrace();
			}
		}
		return null;
	}

	@Override
	public SuggestionProvider getSuggestionProvider(SuggestionProviders provider) {
		switch (provider) {
		case FUNCTION:
			return (context, builder) -> {
				CustomFunctionData functionData = getCLW(context).getServer().getFunctionData();
				ICompletionProvider.a(functionData.g(), builder, "#");
				return ICompletionProvider.a(functionData.f(), builder);
			};
		case RECIPES:
			return CompletionProviders.b;
		case SOUNDS:
			return CompletionProviders.c;
		case ADVANCEMENTS:
			return (cmdCtx, builder) -> {
				Collection<Advancement> advancements = ((CommandListenerWrapper) cmdCtx.getSource()).getServer()
						.getAdvancementData().getAdvancements();
				return ICompletionProvider.a(advancements.stream().map(Advancement::getName), builder);
			};
		case LOOT_TABLES:
			return (context, builder) -> {
				LootTableRegistry lootTables = getCLW(context).getServer().getLootTableRegistry();
				return ICompletionProvider.a(lootTables.a(), builder);
			};
		case BIOMES:
			return CompletionProviders.d;
		case ENTITIES:
			return CompletionProviders.e;
		default:
			return (context, builder) -> Suggestions.empty();
		}
	}

	@Override
	public String getTeam(CommandContext cmdCtx, String key, CommandSender sender) throws CommandSyntaxException {
		return ArgumentScoreboardTeam.a(cmdCtx, key).getName();
	}

	@Override
	public int getTime(CommandContext cmdCtx, String key) {
		return (Integer) cmdCtx.getArgument(key, Integer.class);
	}

	@Override
	public boolean isVanillaCommandWrapper(Command command) {
		return command instanceof VanillaCommandWrapper;
	}

	@Override
	public void resendPackets(Player player) {
		CraftPlayer craftPlayer = (CraftPlayer) player;
		CraftServer craftServer = (CraftServer) Bukkit.getServer();
		net.minecraft.server.v1_16_R1.CommandDispatcher nmsDispatcher = craftServer.getServer().getCommandDispatcher();
		nmsDispatcher.a(craftPlayer.getHandle());
	}

	@Override
	public boolean validateMinecraftKeyRegistered(String argument) {
		try {
			StringReader reader = new StringReader(argument);
			ArgumentMinecraftKeyRegistered.a().parse(reader);
			return true;
		} catch (CommandSyntaxException e) {
			return false;
		}
	}

}