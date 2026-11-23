package com.journalite.journalite;

import android.Manifest;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class AddHomeWorkActivity extends AppCompatActivity {

    private static final int REQUEST_NOTIFICATION_PERMISSION = 101;

    private TextInputEditText etTitle, etDescription, etDate;
    private AutoCompleteTextView actvSubject;
    private SwitchMaterial switchReminder;
    private LinearLayout layoutReminderTime;
    private MaterialButton btnSelectTime, btnSaveHomeWork;

    private AdminSQLiteOpenHelper dbHelper;
    private Calendar selectedCalendar;
    private boolean dateSelected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_agregar_tarea);

        // Evita que la barra superior choque con la hora y batería del teléfono
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainAgregar), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = new AdminSQLiteOpenHelper(this);
        selectedCalendar = Calendar.getInstance();
        selectedCalendar.set(Calendar.HOUR_OF_DAY, 10);
        selectedCalendar.set(Calendar.MINUTE, 0);
        selectedCalendar.set(Calendar.SECOND, 0);

        bindViews();
        setupSubjectSuggestions();
        setupDateTimePickers();

        btnSaveHomeWork.setOnClickListener(v -> saveHomeWork());
    }

    private void bindViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbarAgregar);
        toolbar.setNavigationOnClickListener(v -> finish());

        etTitle = findViewById(R.id.etTitulo);
        actvSubject = findViewById(R.id.actvMateria);
        etDescription = findViewById(R.id.etDescripcion);
        etDate = findViewById(R.id.etFecha);
        switchReminder = findViewById(R.id.switchNotificacion);
        layoutReminderTime = findViewById(R.id.layoutHoraRecordatorio);
        btnSelectTime = findViewById(R.id.btnSeleccionarHora);
        btnSaveHomeWork = findViewById(R.id.btnGuardarTarea);

        updateTimeText();

        // Al activar el switch muestra el selector de hora y pide permiso si es Android 13+
        switchReminder.setOnCheckedChangeListener((buttonView, isChecked) -> {
            layoutReminderTime.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            if (isChecked) {
                requestNotificationPermissionIfNeeded();
            }
        });
    }

    private void setupSubjectSuggestions() {
        Set<String> uniqueSubjects = new LinkedHashSet<>(Arrays.asList(
                "Programación", "Cálculo", "Física", "Inglés", "Bases de Datos", "General"
        ));

        uniqueSubjects.addAll(dbHelper.getSubjects());

        List<String> subjectList = new ArrayList<>(uniqueSubjects);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, subjectList
        );
        actvSubject.setAdapter(adapter);
        actvSubject.setThreshold(1);
    }

    private void setupDateTimePickers() {
        etDate.setOnClickListener(v -> {
            int initialYear = selectedCalendar.get(Calendar.YEAR);
            int initialMonth = selectedCalendar.get(Calendar.MONTH);
            int initialDay = selectedCalendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePicker = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                selectedCalendar.set(Calendar.YEAR, year);
                selectedCalendar.set(Calendar.MONTH, month);
                selectedCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                dateSelected = true;
                updateDateText();
            }, initialYear, initialMonth, initialDay);

            datePicker.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            datePicker.show();
        });

        btnSelectTime.setOnClickListener(v -> {
            int initialHour = selectedCalendar.get(Calendar.HOUR_OF_DAY);
            int initialMinute = selectedCalendar.get(Calendar.MINUTE);

            TimePickerDialog timePicker = new TimePickerDialog(this, (view, hourOfDay, minute) -> {
                selectedCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                selectedCalendar.set(Calendar.MINUTE, minute);
                selectedCalendar.set(Calendar.SECOND, 0);
                updateTimeText();
            }, initialHour, initialMinute, false);

            timePicker.show();
        });
    }

    private void updateDateText() {
        String formattedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d",
                selectedCalendar.get(Calendar.YEAR),
                selectedCalendar.get(Calendar.MONTH) + 1,
                selectedCalendar.get(Calendar.DAY_OF_MONTH));
        etDate.setText(formattedDate);
    }

    private void updateTimeText() {
        int hour24 = selectedCalendar.get(Calendar.HOUR_OF_DAY);
        int minute = selectedCalendar.get(Calendar.MINUTE);
        String amPm = (hour24 >= 12) ? "PM" : "AM";
        int hour12 = hour24 % 12;
        if (hour12 == 0) hour12 = 12;

        btnSelectTime.setText(String.format(Locale.getDefault(), "%02d:%02d %s", hour12, minute, amPm));
    }

    private void saveHomeWork() {
        String title = etTitle.getText() != null ? etTitle.getText().toString().trim() : "";
        String subject = actvSubject.getText() != null ? actvSubject.getText().toString().trim() : "";
        String description = etDescription.getText() != null ? etDescription.getText().toString().trim() : "";
        boolean reminderEnabled = switchReminder.isChecked();

        if (title.isEmpty()) {
            etTitle.setError(getString(R.string.error_title_required));
            etTitle.requestFocus();
            return;
        }
        if (subject.isEmpty()) {
            actvSubject.setError(getString(R.string.error_subject_required));
            actvSubject.requestFocus();
            return;
        }
        if (!dateSelected) {
            Toast.makeText(this, "Seleccione una fecha para la tarea", Toast.LENGTH_SHORT).show();
            return;
        }

        if (reminderEnabled && selectedCalendar.getTimeInMillis() <= System.currentTimeMillis()) {
            Toast.makeText(this, R.string.error_past_time, Toast.LENGTH_SHORT).show();
            return;
        }

        String reviewDate = new SimpleDateFormat(Constants.DATE_FORMAT_ISO, Locale.US)
                .format(selectedCalendar.getTime());

        HomeWork newHomeWork = new HomeWork(
                0,
                title,
                subject,
                description,
                reviewDate,
                reminderEnabled,
                HomeWork.STATUS_PENDING,
                null,
                null
        );

        long generatedId = dbHelper.insertHomeWork(newHomeWork);

        if (generatedId != -1) {
            if (reminderEnabled) {
                scheduleReminder((int) generatedId, title, subject);
            }
            Toast.makeText(this, R.string.homework_saved, Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, R.string.error_saving, Toast.LENGTH_SHORT).show();
        }
    }

    private void scheduleReminder(int homeWorkId, String title, String subject) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(this, ReminderReceiver.class);
        intent.putExtra(Constants.EXTRA_HOMEWORK_ID, homeWorkId);
        intent.putExtra(Constants.EXTRA_TITLE, title);
        intent.putExtra(Constants.EXTRA_SUBJECT, subject);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                homeWorkId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        long alarmTime = selectedCalendar.getTimeInMillis();

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
            }
        } catch (SecurityException e) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
        }
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_NOTIFICATION_PERMISSION);
            }
        }
    }
}
