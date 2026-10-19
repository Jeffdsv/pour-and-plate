package se.svent.tasting;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class TastingServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
