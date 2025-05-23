package com.example.demo;

// import javax.crypto.SecretKey;

// import java.util.Base64;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
// import io.jsonwebtoken.SignatureAlgorithm;
// import io.jsonwebtoken.io.Encoders;
// import io.jsonwebtoken.security.Keys;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableJpaAuditing
@EnableScheduling
@SpringBootApplication
public class PrimaryApplication {

	public static void main(String[] args) {
		SpringApplication.run(PrimaryApplication.class, args);

		// SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS512);
		// String base64Key = Encoders.BASE64.encode(key.getEncoded());
		// System.out.println(base64Key);
	}
}
