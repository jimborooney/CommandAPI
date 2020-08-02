# Block predicate arguments

The `BlockPredicateArgument` is used to represent a "test" for Minecraft blocks. This can consist of tags, such as the ones listed [here on the MinecraftWiki](https://minecraft.gamepedia.com/Tag#Blocks), or individual blocks. If a block matches the tag or block, then the predicate is satisfied.

For example, if we were to use the predicate `#leaves`, then the following blocks will be satisfied by that predicate: `jungle_leaves`, `oak_leaves`, `spruce_leaves`, `dark_oak_leaves`, `acacia_leaves`, `birch_leaves`.

When used, this argument must be casted to a `Predicate<Block>`. As with other similar arguments with parameterized types, you can ignore Java's unchecked cast type safety warning.

<div class="example">

### Example - Replacing specific blocks in a radius

Say you want to replace blocks in a given radius. To do this, we'll use the following command structure:

```
/replace <radius> <fromBlock> <toBlock>
```

Of course, we could simply use a `BlockStateArgument` or even an `ItemStackArgument` as our `<fromBlock>` in order to get the material to replace, but the block predicate argument provides a test for a given block, which if satisfied, allows certain code to be executed. 

First, we declare our arguments. We want to use the `BlockPredicateArgument` since it also allows us to use Minecraft tags to identify blocks, as well as individual blocks. We then use `BlockStateArgument` to set the block to a given type. The `BlockStateArgument` also allows the user to provide any block data (e.g. contents of a chest or a stair's orientation).

```java
LinkedHashMap<String, Argument> arguments = new LinkedHashMap<>();
arguments.put("radius", new IntegerArgument());
arguments.put("fromBlock", new BlockPredicateArgument());
arguments.put("toBlock", new BlockStateArgument());
```

We then register our `/replace` command. First, we parse the arguments making sure to cast to `Predicate<Block>` and `BlockData` (and not `BlockState`). After that, we use a few simple for loops to find the blocks within a radius sphere from the player.

In our most nested loop, we can then check if the block meets the requirements of our predicate. This is simply performed using `predicate.test(block)`, and if satisfied, we can set the block's type.

Lastly, we register our command as normal using the `register()` method.

```java
new CommandAPICommand("replace")
.withArguments(arguments)
.executesPlayer((player, args) -> {
	
	// Parse the arguments
	int radius = (int) args[0];
	@SuppressWarnings("unchecked")
	Predicate<Block> predicate = (Predicate<Block>) args[1];
	BlockData blockData = (BlockData) args[2];
	
	// Find a (solid) sphere of blocks around the player with a given radius
	Location center = player.getLocation();
	for (int Y = -radius; Y < radius; Y++) {
		for (int X = -radius; X < radius; X++) {
			for (int Z = -radius; Z < radius; Z++) {
				if (Math.sqrt((X * X) + (Y * Y) + (Z * Z)) <= radius) {
					Block block = center.getWorld().getBlockAt(X + center.getBlockX(), Y + center.getBlockY(), Z + center.getBlockZ());
                    
                    // If that block matches a block from the predicate, set it
                    if(predicate.test(block)) {
                        block.setType(blockData.getMaterial());
						block.setBlockData(blockData);
                    }
				}
			}
		}
	}
    return;
})
.register();
```

</div>