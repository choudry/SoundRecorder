package sultaani.com.soundrecorder;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.util.Patterns;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.awesomedialog.blennersilva.awesomedialoglibrary.AwesomeErrorDialog;
import com.awesomedialog.blennersilva.awesomedialoglibrary.AwesomeProgressDialog;
import com.awesomedialog.blennersilva.awesomedialoglibrary.AwesomeSuccessDialog;
import com.awesomedialog.blennersilva.awesomedialoglibrary.AwesomeWarningDialog;
import com.awesomedialog.blennersilva.awesomedialoglibrary.interfaces.Closure;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class RegistrationActivity extends AppCompatActivity {

    EditText etemail,etpassword,etname;
    ImageView ivprofile;
    Button btnSignup;
    String stemail,stpassword,stname,stprofile,userid;
    Uri selectedImageUri;

    private static final int IMAGE_GALLERY_REQUEST = 1;

    AwesomeProgressDialog awesomeProgressDialog;

    private FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseStorage storage = FirebaseStorage.getInstance();


    StorageReference storageRef = storage.getReferenceFromUrl("gs://soundrecorder-e16e5.appspot.com").child("profilepics");
    DatabaseReference root = FirebaseDatabase.getInstance().getReference().getRoot().child("SoundRecorder").child("Users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        initViews();


        ivprofile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                photoGalleryIntent();
            }
        });

        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                awesomeProgressDialog = new AwesomeProgressDialog(RegistrationActivity.this)
                        .setTitle(R.string.app_name)
                        .setMessage("Creating your account")
                        .setColoredCircle(R.color.dialogInfoBackgroundColor)
                        .setDialogIconAndColor(R.drawable.ic_dialog_info, R.color.white)
                        .setCancelable(true);
                awesomeProgressDialog.show();

                stemail = etemail.getText().toString();
                stname = etname.getText().toString();
                stpassword = etpassword.getText().toString();

                if (!Utill.verifyConection(RegistrationActivity.this)){
                    awesomeProgressDialog.hide();
                    final AwesomeWarningDialog awesomeWarningDialog =  new AwesomeWarningDialog(RegistrationActivity.this);
                    awesomeWarningDialog.setTitle(R.string.app_name)
                            .setMessage("Required field(s) missing..!")
                            .setColoredCircle(R.color.dialogNoticeBackgroundColor)
                            .setDialogIconAndColor(R.drawable.ic_notice, R.color.white)
                            .setCancelable(true)
                            .setButtonText(getString(R.string.dialog_ok_button))
                            .setButtonBackgroundColor(R.color.dialogNoticeBackgroundColor)
                            .setButtonText(getString(R.string.dialog_ok_button))
                            .setWarningButtonClick(new Closure() {
                                @Override
                                public void exec() {
                                    awesomeWarningDialog.hide();
                                }
                            })
                            .show();
                    return;
                }else if (stname.trim().isEmpty()||stemail.trim().isEmpty()||stpassword.trim().isEmpty()){
                    awesomeProgressDialog.hide();
                    final AwesomeWarningDialog awesomeWarningDialog =  new AwesomeWarningDialog(RegistrationActivity.this);
                    awesomeWarningDialog.setTitle(R.string.app_name)
                            .setMessage("Required field(s) missing..!")
                            .setColoredCircle(R.color.dialogNoticeBackgroundColor)
                            .setDialogIconAndColor(R.drawable.ic_notice, R.color.white)
                            .setCancelable(true)
                            .setButtonText(getString(R.string.dialog_ok_button))
                            .setButtonBackgroundColor(R.color.dialogNoticeBackgroundColor)
                            .setButtonText(getString(R.string.dialog_ok_button))
                            .setWarningButtonClick(new Closure() {
                                @Override
                                public void exec() {
                                    awesomeWarningDialog.hide();
                                }
                            })
                            .show();
                    return;
                }else if (!checkValidEmail(stemail)){
                    awesomeProgressDialog.hide();
                    final AwesomeWarningDialog awesomeWarningDialog =  new AwesomeWarningDialog(RegistrationActivity.this);
                    awesomeWarningDialog.setTitle(R.string.app_name)
                            .setMessage("Enter a valid email..!")
                            .setColoredCircle(R.color.dialogNoticeBackgroundColor)
                            .setDialogIconAndColor(R.drawable.ic_notice, R.color.white)
                            .setCancelable(true)
                            .setButtonText(getString(R.string.dialog_ok_button))
                            .setButtonBackgroundColor(R.color.dialogNoticeBackgroundColor)
                            .setButtonText(getString(R.string.dialog_ok_button))
                            .setWarningButtonClick(new Closure() {
                                @Override
                                public void exec() {
                                    awesomeWarningDialog.hide();
                                }
                            })
                            .show();
                }else if (stpassword.length()<6){
                    awesomeProgressDialog.hide();
                    final AwesomeWarningDialog awesomeWarningDialog =  new AwesomeWarningDialog(RegistrationActivity.this);
                    awesomeWarningDialog.setTitle(R.string.app_name)
                            .setMessage("Password length must be atleast six digits..!")
                            .setColoredCircle(R.color.dialogNoticeBackgroundColor)
                            .setDialogIconAndColor(R.drawable.ic_notice, R.color.white)
                            .setCancelable(true)
                            .setButtonText(getString(R.string.dialog_ok_button))
                            .setButtonBackgroundColor(R.color.dialogNoticeBackgroundColor)
                            .setButtonText(getString(R.string.dialog_ok_button))
                            .setWarningButtonClick(new Closure() {
                                @Override
                                public void exec() {
                                    awesomeWarningDialog.hide();
                                }
                            })
                            .show();
                }else {

                    if (selectedImageUri == null){
                        awesomeProgressDialog.hide();
                        final AwesomeWarningDialog awesomeWarningDialog =  new AwesomeWarningDialog(RegistrationActivity.this);
                        awesomeWarningDialog.setTitle(R.string.app_name)
                                .setMessage("Please select your profile picture..!")
                                .setColoredCircle(R.color.dialogNoticeBackgroundColor)
                                .setDialogIconAndColor(R.drawable.ic_notice, R.color.white)
                                .setCancelable(true)
                                .setButtonText(getString(R.string.dialog_ok_button))
                                .setButtonBackgroundColor(R.color.dialogNoticeBackgroundColor)
                                .setButtonText(getString(R.string.dialog_ok_button))
                                .setWarningButtonClick(new Closure() {
                                    @Override
                                    public void exec() {
                                        awesomeWarningDialog.hide();
                                    }
                                })
                                .show();
                        return;
                    }else {

                        auth.createUserWithEmailAndPassword(stemail, stpassword).addOnCompleteListener(RegistrationActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    userid = task.getResult().getUser().getUid();

                                    try {
                                        sendFileFirebase(storageRef,selectedImageUri);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }


                                } else {
                                    awesomeProgressDialog.hide();

                                    final AwesomeErrorDialog awesomeErrorDialog = new AwesomeErrorDialog(RegistrationActivity.this);
                                    awesomeErrorDialog.setTitle(R.string.app_name)
                                            .setMessage("Email already exist..!")
                                            .setColoredCircle(R.color.dialogErrorBackgroundColor)
                                            .setDialogIconAndColor(R.drawable.ic_dialog_error, R.color.white)
                                            .setCancelable(true).setButtonText(getString(R.string.dialog_ok_button))
                                            .setButtonBackgroundColor(R.color.dialogErrorBackgroundColor)
                                            .setButtonText(getString(R.string.dialog_ok_button))
                                            .setErrorButtonClick(new Closure() {
                                                @Override
                                                public void exec() {
                                                    awesomeErrorDialog.hide();
                                                }
                                            })
                                            .show();
                                }
                            }
                        });

                    }

                }
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == IMAGE_GALLERY_REQUEST && resultCode == RESULT_OK && data.getData() != null) {
            Uri filePath = data.getData();
            selectedImageUri = data.getData();
            try {

                Bitmap bitmap = MediaStore.Images.Media.getBitmap(RegistrationActivity.this.getContentResolver(), filePath);
                ivprofile.setImageBitmap(bitmap);


            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private void initViews() {

        etemail = (EditText) findViewById(R.id.etemail);
        etname = (EditText) findViewById(R.id.etname);
        etpassword = (EditText) findViewById(R.id.etpassword);
        ivprofile = (ImageView) findViewById(R.id.ivprofile);
        btnSignup = (Button) findViewById(R.id.btnsignup);

        etemail.startAnimation(AnimationUtils.loadAnimation(RegistrationActivity.this, R.anim.blink));
        etname.startAnimation(AnimationUtils.loadAnimation(RegistrationActivity.this, R.anim.blink));
        etpassword.startAnimation(AnimationUtils.loadAnimation(RegistrationActivity.this, R.anim.blink));
        ivprofile.startAnimation(AnimationUtils.loadAnimation(RegistrationActivity.this, R.anim.blink));
        btnSignup.startAnimation(AnimationUtils.loadAnimation(RegistrationActivity.this, R.anim.blink));
    }

    /**
     * Intent for photo gallery
     */
    private void photoGalleryIntent(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), IMAGE_GALLERY_REQUEST);
    }

    private boolean checkValidEmail(String e) {
        return Patterns.EMAIL_ADDRESS.matcher(e).matches();

    }

    private void sendFileFirebase(StorageReference storageReference, final Uri file) throws IOException {
        if (storageReference != null) {
            final String name = DateFormat.format("yyyy-MM-dd_hhmmss", new Date()).toString();
            StorageReference imageGalleryRef = storageReference.child(name + "_gallery");

            Bitmap thumb = MediaStore.Images.Media.getBitmap(this.getContentResolver(), file);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            thumb.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] thumbByte = baos.toByteArray();

            UploadTask uploadTask = imageGalleryRef.putBytes(thumbByte);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    Map<String,Object> map = new HashMap<>();
                    map.put("name",stname);
                    map.put("email",stemail);
                    map.put("profilepic",taskSnapshot.getDownloadUrl().toString());

                    root.child(userid).updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                awesomeProgressDialog.hide();
                                final AwesomeSuccessDialog awesomeSuccessDialog =  new AwesomeSuccessDialog(RegistrationActivity.this);
                                awesomeSuccessDialog.setTitle(R.string.app_name)
                                        .setMessage("Your account created successfully")
                                        .setColoredCircle(R.color.dialogNoticeBackgroundColor)
                                        .setDialogIconAndColor(R.drawable.ic_notice, R.color.white)
                                        .setCancelable(true)
                                        .setPositiveButtonText(getString(R.string.dialog_ok_button))
                                        .setPositiveButtonbackgroundColor(R.color.dialogSuccessBackgroundColor)
                                        .setPositiveButtonClick(new Closure() {
                                            @Override
                                            public void exec() {
                                                awesomeSuccessDialog.hide();
                                                finish();
                                                startActivity(new Intent(RegistrationActivity.this,LoginActivity.class));
                                            }
                                        })

                                        .show();
                            }else {

                                awesomeProgressDialog.hide();

                                final AwesomeErrorDialog awesomeErrorDialog = new AwesomeErrorDialog(RegistrationActivity.this);
                                awesomeErrorDialog.setTitle(R.string.app_name)
                                        .setMessage("Error while creating account")
                                        .setColoredCircle(R.color.dialogErrorBackgroundColor)
                                        .setDialogIconAndColor(R.drawable.ic_dialog_error, R.color.white)
                                        .setCancelable(true).setButtonText(getString(R.string.dialog_ok_button))
                                        .setButtonBackgroundColor(R.color.dialogErrorBackgroundColor)
                                        .setButtonText(getString(R.string.dialog_ok_button))
                                        .setErrorButtonClick(new Closure() {
                                            @Override
                                            public void exec() {
                                                awesomeErrorDialog.hide();
                                            }
                                        })
                                        .show();

                            }
                        }
                    });


                }
            });
        } else {
            //IS NULL
        }

    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        startActivity(new Intent(RegistrationActivity.this,LoginActivity.class));
    }
}
