package com.exemplo.bancodigital.usuario;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.exemplo.bancodigital.R;
import com.exemplo.bancodigital.helper.FirebaseHelper;
import com.exemplo.bancodigital.model.Usuario;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.normal.TedPermission;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;

public class MinhaContaActivity extends AppCompatActivity {

    private final int REQUEST_GALERIA = 100;

    private ImageView imagemPerfil;

    private EditText edtNome;
    private EditText edtEmail;
    private EditText edtTelefone;

    private ProgressBar progressBar;

    private String caminhoImagem;

    private Usuario usuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_minha_conta);

        configToolbar();

        iniciaComponentes();

        configDados();

        configCliques();
    }

    private void configCliques() {
        imagemPerfil.setOnClickListener(v -> {
            verificaPermissaoGaleria();
        });
    }

    private void verificaPermissaoGaleria() {
        PermissionListener permissionListener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                abrirGaleria();
            }

            @Override
            public void onPermissionDenied(List<String> deniedPermissions) {
                Toast.makeText(MinhaContaActivity.this, "Permiss??o negada", Toast.LENGTH_SHORT).show();
            }
        };

        TedPermission.create()
                .setPermissionListener(permissionListener)
                .setDeniedTitle("Permiss??o negada")
                .setDeniedMessage("Permiss??o de acesso ?? galeria do dispositivo negada. Deseja ativar agora?")
                .setDeniedCloseButtonText("N??o")
                .setGotoSettingButtonText("Sim")
                .setPermissions(Manifest.permission.READ_EXTERNAL_STORAGE)
                .check();
    }

    private void abrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        startActivityForResult(intent, REQUEST_GALERIA);
    }

    public void validaDados(View view) {
        String nome = edtNome.getText().toString().trim();
        String telefone = edtTelefone.getText().toString().trim();

        if (!nome.isEmpty()) {
            if (!telefone.isEmpty()) {
                ocultarTeclado();

                progressBar.setVisibility(View.VISIBLE);

                usuario.setNome(nome);
                usuario.setTelefone(telefone);

                if(caminhoImagem != null) {
                    salvarImagemFirebase(true);
                } else {
                    salvarImagemFirebase(false);
                }
            } else {
                edtTelefone.requestFocus();
                edtTelefone.setError("informe o telefone");
            }
        } else {
            edtNome.requestFocus();
            edtNome.setError("informe o nome");
        }
    }

    private void salvarImagemFirebase(boolean temImagem) {
        if(temImagem) {
            StorageReference storageReference = FirebaseHelper.getStorageReference()
                    .child("imagens")
                    .child("perfil")
                    .child(FirebaseHelper.getIdFirebase() + ".jpeg");

            UploadTask uploadTask = storageReference.putFile(Uri.parse(caminhoImagem));
            uploadTask.addOnSuccessListener(taskSnapshot -> storageReference.getDownloadUrl().addOnCompleteListener(task -> {
                usuario.setUrlImagem(task.getResult().toString());
                salvarDadosUsuario();
            })).addOnFailureListener(e -> Toast.makeText(this, "Erro ao gravar imagem. Motivo: " + e.getMessage(), Toast.LENGTH_LONG).show());
        } else {
            salvarDadosUsuario();
        }
    }

    private void salvarDadosUsuario() {
        DatabaseReference usuarioRef = FirebaseHelper.getDatabaseReference()
                .child("usuarios")
                .child(usuario.getId());

        usuarioRef.setValue(usuario).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Informa????es do usu??rio salvas com sucesso", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Erro ao salvar as informa????es do usu??rio. Motivo: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }

            progressBar.setVisibility(View.GONE);
        });
    }

    private void configDados() {
        usuario = (Usuario) getIntent().getSerializableExtra("usuario");

        edtNome.setText(usuario.getNome());
        edtTelefone.setText(usuario.getTelefone());
        edtEmail.setText(usuario.getEmail());

        if(usuario.getUrlImagem() != null) {
            Picasso.get().load(usuario.getUrlImagem()).placeholder(R.drawable.loading).into(imagemPerfil);
        }

        progressBar.setVisibility(View.GONE);
    }

    private void configToolbar() {
        TextView textTitulo = findViewById(R.id.textTitulo);
        textTitulo.setText("Perfil");

        findViewById(R.id.ibVoltar).setOnClickListener(v -> {
            finish();
        });
    }

    private void iniciaComponentes() {
        imagemPerfil = findViewById(R.id.imagemPerfil);

        edtNome = findViewById(R.id.edtNome);
        edtEmail = findViewById(R.id.edtEmail);
        edtTelefone = findViewById(R.id.edtTelefone);

        progressBar = findViewById(R.id.progressBar);
    }

    // Oculta o teclado do dispositivo
    private void ocultarTeclado() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(edtNome.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK) {
            if(requestCode == REQUEST_GALERIA) {
                Bitmap bitmap;

                Uri imagemSelecionada = data.getData();
                caminhoImagem = data.getData().toString();

                try {
                    if (Build.VERSION.SDK_INT < 28) {
                        bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imagemSelecionada);
                    } else {
                        ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(), imagemSelecionada);

                        bitmap = ImageDecoder.decodeBitmap(source);
                    }

                    imagemPerfil.setImageBitmap(bitmap);
                } catch (IOException e) {
                    Toast.makeText(this, "Erro ao carregar a imagem selecionada. Motivo: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}