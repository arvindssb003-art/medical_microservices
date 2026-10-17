package com.arvind.delivery.service;

import com.arvind.delivery.dto.DeliveryRequest;
import com.arvind.delivery.dto.DeliveryResponse;

import java.util.List;

public interface DeliveryService {

    DeliveryResponse createDelivery(DeliveryRequest request, String userId);

    DeliveryResponse getDeliveryById(Long id, String userId);

    DeliveryResponse getDeliveryByOrderId(Long orderId, String userId);

    List<DeliveryResponse> getDeliveriesByUserId(String userId);

    DeliveryResponse updateDeliveryStatus(Long id, String status);
}