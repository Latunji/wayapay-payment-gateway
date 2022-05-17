package com.wayapaychat.paymentgateway.pojo.waya.merchant;

import com.wayapaychat.paymentgateway.common.enums.PricingStatus;
import com.wayapaychat.paymentgateway.common.enums.ProductName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductPricingResponse {
    private Long id;
    private String merchantId;
    private String merchantProductPricingId;
    private String productPricingId;
    private String merchantName;
    private Long merchantUserId;
    private PricingStatus pricingStatus;
    private Double localRate;
    private Double localDiscountRate;
    private Double internationalRate;
    private Double internationalDiscountRate;
    private ProductName productName;
    private LocalDateTime dateCreated;
    private LocalDateTime dateModified;
    private BigDecimal localProcessingFeeCappedAt;
    private BigDecimal internationalProcessingFeeCappedAt;
    private Long createdBy;
    private Long modifiedBy;
    private Boolean deleted;
}
