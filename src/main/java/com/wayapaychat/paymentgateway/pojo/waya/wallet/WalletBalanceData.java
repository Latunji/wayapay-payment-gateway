package com.wayapaychat.paymentgateway.pojo.waya.wallet;

import lombok.Data;

import java.time.LocalDate;

@Data
public class WalletBalanceData {

    private boolean del_flg;
    private boolean entity_cre_flg;
    private String sol_id;
    private String bacid;
    private String accountNo;
    private String nubanAccountNo = "0";
    private String acct_name;
    private String gl_code;
    private String product_code;
    private String acct_ownership;
    private String frez_code;
    private String frez_reason_code;
    private LocalDate acct_opn_date;
    private boolean acct_cls_flg;
    private double clr_bal_amt;
    private double un_clr_bal_amt;
    private String hashed_no;
    private boolean int_paid_flg;
    private boolean int_coll_flg;
    private String lchg_user_id;
    private LocalDate lchg_time;
    private String rcre_user_id;
    private LocalDate rcre_time;
    private String acct_crncy_code;
    private double lien_amt;
    private String product_type;
    private double cum_dr_amt;
    private double cum_cr_amt;
    private boolean chq_alwd_flg;
    private double cash_dr_limit;
    private double xfer_dr_limit;
    private double cash_cr_limit;
    private double xfer_cr_limit;
    private LocalDate acct_cls_date;
    private LocalDate last_tran_date;
    private String last_tran_id_dr;
    private String last_tran_id_cr;
    private boolean walletDefault;
    private String lien_reason;
    private String accountType;
    private String description;
}