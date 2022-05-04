package tech.idawud.gateway.models;

import java.util.List;

public record CustomerOrder(Customer customer, List<Order> orders) {
}
