package com.exemplo.bancodigital.autenticacao;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.exemplo.bancodigital.app.MainActivity;
import com.exemplo.bancodigital.R;
import com.exemplo.bancodigital.helper.FirebaseHelper;
import com.exemplo.bancodigital.model.Usuario;
import com.google.firebase.database.DatabaseReference;

public class CadastroActivity extends AppCompatActivity {

    private EditText edtNome;
    private EditText edtEmail;
    private EditText edtTelefone;
    private EditText edtSenha;
    private EditText edtConfirmaSenha;

    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        iniciaComponentes();
    }

    public void validaDados(View view ) {
        String nome = edtNome.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String telefone = edtTelefone.getText().toString().trim();
        String senha = edtSenha.getText().toString().trim();
        String confirmaSenha = edtConfirmaSenha.getText().toString().trim();

        if(!nome.isEmpty()) {
            if(!email.isEmpty()) {
                if(!telefone.isEmpty()) {
                    if(!senha.isEmpty()) {
                        if(!confirmaSenha.isEmpty()) {
                            if(senha.equals(confirmaSenha)) {
                                progressBar.setVisibility(View.VISIBLE);

                                Usuario usuario = new Usuario();
                                usuario.setNome(nome);
                                usuario.setEmail(email);
                                usuario.setTelefone(telefone);
                                usuario.setSenha(senha);
                                usuario.setSaldo(0);

                                cadastrarUsuario(usuario);
                            } else {
                                edtSenha.setError("Senhas n??o conferem");
                                edtConfirmaSenha.setError("Senhas n??o conferem");
                            }
                        } else {
                            edtConfirmaSenha.requestFocus();
                            edtConfirmaSenha.setError("Confirme a senha");
                        }
                    } else {
                        edtSenha.requestFocus();
                        edtSenha.setError("Informe a senha");
                    }
                } else {
                    edtTelefone.requestFocus();
                    edtTelefone.setError("Informe o telefone");
                }
            } else {
                edtEmail.requestFocus();
                edtEmail.setError("Informe o email");
            }
        } else {
            edtNome.requestFocus();
            edtNome.setError("Informe o nome");
        }
    }

    private void cadastrarUsuario(Usuario usuario) {
        FirebaseHelper.getAuth().createUserWithEmailAndPassword(
                usuario.getEmail(), usuario.getSenha()
        ).addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                String id = task.getResult().getUser().getUid();
                usuario.setId(id);

                salvarDadosUsuario(usuario);
            } else {
                progressBar.setVisibility(View.GONE);

                Toast.makeText(this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void salvarDadosUsuario(Usuario usuario) {
        DatabaseReference usuarioRef = FirebaseHelper.getDatabaseReference()
                .child("usuarios")
                .child(usuario.getId());

        usuarioRef.setValue(usuario).addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                finish();

                startActivity(new Intent(this, MainActivity.class));
            } else {
                progressBar.setVisibility(View.GONE);

                Toast.makeText(this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void iniciaComponentes() {
        edtNome = findViewById(R.id.edtNome);
        edtEmail = findViewById(R.id.edtEmail);
        edtTelefone = findViewById(R.id.edtTelefone);
        edtSenha = findViewById(R.id.edtSenha);
        edtConfirmaSenha = findViewById(R.id.edtConfirmaSenha);

        progressBar = findViewById(R.id.progressBar);
    }

}