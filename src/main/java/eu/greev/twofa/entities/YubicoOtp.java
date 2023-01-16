package eu.greev.twofa.entities;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class YubicoOtp {

    private String name;
    private String publicId;

    @Override
    public String toString() {
        return "YubicoOtp{" +
                "name='" + name + '\'' +
                ", publicKey='" + publicId + '\'' +
                '}';
    }
}
