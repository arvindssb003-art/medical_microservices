package com.arvind.delivery.service.impl;

import com.arvind.delivery.dto.DeliveryRequest;
import com.arvind.delivery.dto.DeliveryResponse;
import com.arvind.delivery.entity.Delivery;
import com.arvind.delivery.exception.DeliveryNotFoundException;
import com.arvind.delivery.repository.DeliveryRepository;
import com.arvind.delivery.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DeliveryServiceImpl implements DeliveryService {

    private final DeliveryRepository deliveryRepository;

    @Override
    @Transactional
    public DeliveryResponse createDelivery(DeliveryRequest request) {

        Delivery delivery = Delivery.builder()
                .orderId(request.orderId())
                .userId(request.userId())
                .address(request.address())
                .status("PENDING")
                .build();

        return toResponse(deliveryRepository.save(delivery));
    }

    @Override
    @Transactional(readOnly = true)
    public DeliveryResponse getDeliveryById(Long id) {

        Delivery delivery = deliveryRepository.findById(id)
                .orElseThrow(() ->
                        new DeliveryNotFoundException(
                                "Delivery not found: " + id
                        )
                );

        return toResponse(delivery);
    }

    @Override
    @Transactional(readOnly = true)
    public DeliveryResponse getDeliveryByOrderId(Long orderId) {

        Delivery delivery = deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() ->
                        new DeliveryNotFoundException(
                                "Delivery not found for order: " + orderId
                        )
                );

        return toResponse(delivery);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeliveryResponse> getDeliveriesByUserId(String userId) {

        return deliveryRepository.findByUserId(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public DeliveryResponse updateDeliveryStatus(
            Long id,
            String status) {

        Delivery delivery = deliveryRepository.findById(id)
                .orElseThrow(() ->
                        new DeliveryNotFoundException(
                                "Delivery not found: " + id
                        )
                );

        delivery.setStatus(status);

        return toResponse(deliveryRepository.save(delivery));
    }

    private DeliveryResponse toResponse(Delivery delivery) {

        return new DeliveryResponse(
                delivery.getId(),
                delivery.getOrderId(),
                delivery.getUserId(),
                delivery.getAddress(),
                delivery.getStatus(),
                delivery.getCreatedAt(),
                delivery.getUpdatedAt()
        );
    }
}