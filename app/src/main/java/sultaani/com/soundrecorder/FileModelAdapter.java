package sultaani.com.soundrecorder;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by CH_M_USMAN on 30-Nov-16.
 */
public class FileModelAdapter extends ArrayAdapter<FilesModel> {
    ArrayList<FilesModel> list;
    Context context;
    MainInterface mainInterface;

    public FileModelAdapter(Context context, int resource, ArrayList<FilesModel> objects) {
        super(context, resource, objects);
        this.list = objects;
        this.context = context;
        mainInterface = (MainInterface) context;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView==null){
            holder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.singleitemlayout, parent, false);
            holder.Title = (TextView) convertView.findViewById(R.id.singlelistid);
            holder.ivdelete = convertView.findViewById(R.id.ivdelete);
            holder.ivdelete.setVisibility(View.GONE);
            convertView.setTag(holder);
        }else {
            holder = (ViewHolder) convertView.getTag();
        }

//        Typeface typeface = Typeface.createFromAsset(context.getAssets(),"fonts/DejaVuSans.ttf");
//        holder.Title.setTypeface(typeface);

        holder.populateList(list.get(position).getName());

        holder.ivdelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder alert = new AlertDialog.Builder(
                        context);
                alert.setTitle("Alert!!");
                alert.setMessage("Are you sure to delete record");
                alert.setIcon(R.drawable.delete);
                alert.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String selectedFilePath = Environment.getExternalStorageDirectory().toString()+"/"+list.get(position).getFolder()+"/"+list.get(position);
                        File file = new File(selectedFilePath);
                        boolean deleted = file.delete();
                        if (deleted){
                            Utill.showToast("File deleted successfully..",context);
                            list.remove(position);
                            notifyDataSetChanged();
                            mainInterface.reloadActivity();
                        }else {
                            Utill.showToast("File deleted successfully..",context);
                        }
                        dialog.dismiss();

                    }
                });
                alert.setNegativeButton("NO", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                    }
                });

                alert.show();




            }
        });
        return convertView;
    }

    class ViewHolder {
        TextView Title;
        ImageView ivdelete;
        void populateList(String title) {
            Title.setText((title.length()>15? title.substring(0,25)+"..." : title));

        }
    }

}
