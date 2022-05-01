package com.exemplo.bancodigital.transferencia;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
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
    private LinearLayout llPesquisa;
    private TextView textPesquisa;
    private TextView textLimpar;
    private String pesquisa = "";

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

        configPesquisa();

        configCliques();
    }

    private void configCliques() {
        textLimpar.setOnClickListener(v -> {
            pesquisa = "";

            configFiltro();

            recuperaUsuario();

            ocultarTeclado();
        });
    }

    private void configPesquisa() {
        edtPesquisa.setOnEditorActionListener((v, actionId, event) -> {
            if(actionId == EditorInfo.IME_ACTION_SEARCH) {
                ocultarTeclado();

                progressBar.setVisibility(View.VISIBLE);

                pesquisa = v.getText().toString();
                if(!pesquisa.equals("")) {
                    configFiltro();

                    pesquisaUsuarios();
                } else {
                    recuperaUsuario();

                    configFiltro();
                }
            }

            return false;
        });
    }

    private void pesquisaUsuarios() {
        for(Usuario usuario : new ArrayList<>(usuarioList)) {
            if(!usuario.getNome().toLowerCase().contains(pesquisa.toLowerCase())) {
                usuarioList.remove(usuario);
            }
        }

        if(usuarioList.isEmpty()) {
            textInfo.setText("Nenhum usuário encontrado com este nome");
        }

        progressBar.setVisibility(View.GONE);

        usuarioAdapter.notifyDataSetChanged();
    }

    private void configFiltro() {
        if(!pesquisa.equals("")) {
            textPesquisa.setText("Pesquisa: " + pesquisa);
            llPesquisa.setVisibility(View.VISIBLE);
        } else {
            textPesquisa.setText("");
            llPesquisa.setVisibility(View.GONE);
        }
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
        llPesquisa = findViewById(R.id.llPesquisa);
        textPesquisa = findViewById(R.id.textPesquisa);
        textLimpar = findViewById(R.id.textLimpar);

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
                    usuarioList.clear();

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