package com.wayapaychat.paymentgateway.common.enums;

public class Constant {


    public static final String INVALID_USER_ID = "Please provide a valid user Id";
    public static final String UNABLE_TO_VALIDATE_PIN = "Unable to validate pin at the moment, please try again later";
    public static final String INALID_REFERENCE = "provide a valid payment reference";
    public static final String DR_ACCOUNT_NAME = "WAYA Multi Link Technology Limited";

    private Constant() {
    }

    
    public static final int SUCCESS_CODE = 0;
    public static final int ERROR_CODE = 10;
    public static final int PENDING_CODE = 99;
    public static final String ERROR_PROCESSING = "Error Processing Request";
    public static final String OPERATION_SUCCESS = "Operation Successful";
    public static final String FORWARDED_FOR = "X-FORWARDED-FOR";
    public static final String INVALID_AUTH = "Invalid Auth key";
    public static final String AUTH_HEADER = "Authorization";
    public static final String RUBIES_SUCCESS_CODE = "00";
    public static final String RUBIES_PENDING_CODE = "-1";

    public static final String INVALID_BVN = "Please provide a valid BVN";
    public static final String INVALID_ACCOUNT_NUMBER = "Please provide a valid account number";
    public static final String INVALID_BANK_CODE = "Please provide a valid bank code";
    public static final String INVALID_BANK_NAME = "Please provide a valid bank name";
    public static final String INVALID_CREDIT_ACCOUNT_NUMBER = "Please provide a valid credit account number";
    public static final String UNABLE_TO_FETCH_CREDIT_ACCOUNT_NUMBER = "Credit Account Number Could Not Be Fetched";
    public static final String INSUFFICIENT_FUNDS = "Insufficient Funds In Merchant Account";
    public static final String INVALID_CREDIT_ACCOUNT_NAME = "Please provide a valid credit account name";
    public static final String INVALID_DEBIT_ACCOUNT = "Please provide a valid debit account";
    public static final String INVALID_TRANSACTION_PIN = "Please provide a valid transaction pin";
    public static final String REQUEST_IN_PROGRESS = "Your Request is being processed";
    public static final String REQUEST_NOT_FOUND = "Payment request not found or already settled/rejected";
    public static final String INITIATOR = "waya-withdrawal-service";
    public static final String PAYMENT_REQUEST_MESSAGE = "## requested an amount of $$ naira";
    public static final String PAYMENT_REQUEST_SETTLED_MESSAGE = "## has settled your payment request of $$ naiea";
    public static final String PAYMENT_REQUEST_REJECTED_MESSAGE = "## rejected your payment request of $$ naira";
    public static final String PERMISSION_ERROR = "User Does Not Have Permission";

}
