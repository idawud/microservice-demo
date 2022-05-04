package tech.idawud.gateway.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import reactor.core.publisher.Flux;
import tech.idawud.gateway.models.CustomerOrder;
import tech.idawud.gateway.services.CrmClient;

@Controller
@ResponseBody
class CrmRestController {
    private final CrmClient client;

    @Autowired
    CrmRestController(CrmClient client) {
        this.client = client;
    }

    @GetMapping("/cos")
    Flux<CustomerOrder> getCustomerOrders() {
        return this.client.getCustomerOrders();
    }
}
