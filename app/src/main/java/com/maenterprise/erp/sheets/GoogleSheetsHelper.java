package com.maenterprise.erp.sheets;

import android.content.Context;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

public class GoogleSheetsHelper {
    private static final String APPLICATION_NAME = "ERP Distribution App";
    private static final GsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static Sheets sheetsService;
    private final String spreadsheetId;

    public GoogleSheetsHelper(Context context, String spreadsheetId) throws GeneralSecurityException, IOException {
        this.spreadsheetId = spreadsheetId;
        if (sheetsService == null) {
            InputStream in = context.getAssets().open("service_account_credentials.json");
            GoogleCredentials credentials = ServiceAccountCredentials.fromStream(in)
                    .createScoped(Collections.singleton(SheetsScopes.SPREADSHEETS));
            sheetsService = new Sheets.Builder(GoogleNetHttpTransport.newTrustedTransport(),
                    JSON_FACTORY, new HttpCredentialsAdapter(credentials))
                    .setApplicationName(APPLICATION_NAME)
                    .build();
        }
    }

    public void appendRow(String sheetName, List<Object> rowData) throws IOException {
        ValueRange body = new ValueRange().setValues(Collections.singletonList(rowData));
        sheetsService.spreadsheets().values()
                .append(spreadsheetId, sheetName + "!A1", body)
                .setValueInputOption("USER_ENTERED")
                .execute();
    }

    public List<List<Object>> getSheetData(String range) throws IOException {
        ValueRange response = sheetsService.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute();
        return response.getValues();
    }
    
    public void updateCell(String range, Object value) throws IOException {
        ValueRange body = new ValueRange().setValues(Collections.singletonList(Collections.singletonList(value)));
        sheetsService.spreadsheets().values()
                .update(spreadsheetId, range, body)
                .setValueInputOption("USER_ENTERED")
                .execute();
    }
}