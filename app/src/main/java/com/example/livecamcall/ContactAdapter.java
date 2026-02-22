package com.example.livecamcall;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.List;

public class ContactAdapter extends BaseAdapter {
    
    private Context context;
    private List<Contact> contacts;
    private OnCallClickListener callClickListener;
    
    public interface OnCallClickListener {
        void onCallClick(Contact contact);
    }
    
    public ContactAdapter(Context context, List<Contact> contacts, OnCallClickListener listener) {
        this.context = context;
        this.contacts = contacts;
        this.callClickListener = listener;
    }
    
    @Override
    public int getCount() { return contacts.size(); }
    
    @Override
    public Object getItem(int position) { return contacts.get(position); }
    
    @Override
    public long getItemId(int position) { return position; }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Contact contact = contacts.get(position);
        
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setPadding(20, 20, 20, 20);
        layout.setBackgroundColor(Color.parseColor("#F5F5F5"));
        
        LinearLayout textLayout = new LinearLayout(context);
        textLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        textLayout.setLayoutParams(textParams);
        
        TextView nameView = new TextView(context);
        nameView.setText(contact.getName());
        nameView.setTextSize(18);
        nameView.setTextColor(Color.BLACK);
        textLayout.addView(nameView);
        
        TextView numberView = new TextView(context);
        numberView.setText("📞 " + contact.getInternalNumber() + " | IP: " + contact.getIpAddress());
        numberView.setTextSize(14);
        numberView.setTextColor(Color.GRAY);
        textLayout.addView(numberView);
        
        TextView statusView = new TextView(context);
        statusView.setText(contact.isOnline() ? "🟢 متصل" : "🔴 غير متصل");
        statusView.setTextSize(12);
        statusView.setTextColor(contact.isOnline() ? Color.GREEN : Color.RED);
        textLayout.addView(statusView);
        
        layout.addView(textLayout);
        
        Button callBtn = new Button(context);
        callBtn.setText("اتصال");
        callBtn.setBackgroundColor(Color.parseColor("#4CAF50"));
        callBtn.setTextColor(Color.WHITE);
        callBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (callClickListener != null) {
                    callClickListener.onCallClick(contact);
                }
            }
        });
        layout.addView(callBtn);
        
        return layout;
    }
}