package sultaani.com.soundrecorder;


import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
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

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static sultaani.com.soundrecorder.SoundRecordFragment.RequestPermissionCode;


/**
 * A simple {@link Fragment} subclass.
 */
public class FilesFragment extends Fragment {

    ListView lvfiles;
    ArrayList<String> recordlist;
    Context context;

    MainInterface mainInterface;
    int duration;
    MediaPlayer mp;
    SeekBar seekBar;
    ImageView ivstop, ivplay;
    ImageView ivclose;

    TextView tvstarttime, tvendtime, tvfilename;

    private Handler hdlr = new Handler();


    private static int oTime = 0, sTime = 0, eTime = 0, fTime = 5000, bTime = 5000;

    ArrayList<FilesModel> modelArrayList;

    public FilesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_files, container, false);
        lvfiles = view.findViewById(R.id.lvfiles);


        context = getContext();
        recordlist = new ArrayList<>();
        modelArrayList = new ArrayList<>();

        lvfiles.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                mp = new MediaPlayer();

                String sname = recordlist.get(i);

                try {
                     mp.setDataSource(Environment.getExternalStorageDirectory().toString() + "/"+modelArrayList.get(i).getFolder() + "/" + File.separator + modelArrayList.get(i).getName());
                    mp.prepare();
                    duration = mp.getDuration();
                    eTime = duration;

                } catch (Exception e) {
                    e.printStackTrace();
                }

                mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {

                        ivstop.setVisibility(View.GONE);
                        ivplay.setVisibility(View.VISIBLE);

                    }
                });


                showDialog(getActivity(), recordlist.get(i));
            }
        });


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(!checkPermission()) {
                requestPermission();
            }else {
                loadData();
            }

        }else {
            loadData();
        }





        return view;
    }

    public void showDialog(Activity activity, String filename) {

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

        tvfilename.setText(filename);

        seekBar.setMax(duration);

        ivclose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
                if (mp.isPlaying()) {
                    mp.stop();
                    //mp.release();
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
//                    try {
//                        //  tvCurrentTime.setText(uMediaPlayer.getTimestamp().toString());
//                    } catch (NoSuchMethodError n) {
//                    }
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
                TimeUnit.MILLISECONDS.toSeconds(eTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(eTime))));

        seekBar.setProgress(mp.getCurrentPosition());

        sTime = mp.getCurrentPosition();
        hdlr.postDelayed(UpdateSongTime, 100);
        ivplay.setVisibility(View.GONE);
        ivstop.setVisibility(View.VISIBLE);
        //new Thread(this).start();

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

    void loadData(){
        File dir = new File(Environment.getExternalStorageDirectory().toString());

        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files !=null) {

                for (int i = 0; i < files.length; i++) {

                    File dirchild = new File(Environment.getExternalStorageDirectory().toString() + "/" + files[i].getName());
                    File[] filesarray = dirchild.listFiles(new FilenameFilter() {
                        @Override
                        public boolean accept(File dir, String name) {
                            if (name.endsWith(".3gp")) {
                                return true;
                            } else if (name.endsWith(".mp3")) {
                                return true;
                            }
                            return false;
                        }
                    });

                    if (filesarray != null) {

                        for (int m = 0; m < filesarray.length; m++) {
                            recordlist.add(filesarray[m].getName());

                            FilesModel filesModel = new FilesModel();
                            filesModel.setFolder(files[i].getName());
                            filesModel.setName((filesarray[m].getName()));
                            modelArrayList.add(filesModel);
                        }
                    }

                }

            }
            FileModelAdapter adapter = new FileModelAdapter(context, R.layout.singleitemlayout, modelArrayList);
            lvfiles.setAdapter(adapter);


        }


    }

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
