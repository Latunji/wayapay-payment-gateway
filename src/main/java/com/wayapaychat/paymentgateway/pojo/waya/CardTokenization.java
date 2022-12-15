/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.wayapaychat.paymentgateway.pojo.waya;


import lombok.Data;

/**
 *
 * @author oluwatosin
 */
@Data
public class CardTokenization {
    
    private String merchantId;
    private String customerId;
    private String cvv2;
    private String expiryDate;
    private String pan;
    private String pin;
    private String transactionRef;
    

}
