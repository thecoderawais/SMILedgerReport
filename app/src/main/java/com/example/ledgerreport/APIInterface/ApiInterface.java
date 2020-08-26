package com.example.ledgerreport.APIInterface;
import com.example.ledgerreport.Models.LedgerReportModel;
import com.example.ledgerreport.Models.TbAccountsModel;
import com.example.ledgerreport.Models.TrialBalanceModel;
import com.example.ledgerreport.Models.UserLoginModel;
import com.example.ledgerreport.Utils.CONST;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiInterface {

    @GET(CONST.READ_LEDGER_REPORT_DATA)
    Call<List<LedgerReportModel>> getLedgerReportData(
            @Query("V_C_NO") int V_C_NO,
            @Query("AC_NO") String AC_NO,
            @Query("FROM_DATE") String FROM_DATE,
            @Query("TO_DATE") String TO_DATE
    );

    @GET(CONST.READ_MULTIPLE_LEDGER_REPORT_DATA)
    Call<List<LedgerReportModel>> getCombinedLedgerReportData(@Query("WhereClause") String WhereClause);

    @GET(CONST.USER_LOGIN)
    Call<List<UserLoginModel>> userLogin(
            @Query("Code") int Code,
            @Query("Username") String Username,
            @Query("Password") String Password
    );

    @GET(CONST.READ_TBACCOUNTS)
    Call<List<TbAccountsModel>> getAllTbAccounts();

    @GET(CONST.READ_TRIAL_BALANCE)
    Call<List<TrialBalanceModel>> getTrialBalanceData(@Query("TO_DATE") String toDate);
}
