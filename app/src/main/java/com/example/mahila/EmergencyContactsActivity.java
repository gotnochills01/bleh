package com.example.mahila;

import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
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
import java.util.Locale;

public class EmergencyContactsActivity extends BaseActivity {

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

    public static String normalizeUserEmail(String email) {
        if (email == null) return "";
        return email.trim().toLowerCase(Locale.US);
    }

    public static String sanitizeContactName(String rawName) {
        if (rawName == null) return "";
        String normalized = rawName.trim().replaceAll("\\s+", " ");
        if (normalized.isEmpty()) return "";

        String[] parts = normalized.split(" ");
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (part.isEmpty()) continue;
            if (builder.length() > 0) builder.append(" ");
            builder.append(part.substring(0, 1).toUpperCase(Locale.US));
            if (part.length() > 1) {
                builder.append(part.substring(1).toLowerCase(Locale.US));
            }
        }
        return builder.toString();
    }

    public static boolean isValidPhoneNumber(String phoneNumber) {
        if (phoneNumber == null) return false;
        String digits = phoneNumber.replaceAll("\\D", "");
        return digits.length() >= 10 && digits.length() <= 15;
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
                getSupportActionBar().setTitle(R.string.emergency_contacts_title);
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

    private String getCurrentUserEmail() {
        String storedEmail = new AppPreferences(this).getCurrentUserEmail();
        String sessionEmail = tokenManager != null ? tokenManager.getUserEmail() : null;
        return normalizeUserEmail(AppPreferences.resolveCurrentUserEmail(storedEmail, sessionEmail));
    }

    private List<ContactItem> getContactsForCurrentUser() {
        List<ContactItem> list = new ArrayList<>();
        String userEmail = getCurrentUserEmail();
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
                if (name != null && phone != null) {
                    list.add(new ContactItem(id, name, phone));
                }
            }
            cursor.close();
        }
        return list;
    }

    private void saveContact() {
        if (nameLayout != null) nameLayout.setError(null);
        if (phoneLayout != null) phoneLayout.setError(null);

        String userEmail = getCurrentUserEmail();
        if (userEmail == null || userEmail.isEmpty()) {
            Toast.makeText(this, getString(R.string.contact_login_required), Toast.LENGTH_SHORT).show();
            return;
        }

        String name = nameInput != null ? sanitizeContactName(textOf(nameInput)) : "";
        String phone = phoneInput != null ? textOf(phoneInput) : "";

        if (name.length() < 2) {
            if (nameLayout != null) nameLayout.setError(getString(R.string.contact_name_error));
            return;
        }

        if (!isValidPhoneNumber(phone)) {
            if (phoneLayout != null) phoneLayout.setError(getString(R.string.contact_phone_error));
            return;
        }

        List<ContactItem> contacts = getContactsForCurrentUser();
        String digits = phone.replaceAll("\\D", "");
        for (ContactItem contact : contacts) {
            boolean sameNumber = contact.phone != null && contact.phone.replaceAll("\\D", "").equals(digits);
            boolean differentRecord = editingId == null || contact.id != editingId;
            if (sameNumber && differentRecord) {
                    if (phoneLayout != null) phoneLayout.setError(getString(R.string.contact_duplicate_phone));
                    return;
                }
            }

        boolean success;
        if (editingId == null) {
            success = dbHelper.saveContact(userEmail, name, phone, false);
            if (success) {
                Toast.makeText(this, getString(R.string.contact_saved_local), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.contact_save_failed), Toast.LENGTH_SHORT).show();
            }
        } else {
            success = dbHelper.updateContact(editingId, userEmail, name, phone);
            if (success) {
                Toast.makeText(this, getString(R.string.contact_updated_local), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.contact_update_failed), Toast.LENGTH_SHORT).show();
            }
            editingId = null;
            if (saveButton != null) saveButton.setText(R.string.save_contact);
            if (phoneInput != null) phoneInput.setText("");
            showContacts();
        }
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
            MaterialButton callButton = item.findViewById(R.id.button_call_contact);
            MaterialButton editButton = item.findViewById(R.id.button_edit_contact);
            MaterialButton deleteButton = item.findViewById(R.id.button_delete_contact);

            if (nameView != null) nameView.setText(contact.name);
            if (phoneView != null) phoneView.setText(contact.phone);

            if (callButton != null) callButton.setOnClickListener(v -> openDialer(contact.phone));
            if (editButton != null) editButton.setOnClickListener(v -> startEdit(contact));
            if (deleteButton != null) deleteButton.setOnClickListener(v -> deleteContact(contact.id));

            contactsContainer.addView(item);
        }
    }

    private void initiatePhoneCall(String number) {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Open Phone Dialer")
                .setMessage("Open the phone dialer with " + number + "?")
                .setPositiveButton("Open Dialer", (dialog, which) -> openDialer(number))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void openDialer(String number) {
        String uriStr = buildDialUriString(number);
        try {
            Intent dialIntent = new Intent(Intent.ACTION_DIAL, Uri.parse(uriStr));
            startActivity(dialIntent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "No phone dialer app available on device.", Toast.LENGTH_SHORT).show();
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
        String userEmail = getCurrentUserEmail();
        if (userEmail != null && !userEmail.isEmpty()) {
            boolean deleted = dbHelper.deleteContact(id, userEmail);
            if (!deleted) {
                Toast.makeText(this, getString(R.string.contact_delete_failed), Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (editingId != null && editingId == id) {
            editingId = null;
            if (nameInput != null) nameInput.setText("");
            if (phoneInput != null) phoneInput.setText("");
            if (saveButton != null) saveButton.setText(R.string.save_contact);
        }

        showContacts();
        Toast.makeText(this, getString(R.string.contact_deleted), Toast.LENGTH_SHORT).show();
    }

    private String textOf(TextInputEditText input) {
        return input.getText() == null ? "" : input.getText().toString().trim();
    }
}
