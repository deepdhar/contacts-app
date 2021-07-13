package com.example.mycontacts.adapter;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.mycontacts.contentprovider.Contract;
import com.example.mycontacts.R;
import com.squareup.picasso.Picasso;

public class ContactsAdapter extends CursorAdapter {


    public ContactsAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.contact_layout, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView nameView, numberView;
        ImageView mContactImageView;

        nameView = view.findViewById(R.id.textName);
        numberView = view.findViewById(R.id.textNumber);
        mContactImageView = view.findViewById(R.id.imageContact);

        /// geting position of views
        int name = cursor.getColumnIndex(Contract.ContactEntry.COLUMN_NAME);
        int number = cursor.getColumnIndex(Contract.ContactEntry.COLUMN_PHONENUMBER);
        int picture = cursor.getColumnIndex(Contract.ContactEntry.COLUMN_PICTURE);

        String contactname = cursor.getString(name);
        String contactnumber = cursor.getString(number);
        String contactpicture = cursor.getString(picture);
        Uri imageUri = Uri.parse(contactpicture);

        nameView.setText(contactname);
        numberView.setText(contactnumber);
//        mContactImageView.setImageURI(imageUri);
        Picasso.get().load(imageUri).into(mContactImageView);
    }
}
