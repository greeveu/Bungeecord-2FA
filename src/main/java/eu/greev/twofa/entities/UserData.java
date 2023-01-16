package eu.greev.twofa.entities;

import eu.greev.twofa.utils.TwoFactorState;
import lombok.Data;

@Data
public class UserData {

    private TwoFactorState status;
    private String secret;
    private String yubiOtp;
    private String lastIpHash;

}
