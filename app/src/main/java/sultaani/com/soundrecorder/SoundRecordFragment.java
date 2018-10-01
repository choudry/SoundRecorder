package sultaani.com.soundrecorder;


import android.content.Context;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.awesomedialog.blennersilva.awesomedialoglibrary.AwesomeErrorDialog;
import com.awesomedialog.blennersilva.awesomedialoglibrary.AwesomeProgressDialog;
import com.awesomedialog.blennersilva.awesomedialoglibrary.AwesomeSuccessDialog;
import com.awesomedialog.blennersilva.awesomedialoglibrary.interfaces.Closure;
import com.dinuscxj.progressbar.CircleProgressBar;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static android.Manifest.permission.INTERNET;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;


/**
 * A simple {@link Fragment} subclass.
 */
public class SoundRecordFragment extends Fragment {

    Chronometer chstart;
    CircleProgressBar progressBar;
    TextView tvtime;
    Context context;

    Long minute, hour, second;

    Long startMillis, currentMillis;

    ImageView ivrecord, ivstop;

    String AudioSavePathInDevice = null;
    MediaRecorder mediaRecorder;
    Random random;

    AwesomeProgressDialog awesomeProgressDialog;


    FirebaseStorage storage = FirebaseStorage.getInstance();

    StorageReference storageRef = storage.getReferenceFromUrl("gs://soundrecorder-e16e5.appspot.com/").child("audiofiles");
    DatabaseReference root = FirebaseDatabase.getInstance().getReference().getRoot().child("SoundRecorder");


    public static final int RequestPermissionCode = 1;


    MainInterface mainInterface;

    int filecount;
    Uri selectedFileUri;


    public SoundRecordFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_sound_record, container, false);
        context = getContext();

        chstart = view.findViewById(R.id.chstartrecording);
        tvtime = view.findViewById(R.id.tvtime);
        progressBar = view.findViewById(R.id.line_progress);
        ivrecord = view.findViewById(R.id.ivrecord);
        ivstop = view.findViewById(R.id.ivstop);



        startMillis = null;
        currentMillis = null;

        random = new Random();

        ivrecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startStudyTime();

            }
        });

        ivstop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                awesomeProgressDialog = new AwesomeProgressDialog(context)
                        .setTitle(R.string.app_name)
                        .setMessage("Saving your record...")
                        .setColoredCircle(R.color.dialogInfoBackgroundColor)
                        .setDialogIconAndColor(R.drawable.ic_dialog_info, R.color.white)
                        .setCancelable(true);
                awesomeProgressDialog.show();

                mediaRecorder.stop();
                progressBar.setProgress(0);

                Toast.makeText(context, "Recording Completed",
                        Toast.LENGTH_LONG).show();

                ivstop.setVisibility(View.GONE);
                ivrecord.setVisibility(View.VISIBLE);

                chstart.stop();
                tvtime.setText("00:00:00");

                selectedFileUri = Uri.fromFile(new File(AudioSavePathInDevice));
                try {
                    sendFileFirebase(storageRef,selectedFileUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }



            }
        });

        chstart.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer chronometer) {

                Calendar cl = Calendar.getInstance();
                currentMillis = System.currentTimeMillis();
                long millidiff = currentMillis - startMillis;
                cl.setTimeInMillis(millidiff);  //here your time in miliseconds

                long seconds = millidiff / 1000;
                long minutes = seconds / 60;
                long hours = minutes / 60;
                long days = hours / 24;
                long months = days / 30;


                hour = hours % 24;
                minute = minutes % 60;
                second = seconds % 60;

                String stime = ((hour < 10 ? "0" + hour : hour) + ":") + ((minute < 10 ? "0" + minute : minute) + ":") + ((second < 10 ? "0" + second : second) + "");
                tvtime.setText(stime);
                progressBar.setProgress(Integer.parseInt(second.toString()));


            }
        });
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mainInterface = (MainInterface) context;
    }

    void startStudyTime() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkPermission()) {


                File folder = new File(Environment.getExternalStorageDirectory() +
                        File.separator + "SoundRecorder");
                boolean success = true;
                if (!folder.exists()) {
                    success = folder.mkdirs();
                }
                if (success) {

                    if (Utill.getDataSP("count", context) == null) {
                        filecount = 1;
                        Utill.addDataSP("count", 1 + "", context);
                    } else {
                        filecount = Integer.parseInt(Utill.getDataSP("count", context));
                        Utill.addDataSP("count", (filecount + 1) + "", context);
                    }

                    AudioSavePathInDevice =
                            Environment.getExternalStorageDirectory().getAbsolutePath() + "/SoundRecorder/"
                                    + "Recording_" + filecount + ".3gp";
                    if (mediaRecorder != null)
                        mediaRecorder.release();
                    mediaRecorder = new MediaRecorder();
                    mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                    mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                    mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
                    mediaRecorder.setOutputFile(AudioSavePathInDevice);

                    try {
                        startMillis = System.currentTimeMillis();
                        mediaRecorder.prepare();
                        mediaRecorder.start();

                        Toast.makeText(context, "Recording started",
                                Toast.LENGTH_LONG).show();

                        startMillis = System.currentTimeMillis();

                        chstart.start();

                        ivrecord.setVisibility(View.GONE);
                        ivstop.setVisibility(View.VISIBLE);
                    } catch (IllegalStateException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        Utill.showToast("exception: " + e, context);
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        Utill.showToast("exception: " + e, context);
                    }


                } else {
                    // Do something else on failure
                }


            } else {
                requestPermission();
            }


        } else {

            File folder = new File(Environment.getExternalStorageDirectory() +
                    File.separator + "SoundRecorder");
            boolean success = true;
            if (!folder.exists()) {
                success = folder.mkdirs();
            }
            if (success) {
                if (Utill.getDataSP("count", context) == null) {
                    filecount = 1;
                    Utill.addDataSP("count", 1 + "", context);
                } else {
                    filecount = Integer.parseInt(Utill.getDataSP("count", context));
                    Utill.addDataSP("count", (filecount + 1) + "", context);
                }

                AudioSavePathInDevice =
                        Environment.getExternalStorageDirectory().getAbsolutePath() + "/SoundRecorder/"
                                + "Recording_" + filecount + ".3gp";

                mediaRecorder = new MediaRecorder();
                mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
                mediaRecorder.setOutputFile(AudioSavePathInDevice);


                try {
                    startMillis = System.currentTimeMillis();
                    mediaRecorder.prepare();
                    mediaRecorder.start();

                    Toast.makeText(context, "Recording started",
                            Toast.LENGTH_LONG).show();

                    startMillis = System.currentTimeMillis();

                    chstart.start();

                    ivrecord.setVisibility(View.GONE);
                    ivstop.setVisibility(View.VISIBLE);
                } catch (IllegalStateException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    Utill.showToast("exception: " + e, context);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    Utill.showToast("exception: " + e, context);
                }

            } else {
                // Do something else on failure
            }

        }


    }

    private void sendFileFirebase(StorageReference storageReference, final Uri file) throws IOException {
        if (storageReference != null) {
            final String name = "Recording_" + filecount+".3gp" ;
            StorageReference imageGalleryRef = storageReference.child(name);

            StorageMetadata metadata = new StorageMetadata.Builder()
                    .setContentType("audio/3gp")
                    .build();

            UploadTask uploadTask = imageGalleryRef.putFile(file,metadata);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    awesomeProgressDialog.hide();
                    Utill.showToast("Error while uploading file..",context);
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    Map<String, Object> map = new HashMap<>();
                    map.put("mediafile", taskSnapshot.getDownloadUrl().toString());
                    map.put("name",name);

                    root.child("Users").child(Utill.getDataSP("userid",context)).child("MediaFiles").push().updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()) {
                                awesomeProgressDialog.hide();
                                final AwesomeSuccessDialog awesomeSuccessDialog = new AwesomeSuccessDialog(context);
                                awesomeSuccessDialog.setTitle(R.string.app_name)
                                        .setMessage("File uploaded successfully")
                                        .setColoredCircle(R.color.dialogNoticeBackgroundColor)
                                        .setDialogIconAndColor(R.drawable.ic_notice, R.color.white)
                                        .setCancelable(true)
                                        .setPositiveButtonText(getString(R.string.dialog_ok_button))
                                        .setPositiveButtonbackgroundColor(R.color.dialogSuccessBackgroundColor)
                                        .setPositiveButtonClick(new Closure() {
                                            @Override
                                            public void exec() {
                                                awesomeSuccessDialog.hide();

                                            }
                                        }).show();
                            } else {

                                awesomeProgressDialog.hide();

                                final AwesomeErrorDialog awesomeErrorDialog = new AwesomeErrorDialog(context);
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


    private void requestPermission() {

        ActivityCompat.requestPermissions(getActivity(), new
                String[]{WRITE_EXTERNAL_STORAGE, RECORD_AUDIO,INTERNET}, RequestPermissionCode);
    }

    public boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(context,
                WRITE_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(context,
                RECORD_AUDIO);
        int result2 = ContextCompat.checkSelfPermission(context,
                INTERNET);
        return result == PackageManager.PERMISSION_GRANTED &&
                result1 == PackageManager.PERMISSION_GRANTED &&
                result2 == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case RequestPermissionCode:
                if (grantResults.length > 0) {
                    boolean StoragePermission = grantResults[0] ==
                            PackageManager.PERMISSION_GRANTED;
                    boolean RecordPermission = grantResults[1] ==
                            PackageManager.PERMISSION_GRANTED;

                    boolean InternetPermission = grantResults[2] ==
                            PackageManager.PERMISSION_GRANTED;

                    if (StoragePermission && RecordPermission && InternetPermission) {

                        Toast.makeText(getActivity(), "Permission Granted",
                                Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getActivity(), "Permission Denied", Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }



}
