package sultaani.com.soundrecorder;


import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static sultaani.com.soundrecorder.SoundRecordFragment.RequestPermissionCode;


/**
 * A simple {@link Fragment} subclass.
 */
public class PlayRecordFragment extends Fragment  {

    ListView lvrecord;
    ArrayList<FilesModel> recordlist;
    ImageView ivclose;
    Context context;
    String path;


    MainInterface mainInterface;
    int duration;
    MediaPlayer mp;
    SeekBar seekBar;
    ImageView ivstop,ivplay;

    TextView tvstarttime,tvendtime,tvfilename;

    private Handler hdlr = new Handler();

    private static int sTime =0, eTime =0;

    DatabaseReference ref;
    ProgressDialog progressDialog;

    public PlayRecordFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_play_record, container, false);

        context = getContext();
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);

        lvrecord = view.findViewById(R.id.lvrecord);



        path = Environment.getExternalStorageDirectory().toString()+"/SoundRecorder";



        lvrecord.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                mp = new MediaPlayer();
                if (!Utill.verifyConection(context) || recordlist.get(i).getFolder() == null){
                    try {
                        mp.setDataSource(path + File.separator + recordlist.get(i).getName());
                        mp.prepare();
                        duration = mp.getDuration();
                        eTime = duration;

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    showDialog(getActivity(),recordlist.get(i).getFolder(),recordlist.get(i).getName());
                }else {
                    progressDialog.show();
                    fetchAudioUrlFromFirebase(recordlist.get(i).getFolder(),recordlist.get(i).getName());
                }


                mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {

                        ivstop.setVisibility(View.GONE);
                        ivplay.setVisibility(View.VISIBLE);

                    }
                });




            }
        });

        return view;
    }

    private void fetchAudioUrlFromFirebase(final String sname, final String stitle) {
        final FirebaseStorage storage = FirebaseStorage.getInstance();
        // Create a storage reference from our app

        StorageReference storageRef = storage.getReferenceFromUrl(sname);
        storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                try {
                    // Download url of file
                    final String url = uri.toString();
                    mp.setDataSource(url);
                    // wait for media player to get prepare
                    mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mediaPlayer) {
                            if (progressDialog.isShowing())
                                progressDialog.dismiss();
                            duration = mp.getDuration();
                            eTime = duration;
                            showDialog(getActivity(),sname,stitle);
                            mp.start();
                            ivstop.setVisibility(View.VISIBLE);
                            ivplay.setVisibility(View.GONE);
                        }
                    });
                    mp.prepareAsync();


                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i("TAG", e.getMessage());
                    }
                });

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mainInterface = (MainInterface) context;
    }

    @Override
    public void onResume() {
        super.onResume();






        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(!checkPermission()) {
                requestPermission();
            }else {
                if (Utill.verifyConection(context)){
                    loadOnlineData();
                    Intent service = new Intent(context, BackgroundService.class);
                    context.startService(service);
                }else
                    loadData();
            }

        }else {

            if (Utill.verifyConection(context)){
                loadOnlineData();
                Intent service = new Intent(context, BackgroundService.class);
                context.startService(service);
            }else
                loadData();
        }

    }

    void loadOnlineData(){
        DatabaseReference ref =
                FirebaseDatabase.getInstance().getReference().getRoot().child("SoundRecorder")
                        .child("Users").child(Utill.getDataSP("userid",context)).child("MediaFiles");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                recordlist = new ArrayList<>();
                for (DataSnapshot ds : dataSnapshot.getChildren()){
                    FilesModel model = new FilesModel();
                    model.setName(ds.child("name").getValue()+"");
                    model.setFolder(ds.child("mediafile").getValue()+"");
                    model.setKey(ds.getKey());
                    recordlist.add(model);
                }

                SingleItemAdapter adapter = new SingleItemAdapter(context,R.layout.singleitemlayout,recordlist);
                lvrecord.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    void loadData(){

        recordlist = new ArrayList<>();
        File directory = new File(path);

        if (!directory.exists()){
            return;
        }
        File[] files = directory.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                FilesModel model = new FilesModel();
                model.setName(files[i].getName());
                recordlist.add(model);

            }
        }

        SingleItemAdapter adapter = new SingleItemAdapter(context,R.layout.singleitemlayout,recordlist);
        lvrecord.setAdapter(adapter);
    }

    public void showDialog(Activity activity, String filename, String stitle){

        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.mediaplayerlayout);

        seekBar = dialog.findViewById(R.id.seekBar);
        ivstop = dialog.findViewById(R.id.ivstop);
        tvstarttime = dialog.findViewById(R.id.tvstarttime);
        tvendtime = dialog.findViewById(R.id.tvendtime);
        tvfilename = dialog.findViewById(R.id.tvfilename);
        ivclose = dialog.findViewById(R.id.ivclose);

        tvfilename.setText(stitle);

        seekBar.setMax(duration);

        ivclose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
                if (mp.isPlaying()){
                    mp.stop();
                }
            }
        });


        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                Log.d("woww", "fs");
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mp != null && fromUser) {
                    mp.seekTo(progress);
                }
            }
        });

        ivplay = (ImageView) dialog.findViewById(R.id.ivplay);
        ivplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mp.start();

                ivplay.setVisibility(View.GONE);
                ivstop.setVisibility(View.VISIBLE);

                hdlr.postDelayed(UpdateSongTime, 100);

            }
        });

        ivstop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mp.stop();
                ivstop.setVisibility(View.GONE);
                ivplay.setVisibility(View.VISIBLE);
                dialog.dismiss();
            }
        });

        dialog.show();
        mp.start();
        tvendtime.setText(String.format("%d:%d ", TimeUnit.MILLISECONDS.toMinutes(eTime),
                TimeUnit.MILLISECONDS.toSeconds(eTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(eTime))) );

        seekBar.setProgress(mp.getCurrentPosition());

        sTime = mp.getCurrentPosition();
        hdlr.postDelayed(UpdateSongTime, 100);
        ivplay.setVisibility(View.GONE);
        ivstop.setVisibility(View.VISIBLE);

    }



    private Runnable UpdateSongTime = new Runnable() {
        @Override
        public void run() {
            if (mp != null) {
                if (mp.isPlaying()) {
                    sTime = mp.getCurrentPosition();
                    tvstarttime.setText(String.format("%d:%d ", TimeUnit.MILLISECONDS.toMinutes(sTime),
                            TimeUnit.MILLISECONDS.toSeconds(sTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(sTime))));
                    seekBar.setProgress(sTime);
                    hdlr.postDelayed(this, 100);
                }
            }
        }
    };


    private void requestPermission() {
        ActivityCompat.requestPermissions(getActivity(), new
                String[]{WRITE_EXTERNAL_STORAGE, RECORD_AUDIO}, RequestPermissionCode);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case RequestPermissionCode:
                if (grantResults.length> 0) {
                    boolean StoragePermission = grantResults[0] ==
                            PackageManager.PERMISSION_GRANTED;
                    boolean RecordPermission = grantResults[1] ==
                            PackageManager.PERMISSION_GRANTED;

                    if (StoragePermission && RecordPermission) {
                        if (Utill.verifyConection(context)){
                            loadOnlineData();
                            Intent service = new Intent(context, BackgroundService.class);
                            context.startService(service);
                        }else
                            loadData();

                        Toast.makeText(getActivity(), "Permission Granted",
                                Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getActivity(),"Permission Denied",Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }

    public boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(context,
                WRITE_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(context,
                RECORD_AUDIO);
        return result == PackageManager.PERMISSION_GRANTED &&
                result1 == PackageManager.PERMISSION_GRANTED;
    }




}
