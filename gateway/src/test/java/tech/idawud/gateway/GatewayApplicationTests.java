package tech.idawud.gateway;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class GatewayApplicationTests {

	@BeforeEach
	void setup() {
		System.setProperty("ORDERS_SERVICE_HOST", "orders");
		System.setProperty("ORDERS_SERVICE_PORT", "8081");
		System.setProperty("CUSTOMERS_SERVICE_HOST", "customers");
		System.setProperty("CUSTOMERS_SERVICE_PORT", "8080");
	}

	@Test
	void contextLoads() {
		
	}

}
