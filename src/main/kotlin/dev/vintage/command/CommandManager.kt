package dev.vintage.command

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

class CommandManager : CommandExecutor, TabCompleter {

    private val commands = mutableMapOf<String, dev.vintage.command.Command>()

    init {
        commands["achievements"] = AchievementCommand()
        commands["join"] = JoinCommand()
        commands["leave"] = LeaveCommand()
        commands["list"] = ListCommand()
        commands["party"] = PartyCommand()
        commands["admin"] = AdminCommand()
        commands["cosmetics"] = CosmeticsCommand()
        commands["emote"] = EmoteCommand()
        commands["history"] = HistoryCommand()
        commands["leaderboard"] = LeaderboardCommand()
        commands["reload"] = ReloadCommand()
        commands["selecttrail"] = SelectTrailCommand()
        commands["setlobby"] = SetLobbyCommand()
        commands["createarena"] = CreateArenaCommand()
        commands["stats"] = StatsCommand()
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (args.isEmpty()) return false
        val sub = args[0].lowercase()
        return commands[sub]?.execute(sender, args.drop(1).toTypedArray()) ?: false
    }

    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<String>): List<String> {
        if (args.size == 1) {
            return commands.keys.filter { it.startsWith(args[0], ignoreCase = true) }
        }
        val sub = args[0].lowercase()
        return commands[sub]?.tabComplete(sender, args.drop(1).toTypedArray()) ?: emptyList()
    }
}