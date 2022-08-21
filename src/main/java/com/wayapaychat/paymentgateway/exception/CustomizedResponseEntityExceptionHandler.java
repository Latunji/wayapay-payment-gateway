package com.wayapaychat.paymentgateway.exception;

import com.wayapaychat.paymentgateway.pojo.waya.CustomErrorResponse;
import feign.RetryableException;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.service.spi.ServiceException;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.*;

@SuppressWarnings("ALL")
@ControllerAdvice
@RestController
@Slf4j
public class CustomizedResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(Exception.class)
    public final ResponseEntity<CustomErrorResponse> handleAllExceptions(Exception ex, WebRequest request) {
        List<String> details = new ArrayList<>();
        String localizedMessage = ex.getMessage() == null ? "Server Error Occurred." : ex.getMessage();
        String message;
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        if (localizedMessage.contains("ix_tbl_m_waya_merchant_col__user_id_uq")) {
            message = "Merchant account with the validated user already exists";
            httpStatus = HttpStatus.BAD_REQUEST;
            details.add(message);
        } else if (localizedMessage.contains("ix_tbl_customer_col_phone_number__email__merchant_id__uq")) {
            message = "Customer account with the same email and phone  number already exists";
            httpStatus = HttpStatus.BAD_REQUEST;
            details.add(message);
        } else if (localizedMessage.contains("Invalid format")) {
            message = "Invalid date format, please check date value passed.";
            httpStatus = HttpStatus.BAD_REQUEST;
            details.add(message);
        } else if (localizedMessage.contains("does not exist")) {
            message = "Invalid request. Please make sure correct fields are entered";
            httpStatus = HttpStatus.BAD_REQUEST;
            details.add(message);
        } else if (localizedMessage.contains("null value in column")) {
            message = "fields(s) missing from request. please provide all required fields";
            httpStatus = HttpStatus.BAD_REQUEST;
            details.add(message);
        } else if (localizedMessage.contains("[401] during [POST]") || localizedMessage.contains("[404] during [POST]")) {
            message = "Oops! I'm sorry seems you are not a valid user. please try again or contact support";
            httpStatus = HttpStatus.UNAUTHORIZED;
            details.add(message);
        } else if (localizedMessage.contains("[404] during [GET]")) {
            message = "Resource(s) provided to process this request does not exists";
            httpStatus = HttpStatus.NOT_FOUND;
            details.add(message);
        }  else if (localizedMessage.contains("[404] during [GET]") && localizedMessage.contains("payment-link")) {
            message = "Payment link does not exists with the provided credential";
            httpStatus = HttpStatus.NOT_FOUND;
            details.add(message);
        }  else if (localizedMessage.contains("[401] during [GET]")) {
            message = "Oops! failed to comlete request";
            httpStatus = HttpStatus.FORBIDDEN;
            details.add(message);
        } else if (localizedMessage.contains("[400] during [POST] ") || localizedMessage.contains("auth/validate-user]")) {
            message = "Oops! valid user credentials required";
            httpStatus = HttpStatus.BAD_REQUEST;
            details.add(message);
        } else if (localizedMessage.contains("[400] during [GET] ") || localizedMessage.contains("/waya-merchant")) {
            message = "Oops! Bad request | One or more resources not found!";
            httpStatus = HttpStatus.BAD_REQUEST;
            details.add(message);
        } else if (localizedMessage.contains("IAuthenticationServiceProxy#validateUser") && localizedMessage.contains("503 Service Temporarily Unavailable")) {
            httpStatus = HttpStatus.SERVICE_UNAVAILABLE;
            message = "Unavailable to authenticate user... please try again later";
            details.add(message);
        } else if (localizedMessage.contains("Cannot construct instance of `java.time.LocalDateTime`")) {
            httpStatus = HttpStatus.BAD_REQUEST;
            message = "Oops! Date time format provided is invalid";
            details.add(message);
        } else {
            message = "Oops! Our system failed to process this request!";
            details.add(message);
        }
        log.error("ERROR: ", ex);
        CustomErrorResponse error = new CustomErrorResponse(message, details);
        return new ResponseEntity<>(error, httpStatus);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public final ResponseEntity<Object> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
        return new ResponseEntity<>(getError(ex.getMessage(),
                request.getDescription(false)), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public final ResponseEntity<CustomErrorResponse> handleMissingRequestHeaderException
            (MissingRequestHeaderException
                     ex, WebRequest request) {
        List<String> details = new ArrayList<>();
        String localizedMessage = ex.getLocalizedMessage();
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
        if (localizedMessage.contains("Authorization")) {
            details.add("Oops! Authorization required");
            httpStatus = HttpStatus.UNAUTHORIZED;
        }
        CustomErrorResponse error = new CustomErrorResponse(request.getDescription(false), details);
        log.error("{0}", ex);
        return new ResponseEntity<>(error, httpStatus);
    }


    @ExceptionHandler(InvalidDataAccessResourceUsageException.class)
    public final ResponseEntity<CustomErrorResponse> handleSqlExceptionHelper(InvalidDataAccessResourceUsageException
                                                                                      ex, WebRequest request) {
        List<String> details = new ArrayList<>();
        details.add("Oops! Invalid request. Please make sure all fields are entered correctly");
        CustomErrorResponse error = new CustomErrorResponse(request.getDescription(false), details);
        log.error("ERROR", ex);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RetryableException.class)
    public final ResponseEntity<CustomErrorResponse> failedToValidateToken(RetryableException ex, WebRequest request) {
        List<String> details = new ArrayList<>();
        details.add("Oops! Failed to validate user authentication");
        CustomErrorResponse error = new CustomErrorResponse(request.getDescription(false), details);
        log.error("{0}", ex);
        return new ResponseEntity<>(error, HttpStatus.REQUEST_TIMEOUT);
    }

    @ExceptionHandler(ApplicationException.class)
    public final ResponseEntity<CustomErrorResponse> applicationException(ApplicationException ex, WebRequest request) {
        List<String> details = new ArrayList<>();
        details.add(ex.getLocalizedMessage());
        CustomErrorResponse error = new CustomErrorResponse(request.getDescription(false), details);
        log.error("{0}", ex);
        return ResponseEntity.status(ex.getHttpStatus()).body(error);
    }

    @ExceptionHandler(ServiceException.class)
    public final ResponseEntity<CustomErrorResponse> handleServiceException(ServiceException ex, WebRequest request) {
        List<String> details = new ArrayList<>();
        details.add(ex.getMessage());
        log.error(ex.getMessage());
        CustomErrorResponse error = new CustomErrorResponse(request.getDescription(false), details);
        log.error("{0}", ex);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    //    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        List<String> details = new ArrayList<>();
        String localizedMessage = ex.getMessage() != null ? ex.getMessage() : "";
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        String message = "";
        if (localizedMessage.contains("Unexpected character")) {
            httpStatus = HttpStatus.EXPECTATION_FAILED;
            details.add("Oops! Unexpected body request payload");
        } else if (localizedMessage.contains("Cannot deserialize value of type `java.time.LocalDateTime`")) {
            httpStatus = HttpStatus.BAD_REQUEST;
            details.add("Oops! request one or more date validation failed.");
        } else if (localizedMessage.toLowerCase().contains("required request body is missing")) {
            details.add("Required request body is missing");
            httpStatus = HttpStatus.BAD_REQUEST;
        } else if (localizedMessage.contains("com.wayapaychat.identity.webpos.enums.PaymentLinkType")) {
            httpStatus = HttpStatus.BAD_REQUEST;
            message = "Oops! only paymentLinkType[ONE_TIME_PAYMENT_LINK, SUBSCRIPTION_PAYMENT_LINK] are allowed";
            details.add(message);
        } else details.add("Oops! Server unexpected exception occurred. please try again later or contact support");
        CustomErrorResponse error = new CustomErrorResponse(request.getDescription(false), details);
        log.error("{0}", ex);
        return new ResponseEntity<>(error, httpStatus);
    }

    @ExceptionHandler(MissingHeaderInfoException.class)
    public final ResponseEntity<Object> handleInvalidTraceIdException(MissingHeaderInfoException ex, WebRequest request) {
        String message = ex.getLocalizedMessage();
        log.error(ex.getMessage());
        return buildResponseEntity(message, HttpStatus.EXPECTATION_FAILED);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public final ResponseEntity<Object> handleBadCredentialsException(BadCredentialsException ex) {
        String message = ex.getLocalizedMessage();
        log.error(ex.getMessage());
        return buildResponseEntity(message, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Object> handleMaxSizeException(MaxUploadSizeExceededException exc) {
        String message = "File too Large for Upload. Maximum file Size: " + exc.getMaxUploadSize();
        return buildResponseEntity(message, HttpStatus.EXPECTATION_FAILED);
    }

    /**
     * Handle MethodArgumentNotValidException. Triggered when an object fails @Valid validation.
     *
     * @param ex      the MethodArgumentNotValidException that is thrown when @Valid validation fails
     * @param headers HttpHeaders
     * @param status  HttpStatus
     * @param request WebRequest
     * @return the ApiError object
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return buildResponseEntity(getBodyValidationErrors(ex.getBindingResult().getFieldErrors()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolationException(ConstraintViolationException cve) {
        return buildResponseEntity(getValidationErrors(cve.getConstraintViolations()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleResourceNotFoundException(ResourceNotFoundException re) {
        return buildResponseEntity(re.getMessage(), HttpStatus.NOT_FOUND);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestPart(
            MissingServletRequestPartException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return buildResponseEntity(ex.getMessage(), status);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ResponseEntity<Object> handleMissingPathVariable(
            MissingPathVariableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return handleExceptionInternal(ex, getError(ex), headers, status, request);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotAcceptable(
            HttpMediaTypeNotAcceptableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {

        return handleExceptionInternal(ex, getError(ex), headers, status, request);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return handleExceptionInternal(ex, getError(ex), headers, status, request);
    }

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<Object> handleCustomException(CustomException ce) {
        return buildResponseEntity(ce.getMessage(), ce.getStatus());
    }

    /**
     * Handle HttpMediaTypeNotSupportedException. This one triggers when JSON is invalid as well.
     *
     * @param ex      HttpMediaTypeNotSupportedException
     * @param headers HttpHeaders
     * @param status  HttpStatus
     * @param request WebRequest
     * @return the ApiError object
     */
    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        StringBuilder builder = new StringBuilder();
        builder.append(ex.getContentType());
        builder.append(" media type is not supported. Supported media types are ");
        ex.getSupportedMediaTypes().forEach(t -> builder.append(t).append(", "));
        return buildResponseEntity(builder.substring(0, builder.length() - 2), HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    /**
     * Handle HttpMessageNotWritableException.
     *
     * @param ex      HttpMessageNotWritableException
     * @param headers HttpHeaders
     * @param status  HttpStatus
     * @param request WebRequest
     * @return the ApiError object
     */
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotWritable(
            HttpMessageNotWritableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return buildResponseEntity("Error writing JSON output", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handle NoHandlerFoundException
     *
     * @param ex      Exception Object
     * @param headers Headers
     * @param status  Status
     * @param request Request
     * @return ResponseEntity<Object>
     */
    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(
            NoHandlerFoundException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        String message = String.format("Could not find the %s method for URL %s", ex.getHttpMethod(), ex.getRequestURL());
        return buildResponseEntity(message, HttpStatus.NOT_FOUND);
    }

    private ResponseEntity<Object> buildResponseEntity(String apiResponse, HttpStatus status) {
        return new ResponseEntity<>(getError(apiResponse), status);
    }

    private ResponseEntity<Object> buildResponseEntity(Object apiResponse, HttpStatus status) {
        return new ResponseEntity<>(getError("Validation Errors", apiResponse), status);
    }

    private Map<String, String> getValidationErrors(Set<ConstraintViolation<?>> constraintViolations) {
        Map<String, String> errors = new HashMap<>();
        constraintViolations.forEach(e ->
                errors.put(((PathImpl) e.getPropertyPath()).getLeafNode().asString(), e.getMessage())
        );
        return errors;
    }

//    private Map<String, String> getValidationErrors(List<FieldError> fieldErrors) {
//        Map<String, String> errors = new HashMap<>();
//        fieldErrors.forEach(e -> errors.put(e.getField(), e.getDefaultMessage()));
//        return errors;
//    }

    private List<String> getBodyValidationErrors(List<FieldError> fieldErrors) {
        List<String> errors = new ArrayList<>();
        fieldErrors.forEach(e -> errors.add(e.getDefaultMessage()));
        return errors;
    }

    private Map<String, Object> getError(Exception ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", ex.getMessage());
        response.put("status", false);
        response.put("timestamp", new Date());
        response.put("data", null);
        return response;
    }

    private Map<String, Object> getError(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", message);
        response.put("status", false);
        response.put("timestamp", new Date());
        response.put("data", null);

        return response;
    }

    private Map<String, Object> getError(String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", message);
        response.put("status", false);
        response.put("timestamp", new Date());
        response.put("data", data);

        return response;
    }
}
