package com.wayapaychat.paymentgateway.pojo.notification;

import com.wayapaychat.paymentgateway.enumm.EventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InAppNotificationEvent {
    @NotNull(message = "make sure you entered the right key *initiator* , and the value must not be null")
    @NotBlank(message = "initiator field must not be blank, and make sure you use the right key *initiator*")
    private String initiator;

    private EventType eventType;

    @Valid
    @NotNull(message = "make sure you entered the right key *data* , and the value must not be null")
    private InAppPayload data;

    private String token;
}
