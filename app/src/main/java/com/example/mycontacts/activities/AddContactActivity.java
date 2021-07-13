package com.example.mycontacts.activities;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.mycontacts.contentprovider.Contract;
import com.example.mycontacts.R;

public class AddContactActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    EditText nameEt, numberEt, emailEt, pincodeEt;
    private Uri mPhotoUri;
    private Uri mCurrentContactUri;
    ImageView mPhoto;
    private boolean contactChanged = false;
    public static final int LOADER = 0;
    public static int flag = 0;

    private View.OnTouchListener mOnTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            contactChanged = true;
            return false;
        }
    };

    boolean hasAllRequiredValues = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);

        Intent intent = getIntent();
        mCurrentContactUri = intent.getData();

        nameEt = findViewById(R.id.name_et);
        numberEt = findViewById(R.id.phone_et);
        emailEt = findViewById(R.id.email_et);
        pincodeEt = findViewById(R.id.pincode_et);
        mPhoto = findViewById(R.id.profile_image);

        if (mCurrentContactUri == null) {
            mPhoto.setImageResource(R.drawable.photo_edit);
            setTitle("Add New Contact");
            // we want to hide delete menu when we are adding a new contact
            invalidateOptionsMenu();

        } else {
            setTitle("Edit Contact");
            getLoaderManager().initLoader(LOADER, null, this);

        }

        nameEt.setOnTouchListener(mOnTouchListener);
        numberEt.setOnTouchListener(mOnTouchListener);
        emailEt.setOnTouchListener(mOnTouchListener);
        pincodeEt.setOnTouchListener(mOnTouchListener);
        mPhoto.setOnTouchListener(mOnTouchListener);

        mPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                trySelector();
                contactChanged = true;
            }
        });

    }


    public void trySelector() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            return;
        }
        openSelector();
        flag = 1;
    }

    private void openSelector() {
        Intent intent;
        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }
        intent.setType(getString(R.string.intent_type));
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_image)), 0);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openSelector();
                }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0 && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                mPhotoUri = data.getData();
                mPhoto.setImageURI(mPhotoUri);
                mPhoto.invalidate();
            }
        }
    }

    // now worrking on menu options
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menueditor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // because we want to hide delete option when we are adding a new contact
        super.onPrepareOptionsMenu(menu);
        if (mCurrentContactUri == null) {
            MenuItem item = (MenuItem) menu.findItem(R.id.delete);
            item.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_save:
                saveContact();
                if (hasAllRequiredValues == true) {
                    finish();
                }
                return true;

            case R.id.delete:
                showDeleteConfirmationDialog();
                return true;

            case android.R.id.home:
                if (!contactChanged) {
                    // we will be displayed a dialog asking us to discard or keeping editing when we press back in case
                    // we have not finished filling up some field
                    NavUtils.navigateUpFromSameTask(AddContactActivity.this);
                    return true;
                }

                DialogInterface.OnClickListener discardButton = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        NavUtils.navigateUpFromSameTask(AddContactActivity.this);

                    }
                };
                showUnsavedChangesDialog(discardButton);
                return true;



        }
            return super.onOptionsItemSelected(item);
    }

    private boolean saveContact() {

        // last step of this activity we have to create savecontact method
        String name = nameEt.getText().toString().trim();
        String email = emailEt.getText().toString().trim();
        String phone = numberEt.getText().toString().trim();
        String pincode = pincodeEt.getText().toString().trim();

        // what if we have not entered any text in field and we click save it will crash so how to save it from crashing
        // when fields are empty
        if (mCurrentContactUri == null && TextUtils.isEmpty(name) && TextUtils.isEmpty(phone) && mPhotoUri == null) {
            hasAllRequiredValues = true;
            return hasAllRequiredValues;
        }

        ContentValues values = new ContentValues();

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Name is Required", Toast.LENGTH_SHORT).show();
            return hasAllRequiredValues;
        } else {
            values.put(Contract.ContactEntry.COLUMN_NAME, name);
        }

        if (TextUtils.isEmpty(phone)) {
            Toast.makeText(this, "Phone Number is Required", Toast.LENGTH_SHORT).show();
            return hasAllRequiredValues;
        } else {
            values.put(Contract.ContactEntry.COLUMN_PHONENUMBER, phone);
        }

        values.put(Contract.ContactEntry.COLUMN_EMAIL, email);
        values.put(Contract.ContactEntry.COLUMN_PINCODE, pincode);

        // optional values
        if(flag==1) {
            values.put(Contract.ContactEntry.COLUMN_PICTURE, mPhotoUri.toString());
        } else {
            Toast.makeText(this, "Contact Image required", Toast.LENGTH_SHORT).show();
            return hasAllRequiredValues;
        }

        if (mCurrentContactUri == null) {

            Uri newUri = getContentResolver().insert(Contract.ContactEntry.CONTENT_URI, values);
            if (newUri == null) {
                Toast.makeText(this, "Error with Saving", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Saved contact", Toast.LENGTH_SHORT).show();

            }
        } else {

            int rowsAffected = getContentResolver().update(mCurrentContactUri, values, null, null);
            if (rowsAffected == 0) {
                Toast.makeText(this, "Error with Update", Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(this, "Updated contact", Toast.LENGTH_SHORT).show();

            }

        }

        hasAllRequiredValues = true;

        return hasAllRequiredValues;

    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

       String[] projection = {Contract.ContactEntry._ID,
               Contract.ContactEntry.COLUMN_NAME,
               Contract.ContactEntry.COLUMN_EMAIL,
               Contract.ContactEntry.COLUMN_PICTURE,
               Contract.ContactEntry.COLUMN_PHONENUMBER,
               Contract.ContactEntry.COLUMN_PINCODE
       };

       return new CursorLoader(this, mCurrentContactUri, projection,
               null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        if (cursor.moveToFirst()) {
            // getting position of each column
            int name = cursor.getColumnIndex(Contract.ContactEntry.COLUMN_NAME);
            int email = cursor.getColumnIndex(Contract.ContactEntry.COLUMN_EMAIL);
            int number = cursor.getColumnIndex(Contract.ContactEntry.COLUMN_PHONENUMBER);
            int picture = cursor.getColumnIndex(Contract.ContactEntry.COLUMN_PICTURE);
            int pincode = cursor.getColumnIndex(Contract.ContactEntry.COLUMN_PINCODE);

            String contactname = cursor.getString(name);
            String contactemail = cursor.getString(email);
            String contactnumber = cursor.getString(number);
            String contactpincode = cursor.getString(pincode);
            String contactpicture = cursor.getString(picture);
            mPhotoUri = Uri.parse(contactpicture);

            numberEt.setText(contactnumber);
            nameEt.setText(contactname);
            emailEt.setText(contactemail);
            pincodeEt.setText(contactpincode);
            mPhoto.setImageURI(mPhotoUri);

        }


    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        numberEt.setText("");
        nameEt.setText("");
        emailEt.setText("");
        pincodeEt.setText("");
        mPhoto.setImageResource(R.drawable.photo);

    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the product.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Prompt the user to confirm that they want to delete this product.
     */
    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the product.
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the product.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the product in the database.
     */
    private void deleteProduct() {
        // Only perform the delete if this is an existing product.
        if (mCurrentContactUri != null) {
            int rowsDeleted = getContentResolver().delete(mCurrentContactUri, null, null);

            if (rowsDeleted == 0) {
                Toast.makeText(this, getString(R.string.editor_delete_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_delete_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        // Close the activity
        finish();
    }

    @Override
    public void onBackPressed() {
        // If the product hasn't changed, continue with handling back button press
        if (!contactChanged) {
            super.onBackPressed();
            overridePendingTransition(R.anim.fade_in, R.anim.slide_out_right);
            return;
        }

        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                        overridePendingTransition(R.anim.fade_in, R.anim.slide_out_right);
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }
}

