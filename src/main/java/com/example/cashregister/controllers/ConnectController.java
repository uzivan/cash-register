package com.example.cashregister.controllers;

import com.example.cashregister.entities.Service;
import com.example.cashregister.services.OrderService;
import com.example.cashregister.services.ServiceService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.client.HttpServerErrorException;

import java.io.IOException;
import java.util.List;


@RestController
public class ConnectController {
    public static final String ERROR_AUTHORIZATION_TO_SERVICE_MESSAGE = "error authorization to service: ";
    @Autowired
    private ServiceService serviceService;
    @GetMapping(value = "/connection")
    public ResponseEntity<Object> connect(){
        connectToAllServices();
        return ResponseEntity.ok().build();
    }
    public void connectToAllServices(){
        List<Service> services = serviceService.findAll();
        for(Service service: services){
            JsonNode jsonNode = null;
            try{
                jsonNode = createJsonLogin(service);
                connectToService(jsonNode, service);
            }catch (HttpClientErrorException e){
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ERROR_AUTHORIZATION_TO_SERVICE_MESSAGE + service.getId());
            }
        }
    }

    public JsonNode createJsonLogin(Service service){
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = null;
        try {
            jsonNode = objectMapper.readTree("{}");
        } catch (IOException ignored) {
        }
        ObjectNode element = objectMapper.createObjectNode();
        element.put("username", service.getName());
        ((ObjectNode) jsonNode).set("username", element.get("username"));
        element.put("password", service.getPassword());
        ((ObjectNode) jsonNode).set("password", element.get("password"));
        return jsonNode;
    }

    public void connectToService(JsonNode jsonNode, Service service){
        RestTemplate restTemplate = new RestTemplate();
        String url = service.getUrl() + "/" + "api/login";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<JsonNode> requestEntity = new HttpEntity<>(jsonNode);
        ResponseEntity<JsonNode> responseEntity;
        responseEntity = restTemplate.postForEntity(url, requestEntity, JsonNode.class);
        String token_with_quotes = responseEntity.getBody().get("token").toString();
        String token_without_quotes = token_with_quotes.substring(1, token_with_quotes.length()-1);
        service.setToken("Bearer_" + token_without_quotes);
        serviceService.save(service);
    }


}
