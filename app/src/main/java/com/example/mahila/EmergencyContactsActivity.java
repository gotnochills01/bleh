package com.example.mahila;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.mahila.api.TokenManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

public class EmergencyContactsActivity extends AppCompatActivity {

    private static final int REQ_CALL_PERMISSION = 301;
    private String pendingCallNumber = null;

    private TokenManager tokenManager;
    private DatabaseHelper dbHelper;
    private TextInputLayout nameLayout;
    private TextInputLayout phoneLayout;
    private TextInputEditText nameInput;
    private TextInputEditText phoneInput;
    private MaterialButton saveButton;
    private LinearLayout contactsContainer;
    private TextView emptyText;
    private Long editingId = null;

    public static class ContactItem {
        public long id;
        public String name;
        public String phone;

        public ContactItem(long id, String name, String phone) {
            this.id = id;
            this.name = name;
            this.phone = phone;
        }
    }

    public static String buildDialUriString(String phoneNumber) {
        if (phoneNumber == null) return "tel:";
        String digits = phoneNumber.replaceAll("\\D", "");
        return "tel:" + digits;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_emergency_contacts);

        View root = findViewById(R.id.emergency_contacts_root);
        if (root != null) {
            ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        MaterialToolbar toolbar = findViewById(R.id.emergency_contacts_toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle("Emergency Contacts");
            }
            toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        }

        tokenManager = new TokenManager(this);
        dbHelper = new DatabaseHelper(this);

        nameLayout = findViewById(R.id.layout_contact_name);
        phoneLayout = findViewById(R.id.layout_contact_phone);
        nameInput = findViewById(R.id.input_contact_name);
        phoneInput = findViewById(R.id.input_contact_phone);
        saveButton = findViewById(R.id.button_save_contact);
        contactsContainer = findViewById(R.id.saved_contacts_container);
        emptyText = findViewById(R.id.text_no_contacts);

        View cardNational = findViewById(R.id.card_national_emergency);
        if (cardNational != null) cardNational.setOnClickListener(v -> initiatePhoneCall("112"));

        View cardWomen = findViewById(R.id.card_women_helpline);
        if (cardWomen != null) cardWomen.setOnClickListener(v -> initiatePhoneCall("1091"));

        View cardAmbulance = findViewById(R.id.card_ambulance);
        if (cardAmbulance != null) cardAmbulance.setOnClickListener(v -> initiatePhoneCall("108"));

        if (saveButton != null) {
            saveButton.setOnClickListener(v -> saveContact());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        showContacts();
    }

    private List<ContactItem> getContactsForCurrentUser() {
        List<ContactItem> list = new ArrayList<>();
        String userEmail = tokenManager.getUserEmail();
        if (userEmail == null || userEmail.isEmpty()) {
            return list;
        }

        Cursor cursor = dbHelper.getContacts(userEmail);
        if (cursor != null) {
            int idIndex = cursor.getColumnIndex(DatabaseHelper.KEY_ID);
            int nameIndex = cursor.getColumnIndex(DatabaseHelper.KEY_CONTACT_NAME);
            int phoneIndex = cursor.getColumnIndex(DatabaseHelper.KEY_CONTACT_PHONE);

            while (cursor.moveToNext()) {
                long id = idIndex != -1 ? cursor.getLong(idIndex) : -1;
                String name = nameIndex != -1 ? cursor.getString(nameIndex) : "";
                String phone = phoneIndex != -1 ? cursor.getString(phoneIndex) : "";
                list.add(new ContactItem(id, name, phone));
            }
            cursor.close();
        }
        return list;
    }

    private void saveContact() {
        if (nameLayout != null) nameLayout.setError(null);
        if (phoneLayout != null) phoneLayout.setError(null);

        String userEmail = tokenManager.getUserEmail();
        if (userEmail == null || userEmail.isEmpty()) {
            Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String name = nameInput != null ? textOf(nameInput) : "";
        String phone = phoneInput != null ? textOf(phoneInput) : "";

        if (name.length() < 2) {
            if (nameLayout != null) nameLayout.setError("Enter a name with at least 2 characters.");
            return;
        }

        String digits = phone.replaceAll("\\D", "");
        if (digits.length() < 10 || digits.length() > 15) {
            if (phoneLayout != null) phoneLayout.setError("Enter a valid phone number with 10 to 15 digits.");
            return;
        }

        List<ContactItem> contacts = getContactsForCurrentUser();
        for (ContactItem contact : contacts) {
            boolean sameNumber = contact.phone.replaceAll("\\D", "").equals(digits);
            boolean differentRecord = editingId == null || contact.id != editingId;
            if (sameNumber && differentRecord) {
                if (phoneLayout != null) phoneLayout.setError("This phone number is already saved.");
                return;
            }
        }

        if (editingId == null) {
            boolean success = dbHelper.saveContact(userEmail, name, phone, false);
            if (success) {
                Toast.makeText(this, "Contact saved locally", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to save contact", Toast.LENGTH_SHORT).show();
            }
        } else {
            ContentValues cv = new ContentValues();
            cv.put(DatabaseHelper.KEY_CONTACT_NAME, name);
            cv.put(DatabaseHelper.KEY_CONTACT_PHONE, phone);
            cv.put(DatabaseHelper.KEY_IS_SYNCED, 0);
            dbHelper.getWritableDatabase().update(
                    DatabaseHelper.TABLE_CONTACTS,
                    cv,
                    DatabaseHelper.KEY_ID + " = ? AND " + DatabaseHelper.KEY_USER_EMAIL + " = ?",
                    new String[]{String.valueOf(editingId), userEmail}
            );
            Toast.makeText(this, "Contact updated locally", Toast.LENGTH_SHORT).show();
            editingId = null;
            if (saveButton != null) saveButton.setText("Save Contact");
        }

        if (nameInput != null) nameInput.setText("");
        if (phoneInput != null) phoneInput.setText("");
        showContacts();
    }

    private void showContacts() {
        if (contactsContainer == null) return;
        contactsContainer.removeAllViews();

        List<ContactItem> contacts = getContactsForCurrentUser();
        if (emptyText != null) {
            emptyText.setVisibility(contacts.isEmpty() ? View.VISIBLE : View.GONE);
        }

        LayoutInflater inflater = LayoutInflater.from(this);
        for (ContactItem contact : contacts) {
            View item = inflater.inflate(R.layout.item_emergency_contact, contactsContainer, false);
            TextView nameView = item.findViewById(R.id.text_contact_name);
            TextView phoneView = item.findViewById(R.id.text_contact_phone);
            MaterialButton editButton = item.findViewById(R.id.button_edit_contact);
            MaterialButton deleteButton = item.findViewById(R.id.button_delete_contact);

            if (nameView != null) nameView.setText(contact.name);
            if (phoneView != null) phoneView.setText(contact.phone);

            item.setOnClickListener(v -> initiatePhoneCall(contact.phone));
            if (editButton != null) editButton.setOnClickListener(v -> startEdit(contact));
            if (deleteButton != null) deleteButton.setOnClickListener(v -> deleteContact(contact.id));

            contactsContainer.addView(item);
        }
    }

    private void initiatePhoneCall(String number) {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Emergency Call")
                .setMessage("Do you want to call " + number + " directly?")
                .setPositiveButton("Call Now", (dialog, which) -> checkCallPermissionAndExecute(number))
                .setNeutralButton("Open Dialer", (dialog, which) -> fallbackToDialer(number))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void checkCallPermissionAndExecute(String number) {
        pendingCallNumber = number;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CALL_PHONE}, REQ_CALL_PERMISSION);
        } else {
            executeDirectCall(number);
        }
    }

    private void executeDirectCall(String number) {
        String uriStr = buildDialUriString(number);
        try {
            Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse(uriStr));
            startActivity(callIntent);
        } catch (SecurityException e) {
            fallbackToDialer(number);
        } catch (Exception e) {
            fallbackToDialer(number);
        }
    }

    private void fallbackToDialer(String number) {
        String uriStr = buildDialUriString(number);
        try {
            Intent dialIntent = new Intent(Intent.ACTION_DIAL, Uri.parse(uriStr));
            startActivity(dialIntent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "No phone app available.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_CALL_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (pendingCallNumber != null) {
                    executeDirectCall(pendingCallNumber);
                }
            } else {
                Toast.makeText(this, "Call permission denied. Opening phone dialer...", Toast.LENGTH_SHORT).show();
                if (pendingCallNumber != null) {
                    fallbackToDialer(pendingCallNumber);
                }
            }
        }
    }

    private void startEdit(ContactItem contact) {
        editingId = contact.id;
        if (nameLayout != null) nameLayout.setError(null);
        if (phoneLayout != null) phoneLayout.setError(null);
        if (nameInput != null) nameInput.setText(contact.name);
        if (phoneInput != null) phoneInput.setText(contact.phone);
        if (saveButton != null) saveButton.setText("Update Contact");
        if (nameInput != null) nameInput.requestFocus();
    }

    private void deleteContact(long id) {
        String userEmail = tokenManager.getUserEmail();
        if (userEmail != null && !userEmail.isEmpty()) {
            dbHelper.getWritableDatabase().delete(
                    DatabaseHelper.TABLE_CONTACTS,
                    DatabaseHelper.KEY_ID + " = ? AND " + DatabaseHelper.KEY_USER_EMAIL + " = ?",
                    new String[]{String.valueOf(id), userEmail}
            );
        }

        if (editingId != null && editingId == id) {
            editingId = null;
            if (nameInput != null) nameInput.setText("");
            if (phoneInput != null) phoneInput.setText("");
            if (saveButton != null) saveButton.setText("Save Contact");
        }

        showContacts();
        Toast.makeText(this, "Contact deleted", Toast.LENGTH_SHORT).show();
    }

    private String textOf(TextInputEditText input) {
        return input.getText() == null ? "" : input.getText().toString().trim();
    }
}
