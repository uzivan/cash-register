package com.example.cashregister.controllers;

import com.example.cashregister.dto.OrderDto;
import com.example.cashregister.dto_validation.groups.OnCreate;
import com.example.cashregister.dto_validation.groups.OnUpdate;
import com.example.cashregister.entities.Order;
import com.example.cashregister.entities.OrderInService;
import com.example.cashregister.entities.Service;
import com.example.cashregister.services.OrderInServiceService;
import com.example.cashregister.services.OrderService;
import com.example.cashregister.services.ServiceService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.io.IOException;
import java.net.ConnectException;
import java.util.*;

import static java.util.Objects.isNull;

@RestController
@RequestMapping(value = "/orders")
public class OrderController {
    private static final Class<OnCreate> CREATE_OPTION = OnCreate.class;
    private static final Class<OnUpdate> UPDATE_OPTION = OnUpdate.class;
    private static final String ERROR_IN_SERVICE_MESSAGE = "error in service number: ";
    private static final String NO_SUCH_SERVICE_MESSAGE = "no service with such ID is present";
    private static final String NO_SUCH_ORDER_MESSAGE = "no order with such ID is present";
    private static final String INVALID_ID_MESSAGE = "invalid ID; must be more than 0";
    private static final String NO_PROPERTIES_TO_UPDATE_MESSAGE = "no properties to update in request body";
    private static final String ERROR_IN_SERVER_MESSAGE = "error in server with id: ";
    private static final String ERROR_FOR_CONNECTION_TO_SERVICE_MESSAGE = "error of connection to service with id: ";
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    @Autowired
    private OrderService orderService;
    @Autowired
    private ServiceService serviceService;
    @Autowired
    private OrderInServiceService orderInServiceService;

    @GetMapping
    public ResponseEntity<List<Order>> showAll() {
        return ResponseEntity.ok(orderService.findAll());
    }

    @PostMapping(value = "/new")
    public ResponseEntity<Order> createOrder(@RequestBody OrderDto orderDto) {
        validateOrderDto(orderDto, UPDATE_OPTION);
        JsonNode jsonNode = checkForPresentAndDoOrders(orderDto.getProductId());
        System.out.println(jsonNode.toString());
        CheckForError(jsonNode);
        ProductsIdChangesToOrderId(jsonNode, orderDto);
        Order order = mapToOrderAndSave(orderDto);
        return ResponseEntity.ok(order);
    }

    private void CheckForError(JsonNode jsonNode) {
        for (JsonNode jn : jsonNode) {
            if (jn.get("error") != null) {
                canselOfOrder(jsonNode);
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, String.valueOf(jn.get("error")));
            }
        }
    }

    private void canselOfOrder(JsonNode jsonNode) {
        for (JsonNode jn : jsonNode) {
            if (jn.get("service_id") != null) {
                Service service = serviceService.findById(Integer.parseInt(String.valueOf(jn.get("service_id")))).get();
                RestTemplate restTemplate = new RestTemplate();
                String url = service.getUrl() + "/" + service.getUrl_for_get_order_by_id();
                try {
                    restTemplate.delete(url);
                } catch (HttpClientErrorException | HttpServerErrorException ex) {
                    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
        }
    }

    @GetMapping(value = "/{id}")
    private ResponseEntity<Order> getOrder(@PathVariable("id") int id) {
        validateOrderId(id);
        return orderService.findById(id).map(ResponseEntity::ok).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, NO_SUCH_ORDER_MESSAGE));
    }
//    @PutMapping(value="/{id}")
//    private ResponseEntity<Order> updateOrder(@PathVariable("id") int id, @RequestBody OrderDto orderDto){
//        validateOrderId(id);
//        validateOrderDto(orderDto, UPDATE_OPTION);
//        Order order = checkForPresentAndGetOrder(id);
//        updateOrders(order, orderDto);
//        orderService.save(order);
//        return ResponseEntity.ok(order);
//    }
//    @DeleteMapping(value="{/id}")
//    private ResponseEntity<Order> deleteOrder(@PathVariable("id") int id){
//        validateOrderId(id);
//        Order order = checkForPresentAndGetOrder(id);
//        orderService.delete(order);
//        return ResponseEntity.ok().build();
//    }

    private void ProductsIdChangesToOrderId(JsonNode jsonNode, OrderDto orderDto) {
        for (JsonNode jN : jsonNode) {
            Integer key = Integer.valueOf(String.valueOf(jN.get("service_id")));
            Integer[] value = {Integer.valueOf(String.valueOf(jN.get("id")))};
            orderDto.getProductId().put(key, value);
        }
    }

    private void validateOrderDto(OrderDto orderDto, Class<?> option) {
        if (isNull(orderDto)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    NO_PROPERTIES_TO_UPDATE_MESSAGE);
        }

        if (isNull(option) || !option.equals(UPDATE_OPTION)) {
            option = CREATE_OPTION;
        }

        Set<ConstraintViolation<OrderDto>> violations = validator.validate(orderDto, option);

        if (!violations.isEmpty()) {
            StringBuilder str = new StringBuilder();
            for (ConstraintViolation<OrderDto> violation : violations) {
                str.append(violation.getPropertyPath()).append(": ").append(violation.getMessage()).append("; ");
            }
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, str.toString());
        }
    }

    private Order mapToOrderAndSave(OrderDto orderDto) {
        Order order = new Order();
        order.setClient_name(orderDto.getClient_name());
        //orderService.save(order);
        List<OrderInService> orderInServices = new ArrayList<>();
        Set<Map.Entry<Integer, Integer[]>> entries = orderDto.getProductId().entrySet();
        for (Map.Entry<Integer, Integer[]> entry : entries) {

            OrderInService orderInService = new OrderInService();

            Integer id_service = entry.getKey();
            Integer id_order = entry.getValue()[0];

            orderInService.setId_order_in_service(id_order);
            orderInService.setService(checkForPresentAndGetService(id_service));
            orderInService.setOrder(order);
            //orderInService = orderInServiceService.save(orderInService);
            orderInServices.add(orderInService);
        }
        order.setOrderInServices(orderInServices);
        return orderService.save(order);
    }

    private JsonNode checkForPresentAndDoOrders(Map<Integer, Integer[]> products_from_services) {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = null;
        ArrayNode arrayNode = objectMapper.createArrayNode();

        Set<Map.Entry<Integer, Integer[]>> entries = products_from_services.entrySet();
        for (Map.Entry<Integer, Integer[]> entry : entries) {
            Service service = checkForPresentAndGetService(entry.getKey());
            try {
                jsonNode = objectMapper.readTree("{}");
            } catch (IOException ignored) {
            }
            try {
                jsonNode = doOrder(service, entry.getValue());
            } catch (ResourceAccessException e) {
                ObjectNode element = objectMapper.createObjectNode();
                element.put("error", ERROR_FOR_CONNECTION_TO_SERVICE_MESSAGE + service.getId());
                ((ObjectNode) jsonNode).set("error", element.get("error"));
                products_from_services.remove(service.getId());
            } catch (HttpClientErrorException e) {
                JsonNode jsonNode_error = null;
                try {
                    jsonNode_error = objectMapper.readTree(e.getResponseBodyAsString());
                } catch (IOException ignored) {
                }
                ObjectNode element = objectMapper.createObjectNode();
                element.put("error", jsonNode_error.get("message"));
                ((ObjectNode) jsonNode).set("error", element.get("error"));
                products_from_services.remove(service.getId());
            } catch (RuntimeException e) {
                ObjectNode element = objectMapper.createObjectNode();
                element.put("error", ERROR_IN_SERVICE_MESSAGE + service.getId());
                ((ObjectNode) jsonNode).set("error", element.get("error"));
                products_from_services.remove(service.getId());
            }
            arrayNode.add(jsonNode);
        }
        return arrayNode;
    }

    private JsonNode doOrder(Service service, Integer[] id_products) {
        RestTemplate restTemplate = new RestTemplate();

        JsonNode jsonNodeOrder = getJsonForDoOrder(id_products, service);

        String url = service.getUrl() + "/" + service.getUrl_for_get_order_by_id();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<JsonNode> requestEntity = new HttpEntity<>(jsonNodeOrder);
        ResponseEntity<String> responseEntity;
        responseEntity = restTemplate.postForEntity(url, requestEntity, String.class);
        return addOrderIdAndConvertToJson(responseEntity.getBody(), service.getId());
    }

    private JsonNode addOrderIdAndConvertToJson(String jsonWithoutId, Integer id) {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = null;
        try {
            jsonNode = objectMapper.readTree(jsonWithoutId);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ERROR_IN_SERVICE_MESSAGE);
        }
        ObjectNode element = objectMapper.createObjectNode();
        element.put("id", id);
        ((ObjectNode) jsonNode).set("service_id", element.get("id"));
        return jsonNode;
    }

    private JsonNode getJsonForDoOrder(Integer[] id_products, Service service) {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = null;
        try {
            jsonNode = objectMapper.readTree("{}");
        } catch (IOException ignored) {
        }
        ObjectNode element = objectMapper.createObjectNode();
        element.put("time", "2021-06-30T15:30:00");
        ((ObjectNode) jsonNode).set("orderedTime", element.get("time"));
        element.put("status", "prepare");
        ((ObjectNode) jsonNode).set("status", element.get("status"));
        ArrayNode arrayNodeOfIdProducts = objectMapper.createArrayNode();
        for (Integer i : id_products) {
            arrayNodeOfIdProducts.add(i);
        }
        element.put("pizzaId", arrayNodeOfIdProducts);
        ((ObjectNode) jsonNode).set(service.getName_for_id_list(), element.get("pizzaId"));
        return jsonNode;
    }

    private Service checkForPresentAndGetService(int id_service) {
        return serviceService.findById(id_service).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, NO_SUCH_SERVICE_MESSAGE));
    }

    private void validateOrderId(Integer orderId) {
        if (isNull(orderId) || orderId < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, INVALID_ID_MESSAGE);
        }
    }
}
