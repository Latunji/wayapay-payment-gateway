package com.wayapaychat.paymentgateway.service.impl;

import com.wayapaychat.paymentgateway.apihelper.API;
import com.wayapaychat.paymentgateway.cardservice.CardTransactionResponse;
import com.wayapaychat.paymentgateway.pojo.RolePermissionResponsePayload;
import com.wayapaychat.paymentgateway.pojo.RoleResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.Map;

@Transactional
@Slf4j
@Service
public class RoleProxy {

    @Autowired
    private API api;

    @Value("${waya.role-service.baseurl}")
    private String baseUrl;

    public RolePermissionResponsePayload fetchUserRoleAndPermissions(Long userId, String token){
        Map<String,String> map = new HashMap();
        Long roleId = null;
        map.put("Authorization", token);
        RoleResponse response = api.get(baseUrl+"/auth/"+userId, RoleResponse.class, map);
        if(response.isStatus()){
            roleId = response.getData().getId();
        }
        RolePermissionResponsePayload resp = api.get(baseUrl+"/merchant/list/role-permissions/"+roleId, RolePermissionResponsePayload.class, map);
        return resp;
    }

}
