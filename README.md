# Jeeves

A bot for Discord based on the Discord4J library (https://github.com/austinv11/Discord4J).

Through a number of modules Jeeves has a certain focus on the game Elite: Dangerous, but can also provide useful functions for Discord servers unrelated to the game.

For Elite: Dangerous related Discord servers it offers access to the EDSM and ED Materializer APIs.

Jeeves' more general functions include things welcoming new users, handling of roles or announcements of streams.

This is the second incarnation of Jeeves, with the first one (https://github.com/unrealization/FGEBot) being a fork of the FGEBot (https://github.com/JBHemloque/FGEBot), written in JavaScript.

# Installation

Jeeves uses Maven (https://maven.apache.org/) in order to pull in dependencies.

Run `mvn package` to build the JAR.

# Configuration

In order to operate Jeeves will require you to set up an application with a bot user on the Discord developer site (https://discordapp.com/developers/applications/me).

Once that is done you need to create the file `clientConfig.xml` containing your bot's token.

See `clientConfig.xml.sample` for an example.
