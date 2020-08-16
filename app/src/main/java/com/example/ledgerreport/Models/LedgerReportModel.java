package com.example.ledgerreport.Models;

import java.util.Date;

public class LedgerReportModel {

    private String V_NO;
    private Date V_DATE;
    private int AC_NO;
    private String AC_NAME;
    private String DESCRIPTION = "N/A";
    private int VDEBIT;
    private int V_CREDIT;
    private int BALANCE;
    private int OPENING_BALANCE;
    private String FROM_DATE;
    private String TO_DATE;

    public void setBALANCE(int BALANCE) {
        this.BALANCE = BALANCE;
    }

    public void setOPENING_BALANCE(int OPENING_BALANCE) {
        this.OPENING_BALANCE = OPENING_BALANCE;
    }

    public LedgerReportModel(String v_NO, Date v_DATE, int AC_NO, String AC_NAME, String DESCRIPTION, int VDEBIT, int V_CREDIT, int balance, int OPENING_BALANCE, String FROM_DATE, String TO_DATE) {
        this.DESCRIPTION = DESCRIPTION;
        V_NO = v_NO;
        V_DATE = v_DATE;
        this.AC_NO = AC_NO;
        this.AC_NAME = AC_NAME;
        this.VDEBIT = VDEBIT;
        this.V_CREDIT = V_CREDIT;
        BALANCE = balance;
        this.OPENING_BALANCE = OPENING_BALANCE;
        this.FROM_DATE = FROM_DATE;
        this.TO_DATE = TO_DATE;
    }

    public String getV_NO() {

        return V_NO;
    }

    public Date getV_DATE() {
        return V_DATE;
    }

    public int getAC_NO() {
        return AC_NO;
    }

    public String getAC_NAME() {
        return AC_NAME;
    }

    public int getBALANCE() {
        return BALANCE;
    }

    public String getDESCRIPTION() {
        if (DESCRIPTION != null && !DESCRIPTION.isEmpty())
            return DESCRIPTION;
        else
            return "N/A";
    }

    public int getVDEBIT() {
        return VDEBIT;
    }

    public int getV_CREDIT() {
        return V_CREDIT;
    }

    public int getOPENING_BALANCE() {
        return OPENING_BALANCE;
    }

    public String getFROM_DATE() {
        return FROM_DATE;
    }

    public String getTO_DATE() {
        return TO_DATE;
    }
}
