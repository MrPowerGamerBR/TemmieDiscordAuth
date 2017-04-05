# TemmieDiscordAuth
<p align="center"><img height="200" src ="http://i.imgur.com/bBTjK4H.png"></img></p>

## A simple, straightforward, Discord Authentication (via OAuth2) API for Java
This was created for [Loritta](https://github.com/MrPowerGamerBR/Loritta), because I really wanted to have a fancy bot administration panel.

Anyway, before you spent countless hours trying to figure out wtf is the "authentication code", here's a small tutorial that you will need to do before trying to use TemmieDiscordAuth.

First, open your Discord Application Config (where you created your bot) and add a URL callback (must be a valid URL & you need to be able to get the GET responses on it!), after that, edit this URL:

```
https://discordapp.com/oauth2/authorize?redirect_uri={YOUR URL CALLBACK}&scope=identify%20guilds&response_type=code&client_id={YOUR BOT CLIENT ID}
```

You can get your client ID on the application config page.

After doing that, try using your URL you created, if it was done correctly, your browser will auto redirect to your callback URL and it will append "&code=YOUR AUTHENTICATION CODE" to the URL, that "YOUR AUTHENTICATION CODE" is your... well... authentication code. :P

Now use that on the TemmieDiscordAuth class and have fun!

### Login
**Code:**
```
TemmieDiscordAuth temmie = new TemmieDiscordAuth("your authentication code here", "your callback URL here", "your client ID here", "your client token here");
temmie.doTokenExchange(); // ALWAYS do an token exchange before using any of the methods in TemmieDiscordAuth!
```
### Get User Info
**Code:**
```
temmie.getCurrentUserIdentification();
```
### Get User Guilds
**Code:**
```
for (TemmieGuild guild : temmie.getUserGuilds()) {
    System.out.println(guild.getName());
}
```

___

Simple as that, have fun!

### Maven
You can use TemmieDiscordAuth with Maven by using Jitpack. (sorry, I don't have a maven repo yet :cry:)
```
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```
```
<dependency>
    <groupId>com.github.MrPowerGamerBR</groupId>
    <artifactId>TemmieDiscordAuth</artifactId>
    <version>-SNAPSHOT</version>
</dependency>
```
### Dependencies
Gson

[HttpRequest](https://github.com/kevinsawicki/http-request) by @kevinsawicki

lombok

### Why Temmie?
Why not Temmie?
