
package com.wayapaychat.paymentgateway.cardservice;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.wordnik.swagger.annotations.ApiModelProperty;

import java.util.Date;
import java.util.List;


@SuppressWarnings("ALL")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response {


    @ApiModelProperty(value = "", required = true)
    private String status;
    @ApiModelProperty(value = "", required = true)
    private String message;
    private List<Error> errors;

   // @JsonIgnore
    private Object data;

    public Response() {
    }

    public Response(String status, String message) {
        this.status = status;
        this.message = message;
    }

    /**
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * @param status the code to set
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * @param message the description to set
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * @return the errors
     */
    public List<Error> getErrors() {
        return errors;
    }

    /**
     * @param errors the errors to set
     */
    public void setErrors(List<Error> errors) {
        this.errors = errors;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
