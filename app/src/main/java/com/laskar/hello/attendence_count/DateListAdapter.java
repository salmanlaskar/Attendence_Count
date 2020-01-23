package com.laskar.hello.attendence_count;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class DateListAdapter extends ArrayAdapter<Message> {
    private final Activity context;
    private List<Message> message;

    public DateListAdapter(Activity context, List<Message> message) {
        super(context,R.layout.datelist,message);
        this.context = context;
        this.message = message;
    }
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater=context.getLayoutInflater();
        View rowView=inflater.inflate(R.layout.datelist,null,true);
        TextView name=(TextView)rowView.findViewById(R.id.name);
        TextView status=(TextView)rowView.findViewById(R.id.status);
        Message m=message.get(position);
        name.setText(m.getName());
        status.setText("Present : "+m.getMessage());
        return rowView;
    }
}
