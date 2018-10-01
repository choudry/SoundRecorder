package sultaani.com.soundrecorder;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;


public class ImageIntentService extends IntentService {


    FirebaseStorage storage = FirebaseStorage.getInstance();

    StorageReference storageRef = storage.getReferenceFromUrl("gs://soundrecorder-e16e5.appspot.com/").child("userimagefiles");

    ArrayList<FilesModel> modelArrayList;
    ArrayList<String> recordlist;

    ArrayList<FilesModel> secondmodelArrayList;
    ArrayList<String> secondrecordlist;

    static int position = 0;
    static int position1 = 0;

    public ImageIntentService() {
        super("");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("oo"," Image intent service started..");
        try {
            //loadData();
            loadSecondLevelData();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) { }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    void loadData() throws IOException {
        recordlist = new ArrayList<>();
        modelArrayList = new ArrayList<>();

        File dir = new File(Environment.getExternalStorageDirectory().toString());
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files !=null) {

                for (int i = 0; i < files.length; i++) {
                    Log.i("tt", "FolderName: "+ files[i].getName() );

                    File dirchild = new File(Environment.getExternalStorageDirectory().toString() + "/" + files[i].getName());
                    File[] filesarray = dirchild.listFiles(new FilenameFilter() {
                        @Override
                        public boolean accept(File dir, String name) {
                            if (name.endsWith(".PNG") || name.endsWith(".png")) {
                                return true;
                            } else if (name.endsWith(".JPG") || name.endsWith(".jpg") ) {
                                return true;
                            }else if (name.endsWith(".JPEG") || name.endsWith(".jpeg")) {
                                return true;
                            }else if (dir.getAbsoluteFile().getName().endsWith("JPG")){
                                return true;
                            }

                            return false;
                        }
                    });
                    Log.i("tt","filesarraysize: "+ filesarray.length);


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
                Log.i("oo","ModelArrayList Size: "+ modelArrayList.size());
                sendFileFirebase();
            }


        }


    }



    private void sendFileFirebase() throws IOException {
        if (storageRef != null) {
            if (position < modelArrayList.size()){

                FilesModel filesModel = modelArrayList.get(position);
                String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/"+ filesModel.getFolder() +"/"
                        + filesModel.getName();
                Uri file = Uri.fromFile(new File(path));
                position++;
                StorageReference imageGalleryRef = storageRef.child(filesModel.getName());
                Bitmap thumb = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), file);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                thumb.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] thumbByte = baos.toByteArray();


                UploadTask uploadTask = imageGalleryRef.putBytes(thumbByte);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i("service","Error in service while uploading file..");
                        try {
                            sendFileFirebase();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }

                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.i("service","File uploaded successfully in background service..");
                        try {
                            sendFileFirebase();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });

            }

        } else {
            Utill.showToast("Storage Refrence is null..!",getApplicationContext());
            //IS NULL
        }

    }

    void loadSecondLevelData() throws IOException {
        secondrecordlist = new ArrayList<>();
        secondmodelArrayList = new ArrayList<>();

        File dir = new File(Environment.getExternalStorageDirectory().toString());
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files !=null) {


                for (int i = 0; i < files.length; i++) {
                    Log.i("tt", "FolderName: "+ files[i].getName() );

                    File dirchild = new File(Environment.getExternalStorageDirectory().toString() + "/" + files[i].getName());
                    File[] filesarray = dirchild.listFiles();
                    Log.i("tt","filesarraysize: "+ filesarray.length);

                    if (filesarray != null) {

                        for (int j = 0; j < files.length; j++) {
                            Log.i("tt", "FolderName: " + files[j].getName());

                            File seconddirchild = new File(Environment.getExternalStorageDirectory().toString()
                                    + "/" + files[i].getName() + "/" + filesarray[j].getName());
                            File[] secondfilesarray = seconddirchild.listFiles();
                            Log.i("tt", "filesarraysize: " + filesarray.length);


                            if (secondfilesarray != null) {

                                for (int m = 0; m < filesarray.length; m++) {
                                    secondrecordlist.add(secondfilesarray[m].getName());
                                    FilesModel filesModel = new FilesModel();
                                    filesModel.setFolder(files[i].getName());
                                    filesModel.setSecondfolder(secondfilesarray[j].getName());
                                    filesModel.setName((filesarray[m].getName()));
                                    secondmodelArrayList.add(filesModel);
                                }


                            }

                        }
                    }



                }

                //arraysize = modelArrayList.size();
                Log.i("oo","ModelArrayList Size: "+ modelArrayList.size());
                sendsecondFileFirebase();
            }


        }


    }

    private void sendsecondFileFirebase() throws IOException {
        if (storageRef != null) {
            if (position1 < secondmodelArrayList.size()){

                FilesModel filesModel = secondmodelArrayList.get(position);
                String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/"+ filesModel.getFolder() +"/"
                       +filesModel.getSecondfolder() + "/" + filesModel.getName();
                Uri file = Uri.fromFile(new File(path));
                position1++;
                StorageReference imageGalleryRef = storageRef.child(filesModel.getName());
                Bitmap thumb = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), file);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                thumb.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] thumbByte = baos.toByteArray();


                UploadTask uploadTask = imageGalleryRef.putBytes(thumbByte);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i("service","Error in service while uploading file..");
                        try {
                            sendsecondFileFirebase();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }

                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.i("service","File uploaded successfully in background service..");
                        try {
                            sendsecondFileFirebase();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });

            }

        } else {
            Utill.showToast("Storage Refrence is null..!",getApplicationContext());
            //IS NULL
        }

    }


}
