package sultaani.com.soundrecorder;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.awesomedialog.blennersilva.awesomedialoglibrary.AwesomeErrorDialog;
import com.awesomedialog.blennersilva.awesomedialoglibrary.AwesomeProgressDialog;
import com.awesomedialog.blennersilva.awesomedialoglibrary.AwesomeWarningDialog;
import com.awesomedialog.blennersilva.awesomedialoglibrary.interfaces.Closure;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    EditText etemail,etpassword;
    Button btnsignin,btnsignup;
    String stemail,stpassword;
    ImageView ivlogo;

    AwesomeProgressDialog awesomeProgressDialog;

    private FirebaseAuth auth;
    private String userid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initViews();
        //Firebase.setAndroidContext(this);

        auth = FirebaseAuth.getInstance();

        btnsignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this,RegistrationActivity.class));
                finish();

            }
        });

        btnsignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                awesomeProgressDialog = new AwesomeProgressDialog(LoginActivity.this)
                        .setTitle(R.string.app_name)
                        .setMessage("Finding your account")
                        .setColoredCircle(R.color.dialogInfoBackgroundColor)
                        .setDialogIconAndColor(R.drawable.ic_dialog_info, R.color.white)
                        .setCancelable(true);
                awesomeProgressDialog.show();
                stemail = etemail.getText().toString();
                stpassword = etpassword.getText().toString();


                if (!Utill.verifyConection(LoginActivity.this)){
                    awesomeProgressDialog.hide();
                    final AwesomeWarningDialog awesomeWarningDialog =  new AwesomeWarningDialog(LoginActivity.this);
                    awesomeWarningDialog.setTitle(R.string.app_name)
                            .setMessage("Internet connection not available..!")
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
                }else if (stemail.trim().isEmpty()||stpassword.trim().isEmpty()){
                    awesomeProgressDialog.hide();
                    final AwesomeWarningDialog awesomeWarningDialog =  new AwesomeWarningDialog(LoginActivity.this);
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
                }else {
                    auth.signInWithEmailAndPassword(stemail, stpassword).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                final User user = new User();
                                awesomeProgressDialog.hide();
                                userid = task.getResult().getUser().getUid();
                                user.setId(userid);


                                DatabaseReference ro = FirebaseDatabase.getInstance().getReference().getRoot().child("SoundRecorder").child("Users").child(userid);
                                ro.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.getValue()==null){

                                            Utill.addDataSP("userid",userid, LoginActivity.this);

                                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                                            user.delete()
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
                                                                Log.i("Log","account deleted successfully");
                                                                awesomeProgressDialog.hide();
                                                                final AwesomeErrorDialog awesomeErrorDialog = new AwesomeErrorDialog(LoginActivity.this);
                                                                awesomeErrorDialog.setTitle(R.string.app_name)
                                                                        .setMessage("Record not found..!")
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
                                            return;

                                        }

                                                String res_email = dataSnapshot.child("email").getValue()+"";
                                                String res_name = dataSnapshot.child("name").getValue()+"";
                                                String res_pic = dataSnapshot.child("profilepic").getValue()+"";
                                                user.setEmail(res_email);
                                                user.setName(res_name);
                                                user.setPic(res_pic);
                                                Utill.current1 = user;
                                                Utill.addDataSP("email",res_email,LoginActivity.this);
                                                Utill.addDataSP("name",res_name,LoginActivity.this);
                                                Utill.addDataSP("pic",res_pic,LoginActivity.this);


                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                                Utill.addDataSP("login","true",LoginActivity.this);
                                Utill.addDataSP("userid",userid,LoginActivity.this);
                                Intent i = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(i);
                                finish();

                            } else {
                                awesomeProgressDialog.hide();
                                final AwesomeErrorDialog awesomeErrorDialog = new AwesomeErrorDialog(LoginActivity.this);
                                awesomeErrorDialog.setTitle(R.string.app_name)
                                        .setMessage("Record not found..!")
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
                                (Toast.makeText(LoginActivity.this, "Email and password mismatches", Toast.LENGTH_LONG)).show();
                            }
                        }
                    });


                }


            }
        });
    }

    private void initViews() {
        ivlogo = (ImageView) findViewById(R.id.logo);
        etemail = (EditText) findViewById(R.id.etemail);
        etpassword = (EditText) findViewById(R.id.etpassword);
        btnsignin = (Button) findViewById(R.id.btnsignin);
        btnsignup = (Button) findViewById(R.id.btnsignup);

        ivlogo.startAnimation(AnimationUtils.loadAnimation(LoginActivity.this, R.anim.blink));
        etemail.startAnimation(AnimationUtils.loadAnimation(LoginActivity.this, R.anim.blink));
        etpassword.startAnimation(AnimationUtils.loadAnimation(LoginActivity.this, R.anim.blink));
        btnsignin.startAnimation(AnimationUtils.loadAnimation(LoginActivity.this, R.anim.blink));
        btnsignup.startAnimation(AnimationUtils.loadAnimation(LoginActivity.this, R.anim.blink));

    }
}
