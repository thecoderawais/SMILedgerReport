package com.example.ledgerreport.Models;

public class TbAccountsModel {
    private String AC_CODE;
    private String AC_NAME;
    private String OP_DATE;
    private String OP_DEBIT;
    private String OP_CREDIT;
    private String ADDRESS;

    public TbAccountsModel(String AC_CODE, String AC_NAME, String OP_DATE, String OP_DEBIT, String OP_CREDIT, String ADDRESS) {
        this.AC_CODE = AC_CODE;
        this.AC_NAME = AC_NAME;
        this.OP_DATE = OP_DATE;
        this.OP_DEBIT = OP_DEBIT;
        this.OP_CREDIT = OP_CREDIT;
        this.ADDRESS = ADDRESS;
    }

    public String getAC_CODE() {
        return AC_CODE;
    }

    public void setAC_CODE(String AC_CODE) {
        this.AC_CODE = AC_CODE;
    }

    public String getAC_NAME() {
        return AC_NAME;
    }

    public void setAC_NAME(String AC_NAME) {
        this.AC_NAME = AC_NAME;
    }

    public String getOP_DATE() {
        return OP_DATE;
    }

    public void setOP_DATE(String OP_DATE) {
        this.OP_DATE = OP_DATE;
    }

    public String getOP_DEBIT() {
        return OP_DEBIT;
    }

    public void setOP_DEBIT(String OP_DEBIT) {
        this.OP_DEBIT = OP_DEBIT;
    }

    public String getOP_CREDIT() {
        return OP_CREDIT;
    }

    public void setOP_CREDIT(String OP_CREDIT) {
        this.OP_CREDIT = OP_CREDIT;
    }

    public String getADDRESS() {
        return ADDRESS;
    }

    public void setADDRESS(String ADDRESS) {
        this.ADDRESS = ADDRESS;
    }
}
