# Entity & player arguments

## Entity selector argument

Minecraft's [target selectors](https://minecraft.gamepedia.com/Commands#Target_selectors) (e.g. `@a` or `@e`) are implemented using the `EntitySelectorArgument` class. This allows you to select specific entities based on certain attributes.

The `EntitySelectorArgument` constructor requires an `EntitySelector` argument to determine what type of data to return. There are 4 types of entity selections which are available:

* `EntitySelector.ONE_ENTITY` - A single entity, which returns a `Entity` object.
* `EntitySelector.MANY_ENTITIES`  - A collection of many entities, which returns a `Collection<Entity>` object.
* `EntitySelector.ONE_PLAYER` - A single player, which returns a `Player` object.
* `EntitySelector.MANY_PLAYERS` - A collection of players, which returns a `Collection<Player>` object.

The return type is the type to be cast when retrieved from the `Object[] args` in the command declaration.

<div class="example">

### Example - Remove entities command

Say we want a command to remove certain types of entities. Typically, this would be implemented using a simple command like:

```
/remove <player>
/remove <mob type>
/remove <radius>
```

Instead, we can combine all of these into one by using the `EntitySelectorArgument`. We want to be able to target multiple entities at a time, so we want to use the `EntitySelector.MANY_ENTITIES` value in our constructor. We can simply retrieve the `Collection<Entity>` from this argument and iteratively remove each entity:

```java
{{ #include examples/5.6entityselector.java }}
```

We could then use this to target specific entities, for example:

* To remove all cows:
  ```
  /remove @e[type=cow]
  ```
* To remove the 10 furthest pigs from the command sender:
  ```
  /remove @e[type=pig,limit=10,sort=furthest]
  ```

</div>

-----

## Player argument

The `PlayerArgument` class is very similar _(almost identical)_ to `EntitySelectorArgument`, with the EntitySelector `ONE_PLAYER`. It also allows you to select a player based on their UUID.

> **Developer's Note:** 
>
> I've not tested the `PlayerArgument` enough to recommend using it over the `EntitySelectorArgument(EntitySelector.ONE_PLAYER)`. There may be other advantages to using this than the regular EntitySelectorArgument, but as of writing this documentation, I know not of the advantages nor disadvantages to using this argument type. Internally, the `PlayerArgument` uses the `GameProfile` class from Mojang's authlib, which may be able to retrieve offline players (untested).
>
> _(Of course, if anyone is able to confirm any major differences between the `PlayerArgument` and the `EntitySelectorArgument(EntitySelector.ONE_PLAYER)`, I would be more than happy to include your findings in the documentation. If so, feel free to make a documentation amendment [here](https://github.com/JorelAli/1.13-Command-API/issues/new/choose).)_

-----

## Entity type argument

The `EntityTypeArgument` class is used to retrieve a type of entity as defined in the [`EntityType`](https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/entity/EntityType.html) enum. In other words, this is an entity type, for example a pig or a zombie.

<div class="example">

### Example - Spawning entities

Say we want a command to spawn a specific type of entity, similar to the `/summon` command in Vanilla Minecraft, with the addition of specifying how many entities to spawn. We want to create a command of the following form:

```
/spawnmob <entity> <amount>
```

Since we're trying to specify an entity type, we will use the `EntityTypeArgument` as our argument type for `<entity>`. We combine this with the `IntegerArgument` class with a specified range of \\( 1 \le \textit{amount} \le 100 \\):

```java
{{ #include examples/5.6entitytype.java }}
```

Note how in this example above, we have to explicitly state `Player player, Object[] args`. This is due to a limitation of Java's type inference system which is discussed [here](./commandregistration.md#setting-the-commands-executor).

</div>