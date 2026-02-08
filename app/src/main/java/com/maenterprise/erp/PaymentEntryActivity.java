package com.maenterprise.erp;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.maenterprise.erp.data.AppDatabase;
import com.maenterprise.erp.databinding.ActivityPaymentEntryBinding;
import com.maenterprise.erp.models.Payment;
import com.maenterprise.erp.utils.IdGenerator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PaymentEntryActivity extends AppCompatActivity {
    private ActivityPaymentEntryBinding binding;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPaymentEntryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.btnSave.setOnClickListener(v -> savePayment());
    }

    private void savePayment() {
        String saleId = binding.etSaleId.getText().toString();
        String date = binding.etDate.getText().toString();
        String amountStr = binding.etAmount.getText().toString();
        String mode = binding.etMode.getText().toString();
        String remarks = binding.etRemarks.getText().toString();

        if (saleId.isEmpty() || date.isEmpty() || amountStr.isEmpty()) {
            Toast.makeText(this, "Please fill required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Payment payment = new Payment();
        payment.paymentId = IdGenerator.generatePaymentId();
        payment.saleId = saleId;
        payment.paymentDate = date;
        payment.amountReceived = Double.parseDouble(amountStr);
        payment.paymentMode = mode;
        payment.remarks = remarks;
        payment.isSynced = false;

        executor.execute(() -> {
            AppDatabase.getDatabase(this).erpDao().insertPayment(payment);
            runOnUiThread(() -> {
                Toast.makeText(this, "Payment Recorded", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }
}