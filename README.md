# Movecraft-Warfare Addon
![Warfare](https://github.com/APDevTeam/Movecraft-Warfare/actions/workflows/maven.yml/badge.svg)

Home of the code for the following features:
 - Siege
 - Assault

## Version support
The `legacy` branch is coded for 1.10.2 to 1.16.5 and Movecraft 7.x.

The `main` branch is coded for 1.14.4 to 1.16.5 and Movecraft 8.x.

## Download
Devevlopment builds can be found on the [GitHub Actions tab](https://github.com/APDevTeam/Movecraft-Warfare/actions) of this repository.

Stable builds can be found on [our SpigotMC page](https://www.spigotmc.org/resources/movecraft-warfare.87359/).

## Building
This plugin requires that the user setup and build their [Movecraft](https://github.com/APDevTeam/Movecraft), [Movecraft-Repair](https://github.com/APDevTeam/Movecraft-Repair), [Movecraft-Combat](https://github.com/TylerS1066/Movecraft-Combat), and [Movecraft-WorldGuard](https://github.com/APDevTeam/Movecraft-WorldGuard) development environments, and then clone this into the same folder as your Movecraft development environment such that both Movecraft-Warfare and Movecraft are contained in the same folder.  This plugin also requires you to build the latest version of 1.13.2 using build tools.

```
java -jar BuildTools.jar --rev 1.14.4
```

Then, run the following to build Movecraft-Warfare through `maven`.
```
mvn clean install
```
Jars are located in `/target`.


## Support
[Github Issues](https://github.com/APDevTeam/Movecraft-Warfare/issues)

[Discord](http://bit.ly/JoinAP-Dev)

The plugin is released here under the GNU General Public License v3.
