package com.wayapaychat.paymentgateway.apihelper;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.wayapaychat.paymentgateway.cardservice.exceptions.ProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Map;

//import com.sun.istack.Nullable;


/**
 *
 * This class is responsible for handling rest calls
 */

@Slf4j
@SuppressWarnings("ALL")
@Component
public class API {



    private static ObjectMapper objectMapper = new ObjectMapper();
    private final ObjectMapper mapper;
    private RestTemplate restTemplate = new RestTemplate();
    private Gson gson;



    @Autowired
    public API(RestTemplateBuilder restTemplateBuilder,
               RestTemplateResponseErrorHandler gatewayProviderResponseErrorHandler) {
        this.mapper = objectMapper;
        this.restTemplate = restTemplateBuilder
                .errorHandler(gatewayProviderResponseErrorHandler)
                .build();
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    // this is a get method
    public <T> T get(String requestPath, Class<T> responseClass, Map<String, String> headers) {
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        headers.forEach(requestHeaders::set);
        log.info("Headers " + headers);
        try {
            log.info(" Headers " + headers);
            log.info(" ::: URI ::" + requestPath);
            URI uri = new URI(requestPath);
            HttpEntity<?> requestEntity = new HttpEntity<>("", requestHeaders);
            ResponseEntity<String> responseEntity = restTemplate
                    .exchange(uri, HttpMethod.GET, requestEntity, String.class);
//            log.info(" response payload from client : " + responseEntity.getBody());
            log.info(" response HTTP status code from client : " + responseEntity
                            .getStatusCode()
                            .toString());
            return gson.fromJson(responseEntity.getBody(), responseClass);
        } catch (Exception e) {
            log.error(" Request failed", e);
            throw new ProcessingException(e.getMessage());
        }
    }


    public <T> T post(String url, Object requestObject, Class<T> responseClass,
                      @Nullable Map<String, String> headers) {
        HttpServerErrorException httpServerErrorException;
        try {
            log.info(" Headers " + headers);
            HttpHeaders requestHeaders = new HttpHeaders();
            requestHeaders.setContentType(MediaType.APPLICATION_JSON);
            if (headers != null) {
                headers.forEach(requestHeaders::set);
            }
//            String request = new Gson().toJson(requestObject);

            HttpEntity<?> requestEntity = new HttpEntity<>(requestObject, requestHeaders);
            log.info("request payload to client :" + requestObject);
            ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
            log.info("response payload from client :" + responseEntity.getBody().toString());
            log.info("response HTTP status code from client : " + responseEntity.getStatusCode().toString());
            return gson.fromJson(responseEntity.getBody(), responseClass);

        } catch (Exception e) {
            log.error(" Request failed", e);
            log.error("response from client (Error): " + e.getMessage());
            throw new ProcessingException("response from client (Error): " + e.getMessage());
        }
    }

    // this is a post method
    public <T> T post(String url, Object requestObject, Class<T> responseClass) {
        return post(url, requestObject, responseClass, null);
    }





    public <T> T patch(String url, Object requestObject, Class<T> responseClass,
            @Nullable Map<String, String> headers, String requestId) {
        HttpServerErrorException httpServerErrorException;
        try {
            log.info(requestId + " Headers " + headers);
            log.info(requestId + " request payload to client : " + gson.toJson(requestObject));
            HttpHeaders requestHeaders = new HttpHeaders();
            requestHeaders.setContentType(MediaType.APPLICATION_JSON);
            if (headers != null) {
                headers.forEach(requestHeaders::set);
            }
            HttpEntity<?> requestEntity = new HttpEntity<>(requestObject, requestHeaders);
            HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
            restTemplate.setRequestFactory(requestFactory);
            ResponseEntity<String> responseEntity = restTemplate
                    .exchange(url, HttpMethod.PATCH, requestEntity, String.class, requestObject);
            log.info(requestId + " response payload from client : " + responseEntity.getBody());
            log.info(requestId + " response HTTP status code from client : " + responseEntity
                    .getStatusCode()
                    .toString());
            return gson.fromJson(responseEntity.getBody(), responseClass);

        } catch (Exception e) {
            log.error(requestId + " Request failed", e);
            log.error(requestId + " response from client (Error): " + e.getMessage());
            throw new ProcessingException("response from client (Error): " + e.getMessage());
        }
    }













}
