package com.example.ledgerreport.APIInterface;
import com.example.ledgerreport.Models.LedgerReportModel;
import com.example.ledgerreport.Utils.CONST;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiInterface {

    @GET(CONST.READ_LEDGER_REPORT_DATA)
    Call<List<LedgerReportModel>> getLedgerReportData(
            @Query("AC_NO") String AC_NO,
            @Query("FROM_DATE") String FROM_DATE,
            @Query("TO_DATE") String TO_DATE
    );
}
