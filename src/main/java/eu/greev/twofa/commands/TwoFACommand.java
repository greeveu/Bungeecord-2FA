package eu.greev.twofa.commands;

import eu.greev.twofa.service.TwoFaService;
import eu.greev.twofa.utils.Language;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class TwoFACommand extends Command {
    private final TwoFaService service;
    private final Language language;


    public TwoFACommand(TwoFaService service, Language language) {
        super("2fa");

        this.service = service;
        this.language = language;
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
            case "addyubikey":
                if (args.length == 3 && args[1].length() <= 24) {
                    service.enableYubico(player, args[1], args[2]);
                } else {
                    player.sendMessage(new TextComponent(language.getYubikeyAddyubiusage()));
                }
                break;
            case "removeyubikey":
                if (args.length == 2 && args[1].length() <= 24) {
                    service.removeYubico(player, args[1]);
                } else {
                    player.sendMessage(new TextComponent(language.getYubikeyRemoveyubiusage()));
                }
                break;
            case "listyubikey":
                service.listYubico(player);
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
        }
    }
}
