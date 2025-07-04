package app.focusx.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;

import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Configuration
@Profile("!test")
public class RsaKeyConfig {

    @Value("${jwt.public-key}")
    private String publicKey;

    @Bean
    public RSAPublicKey rsaPublicKey() throws Exception {
        String key = publicKey.replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s+", "");

        byte[] decoded = Base64.getDecoder().decode(key);

        X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
        KeyFactory kf = KeyFactory.getInstance("RSA");

        return (RSAPublicKey) kf.generatePublic(spec);
    }
}
