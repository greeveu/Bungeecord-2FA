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
import java.util.stream.Collectors;

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
        if (!(sender instanceof ProxiedPlayer)) {
            return;
        }

        ProxiedPlayer player = (ProxiedPlayer) sender;

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
            case "enable":
                service.enableTFA(player);
                break;
            case "disable":
                service.disableTFA(player);
                break;
            case "logout":
                service.logout(player);
                break;
            case "yubikey":
                if (args.length >= 2) {
                    findYubiKeySubCommand(args, player);
                } else {
                    player.sendMessage(new TextComponent(language.getMissingCode()));
                }
                break;
            case "activate":
                if (args.length == 2) {
                    service.activate(player, args[1]);
                } else {
                    player.sendMessage(new TextComponent(language.getMissingCode()));
                }
                break;
            default:
                player.sendMessage(new TextComponent(language.getHelpMessage()));
                break;
        }
    }

    private void findYubiKeySubCommand(String[] args, ProxiedPlayer player) {
        switch (args[1].toLowerCase()) {
            case "add":
                if (args.length == 4 && args[2].length() <= 24) {
                    service.enableYubico(player, args[2], args[3]);
                } else {
                    player.sendMessage(new TextComponent(language.getYubikeyAddyubiusage()));
                }
                break;
            case "remove":
                if (args.length == 3 && args[2].length() <= 24) {
                    service.removeYubico(player, args[2]);
                } else {
                    player.sendMessage(new TextComponent(language.getYubikeyRemoveyubiusage()));
                }
                break;
            case "list":
                service.listYubico(player);
                break;
            default:
                player.sendMessage(new TextComponent(language.getHelpMessage()));
                break;
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
            return Arrays.stream(subCommands).filter(s -> s.startsWith(args[0])).collect(Collectors.toList());
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("yubikey")) {
            return Arrays.stream(yubikeySubCommands).filter(s -> s.startsWith(args[1])).collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
}
