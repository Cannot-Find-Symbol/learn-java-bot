[discord-invite]: https://discord.gg/HedMWfZ
[discord-shield]: https://discordapp.com/api/guilds/702854497000751166/widget.png?style=shield
[![Build Status](http://ci.learn-java.org:8080/buildStatus/icon?job=Cannot-Find-Symbol%2Flearn-java-bot%2Fmaster)](http://ci.learn-java.org:8080/job/Cannot-Find-Symbol/job/learn-java-bot/job/master/)[ ![discord-shield][] ][discord-invite]

#Summary
This is the server bot for the learn-java discord. This bot is free to be worked on by anyone, and if you'd like to join and helpout click the discord shield above.

#Requirements
* JDK 11 or newer
* Maven
* Git
* MariaDB


#Setup

1. Clone the repo ```git clone https://github.com/Cannot-Find-Symbol/learn-java-bot.git```
2. Install MariaDB and setup a database for the bot to use
3. edit application.properties.example in `src\main\resources` and rename to application.properties
4. run the bot using `mvn spring-boot:run`, if you wish to package it run `mvn package` and execute using `java -jar jarfile`

