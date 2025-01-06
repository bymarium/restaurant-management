package com.example.restaurant.services.order;

import com.example.restaurant.dtos.OrderDTO;
import com.example.restaurant.models.Client;
import com.example.restaurant.models.Order;
import com.example.restaurant.models.OrderDetail;
import com.example.restaurant.repositories.IClientRepository;
import com.example.restaurant.repositories.IOrderRepository;
import com.example.restaurant.services.interfaces.ICommandParametrized;
import com.example.restaurant.services.orderdetail.CreateOrderDetail;
import com.example.restaurant.utils.OrderConverter;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CreateOrder implements ICommandParametrized<Order, OrderDTO> {
	private final IOrderRepository orderRepository;
	private final CreateOrderDetail createOrderDetail;
	private final IClientRepository clientRepository;

	public CreateOrder(IOrderRepository orderRepository, CreateOrderDetail createOrderDetail, IClientRepository clientRepository) {
		this.orderRepository = orderRepository;
		this.createOrderDetail = createOrderDetail;
		this.clientRepository = clientRepository;
	}

	@Override
	public Order execute(OrderDTO orderDTO) {
		Order order = OrderConverter.convertDtoToEntity(orderDTO);
		Client client = clientRepository.findById(orderDTO.getClientId()).orElseThrow(() -> new RuntimeException("Cliente con id " + orderDTO.getClientId() + " no encontrado"));
		order.setClient(client);
		Order newOrder = orderRepository.save(order);
		List<OrderDetail> orderDetails = createOrderDetail.execute(orderDTO.getOrderDetails(), newOrder.getId());
		Float totalPrice = (float) orderDetails.stream().mapToDouble(OrderDetail::getSubTotal).sum();
		newOrder.setTotalPrice(totalPrice);
		orderRepository.save(newOrder);
		newOrder.setOrderDetails(orderDetails.stream().map(details -> {
			details.setDish(null);
			return details;
		}).toList());
		return newOrder;
	}
}