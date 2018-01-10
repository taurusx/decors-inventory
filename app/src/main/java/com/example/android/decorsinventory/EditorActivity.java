/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.decorsinventory;

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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.android.decorsinventory.data.DecorContract.DecorEntry;

import java.io.ByteArrayOutputStream;

/**
 * Allows user to create a new decor or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener {

    /**
     * Tag for log messages
     */
    private static final String LOG_TAG = EditorActivity.class.getName();


    /**
     * Id of a Loader used for displaying decor data.
     */
    private static final int EDITOR_LOADER_ID = 1;

    /**
     * EditText field to enter the decor's name
     */
    private EditText mNameEditText;

    /**
     * EditText field to enter the decor's description
     */
    private EditText mDescriptionEditText;

    /**
     * EditText field to enter the decor's height
     */
    private EditText mHeightEditText;

    /**
     * EditText field to enter the decor's material
     */
    private Spinner mMaterialSpinner;

    /**
     * Material of the decor. The possible valid values are in the DecorContract.java file:
     * {@link DecorEntry#MATERIAL_UNSPECIFIED}, {@link DecorEntry#MATERIAL_GLASS},
     * {@link DecorEntry#MATERIAL_WOOD}, {@link DecorEntry#MATERIAL_METAL} or
     * {@link DecorEntry#MATERIAL_FABRIC}.
     */
    private int mMaterial = DecorEntry.MATERIAL_UNSPECIFIED;

    /**
     * EditText field to enter the decor's price
     */
    private EditText mPriceEditText;

    /**
     * EditText field to enter the decor's quantity
     */
    private EditText mQuantityEditText;

    /**
     * EditText field to enter the decor's quantity change step
     */
    private EditText mQuantityStepEditText;

    /**
     * EditText field to enter the decor's supplier's name
     */
    private EditText mSupplierNameEditText;

    /**
     * EditText field to enter the decor's supplier's email
     */
    private EditText mSupplierEmailEditText;

    /**
     * ImageView showing the decor's image
     */
    private ImageView mImageView;

    /**
     * Button to add decor's image
     */
    private Button mImageButton;

    /**
     * Button to increase quantity of decor
     */
    private Button mIncreaseButton;

    /**
     * Button to decrease quantity of decor
     */
    private Button mDecreaseButton;

    /**
     * Button to send email to a supplier
     */
    private Button mOrderButton;

    /**
     * Keeps existing decor's content URI (null if it's a new decor).
     */
    private Uri mCurrentDecorUri;

    /**
     * Informs whether edit form has been changed or not.
     */
    private boolean mDecorHasChanged = false;

    /**
     * Informs whether user forbids to save not complete data.
     */
    private boolean mCannotSave = true;

    /**
     * The request code for receiving image from gallery.
     */
    private static final int GET_FROM_GALLERY = 1;

    /**
     * The request code for accessing storage (read) permission.
     */
    private static final int STORAGE_PERMISSION_CODE = 51;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mDecorHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mDecorHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new decor or editing an existing one.
        Intent intent = getIntent();
        mCurrentDecorUri = intent.getData();

        // If the intent DOES NOT contain a decor content URI, then we know that we are
        // creating a new decor.
        if (mCurrentDecorUri == null) {
            // This is a new decor, so change the app bar to say "Add a Decor"
            setTitle(getString(R.string.editor_activity_title_new_decor));
            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a decor that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            // Otherwise this is an existing decor, so change the app bar to say "Edit Decor"
            setTitle(getString(R.string.editor_activity_title_edit_decor));

            // Initialize a loader to read the decor data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EDITOR_LOADER_ID, null, this);
        }

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_decor_name);
        mDescriptionEditText = (EditText) findViewById(R.id.edit_decor_desc);
        mHeightEditText = (EditText) findViewById(R.id.edit_decor_height);
        mMaterialSpinner = (Spinner) findViewById(R.id.spinner_material);
        mPriceEditText = (EditText) findViewById(R.id.edit_decor_price);
        mQuantityEditText = (EditText) findViewById(R.id.edit_decor_quantity);
        mSupplierNameEditText = (EditText) findViewById(R.id.edit_decor_supplier_name);
        mSupplierEmailEditText = (EditText) findViewById(R.id.edit_decor_supplier_email);
        mImageView = (ImageView) findViewById(R.id.editor_imageview_image);
        mImageButton = (Button) findViewById(R.id.editor_imageview_button);
        mQuantityStepEditText = (EditText) findViewById(R.id.edit_decor_quantity_change);
        mIncreaseButton = (Button) findViewById(R.id.editor_quantity_button_increase);
        mDecreaseButton = (Button) findViewById(R.id.editor_quantity_button_decrease);
        mOrderButton = (Button) findViewById(R.id.editor_button_order);

        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        mNameEditText.setOnTouchListener(mTouchListener);
        mDescriptionEditText.setOnTouchListener(mTouchListener);
        mHeightEditText.setOnTouchListener(mTouchListener);
        mMaterialSpinner.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mSupplierNameEditText.setOnTouchListener(mTouchListener);
        mSupplierEmailEditText.setOnTouchListener(mTouchListener);
        mImageButton.setOnTouchListener(mTouchListener);
        mIncreaseButton.setOnTouchListener(mTouchListener);
        mDecreaseButton.setOnTouchListener(mTouchListener);
        mQuantityStepEditText.setOnTouchListener(mTouchListener);

        // Set OnClickListeners to establish which button was clicked
        mImageButton.setOnClickListener(this);
        mIncreaseButton.setOnClickListener(this);
        mDecreaseButton.setOnClickListener(this);
        mOrderButton.setOnClickListener(this);


        // Set filter on price input to avoid double dots and more than 2 decimal places
        mPriceEditText.setFilters(new InputFilter[]{
                new DigitsKeyListener(Boolean.FALSE, Boolean.TRUE) {
                    int afterDecimal = 2;

                    @Override
                    public CharSequence filter(CharSequence source, int start, int end,
                                               Spanned dest, int dstart, int dend) {
                        StringBuilder builder = new StringBuilder(dest);
                        builder.replace(dstart, dend, source
                                .subSequence(start, end).toString());
                        String temp = builder.toString();
                        if (temp.contains(".")) {
                            temp = temp.substring(temp.indexOf(".") + 1);
                            if (temp.length() > afterDecimal) {
                                return "";
                            }
                        }
                        return super.filter(source, start, end, dest, dstart, dend);
                    }
                }
        });
        setupSpinner();

    }

    /**
     * Setup the dropdown spinner that allows the user to select the material of the decor.
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter materialSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_material_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        materialSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mMaterialSpinner.setAdapter(materialSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mMaterialSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.material_glass))) {
                        mMaterial = DecorEntry.MATERIAL_GLASS;
                    } else if (selection.equals(getString(R.string.material_wood))) {
                        mMaterial = DecorEntry.MATERIAL_WOOD;
                    } else if (selection.equals(getString(R.string.material_metal))) {
                        mMaterial = DecorEntry.MATERIAL_METAL;
                    } else if (selection.equals(getString(R.string.material_fabric))) {
                        mMaterial = DecorEntry.MATERIAL_FABRIC;
                    } else {
                        mMaterial = DecorEntry.MATERIAL_UNSPECIFIED;
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mMaterial = DecorEntry.MATERIAL_UNSPECIFIED;
            }
        });
    }

    /**
     * Get user input from editor and save new decor into database.
     */
    private boolean saveDecor() {

        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String nameString = mNameEditText.getText().toString().trim();
        String descriptionString = mDescriptionEditText.getText().toString().trim();
        String heightString = mHeightEditText.getText().toString();
        String priceString = mPriceEditText.getText().toString();
        String quantityString = mQuantityEditText.getText().toString();
        String supplierNameString = mSupplierNameEditText.getText().toString().trim();
        String supplierEmailString = mSupplierEmailEditText.getText().toString().trim();

        // Check if this is supposed to be a new decor
        // and check if all the fields in the editor are blank
        if (mCurrentDecorUri == null &&
                TextUtils.isEmpty(nameString) && TextUtils.isEmpty(descriptionString) &&
                TextUtils.isEmpty(heightString) && mMaterial == DecorEntry.MATERIAL_UNSPECIFIED &&
                TextUtils.isEmpty(priceString) && TextUtils.isEmpty(quantityString) &&
                TextUtils.isEmpty(supplierNameString) && TextUtils.isEmpty(supplierEmailString) &&
                mImageView.getDrawable() == null) {
            // Since no fields were modified, we can return early without creating a new decor.
            // No need to create ContentValues and no need to do any ContentProvider operations.
            return true;
        }

        // If User wants to save decor without given name, don't allow it
        if (TextUtils.isEmpty(nameString)) {
            // If no name is given, then the decor cannot be saved.
            mCannotSave = true;
            Toast.makeText(this, getString(R.string.editor_update_decor_noname),
                    Toast.LENGTH_LONG).show();
            return false;
        }

        // Check for situation when not all required fields are filled and User does not approve
        // default values. Stop saving data.
        if (!checkIfFieldsFilled() && mCannotSave) {
            return false;
        }

        // If the height, price or quantity is not provided by the user, don't try to parse
        // the string into an integer/double value. Use 0 by default.
        int height = 0;
        Double price = 0d;
        int quantity = 0;
        if (!TextUtils.isEmpty(heightString)) {
            height = Integer.parseInt(heightString);
        }
        if (!TextUtils.isEmpty(priceString)) {
            price = Double.parseDouble(priceString);
        }
        if (!TextUtils.isEmpty(quantityString)) {
            quantity = Integer.parseInt(quantityString);
        }

        // Prepare image to be inserted into database, null value is acceptable.
        byte[] decorImage = null;
        if (mImageView.getDrawable() != null) {
            decorImage = imageViewToByteArray(mImageView);
        }

        // Create a ContentValues object where column names are the keys,
        // and decor attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(DecorEntry.COLUMN_DECOR_NAME, nameString);
        values.put(DecorEntry.COLUMN_DECOR_DESCRIPTION, descriptionString);
        values.put(DecorEntry.COLUMN_DECOR_MATERIAL, mMaterial);
        values.put(DecorEntry.COLUMN_DECOR_HEIGHT, height);
        values.put(DecorEntry.COLUMN_DECOR_PRICE, price);
        values.put(DecorEntry.COLUMN_DECOR_QUANTITY, quantity);
        values.put(DecorEntry.COLUMN_DECOR_SUPPLIER_NAME, supplierNameString);
        values.put(DecorEntry.COLUMN_DECOR_SUPPLIER_EMAIL, supplierEmailString);
        values.put(DecorEntry.COLUMN_DECOR_IMAGE, decorImage);

        // Determine if this is a new or existing decor by checking if mCurrentDecorUri is null or not
        if (mCurrentDecorUri == null) {
            // Insert a new decor into the provider, returning the content URI for the new decor.
            Uri newUri = getContentResolver().insert(DecorEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_decor_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_decor_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            // Otherwise this is an EXISTING decor, so update the decor with content URI: mCurrentDecorUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentDecorUri will already identify the correct row in the database that
            // we want to modify.
            int mRowsUpdated = getContentResolver().update(mCurrentDecorUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (mRowsUpdated == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_decor_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_decor_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new decor, hide the "Delete" menu item.
        if (mCurrentDecorUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:

                // Starting condition, fields not filled, cannot save incomplete results, but
                // saveDecor can be true if all fields are empty.
                if (!checkIfFieldsFilled() && mCannotSave && saveDecor()) {
                    finish();

                } else if (!checkIfFieldsFilled() && mCannotSave) {
                    // Second condition: Decor editing has started, but some fields are still not filled.
                    // Setup a dialog to warn the user.
                    // Create a click listener to handle the user confirming that
                    // changes should be saved.
                    DialogInterface.OnClickListener saveButtonClickListener =
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    // User clicked "Save" button, try to save.
                                    mCannotSave = false; // User approved default values
                                    if (saveDecor()) {
                                        // Exit activity
                                        finish();
                                    }
                                }
                            };
                    // Show a dialog that notifies the user they have unsaved changes
                    showNotFilledDialog(saveButtonClickListener);
                    return true;

                } else if (checkIfFieldsFilled() && saveDecor()) {
                    // Exit activity
                    finish();
                }
                return true;

            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the decor hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mDecorHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // If the decor hasn't changed, continue with handling back button press
        if (!mDecorHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Since the editor shows all decor attributes, define a projection that contains
        // all columns from the decor table
        String[] projection = {
                DecorEntry._ID,
                DecorEntry.COLUMN_DECOR_NAME,
                DecorEntry.COLUMN_DECOR_DESCRIPTION,
                DecorEntry.COLUMN_DECOR_MATERIAL,
                DecorEntry.COLUMN_DECOR_HEIGHT,
                DecorEntry.COLUMN_DECOR_PRICE,
                DecorEntry.COLUMN_DECOR_QUANTITY,
                DecorEntry.COLUMN_DECOR_SUPPLIER_NAME,
                DecorEntry.COLUMN_DECOR_SUPPLIER_EMAIL,
                DecorEntry.COLUMN_DECOR_IMAGE
        };

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentDecorUri,         // Query the content URI for the current decor
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (data == null || data.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (data.moveToFirst()) {
            // Figure out the index of each column
            // Find the columns of decor attributes that we're interested in
            int nameColumnIndex = data.getColumnIndex(DecorEntry.COLUMN_DECOR_NAME);
            int descriptionColumnIndex = data.getColumnIndex(DecorEntry.COLUMN_DECOR_DESCRIPTION);
            int materialColumnIndex = data.getColumnIndex(DecorEntry.COLUMN_DECOR_MATERIAL);
            int heightColumnIndex = data.getColumnIndex(DecorEntry.COLUMN_DECOR_HEIGHT);
            int priceColumnIndex = data.getColumnIndex(DecorEntry.COLUMN_DECOR_PRICE);
            int quantityColumnIndex = data.getColumnIndex(DecorEntry.COLUMN_DECOR_QUANTITY);
            int supplierNameColumnIndex = data.getColumnIndex(DecorEntry.COLUMN_DECOR_SUPPLIER_NAME);
            int supplierEmailColumnIndex = data.getColumnIndex(DecorEntry.COLUMN_DECOR_SUPPLIER_EMAIL);
            int imageColumnIndex = data.getColumnIndex(DecorEntry.COLUMN_DECOR_IMAGE);

            // Use that index to extract the String or Int value of the word
            // at the current row the cursor is on.
            // Extract out the value from the Cursor for the given column index
            String currentName = data.getString(nameColumnIndex);
            String currentDescription = data.getString(descriptionColumnIndex);
            int currentMaterial = data.getInt(materialColumnIndex);
            int currentHeight = data.getInt(heightColumnIndex);
            Double currentPrice = data.getDouble(priceColumnIndex);
            int currentQuantity = data.getInt(quantityColumnIndex);
            String currentSupplierName = data.getString(supplierNameColumnIndex);
            String currentSupplierEmail = data.getString(supplierEmailColumnIndex);
            byte[] currentImage = data.getBlob(imageColumnIndex);

            // Show and update decor ImageView if image is available
            if (currentImage != null) {
                //Image column has data
                Bitmap bitmap = BitmapFactory.decodeByteArray(currentImage, 0, currentImage.length);
                mImageView.setVisibility(View.VISIBLE);
                mImageView.setImageBitmap(bitmap);
            }

            // Update the views on the screen with the values from the database
            mNameEditText.setText(currentName);
            mDescriptionEditText.setText(currentDescription);
            mHeightEditText.setText(String.valueOf(currentHeight));
            mPriceEditText.setText(String.valueOf(currentPrice));
            mQuantityEditText.setText(String.valueOf(currentQuantity));
            mSupplierNameEditText.setText(currentSupplierName);
            mSupplierEmailEditText.setText(currentSupplierEmail);

            // Material is a dropdown spinner, so map the constant value from the database
            // into one of the dropdown options (0 is Unspecified, 1 is Glass, 2 is Wood,
            // 3 is Metal and 4 is Fabric).
            // Then call setSelection() so that option is displayed on screen as the current selection.
            switch (currentMaterial) {
                case DecorEntry.MATERIAL_GLASS:
                    mMaterialSpinner.setSelection(1);
                    break;
                case DecorEntry.MATERIAL_WOOD:
                    mMaterialSpinner.setSelection(2);
                    break;
                case DecorEntry.MATERIAL_METAL:
                    mMaterialSpinner.setSelection(3);
                    break;
                case DecorEntry.MATERIAL_FABRIC:
                    mMaterialSpinner.setSelection(4);
                    break;
                default:
                    mMaterialSpinner.setSelection(0);
                    break;
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        mNameEditText.getText().clear();
        mDescriptionEditText.getText().clear();
        mHeightEditText.getText().clear();
        mMaterialSpinner.setSelection(DecorEntry.MATERIAL_UNSPECIFIED);
        mPriceEditText.getText().clear();
        mQuantityEditText.getText().clear();
        mSupplierNameEditText.getText().clear();
        mSupplierEmailEditText.getText().clear();
        mImageView.setImageDrawable(null);
        mImageView.setVisibility(View.GONE);
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
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
                // and continue editing the decor.
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
     * Show a dialog that warns the user there are unfilled fields that will be set to default
     * if they continue leaving the editor.
     *
     * @param saveButtonClickListener is the click listener for what to do when
     *                                the user confirms they want to save their changes
     */
    private void showNotFilledDialog(
            DialogInterface.OnClickListener saveButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unfilled_fields_dialog_msg);
        builder.setPositiveButton(R.string.action_save, saveButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the decor.
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
     * Helper method to check if numerical fields (height, price, quantity) are not empty.
     *
     * @return information whether all required numerical fields are filled
     */
    private boolean checkIfFieldsFilled() {
        return !TextUtils.isEmpty(mPriceEditText.getText().toString().trim()) &&
                !TextUtils.isEmpty(mQuantityEditText.getText().toString().trim()) &&
                !TextUtils.isEmpty(mHeightEditText.getText().toString().trim());
    }

    /**
     * Prompt the user to confirm that they want to delete this decor.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the decor.
                deleteDecor();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the decor.
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
     * Perform the deletion of the decor in the database.
     */
    private void deleteDecor() {
        // Only perform the delete if this is an existing decor.
        if (mCurrentDecorUri != null) {
            // Call the ContentResolver to delete the decor at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentDecorUri
            // content URI already identifies the decor that we want.
            int rowsDeleted = getContentResolver().delete(
                    mCurrentDecorUri,             // the decor content URI
                    null,                       // the column to select on
                    null                        // the value to compare to
            );

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_decor_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_decor_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case STORAGE_PERMISSION_CODE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    startActivityForResult(new Intent(Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), GET_FROM_GALLERY);

                } else {
                    Toast.makeText(this, "Please give your permission to set decor image.", Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Check which request we're responding to
        if (requestCode == GET_FROM_GALLERY) {
            // Make sure the request was successful
            if (resultCode == Activity.RESULT_OK && data != null) {
                // The user picked an image.
                // The Intent's data Uri identifies which image was selected.
                Uri selectedImage = data.getData();

                mImageView.setVisibility(View.VISIBLE);
                mImageView.setImageURI(selectedImage);

            } else if (resultCode == Activity.RESULT_CANCELED) {
                Log.d(LOG_TAG, "Selecting picture cancelled");
            }
        }
    }

    /**
     * Helper method to check if User inserted proper email address.
     *
     * @param target text to be validated as email
     * @return information whether given text is email address
     */
    private static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    /**
     * Helper method to change given image from ImageView to byte array.
     *
     * @param imageView View that contains desired image
     * @return byte array conatining information about image
     */
    private byte[] imageViewToByteArray(ImageView imageView) {
        Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    /**
     * Helper method to check if User granted a permission to get images from device storage.
     *
     * @return information whether permission was granted or not
     */
    private boolean checkIfAlreadyhavePermission() {
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.editor_imageview_button:
                //Permission to access external storage
                if (!checkIfAlreadyhavePermission()) {
                    ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
                } else {
                    // Start Intent to pick image from gallery
                    Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    galleryIntent.setType("image/*");
                    startActivityForResult(galleryIntent, GET_FROM_GALLERY);
                }
                break;

            case R.id.editor_quantity_button_decrease:
                reduceQuantity();
                break;

            case R.id.editor_quantity_button_increase:
                increaseQuantity();
                break;

            case R.id.editor_button_order:
                String email = mSupplierEmailEditText.getText().toString().trim();
                if (isValidEmail(email)) {
                    String decor = mNameEditText.getText().toString().trim();
                    String[] sendTo = {email};

                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_EMAIL, sendTo);
                    intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.editor_order_more_email_subject, decor));

                    try {
                        startActivity(Intent.createChooser(intent, "Send mail..."));
                        Log.d(LOG_TAG, "Finished sending email...");
                    } catch (android.content.ActivityNotFoundException ex) {
                        Toast.makeText(EditorActivity.this, "There is no email client installed.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    mSupplierEmailEditText.requestFocus();
                    mSupplierEmailEditText.setError(getString(R.string.editor_order_more_email_error));
                }
                break;

            default:
                break;
        }
    }

    /**
     * Helper method to increase quantity of decors by a step given by User.
     */
    private void increaseQuantity() {
        int quantity;
        int step;

        String stepString = mQuantityStepEditText.getText().toString().trim();
        if (!TextUtils.isEmpty(stepString)) {
            step = Integer.parseInt(stepString);
        } else {
            step = 1;
        }

        String quantityString = mQuantityEditText.getText().toString().trim();
        if (!TextUtils.isEmpty(quantityString)) {
            quantity = Integer.parseInt(quantityString);
        } else {
            quantity = 0;
        }
        quantity += step;
        mQuantityEditText.setText(String.valueOf(quantity));
    }

    /**
     * Helper method to decrease quantity of decors by a step given by User.
     */
    private void reduceQuantity() {
        int quantity;
        int step;

        String stepString = mQuantityStepEditText.getText().toString().trim();
        if (!TextUtils.isEmpty(stepString)) {
            step = Integer.parseInt(stepString);
        } else {
            step = 1;
        }

        String quantityString = mQuantityEditText.getText().toString().trim();
        if (!TextUtils.isEmpty(quantityString)) {
            quantity = Integer.parseInt(quantityString);
        } else {
            quantity = 0;
        }

        if (quantity >= step) {
            quantity -= step;
        } else {
            Toast.makeText(EditorActivity.this, "Cannot reduce by " + step + ". Quantity must be positive number or 0", Toast.LENGTH_SHORT).show();
        }
        mQuantityEditText.setText(String.valueOf(quantity));
    }
}