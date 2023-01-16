/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.wayapaychat.paymentgateway.pojo.waya.wallet;

import lombok.Data;

/**
 *
 * @author Olawale
 */
@Data
public class TokenizePaymentResponse {
    
    private String amount;
    private String message;
    private String transactionIdentifier;
    private String transactionRef;
    
}
