package eu.greev.twofa.commands;

import eu.greev.twofa.service.TwoFaService;
import eu.greev.twofa.utils.Language;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.Arrays;
import java.util.Collections;

public class TwoFACommand extends Command implements TabExecutor {
    private final TwoFaService service;
    private final Language language;

    private final String[] subCommands;
    private final String[] yubikeySubCommands;

    public TwoFACommand(TwoFaService service, Language language) {
        super("2fa");

        this.service = service;
        this.language = language;

        subCommands = new String[]{"enable", "disable", "logout", "yubikey", "activate"};
        yubikeySubCommands = new String[]{"add", "list", "remove"};
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ProxiedPlayer player)) {
            return;
        }

        if (!player.hasPermission("2fa.use")) {
            return;
        }

        if (args.length == 0) {
            player.sendMessage(new TextComponent(language.getHelpMessage()));
            return;
        }

        findSubCommand(args, player);
    }

    private void findSubCommand(String[] args, ProxiedPlayer player) {
        switch (args[0].toLowerCase()) {
            case "enable" -> service.enableTFA(player);
            case "disable" -> service.disableTFA(player);
            case "logout" -> service.logout(player);
            case "yubikey" -> {
                if (args.length >= 2) {
                    findYubiKeySubCommand(args, player);
                } else {
                    player.sendMessage(new TextComponent(language.getMissingCode()));
                }
            }
            case "activate" -> {
                if (args.length == 2) {
                    service.activate(player, args[1]);
                } else {
                    player.sendMessage(new TextComponent(language.getMissingCode()));
                }
            }
            default -> player.sendMessage(new TextComponent(language.getHelpMessage()));
        }
    }

    private void findYubiKeySubCommand(String[] args, ProxiedPlayer player) {
        switch (args[1].toLowerCase()) {
            case "add" -> {
                if (args.length == 4 && args[2].length() <= 24) {
                    service.enableYubico(player, args[2], args[3]);
                } else {
                    player.sendMessage(new TextComponent(language.getYubikeyAddyubiusage()));
                }
            }
            case "remove" -> {
                if (args.length == 3 && args[2].length() <= 24) {
                    service.removeYubico(player, args[2]);
                } else {
                    player.sendMessage(new TextComponent(language.getYubikeyRemoveyubiusage()));
                }
            }
            case "list" -> service.listYubico(player);
            default -> player.sendMessage(new TextComponent(language.getHelpMessage()));
        }
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        if (!sender.hasPermission("2fa.use")) {
            return Collections.emptyList();
        }

        if (args.length == 0) {
            return Arrays.asList(subCommands);
        }

        if (args.length == 1) {
            return Arrays.stream(subCommands).filter(s -> s.startsWith(args[0])).toList();
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("yubikey")) {
            return Arrays.stream(yubikeySubCommands).filter(s -> s.startsWith(args[1])).toList();
        }

        return Collections.emptyList();
    }
}
