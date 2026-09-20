package com.arvind.delivery.service;

import com.arvind.delivery.dto.DeliveryRequest;
import com.arvind.delivery.dto.DeliveryResponse;

import java.util.List;

public interface DeliveryService {

    DeliveryResponse createDelivery(DeliveryRequest request);

    DeliveryResponse getDeliveryById(Long id);

    DeliveryResponse getDeliveryByOrderId(Long orderId);

    List<DeliveryResponse> getDeliveriesByUserId(String userId);

    DeliveryResponse updateDeliveryStatus(Long id, String status);
}