package com.codeGroup;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class PortfolioApplicationTests {

    @Test
    void contextLoads() {
        // Garante que todo o wiring (beans, JPA, seguranca) sobe corretamente.
    }
}
