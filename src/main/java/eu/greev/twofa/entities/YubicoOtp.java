package eu.greev.twofa.entities;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class YubicoOtp {

    private String name;
    private String publicKey;

}
