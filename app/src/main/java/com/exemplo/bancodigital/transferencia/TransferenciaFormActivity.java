package com.exemplo.bancodigital.transferencia;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.blackcat.currencyedittext.CurrencyEditText;
import com.exemplo.bancodigital.R;
import com.exemplo.bancodigital.deposito.DepositoReciboActivity;
import com.exemplo.bancodigital.helper.FirebaseHelper;
import com.exemplo.bancodigital.model.Deposito;
import com.exemplo.bancodigital.model.Extrato;
import com.exemplo.bancodigital.model.Usuario;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.Locale;

public class TransferenciaFormActivity extends AppCompatActivity {

    private CurrencyEditText edtValor;

    private AlertDialog dialog;

    private ProgressBar progressBar;

    private Usuario usuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transferencia_form);

        configToolbar();

        recuperaUsuario();

        iniciaComponentes();
    }

    public void validaTransferencia(View view) {
        double valorTransferencia = (double) edtValor.getRawValue() / 100;

        if(usuario.getSaldo() >= valorTransferencia) {
            if (valorTransferencia > 0) {
                ocultarTeclado();

                progressBar.setVisibility(View.VISIBLE);

                salvarExtrato(valorTransferencia);
            } else {
                showDialog("Digite um valor maior que 0");
            }
        } else {
            showDialog("Saldo em conta insuficiente para efetuar a transferência");
        }
    }

    private void salvarExtrato(Double valorTransferencia) {
        Extrato extrato = new Extrato();
        extrato.setOperacao("TRANSFERÊNCIA");
        extrato.setValor(valorTransferencia);
        extrato.setTipo("SAÍDA");

        DatabaseReference extratoRef = FirebaseHelper.getDatabaseReference()
                .child("extratos")
                .child(FirebaseHelper.getIdFirebase())
                .child(extrato.getId());

        extratoRef.setValue(extrato).addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                DatabaseReference updateExtratoRef = extratoRef.child("data");
                updateExtratoRef.setValue(ServerValue.TIMESTAMP);

                salvarTransferencia(extrato);
            } else {
                showDialog("Erro ao efetuar a transferência. Motivo: " + task.getException().getMessage());
            }
        });
    }

    private void salvarTransferencia(Extrato extrato) {
        Deposito deposito = new Deposito();
        deposito.setId(extrato.getId());
        deposito.setValor(extrato.getValor());

        DatabaseReference depositoRef = FirebaseHelper.getDatabaseReference()
                .child("depositos")
                .child(deposito.getId());

        depositoRef.setValue(deposito).addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                DatabaseReference updateDepositoRef = depositoRef.child("data");
                updateDepositoRef.setValue(ServerValue.TIMESTAMP);

                usuario.setSaldo(usuario.getSaldo() + deposito.getValor());
                usuario.atualizarSaldo();

                Intent intent = new Intent(this, DepositoReciboActivity.class);
                intent.putExtra("idDeposito", deposito.getId());

                startActivity(intent);

                finish();
            } else {
                progressBar.setVisibility(View.GONE);

                showDialog("Erro ao efetuar o depósito. Motivo: " + task.getException().getMessage());
            }
        });
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

    private void configToolbar() {
        TextView textTitulo = findViewById(R.id.textTitulo);
        textTitulo.setText("Transferir");

        findViewById(R.id.ibVoltar).setOnClickListener(v -> {
            finish();
        });
    }

    private void showDialog(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog);

        View view = getLayoutInflater().inflate(R.layout.layout_dialog_info, null);

        TextView textTitulo = view.findViewById(R.id.textTitulo);
        textTitulo.setText("Atenção!");

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

    private void iniciaComponentes() {
        edtValor = findViewById(R.id.edtValor);
        edtValor.setLocale(new Locale("PT", "br"));

        progressBar = findViewById(R.id.progressBar);
    }

    // Oculta o teclado do dispositivo
    private void ocultarTeclado() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(edtValor.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

}