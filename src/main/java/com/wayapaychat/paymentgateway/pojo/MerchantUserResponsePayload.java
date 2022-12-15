/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.wayapaychat.paymentgateway.pojo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 *
 * @author User
 */
@Data
public class MerchantUserResponsePayload {
    
    private String fullName;
    private String emailAddress;
    private LocalDateTime createdAt;
    private String role;
    private long invitorId;
    private String status;
    private long userId;
    
}
