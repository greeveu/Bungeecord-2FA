package eu.greev.twofa.entities;

import eu.greev.twofa.utils.TwoFactorState;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class UserData {

    private TwoFactorState status;
    private String secret;
    private Set<YubicoOtp> yubiOtp = new HashSet<>();
    private String lastIpHash;

    @Override
    public String toString() {
        return "UserData{" +
                "status=" + status +
                ", secret='" + secret + '\'' +
                ", yubiOtp=" + yubiOtp +
                ", lastIpHash='" + lastIpHash + '\'' +
                '}';
    }
}
