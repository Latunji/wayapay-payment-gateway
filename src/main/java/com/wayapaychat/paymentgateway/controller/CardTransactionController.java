package com.wayapaychat.paymentgateway.controller;


import com.wayapaychat.paymentgateway.cardservice.CardTransactionResponse;
import com.wayapaychat.paymentgateway.cardservice.CardTransactionService;
import com.wayapaychat.paymentgateway.cardservice.EncryptedCardRequest;
import com.wayapaychat.paymentgateway.cardservice.Response;
import com.wayapaychat.paymentgateway.cardservice.cardacquiring.CardAcqService;
import com.wayapaychat.paymentgateway.cardservice.cardacquiring.CardAuthorizationRequest;
import com.wayapaychat.paymentgateway.cardservice.cardacquiring.PinRequest;
import com.wayapaychat.paymentgateway.utility.CustomResponseCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/card")
public class CardTransactionController {

    @Autowired
    private CardTransactionService cardTransactionService;
    @Autowired
    private CardAcqService cardAcqService;



    @PostMapping("/payment")
    public ResponseEntity<Response> cardPayment(@Validated @RequestBody EncryptedCardRequest request) throws IOException {
        HttpStatus httpCode ;
        Response resp = new Response();
        CardTransactionResponse response = cardTransactionService.cardPayment(request);
        resp.setStatus(CustomResponseCode.SUCCESS);
        resp.setMessage("Successful");
        resp.setData(response);
        httpCode = HttpStatus.CREATED;
        return new ResponseEntity<>(resp, httpCode);
    }


    @PostMapping("/pin")
    public ResponseEntity<Response> cardPin(@Validated @RequestBody PinRequest request) throws IOException {
        HttpStatus httpCode ;
        Response resp = new Response();
        CardTransactionResponse response = cardAcqService.pinRequest(request);
        resp.setStatus(CustomResponseCode.SUCCESS);
        resp.setMessage("Successful");
        resp.setData(response);
        httpCode = HttpStatus.CREATED;
        return new ResponseEntity<>(resp, httpCode);
    }


    @PostMapping("/authorisation")
    public ResponseEntity<Response> authorisation(@Validated @RequestBody CardAuthorizationRequest request) throws IOException {
        HttpStatus httpCode ;
        Response resp = new Response();
        CardTransactionResponse response = cardAcqService.authorisation(request);
        resp.setStatus(CustomResponseCode.SUCCESS);
        resp.setMessage("Successful");
        resp.setData(response);
        httpCode = HttpStatus.CREATED;
        return new ResponseEntity<>(resp, httpCode);
    }

}
