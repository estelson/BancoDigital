package com.exemplo.bancodigital.transferencia;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.exemplo.bancodigital.R;
import com.exemplo.bancodigital.helper.FirebaseHelper;
import com.exemplo.bancodigital.helper.GetMask;
import com.exemplo.bancodigital.model.Transferencia;
import com.exemplo.bancodigital.model.Usuario;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class TransferenciaConfirmaActivity extends AppCompatActivity {

    private TextView textValor;
    private TextView textUsuario;

    private ImageView imagemUsuario;

    private Usuario usuarioDestino;
    private Usuario usuarioOrigem;

    private Transferencia transferencia;

    private AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transferencia_confirma);

        configToolbar();

        iniciaComponentes();

        configDados();

        recuperaUsuarioOrigem();
    }

    public void confirmarTransferencia(View view) {
        if(transferencia != null) {
            salvarTransferencia();
        } else {
            showDialog("Ocorreu um erro ao finalizar a transferência");
        }
    }

    private void salvarTransferencia() {
        DatabaseReference transferenciaRef = FirebaseHelper.getDatabaseReference()
                .child("transferencias")
                .child(transferencia.getId());

        transferenciaRef.setValue(transferencia).addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                usuarioOrigem.setSaldo(usuarioOrigem.getSaldo() - transferencia.getValor());
                usuarioOrigem.atualizarSaldo();

                usuarioDestino.setSaldo(usuarioDestino.getSaldo() + transferencia.getValor());
                usuarioDestino.atualizarSaldo();

                Intent intent = new Intent(this, TransferenciaReciboActivity.class);
                intent.putExtra("idTransferencia", transferencia.getId());

                startActivity(intent);
            } else {
                showDialog("Não foi possível efetuar a transferência. Motivo: " + task.getException().getMessage());
            }
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

    private void recuperaUsuarioOrigem() {
        DatabaseReference usuarioRef = FirebaseHelper.getDatabaseReference()
                .child("usuarios")
                .child(transferencia.getIdUserOrigem());
//                .child(FirebaseHelper.getIdFirebase());

        usuarioRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usuarioOrigem = snapshot.getValue(Usuario.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void configDados() {
        usuarioDestino = (Usuario) getIntent().getSerializableExtra("usuario");
        transferencia = (Transferencia) getIntent().getSerializableExtra("transferencia");

        textUsuario.setText(usuarioDestino.getNome());
        if(usuarioDestino.getUrlImagem() != null) {
            Picasso.get().load(usuarioDestino.getUrlImagem())
                    .placeholder(R.drawable.loading)
                    .into(imagemUsuario);
        }

        textValor.setText(getString(R.string.text_valor, GetMask.getValor(transferencia.getValor())));
    }

    private void configToolbar() {
        TextView textTitulo = findViewById(R.id.textTitulo);
        textTitulo.setText("Confirme os dados");
    }

    private void iniciaComponentes() {
        textValor = findViewById(R.id.textValor);
        textUsuario = findViewById(R.id.textUsuario);

        imagemUsuario = findViewById(R.id.imagemUsuario);
    }

}