# IMS Bridge Mod for Fabric

IMS Bridge is a Minecraft mod designed for IMS guilds and their members. It facilitates communication between Discord and the in-game guild chat.

- Key authentication and the Hypixel API is used to verify users are members of an IMS guild
- The mod only reads guild messages, which it sends to Discord.
- Users only see client-side messages, they are not sent through a bridge bot like most other bridges do.
- A completely separate combined bridge chat can be used by all members of IMS guilds to communicate with each other both in-game and through Discord.

## Installation

Download the latest .jar file from [releases](https://github.com/Ozmeyham/ImsBridgeMod-1.21.5/releases)
and place it in your mods folder. This is the Fabric version for Minecraft 1.21 and up, and requires
both Fabric and Java to be installed. You additionally need to place the [Fabric API](https://wiki.fabricmc.net/install) in your mods folder. 

For the latest Forge release (for Minecraft 1.8.9) see [here](https://github.com/Ozmeyham/ImsBridgeMod-1.8.9).
## Configuration

Once you have installed the mod, you will need to obtain a bridge key via ```/key``` on Discord.
This key is your bridge "password" and it is important that you do not accidentally send this in guild chat or share it with anyone.

Once you have your bridge key, you can run ```/bridge key <key>``` in-game (replace \<key\> with your bridge key). You should then be connected to the server and can test this with /bridge online.

You can run ```/bridge help``` and ```/cbridge help``` to see available commands for using and configuring the mod.

If you very recently joined an IMS guild and receive an error when attempting to obtain your key, wait 10 minutes for the server to update your guild membership.

## Commands
```/cbridge toggle``` and ```/bridge toggle``` - Enables/disables combined bridge and regular bridge message rendering respectively, similar to ```/guild toggle```.

```/cbc <msg>``` or ```/bc <msg>``` - Sends a single message to cbridge, similar to how ```/gc <msg>``` works.

```/cbridge chat``` or ```/cbc``` or ```/bc``` - Enters/exits cbridge chat, similar to how ```/chat g``` works. This allows users to send messages in cbridge without having to use a prefix command.

```/cbridge colour <tagColour> <nameColour> <msgColour>``` and ```/bridge colour <tagColour> <nameColour> <msgColour>``` - Changes the colour formatting of combined bridge and regular bridge messages respectively. All default Minecraft colours are available in plain-text format.

```/cbridge colour``` and ```/bridge colour``` - Resets the colour formatting back to default.

```/cbridge party <playerCap> <reason>``` - Sends a message in cbridge to say you have an open party, and lets other people join by typing ```!join <yourIGN>```

```/bridge key <key>``` - Allows the user to input their UUID bridge key for authentication to use bridge.

```/bridge online``` or ```/bl``` - Lists all online guild members using the mod. Useful for using cbridge to talk to specific people in other guilds.

```/cbridge help``` and ```/bridge help``` - Displays a help message for the above commands.

## Discord & Support 

Join the [IMS Bridge Testing](https://discord.gg/7QYYeKsfNu) server for support with any issues, feedback or suggestions. Contributions are always welcome.