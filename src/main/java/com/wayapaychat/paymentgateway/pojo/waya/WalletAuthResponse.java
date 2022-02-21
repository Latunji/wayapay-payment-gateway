package com.wayapaychat.paymentgateway.pojo.waya;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class WalletAuthResponse {
	
	private List<WalletData> wallet;
	private String token;

}
