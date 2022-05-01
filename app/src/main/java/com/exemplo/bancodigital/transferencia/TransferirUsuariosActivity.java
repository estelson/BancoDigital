package com.exemplo.bancodigital.transferencia;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.exemplo.bancodigital.R;
import com.exemplo.bancodigital.adapter.UsuarioAdapter;
import com.exemplo.bancodigital.helper.FirebaseHelper;
import com.exemplo.bancodigital.model.Usuario;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class TransferirUsuariosActivity extends AppCompatActivity {

    private UsuarioAdapter usuarioAdapter;
    private final List<Usuario> usuarioList = new ArrayList<>();

    private RecyclerView rvUsuarios;

    private EditText edtPesquisa;

    private TextView textInfo;

    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transferir_usuarios);

        configToolbar();

        iniciaComponentes();

        configRv();

        recuperaUsuario();
    }

    private void configRv() {
        rvUsuarios.setLayoutManager(new LinearLayoutManager(this));
        rvUsuarios.setHasFixedSize(true);

        usuarioAdapter = new UsuarioAdapter(usuarioList);

        rvUsuarios.setAdapter(usuarioAdapter);
    }

    private void configToolbar() {
        TextView textTitulo = findViewById(R.id.textTitulo);
        textTitulo.setText("Selecione o usuário");

        findViewById(R.id.ibVoltar).setOnClickListener(v -> {
            finish();
        });
    }

    // Oculta o teclado do dispositivo
    private void ocultarTeclado() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(edtPesquisa.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    private void iniciaComponentes() {
        rvUsuarios = findViewById(R.id.rvUsuarios);

        edtPesquisa = findViewById(R.id.edtPesquisa);

        textInfo = findViewById(R.id.textInfo);

        progressBar = findViewById(R.id.progressBar);
    }

    private void recuperaUsuario() {
        DatabaseReference usuarioRef = FirebaseHelper.getDatabaseReference()
                .child("usuarios");

        usuarioRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    for(DataSnapshot ds : snapshot.getChildren()) {
                        Usuario usuario = ds.getValue(Usuario.class);

                        if(usuario != null) {
                            if (!usuario.getId().equals(FirebaseHelper.getIdFirebase())) {
                                usuarioList.add(usuario);
                            }
                        }
                    }

                    textInfo.setText("");
                } else {
                    textInfo.setText("Nenhum usuário encontrado");
                }

                progressBar.setVisibility(View.GONE);

                usuarioAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}