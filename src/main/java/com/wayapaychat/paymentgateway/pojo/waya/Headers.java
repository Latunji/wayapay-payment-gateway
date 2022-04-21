package com.wayapaychat.paymentgateway.pojo.waya;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"Content-Type"
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Headers {
	
	@JsonProperty("Content-Type")
	private List<String> contentType;

}
