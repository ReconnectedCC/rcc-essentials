package cc.reconnected.essentials.core;

import cc.reconnected.essentials.RccEssentials;
import cc.reconnected.essentials.struct.PlayerMail;
import cc.reconnected.library.text.Placeholder;

import java.util.List;
import java.util.UUID;

public class MailManager {
    public static void sendMail(UUID playerUuid, PlayerMail mail) {
        var playerState = RccEssentials.state.getPlayerState(playerUuid);
        playerState.mails.add(mail);
        RccEssentials.state.savePlayerState(playerUuid, playerState);
    }

    public static List<PlayerMail> getMailList(UUID playerUuid) {
        var playerState = RccEssentials.state.getPlayerState(playerUuid);
        return playerState.mails.stream().toList();
    }

    public static boolean deleteMail(UUID playerUuid, int index) {
        var playerState = RccEssentials.state.getPlayerState(playerUuid);
        if(index < 0 || index >= playerState.mails.size()) {
            return false;
        }
        playerState.mails.remove(index);
        RccEssentials.state.savePlayerState(playerUuid, playerState);
        return true;
    }

    public static void clearAllMail(UUID playerUuid) {
        var playerState = RccEssentials.state.getPlayerState(playerUuid);
        playerState.mails.clear();
        RccEssentials.state.savePlayerState(playerUuid, playerState);
    }
}
