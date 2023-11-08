package com.example.cashregister.controllers;

import com.example.cashregister.entities.Service;
import com.example.cashregister.repositories.ServiceRepository;
import com.example.cashregister.services.ServiceService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;

import static java.util.Objects.isNull;

@RestController
@RequestMapping(value = "/products")
public class ProductController {
    private static final String INVALID_ID_PRODUCT_MESSAGE = "invalid ID of product; must be more than 0";
    private static final String INVALID_ID_SERVICE_MESSAGE = "invalid ID of service; must be more than 0";
    private static final String NO_SUCH_SERVICE_MESSAGE = "no service with such ID is present";
    private static final String NO_SUCH_PRODUCT_MESSAGE = "no product with such ID is present";
    private static final String NO_AVAILABLE_SERVICES_MESSAGE = "no available services for search products";
    private static final String ERROR_IN_SERVER_MESSAGE = "error in server with id: ";
    private static final String ERROR_FOR_CONNECTION_TO_SERVICE_MESSAGE = "error of connection to service with id: ";
    @Autowired
    private ServiceService serviceService;

    @GetMapping
    public ResponseEntity<JsonNode> getProducts(){
        JsonNode jsonNode = checkForNormalityOutputOfServicesAndGetProducts();
        return ResponseEntity.ok(jsonNode);
    }
    @GetMapping(value = "byId")
    public ResponseEntity<JsonNode> getProduct(@RequestParam("id_service") int id_service,
                                               @RequestParam("id_products") int id_product){
        validateProductId(Integer.valueOf(id_product));
        validateServiceId(Integer.valueOf(id_service));
        JsonNode jsonNode = checkForNormalityOutputOfServiceAndGetProductById(id_product, id_service);
        return ResponseEntity.ok(jsonNode);
    }
    private List<Service> checkForPresentAndGetServices() {
        List<Service> services = serviceService.findAll();
        if (isNull(services)) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, NO_AVAILABLE_SERVICES_MESSAGE);
        }
        return services;
    }

    private void validateProductId(Integer productId) {
        if (isNull(productId) || productId < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, INVALID_ID_PRODUCT_MESSAGE);
        }
    }
    private void validateServiceId(Integer serviceId) {
        if (isNull(serviceId) || serviceId < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, INVALID_ID_SERVICE_MESSAGE);
        }
    }

    private Service checkForPresentAndGetService(int id_service){
        return serviceService.findById(id_service).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, NO_SUCH_SERVICE_MESSAGE));
    }
    private JsonNode checkForNormalityOutputOfServiceAndGetProductById(int id_product, int id_service){
        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper objectMapper = new ObjectMapper();
        Service service = checkForPresentAndGetService(id_service);
        String url = service.getUrl() + "/" + service.getUrl_for_get_product_by_id() + "/" + id_product;
        HttpHeaders headers = new HttpHeaders();
        headers.set("auth", service.getToken());
        System.out.println(service.getToken());
        System.out.println(url);
        String response;
        JsonNode jsonNode;
        try {
            response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class).getBody();
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            JsonNode jex = null;
            try {
                jex = objectMapper.readTree(ex.getResponseBodyAsString());
            } catch (IOException ignored) {}
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, jex.get("message").asText());
        }
        try {
            jsonNode = objectMapper.readTree(response);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return jsonNode;
    }


    private JsonNode checkForNormalityOutputOfServicesAndGetProducts() {
        List<Service> services = checkForPresentAndGetServices();
        ObjectMapper objectMapper = new ObjectMapper();
        ArrayNode arrayNode = objectMapper.createArrayNode();
        JsonNode jsonNode = null;
        try {
            jsonNode = objectMapper.readTree("{}");
        } catch (IOException ignored) {
        }
        ObjectNode errorElement = objectMapper.createObjectNode();
        for (Service service : services) {
            try {
                jsonNode = getProductsFromService(service);
            } catch (ResponseStatusException e) {
                errorElement.put("error_message", ERROR_FOR_CONNECTION_TO_SERVICE_MESSAGE + service.getId());
                ((ObjectNode) jsonNode).set("error", errorElement.get("error_message"));
            } catch (RuntimeException e) {
                errorElement.put("error_message", ERROR_IN_SERVER_MESSAGE + service.getId());
                ((ObjectNode) jsonNode).set("error", errorElement.get("error_message"));
            }
            arrayNode.add(jsonNode);
        }
        return arrayNode;
    }
    private JsonNode getProductsFromService(Service service) {
        RestTemplate restTemplate = new RestTemplate();
        String url = service.getUrl() + "/" + service.getUrl_for_get_all_products();
        HttpHeaders headers = new HttpHeaders();
        headers.set("auth", service.getToken());
        System.out.println(service.getToken());
        System.out.println(url);
        String response;
        try {
            response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class).getBody();
            //response = restTemplate.getForObject(url, String.class);
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode;
        try {
            jsonNode = objectMapper.readTree(response);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ArrayNode arrayNode = objectMapper.createArrayNode();
        ObjectNode newElement = objectMapper.createObjectNode();
        newElement.put("id_service", 1);
        for (JsonNode jN : jsonNode) {
            ((ObjectNode) jN).set("id_service", newElement.get("id_service"));
        }
        return jsonNode;
    }
}
