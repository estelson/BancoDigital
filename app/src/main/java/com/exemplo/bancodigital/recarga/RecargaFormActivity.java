package com.exemplo.bancodigital.recarga;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.blackcat.currencyedittext.CurrencyEditText;
import com.exemplo.bancodigital.R;
import com.exemplo.bancodigital.deposito.DepositoReciboActivity;
import com.exemplo.bancodigital.helper.FirebaseHelper;
import com.exemplo.bancodigital.model.Deposito;
import com.exemplo.bancodigital.model.Extrato;
import com.exemplo.bancodigital.model.Recarga;
import com.exemplo.bancodigital.model.Usuario;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.santalu.maskara.widget.MaskEditText;

import java.util.Locale;

public class RecargaFormActivity extends AppCompatActivity {

    private CurrencyEditText edtValor;

    private MaskEditText edtTelefone;

    private AlertDialog dialog;

    private ProgressBar progressBar;

    private Usuario usuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recarga_form);

        iniciaComponentes();

        recuperaUsuario();

        configToolbar();
    }

    private void recuperaUsuario() {
        DatabaseReference usuarioRef = FirebaseHelper.getDatabaseReference()
                .child("usuarios")
                .child(FirebaseHelper.getIdFirebase());

        usuarioRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usuario = snapshot.getValue(Usuario.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void validaRecarga(View view) {
        double valorRecarga = (double) edtValor.getRawValue() / 100;
        String numeroTelefone = edtTelefone.getUnMasked().trim();

        double saldoContaUsuario = usuario.getSaldo();

        if(saldoContaUsuario >= valorRecarga) {
            if (valorRecarga >= 15) {
                if (!numeroTelefone.isEmpty()) {
                    if (numeroTelefone.length() == 11) {
                        progressBar.setVisibility(View.VISIBLE);

                        ocultarTeclado();

                        salvarExtrato(valorRecarga, numeroTelefone);
                    } else {
                        showDialog("Informe um n??mero de telefone v??lido");
                    }
                } else {
                    showDialog("Informe o n??mero de telefone para recarga");
                }
            } else {
                showDialog("Digite um valor maior que R$ 15,00");
            }
        } else {
            showDialog("Saldo em conta insuficiente para efetuar a recarga");
        }
    }

    private void salvarExtrato(double valorRecarga, String numeroTelefone) {
        Extrato extrato = new Extrato();
        extrato.setOperacao("RECARGA");
        extrato.setValor(valorRecarga);
        extrato.setTipo("SA??DA");
        extrato.setNumeroTelefone(numeroTelefone);

        DatabaseReference extratoRef = FirebaseHelper.getDatabaseReference()
                .child("extratos")
                .child(FirebaseHelper.getIdFirebase())
                .child(extrato.getId());

        extratoRef.setValue(extrato).addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                DatabaseReference updateExtratoRef = extratoRef.child("data");
                updateExtratoRef.setValue(ServerValue.TIMESTAMP);

                salvarRecarga(extrato);
            } else {
                showDialog("Erro ao efetuar a recarga. Motivo: " + task.getException().getMessage());
            }
        });
    }

    private void salvarRecarga(Extrato extrato) {
        Recarga recarga = new Recarga();
        recarga.setId(extrato.getId());
        recarga.setValor(extrato.getValor());
        recarga.setTelefone(extrato.getNumeroTelefone());

        DatabaseReference recargaRef = FirebaseHelper.getDatabaseReference()
                .child("recargas")
                .child(recarga.getId());

        recargaRef.setValue(recarga).addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                DatabaseReference updateRecargaRef = recargaRef.child("data");
                updateRecargaRef.setValue(ServerValue.TIMESTAMP);

                usuario.setSaldo(usuario.getSaldo() - recarga.getValor());
                usuario.atualizarSaldo();

                Intent intent = new Intent(this, RecargaReciboActivity.class);
                intent.putExtra("idRecarga", recarga.getId());

                startActivity(intent);

                finish();
            } else {
                progressBar.setVisibility(View.GONE);

                showDialog("Erro ao efetuar a recarga. Motivo: " + task.getException().getMessage());
            }
        });
    }

    private void showDialog(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog);

        View view = getLayoutInflater().inflate(R.layout.layout_dialog_info, null);

        TextView textTitulo = view.findViewById(R.id.textTitulo);
        textTitulo.setText("Aten????o!");

        TextView textMensagem = view.findViewById(R.id.textMensagem);
        textMensagem.setText(msg);

        Button btnOk = view.findViewById(R.id.btnOk);
        btnOk.setOnClickListener(v -> {
            dialog.dismiss();
        });

        builder.setView(view);

        dialog = builder.create();
        dialog.show();
    }

    private void configToolbar() {
        TextView textTitulo = findViewById(R.id.textTitulo);
        textTitulo.setText("Recarregar");

        findViewById(R.id.ibVoltar).setOnClickListener(v -> {
            finish();
        });
    }

    private void iniciaComponentes() {
        edtValor = findViewById(R.id.edtValor);
        edtValor.setLocale(new Locale("PT", "br"));

        edtTelefone = findViewById(R.id.edtTelefone);

        progressBar = findViewById(R.id.progressBar);
    }

    // Oculta o teclado do dispositivo
    private void ocultarTeclado() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(edtValor.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

}