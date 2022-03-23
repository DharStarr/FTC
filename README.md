# FTC 
The ForTheCrown GitHub repository.

### Projects
- **Core**: contains most of the code and features FTC has
- **Enchantments**: Holds and manages FTC's custom enchantments
### Event projects
The Events directory has the code for most of the Crown Events prior to March 2022. 
- **AprilEvent**: April 2021 Easter egg hunt event
- **AugustEvent**: August 2021 Pinata event
- **BigAssCrownEvent**: December 2020 christmas event
- **DummyEvent**: April 1st 2021 event, Running track event, total disaster
- **Easter**: April 2020 Easter event, get Harold some stuff for a reward
- **Halloween**: October 2020 event, custom cave world where players had to get points by killing mobs.
- **JulyEvent**: July 2021 event, parkour track with tridents
- **MarchEvent**: March 2021 event, PvP arena where players in teams of 2 fought against eachother
- **MayEvent**: May 2021 event, CoD zombies arena with waves of monsters
- **MazeGenerator**: February 2021 event, random maze you had to run through with 3 bombs
- **NetherEvent**: August 2020 event, custom nether world you had to collect items to get points in.
- **Posh_D**: January 2022 event, Parkour event organized mostly by Posh_D
- **RaceEvent**: Race event where players had to run across an obstacle course in the shortest time possible.
## Core's packages
All the core's package start with ``net.forthecrown`` so to find any of these packages just add the package name given here onto the end of the FTC package lol  
  
**I apologise for any errors in this documentation, I wrote this in like an hour while high on a coffee rush**
### vars
This package contains classes and functions relating to Var's, a type of global variable that is automatically serialized and deserialized, all Vars can also be changed via the ``/var`` command.  
You can create var's easily:
````java
// The last argument is a default value, if the variable does not yet exist
// then the default value becomes the value, if it does exist, it will simply
// be the value returned if the actual value of the variable is null
Var<Integer> integerVar = Var.def("variable_name", VarTypes.INT, 4);
````
You can disable var serialization with a simple ``Var.setTransient(boolean);``. If you need to listen to the var's value being changed you can use ``Var.setUpdateListener(Consumer<T>)``. Finally, to get the value of a var, call ``Var.get()``, this will return either the value of the var, or the default value if the actual value is null, if you want it to return a different def value, call ``Var.getValue(T def)``, if you want to manually update the value of the variable you can use ``Var.update(T)`` or ``Var.set(T)`` if you don't want the update listener to be called.  
  
Each var requires a ``VarType<T>``, these are stored as constants in the ``VarTypes`` class. Var types should only be used as static final constants as they need to be registered in the ``Registries.VAR_TYPES``
### utils.math
The math util package contains the block vectors that FTC uses. There's 2 vector classes used by FTC, the difference between them is that one is a simple vector implementation with an x, y and z components. While the other also hold a ``World`` component, meaning it's world-bound. These 2 vector's are ``Vector3i`` and ``WorldVec3i``  
  
The package also holds 2 similarly structured bounding box classes, they again have the same difference. ``Bounds3i`` is a normal bounding class while ``WorldBounds3i`` is world-bound, but this allows the bounds to have a lot more functionality which includes a ``BlockIterator`` for iterating through each block within the bounding box, while the regular Bounds3i only has a ``VectorIterator``. Not to mention the world-bound bounding box has functions for easily getting entities that are inside the bounding box.
### util.transformation
This holds classes for copy pasting regions and the now depracated ``FtcBoundingBox`` class. The ``RegionCopyPaste`` can essentially be seen as a beta version of the ``net.forthecrown.structure.BlockStructure`` class lol
### util.world
This holds 2 classes:
- The ``WorldLoader``: a class which loads a world, it uses the world's WorldBorder to determine how much to load. The chunk loading is done async. The WorldLoader is used by the End resetter and RW resetter.
- The ``WorldReCreator``: which takes a world, deletes the original, and then recreates it with similar world properties
### usables
Usables are objects which hold both ``UsageAction``s and ``UsageCheck``s.  
The types of usable objects are UsableEntity's, UsableBlock's, Kits and Warps. The first two hold both UsageActions and UsageChecks, while the last 2 only hold checks.  
  
All UsageChecks and UsageActions, which I'll combine into UsageObjects for the sake of being brief, must specify a way to serialize an instance of the UsageObject and a way to parse command input into an instance of the UsageObject.
### utils
There's honestly too much diverse stuff in here to talk about in a single section here. I'll try to summarize the most important classes:
- ``FtcUtils``: Holds a lot of generic utility functions for stuff.
- ``Cooldown``: allows for placing ``CommandSender`` objects in cooldown in specific categories or in a general category.
- ``VanillaAccess``: Allows for access to the vanilla versions of Bukkit objects, honestly this just exists because typing ``VanillaAccess.getEntity(Entity)`` is easier to type than ``((CraftEntity) entity).getHandle()``
- ``TickSequence``: Allows for the creation of a sequence of events executed after a tick delay, the tick delay of each execution node is relative to the last node in the sequence.
  
A word of warning, the loot package is worthless and I don't know why I haven't deleted it lol, it's implementation is a thousand times more limited than vanilla's
### user
Holds **everything** about the ``CrownUser`` class used by FTC for data and functions on the players that play on here.  
You can get a user by simply doing: 
````java
Player p = /* Get a player somehow, idk */;
CrownUser user = UserManager.getUser(p);
````
For a second, I'll talk about how user caching works. We have a class called ``UserCache`` which is mostly used for lookups like name -> profile, or nickname -> profile, but it also serves the important purpose of telling us which user profile's can have CrownUser objects, because if a player has never been on the server then it does not have a cache entry. This can be important if you have a OfflinePlayer object that you might need the data for, if that OfflinePlayer has not played before, they have no data and thus no cache entry. Since there's no cache entry, it can't create crown user instance. You have no idea how happy I am that this finally exists, this used to be a problem lol.  
**-CACHING RANT OVER-**  
  
**UserManager** is a class which holds the ``UserSerializer`` and ``UserActionHandler`` instances, as well as storing data of user alt accounts in a Alt2Main UUID Map and provides method for checking alts.  
  
The serializer is a simple class that is meant to serialize a user's instance while the action handler is a class that exists because I didn't know where else to handle the data of a user's actions like sending/reading mail, sending a normal or marriage DM or even handling marriage actions like getting married or divorced (lmao). When I wrote this class I also had a worry that in the future I may need to call on these actions and have them handled without recreating the code that runs the action so the action handler interface seemed logical.  
  
Allow me to quickly state that ``FtcUser`` and ``CrownUser`` are 2 different classes, FtcUser is the implementation of the CrownUser interface, that's the difference.
### structure
Holds classes that allow for the creation and placement of structures into a world. A structure's placement into the world can be modified and transformed with ``BlockProcessor``s and ``EnityProcessor``s.  
**Placing** a structure can be done so:
````java
BlockStructure struct = /* Get the structure somehow */;
Vector3i placePos = /* Placement position */;
BlockPlacer placer = BlockPlacer.world(world);
StructurePlaceContext context = new StructurePlaceContext(struct, placePos, placer)
		// These lines can be ignored if you don't wish to have enitities placed
		.setEnityPlacer(EntityPlacer.world(world))
		.placeEntities(true)
		.addEmptyEntityProcessor()
		// The following relate to placing blocks
		// You can add any processor you want here
		.addEmptyProcessor()
		.addProcessor(new BlockTransformProcessor())

// Places the structure
struct.place(context);
````
**Processors** modify how an entity or block is processed, it's an interface with one method:
````java
@Nullable BlockPlaceData process(BlockPallette, BlockPalette.StateData, StructurePlaceContext, BlockPlaceData);
````
The **block palette** is a list of block states of the same properties and block type. **StateData** represents a single instance in the palette list, it has a ``Vector3i`` which holds position of the state within the structure. If the state is a block entity there will also be a ``CompoundTag`` for the NBT data. **StructurePlaceContext** is obviously just the context of the structure being placed. The last parameter **BlockPlaceData** is the result of the previous processor. The return value of the function is the data the block will be placed at, it holds the ``BlockState`` that will be placed along with the tag and absolute position.  
  
The entity processor is very similar to the block processor, so I'll skip it lol.  
 A ``StructureTransform`` is a functional interface that modifiess the absolute placement position of a block, it takes an input of a start pos, offset, pivot, mirror and rotation and combines these given input into a single absolute vector position.  
  
Structure's can be scanned in from a world, however that's best done with the ``/ftcstruct`` command.  
  
All structure's are stored in ``Registries.STRUCTURES`` from which they are serialized and deserialized.  
### registry
Registries are constants that hold some kind of object with a ``NamespacedKey`` as a key, we use registries to make the job of serializing and deserializing objects easier, because if we have a common key type that we can write as a string that links to a certain registry's entry, we won't have to serialize an entire object, just a key to that object.  
  
There are 2 types of registries, normal and closeable registries. Closeable registries can be closed, or frozen, so they can't be modified after being frozen. This is required for registries like the cosmetic registries as modification after initialization might cause issues with the inventory GUIs linked to them.
  
Registry instances are stored as constants in the Registries class.
### regions
This package just holds the classes and functions that manage ``PopulationRegion``s. Each region has a ``RegionPos`` that lets the ``RegionManager`` know where the region is located.  
  
PopulationRegion itself extends an abstract class called ``RegionData`` which has 2 implementations: ``PopulationRegion`` and ``RegionData.Empty``. These 2 implementations are used by the ``RegionPoleGenerator`` for the signs which display neighbouring regions. 
### inventory
This package contains 2 classes I love a lot. ``ItemStackBuilder`` because of how easy this makes the creation of ItemStack objects of any type with any type of data.  
  
And ``ItemStacks`` which holds utility methods for ItemStack objects, it also has the life saving ``ItemStacks.isEmpty(ItemStack)`` method, because I can never understand what the heck is going with item stacks in bukkit, sometimes they're null, other times they're just items with 0 amount or ``Material.AIR`` as their type, it's weird, that utility method checks for all of that.
### inventory.weapon
This package is home to the classes and functions of the ``RoyalSword``, its goals, upgrades and abilities. ``RoyalWeapons`` is the general utility class for creating and checking for royal swords.
### inventory.crown
The lesser brother to ``net.forthecrown.inventory.weapon``, this holds classes and functions for the Royal Crown.
### inventory.builder
This holds the ``InventoryBuilder`` object to make creating inventories easy. You can create an inventory like so:
````java
BuiltInventory inv = new InventoryBuilder(54)
		.title(Component.text("Inventory title :D")
		.build();
````
You can open the created inventory like so:
````java
CrownUser user = /* Get a user somehow */;
inv.open(user);
````
The rest is handled for you!  
This inventory system uses ``InventoryOption``s to allow for the user to interact with the inventory's buttons, everything in the inventory is technically an option, the border around the edge? An option! The single item that a player can click on? An option!  
There's 2 ways to use the option interface, one being the normal implementation of the ``InventoryOption`` interface which requires you to specify a ``int`` as an inventory slot. The other implementation is the ``CordedInventoryOption``, more words, I know. But this uses a ``InventoryPos`` instead of a simple int slot. It makes it more understandable and creation easier and more understandable for others.  
  
The options require you to also specify an item creation function and an interaction option that is called when the player clicks in the slot or position specified by the option.  
These look like so:
````java
void place(FtcInventory, CrownUser);
void onClick(CrownUser, ClickContext) throws CommandSyntaxException;
````
Note that while the ``place`` method allows you put the item anywhere in the inventory, the ``onClick`` function will only be called if the user clicks on the slot or positition stated by the option.  
  
While the ``ClickContext`` holds data relating to the inventory click it also allows you to modify the click's result with the following functions:
````java
// Allows you to tell the inventory to close itself
// after the click is completed
void setShouldClose(boolean);

// Allows you to specify the amount of ticks the user
// will be stopped from clicking on the the option
void setCooldownTime(int)

// Allows you to cancel the click event
// note: this is automatically set to 'true'
void setCancelEvent(boolean)

// Allows you to tell the inventory to reopen itself so it
// refresh the GUI with potentially new data, 'false' by default
void setReloadInventory(boolean)
````
### economy
Oh boy, this is going to be the most painful part...
``Economy`` and ``FtcEconomy`` are the same class, the second is just the implementation of the first. They are what allow us to modify and get the balances of users on the server. They use a backing ``BalanceMap`` to store the balances.  
  
We currently make use of the ``SortedBalanceMap`` implementation to store the balance, this map is constantly kept sorted by the balance's value. While balance lookup and modification performance suffers slightly as a result, this means that ``/baltop`` loads in an instant.  
  
Say you wanted to get the **price of an item**, for that you would use the `ItemPriceMap` which you can easily access with `Crown.getPriceMap()`.
### economy.shops
This is the package that contains everything related to `SignShop`s. So... let's talk about the organized mess that are SignShops :(  
  
First of all, SignShop's are an interface, their implementation is ``FtcSignShop``. Each shop holds a ``ShopInventory`` instance, this is obviously where the shop stores its stock. Now, the entire shop system is based around an `exampleItem`, an item which a shop holds as an example to do business by. The action of trading items in a sign shop is actually a ruse, the items are removed from the inventory yes, but they aren't then given to the customer, rather, the customer is given a clone of the example item, or vice versa, in the case of sell shops.  
  
The way a shop interaction occurs is through a `ShopInteraction` interface which is specified for each shop by its `ShopType` type. These interactions have 2 phases to them: testing and transaction. In the test phase the interaction tests a given ``SignShopSession`` if it can interact with the shop, it can test things like if the customer has the required balance to use the shop or enough free space in their inventory. If it passes these phases it moves on the transaction phase. In this phase the shop runs its actual logic, aka, giving the shop owner the money gained, removing the item from the shop and so forth.
  
The `SignShopSession` is a simple data class which lets you easily access some details of a shop, the user of a shop isn't a `CrownUser`, well, it can be, but the interface used by shops is the `ShopCustomer` interface.
  
Each sucessfull session (Tests passed, transaction carried through completely) is added to the shop's history so the shop owner could later view the history. The session doesn't end right after the transaction is complete, but rather around 3 seconds later, this is so the owner isn't spammed with messages about people using their shop and that the shop history wouldn't be flooded with entries.  
  
SignShop creation and management is handled by the `ShopManager` class, which is implemented by `FtcShopManager`.  
  
Each shop holds a `LocationFilename` as an identifier for it, this is the `world_156_67_894` file name representation  
  
**`Shop ownership`** is the class that defines who owns a shop, currently its implementation is very basic. The class itself allows for a `House` or a `UUID` player to own a shop with co owners. But in practice, only the single `UUID` player ownership is implemented.
  
**Manually using shops**. If you wanna use a shop with a custom shop customer implementation you can do so with the following code:
```java
Block shopBlock = /* Get a block somehow */;
ShopCustomer customer = /* :shrug: */;

// Check if the block is a shop
if (!ShopManager.isShop(shopBlock, true)) return;

ShopManager manager = Crown.getShopManager();
SignShop shop = manager.getShop(shopBlock.getLocation());

// The isShop() check above can only check the block's tag
// If an error has occurred and the shop's data has been deleted
// that check cannot do anything
if (shop == null) return;

// Run the interaction
manager.getInteractionHandler().handleInteraction(shop, customer, Crown.getEconomy());
```
### economy.selling
SellShop classes and functions. It uses the `SellResult` class to handle most of it.
### economy.market
This package holds the classes and functions (God damn I've used that exact same description for like every package here) for the markets in Hazelguard. For clarification, a market is the shop a player can purchase and own and place sign shops inside, while a shop, is the SignShop itself.  
  
It should be noted that I was stoned out of mind... or something... when I wrote this, because the Market system lacks any trace of object oriented programming. The `MarketShop` interface is a pure data holding class any functionality is found within the `Markets` class which you can access with `Crown.getMarkets()`. I think I was just larping as `C` or something lol  
  
Anyway, i hate the Market system but I also see no reason to rewrite it.
### economy.houses and economy.guilds
Neither of these packages are implemented within the server, guilds is finished, but untested. Houses aren't even finished.
### dungeons
Fuck no, too much work to describe the mess that the Dungeons system is
### cosmetics
Contains everything relating to the cosmetic stuff on FTC. Not much to really add here, Wout wrote the travel effects and it shows, cuz it's better than 98% of what I've written lol.
### core
The core of FTC. I'll just quickly list and describes what each class here does. Note: Will skip any classes that are simply implementations of API interfaces.

- `AfkKicker`: Tracks each player and kick them after they've been afk for longer than the `afkKickDelay` var specifies
- `BootStrap`: Initializes the FTC plugin
- `Crown`: The center and main class for FTC, pretty much any part of FTC can be accessed with the static getter methods in this class, for example: `Crown.getEconomy()` lets you use the economy.
- `DayChange`: Listens to the change of the day and then calls all it's registered listeners
- `DayChangeListener`: the listener type `DayChange` uses.
- `EndOpener`: Handles the End. It will close the end on the first of each month and open it 7 days before the current month ends. And it will quietly reset the End while the end is closed
- `FtcConfig`: Represents the JSON config FTC uses
- `FtcDynmap`: Allows for FTC to interface with Dynmap, however Dynmap is a piece of garbage :>
- `FtcFlags`: Holds and registers FTC's WorldGuard flags
- `FtcVars`: Holds ~~all~~ most of FTC's global var's as constants
- `Keys`: A couple utility methods relating to `NamespacedKey`s
- `Kingship`: Tracks the current king and their title, is serialized into the FTC config
- `Permissions`: Holds constants for permissions used in FTC
- `ResourceWorld`: Holds data for the RW's regeneration and such.
- `Worlds`: Holds constants and easy getter functions the worlds you'll find on FTC
Every other package in the core directory is pretty much useless ngl except for:
### core.chat
Chat holds a lot of required utility functions and classes when it comes to dealing with MC Chat's and Kyori's Adventure API.  
  
The two classes I've used the most often here are `FtcFormatter` which provides a lot of utility functions for formatting strings and components and providing display functions for stuff, like the very useful `FtcFormatter.itemDisplayName(ItemStack)` which returns an chat component that can be easily displayed to a human, I love this method if you can't tell.  
  
The other class I've found myself using a weird amount of times is `TimePrinter` this is a `ComponentPrinter` class that prints a time interval, eg: `4 days, 2 hours, 3 minutes and 4 seconds`.  
  
If you find yourself having to scan a user's input for banned words or anything you can use `BannedWords.checkAndWarn(CommandSender, Component/String)`, it will check if the input contains any banned words and warn the sender of the input and return true if it does.
### commands
Commands contains, well, commands. I've started using the class naming formula of `Command{command_name}` . Other than that, there's nothing to note here, but in:
### commands.manager
This class contains most notably the `FtcCommands` class which is used to initialize FTC's custom argument types and load all of FTC's commands. If you add any new custom argument types or commands, load and register them here.  
  
As well, classes like `FtcExceptionProvider` provide a very easy way to create `CommandSyntaxExceptions`. And `FtcSuggestionProvider` Also helps with suggestions.
