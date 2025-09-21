package app.focusx;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@Import(TestRsaKeyConfig.class)
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class ApplicationTests {

	@Test
	void contextLoads() {
	}

}
