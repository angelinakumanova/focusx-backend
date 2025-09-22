package app.focusx;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;


@SpringBootTest
@Import({TestRsaKeyConfig.class})
@ActiveProfiles("test")
class ApplicationTests {

	@Test
	void contextLoads() {
	}

}
