package com.wayapaychat.paymentgateway.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.wayapaychat.paymentgateway.SpringApplicationContext;
import com.wayapaychat.paymentgateway.pojo.AuthenticatedUser;
import com.wayapaychat.paymentgateway.pojo.ErrorResponse;
import com.wayapaychat.paymentgateway.pojo.LogMessage;
import com.wayapaychat.paymentgateway.pojo.LogRequest;
import com.wayapaychat.paymentgateway.service.UserService;
import com.wayapaychat.paymentgateway.common.utils.PaymentGateWayCommonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("NullableProblems")
@Slf4j
public class LoggableDispatcherServlet extends DispatcherServlet {

    private static final long serialVersionUID = 2453821271976611591L;

    @Override
    protected void doDispatch(HttpServletRequest request, HttpServletResponse response) throws Exception {
        long startTime = System.currentTimeMillis();
        if (!(request instanceof ContentCachingRequestWrapper))
            request = new ContentCachingRequestWrapper(request);
        if (!(response instanceof ContentCachingResponseWrapper))
            response = new ContentCachingResponseWrapper(response);
        HandlerExecutionChain handler = null;
        try {
            handler = getHandler(request);
            super.doDispatch(request, response);
        } catch (HttpMediaTypeNotSupportedException ex) {
            ErrorResponse errorResponse = new ErrorResponse(ex.getMessage());
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            String str = convertObjectToJson(errorResponse);
            PrintWriter pr = response.getWriter();
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            pr.write(str);
        } catch (Exception ex) {
            ErrorResponse errorResponse = new ErrorResponse(ex.getMessage());
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.getWriter().write(convertObjectToJson(errorResponse));
            String str = convertObjectToJson(errorResponse);
            PrintWriter pr = response.getWriter();
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            pr.write(str);
        } finally {
            log(request, response, handler, System.currentTimeMillis() - startTime);
            updateResponse(response);
        }
    }

    private void log(HttpServletRequest request, HttpServletResponse response, HandlerExecutionChain handler, long timeTaken) {
        final String path = request.getRequestURI();
        if (path.contains("swagger") || path.startsWith("/v2/api-docs")
                || path.contains("/auth/validate-user")
                || path.contains("/encryption")
                || path.contains("/transaction/payment")
                || path.contains("/transaction/processing"))
            return;
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        LogMessage logMessage = new LogMessage();
        logMessage.setHttpStatus(response.getStatus());
        logMessage.setHttpMethod(request.getMethod());
        logMessage.setClientIP(PaymentGateWayCommonUtils.getClientRequestIP(request));
        logMessage.setTimeTakenMs(timeTaken);
        logMessage.setPath(path);
        logMessage.setResponse(getResponsePayload(response));
        logMessage.setJavaMethod(handler == null ? "null" : handler.getHandler().toString());
        logMessage.setRequestParams(request.getQueryString());
        String requestData = null;
        try {
            requestData = getRequestData(request);
            if (requestData != null && requestData.length() > 5000) {
                requestData = "Bulky: cannot Write to file";
            }
        } catch (IOException e) {
            log.error("An error Occurred in reading request Input :: {}", e.getMessage());
        }
        logMessage.setRequestBody(Objects.toString(requestData, "null"));
        String json = gson.toJson(logMessage);
        log.info(json);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
            AuthenticatedUser user = ((UserPrincipal) authentication.getPrincipal()).getUser();
            if (user != null) {
                CompletableFuture.runAsync(() -> logRequestAndResponse(logMessage, user.getId()));
            }
        }
    }

    private String getResponsePayload(HttpServletResponse response) {
        ContentCachingResponseWrapper wrapper = WebUtils.getNativeResponse(response,
                ContentCachingResponseWrapper.class);
        if (wrapper != null) {
            byte[] buf = wrapper.getContentAsByteArray();
            if (buf.length > 0) {
                int length = Math.min(buf.length, 5120);
                try {
                    return new String(buf, 0, length, wrapper.getCharacterEncoding());
                } catch (Exception ex) {
                    log.error("Error Occurred in Encoding Response Body: {}", ex.getMessage());
                }
            }
        }
        return "[unknown]";
    }

    private String getRequestData(final HttpServletRequest request) throws UnsupportedEncodingException {
        String payload = null;
        ContentCachingRequestWrapper wrapper = WebUtils.getNativeRequest(request, ContentCachingRequestWrapper.class);
        if (wrapper != null) {
            byte[] buf = wrapper.getContentAsByteArray();
            if (buf.length > 0) {
                payload = new String(buf, 0, buf.length, wrapper.getCharacterEncoding());
            }
        }
        return payload;
    }

    private void updateResponse(HttpServletResponse response) throws IOException {
        ContentCachingResponseWrapper responseWrapper = WebUtils.getNativeResponse(response,
                ContentCachingResponseWrapper.class);
        assert responseWrapper != null;
        responseWrapper.copyBodyToResponse();
    }

    private String convertObjectToJson(Object object) throws JsonProcessingException {
        if (object == null) {
            return null;
        }
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(object);
    }

    private void logRequestAndResponse(LogMessage message, Long id) {
        String httpMethod = message.getHttpMethod(), action;
        switch (httpMethod) {
            case "GET":
            case "PUT":
                action = "MODIFY";
                break;
            case "PATCH":
                action = "MODIFY";
                break;
            case "DELETE":
                action = "DELETE";
                break;
            default:
                action = "CREATE";
        }
        UserService userService = (UserService) SpringApplicationContext
                .getBean("userServiceImpl");
        LogRequest pojo = new LogRequest();
        pojo.setAction(action);
        String mess = "Dispute Service: " + message.getPath();
        pojo.setMessage(mess);
        pojo.setJsonRequest(message.getRequestBody());
        pojo.setJsonResponse(message.getResponse());
        pojo.setUserId(id);
        String controller = message.getJavaMethod();
        if (controller != null && !controller.isBlank() && controller.length() > 39)
            controller = controller.substring(39, controller.indexOf("#"));
        pojo.setModule(controller);
        userService.saveLog(pojo);
    }
}

