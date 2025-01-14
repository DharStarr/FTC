
# FTC Challenges Documentation
# challenges.json
The `plugins/ForTheCrown/challenges/challenges.json` file is where all challenges are loaded from. This section documents the format this file uses to read challenges.
  
First of all, it's a JSON object file, where each entry's key is the ID they will be registered with and where the values are the challenge data. The following is a list of valid keys for challenge data, optional entries will have an `*` after their name.  
  
Some of the possible values accepted in the challenges json are based on 'streak-based values', which means they can dynamically change for each users based on their current streak for the challenge in question.  
  
Accepted inputs for streak-based values are:  
**Fixed value**  
The returned value is a constant which doesn't change
```json
"rhines": 200
```
**Scalar-based value**  
Which calculates the reward value like so: `reward = base * (streak * scalar)`.
Normally, a player's streak begins at 0, however, to ensure this value stays 
functional, if the given streak is 0, it is incremented to 1.
```json
"rhines": { "base": 200, "scalar": 0.5 }
```
**Array-based value**

These will get the value by treating a user's streak as an index to an array of rewards, if a user's streak is higher than the array size,
the index is clamped and the final reward is used. In the following example, a streak of 0 days would return 200, a streak of 1 day
would return 400 and so on
```json
"rhines": [ 200, 300, 400, 600, 1200 ]
```
  
---
### `displayName`
This is the name that's used to display the challenge to users in chat and the challenge book.
This can be either a string or a more complex Chat Component.
  
---
### `description`*
An array of elements that's used as a description of the challenge, displayed in the `displayName` hover event. As such, it's accepted input is similar to `displayName`. If no value is set for this, then the description will simply be empty.
  
---
### `reward`*
Specifies data of rewards given to a user when they complete the challenge, accepts the following entries:
- `guildExp`: The amount of guildExp given to users that complete the challenge, if user is not a guild, no exp is given (streak-based).
- `rhines`: Rhine reward given to the player (streak-based).
- `gems`: Gem reward given to the player (streak-based).
- `item`: Item given to the player upon completion.
- `claimScript`: The script called when a player claims the reward, the system will call the `onRewardClaim` method with the user as the parameter

If no value is set for this, then no reward will be given.
  
---
### `goal`*
A streak-based value specifying how many points are required to complete a challenge, If no goal is set, it defaults to `1`
  
---
### `type`*
Specifies the frequency at which the challenge is reset, accepts one of the following inputs:
- `daily`: Challenge is reset once a day
- `weekly`: Challenge is reset every monday
- `manual`: Challenge will not be selected nor reset by itself, requires code or staff input to make this challenge active.

If no value is specified, then this defaults to `daily`  
  
---
### `script`*
The filename of the script responsible for handling the challenge logic.
Examples of valid input:
- `script_name.js`
- `directory/script_name.js`

If this value is not set, no script is used. See the Scripts section for more info on scripts.
  
---
### `eventClass`
The fully qualified class name of the event this challenge listens for.  
If no script is given, then this class must be a sub-class of `PlayerEvent`, otherwise the challenge handler will not be able to get the player from the event.  
  
If a script is given, but no event handling method is specified AND the event is not a sub-class of `PlayerEvent`, then this system requires that the script specifies a `getPlayer` method which takes the event as input and returns the event's player object.  
  
If this value is unset or left as `"custom"`, then this challenge will never be called from an ingame event, and will instead require to be manually triggered by FTC's plugin.

# Scripting
Script files are stored in `plugins/ForTheCrown/scripts` as `.js` files.  
  
Note: Be aware that script files are reloaded when they are deactivated/activated, so any data stored in the scripts themselves will be lost.

## ChallengeHandle
Before talking about any implementation details, You should know about the `ChallengeHandle` class. This class allows for giving rewards.

Class methods:

---
```java
void givePoint(Object player);
```
**Description**:  
Gives a single point to the player.
  
**Params**:
- `player`: The player object, can be a player, UUID, User, or the player's name.
---
```java
void givePoints(Object player, float points);
```
**Description**:  
Gives the player the given amount of points.  
  
**Params**:
- `player`: The player object, can be a player, UUID, User, or the player's name.
- `points`: The amount of points to give the player.
---
```java
boolean hasCompleted(Object player);
```
**Description**:  
Tests if the given player has completed this handle's challenge.  
  
**Params**:
- `player`: The player object, can be a player, UUID, User, or the player's name.
## Implementation methods
This section specifies methods that can be implemented by challenge scripts
```js
// Called to handle the event logic for this challenge,
// If this method is not specified then the player in the
// event is simply given 1 point.
// If the challenge has a 'custom' event class, the first
// parameter will be an arbitrary object that's given as
// input by the discretion of whoever is writing this lol.
// 
// Params:
// event - The event this challenge is listening to
// handle - The challenge's ChallengeHandle
function onEvent(event, handle);

// This will only be called if onEvent() has not been specified,
// This gets the player in the given event, if this method is not
// specified then the system will attempt to get the player automatically,
// by casting it to a PlayerEvent, if that doesn't work, it fails.
// Params:
// event - The event to get the player of.
// 
// Return: The player event's player
function getPlayer(event);

// Tests if the given User object can complete the challenge
// Params:
// user - The user to test
//
// Return: True, if the user is allowed to complete the 
// 		   challenge, false otherwise
function canComplete(user);

// Called when the player completes this challenge
// Params:
// user - The User that completed this challenge
function onComplete(user);

// Called when the challenge's listeners are registered
// and it becomes 'active' 
// Included for the reason that during the daily reset,
// challenges may be changed, reset, new ones added, and
// this acts as a callback during that time.
// As an example, this is used in `on_join.js` to give
// everyone that's online the challenge.
// 
// Params:
// handle - Challenge handle
function onActivate(handle);

// Called when the challenge is reset
// Params:
// handle - ChallengeHandle for the challenge
function onReset(handle);


```


## Available java classes
The scripting engine automatically contains the following is a full list of java classes that can be used in the same way as you would in Java:
- `org.bukkit.Bukkit`
- `org.bukkit.Location`
- `org.bukkit.enity.EntityType`
- `org.bukkit.Material`
- `net.kyori.adventure.text.Component`
- `net.kyori.adventure.text.format.NamedTextColor`
- `net.kyori.adventure.text.event.ClickEvent`
- `net.kyori.adventure.text.event.HoverEvent`
- `net.kyori.adventure.text.format.Style`
- `net.kyori.adventure.text.format.TextDecoration`
- `net.forthecrown.utils.text.Text`
- `net.forthecrown.utils.Util`
- `net.forthecrown.utils.Cooldowns`
- `net.forthecrown.utils.inventory.ItemStacks`
- `net.forthecrown.utils.math.Bounds3i`
- `net.forthecrown.utils.math.WorldVec3i`
- `net.forthecrown.utils.math.WorldBounds3i`
- `net.forthecrown.utils.math.Vectors`
- `net.forthecrown.user.Users`

Any other classes must be imported using the `Java.type()`, as an example, We can import the `List<E>` class:
```js
var List = Java.type("java.util.List");
var createdList = new List(12);
```
If you attempt to access a class without first importing it using the above shown method, it will cause errors as the script engine will not be able to find the class.  
  
Ontop of this, the script engine also comes with a builtin `Log4J` logger which you can call like so:
```js
logger.info("Logger message, argument: {}", argument);
```
## Extra info
For extra info on the functioning of the challenges system or FTC's script system abstractions, please see the source code included in this repository.  
  
For further info on the `JavaScript` engine used, please see [The nashorn GitHub repository](https://github.com/openjdk/nashorn) or the [Nashorn JavaScript Engine Javadoc](https://www.javadoc.io/doc/org.openjdk.nashorn/nashorn-core)
