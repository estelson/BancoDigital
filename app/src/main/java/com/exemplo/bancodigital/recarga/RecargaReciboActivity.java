package com.exemplo.bancodigital.recarga;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.exemplo.bancodigital.R;
import com.exemplo.bancodigital.helper.FirebaseHelper;
import com.exemplo.bancodigital.helper.GetMask;
import com.exemplo.bancodigital.model.Recarga;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class RecargaReciboActivity extends AppCompatActivity {

    private TextView textCodigo;
    private TextView textData;
    private TextView textValor;
    private TextView textTelefone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recarga_recibo);

        configToolbar();

        iniciaComponentes();

        getRecarga();

        configCliques();
    }

    private void getRecarga() {
        String idRecarga = (String) getIntent().getSerializableExtra("idRecarga");

        DatabaseReference recargaRef = FirebaseHelper.getDatabaseReference()
                .child("recargas")
                .child(idRecarga);

        recargaRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Recarga recarga = snapshot.getValue(Recarga.class);

                configDados(recarga);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void configDados(Recarga recarga) {
        textCodigo.setText(recarga.getId());
        textData.setText(GetMask.getDate(recarga.getData(), 3));
        textValor.setText(getString(R.string.text_valor, GetMask.getValor(recarga.getValor())));
        textTelefone.setText(recarga.getTelefone());
    }

    private void configToolbar() {
        TextView textTitulo = findViewById(R.id.textTitulo);
        textTitulo.setText("Recibo");
    }

    private void iniciaComponentes() {
        textCodigo = findViewById(R.id.textCodigo);
        textData = findViewById(R.id.textData);
        textValor = findViewById(R.id.textValor);
        textTelefone = findViewById(R.id.textTelefone);
    }

    private void configCliques() {
        findViewById(R.id.btnOk).setOnClickListener(v -> {
            finish();
        });
    }

}