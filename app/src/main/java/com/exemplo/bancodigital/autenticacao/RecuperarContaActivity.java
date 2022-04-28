package com.exemplo.bancodigital.autenticacao;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.exemplo.bancodigital.R;
import com.exemplo.bancodigital.helper.FirebaseHelper;

public class RecuperarContaActivity extends AppCompatActivity {

    private EditText edtEmail;

    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recuperar_conta);

        iniciaComponentes();
    }

    public void validaDados(View view ) {
        String email = edtEmail.getText().toString().trim();

        if (!email.isEmpty()) {
            progressBar.setVisibility(View.VISIBLE);

            recuperarConta(email);
        } else {
            edtEmail.requestFocus();
            edtEmail.setError("Informe seu email");
        }
    }

    private void recuperarConta(String email) {
        FirebaseHelper.getAuth().sendPasswordResetEmail(email).addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                Toast.makeText(this, "Acabamos de te enviar um link via e-mail", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }

            progressBar.setVisibility(View.GONE);
        });
    }

    private void iniciaComponentes() {
        edtEmail = findViewById(R.id.edtEmail);

        progressBar = findViewById(R.id.progressBar);
    }

}