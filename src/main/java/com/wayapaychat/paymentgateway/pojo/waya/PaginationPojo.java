package com.wayapaychat.paymentgateway.pojo.waya;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Sort;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaginationPojo {
    private Integer page;
    private Integer size;
    private Sort.Direction order;
    private String[] sortBy;
}
