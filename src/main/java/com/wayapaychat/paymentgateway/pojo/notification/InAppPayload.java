package com.wayapaychat.paymentgateway.pojo.notification;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Builder
@Getter
@Setter
@ToString
public class InAppPayload {
    @NotNull(message = "make sure you entered the right key *message* , and the value must not be null")
    @NotBlank(message = "message cannot be blank, and make sure you use the right key *message*")
    private String message;

    private String type;

    @Valid
    @NotNull(message = "make sure you entered the right key *users* , and the value must not be null")
    @NotEmpty(message = "users list cannot be empty. also make sure you use the right key *users*")
    private List<InAppRecipient> users;
}
