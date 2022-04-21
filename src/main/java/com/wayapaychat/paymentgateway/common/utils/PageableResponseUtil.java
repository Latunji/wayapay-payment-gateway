package com.wayapaychat.paymentgateway.common.utils;


import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.ObjectUtils;

public class PageableResponseUtil {
    private static final Integer DEFAULT_PAGE_REQUEST_SIZE = 20;
    public static Pageable createPageRequest(Integer page, Integer size, Sort.Direction order, String[] sortBy, boolean paged, String defaultProp) {
        if (paged) {
            return PageRequest.of(
                    ObjectUtils.isEmpty(page) || page <= 0 ? 0 : page - 1,
                    ObjectUtils.isEmpty(size) || size <= 0 ? DEFAULT_PAGE_REQUEST_SIZE : size,
                    Sort.by(ObjectUtils.isEmpty(order) ? Sort.Direction.ASC : order, ObjectUtils.isEmpty(sortBy) ? new String[]{defaultProp} : sortBy)
            );
        } else return null;
    }
}