package com.example.orders;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ResponseBody;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SpringBootApplication
public class OrdersApplication {

	public static void main(String[] args) {
		SpringApplication.run(OrdersApplication.class, args);
	}

}

record Order(Integer id, Integer customerId) {}

@Controller
@ResponseBody
class OrdersController {
	private final Map<Integer, List<Order>> db = new ConcurrentHashMap<>();

	public OrdersController() {
		for (int customerId = 0; customerId < 8; customerId++) {
			var orderList = new ArrayList<Order>();
			for (int orderId = 0; orderId < (Math.random() * 100); orderId++) {
				orderList.add(new Order(orderId, customerId));
			}
			this.db.put(customerId, orderList);
		}
	}

	@MessageMapping("orders.{customerId}")
	Flux<Order> getOrdersByCustomerId(@DestinationVariable Integer customerId) {
		 var orders = this.db.getOrDefault(customerId, List.of());
		 return Flux.fromIterable(orders);
	}
}