package aed17.aedproject.aedapplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by KayxShi on 1/3/2017.
 */

public class MyCustomAdapter extends BaseAdapter implements ListAdapter{
    private ArrayList<String> list = new ArrayList<String>();
    private Context context;
    DatabaseHelper aedDb;
    String [] contactno;
    String [] relationship;
    Bitmap[] bitmap;
    public MyCustomAdapter(String[] contactno, String[] relationship, ArrayList<String> list, Context context, Bitmap[] bitmap) {
        this.contactno = contactno;
        this.relationship = relationship;
        this.bitmap = bitmap;
        this.list = list;
        this.context = context;
        aedDb = new DatabaseHelper(context.getApplicationContext());
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public class Holder {
        TextView tvContactNo;
        TextView tvRelationship;
        ImageView imageView;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        final Holder holder = new Holder();
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.layout_custom_list_view, null);
        }
        holder.tvContactNo = (TextView) view.findViewById(R.id.list_item_contactno);
        holder.tvContactNo.setText(this.contactno[position]);
        holder.tvRelationship = (TextView) view.findViewById(R.id.list_item_relationship);
        holder.tvRelationship.setText(this.relationship[position]);
        holder.imageView = (ImageView) view.findViewById(R.id.list_image);
        holder.imageView.setImageBitmap(this.bitmap[position]);

        TextView list_name = (TextView)view.findViewById(R.id.list_item_string);
        list_name.setText(list.get(position));

        //Handle buttons and add onClickListeners
        Button deleteBtn = (Button)view.findViewById(R.id.delete_btn);
        Button editBtn = (Button)view.findViewById(R.id.edit_btn);

        deleteBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                TextView et = (TextView) ((View)v.getParent()).findViewById(R.id.list_item_string);
                String name = et.getText().toString();
                list.remove(position); // Delete row animation but not deleted really in database
                aedDb.deleteUserFromDataBase(name); // This will really delete it in the database
                Toast.makeText(context, "Contact Successfully Deleted", Toast.LENGTH_SHORT).show();
                notifyDataSetChanged();
            }
        });
        editBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                TextView et = (TextView)((View)v.getParent()).findViewById(R.id.list_item_contactno);
                String contactno = et.getText().toString();

                Intent intent = new Intent(context, EditEmergencyContactActivity.class);
                intent.putExtra("global_ContactNo", contactno);
                context.startActivity(intent);
                ((Activity)context).finish();

                //Toast.makeText(context, "", Toast.LENGTH_SHORT).show();
                notifyDataSetChanged();
            }
        });

        return view;
    }

}
