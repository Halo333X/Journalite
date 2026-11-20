package com.journalite.journalite;

import android.Manifest;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class AgregarTareaActivity extends AppCompatActivity {

    private TextInputEditText etTitulo, etDescripcion, etFecha;
    private AutoCompleteTextView actvMateria;
    private SwitchMaterial switchNotificacion;
    private LinearLayout layoutHoraRecordatorio;
    private Button btnSeleccionarHora, btnGuardarTarea;

    private AdminSQLiteOpenHelper dbHelper;
    private Calendar calendarioSeleccionado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_tarea);

        dbHelper = new AdminSQLiteOpenHelper(this);
        calendarioSeleccionado = Calendar.getInstance();
        // Por defecto sugerimos hoy a las 9:00 AM (o +1 hora si ya pasó)
        calendarioSeleccionado.add(Calendar.HOUR_OF_DAY, 1);
        calendarioSeleccionado.set(Calendar.MINUTE, 0);
        calendarioSeleccionado.set(Calendar.SECOND, 0);

        enlazarVistas();
        configurarSugerenciasMaterias();
        configurarSelectoresFechaHora();
        pedirPermisoNotificacionesSiEsNecesario();

        btnGuardarTarea.setOnClickListener(v -> guardarTarea());
    }

    private void enlazarVistas() {
        MaterialToolbar toolbar = findViewById(R.id.toolbarAgregar);
        toolbar.setNavigationOnClickListener(v -> finish());

        etTitulo = findViewById(R.id.etTitulo);
        actvMateria = findViewById(R.id.actvMateria);
        etDescripcion = findViewById(R.id.etDescripcion);
        etFecha = findViewById(R.id.etFecha);
        switchNotificacion = findViewById(R.id.switchNotificacion);
        layoutHoraRecordatorio = findViewById(R.id.layoutHoraRecordatorio);
        btnSeleccionarHora = findViewById(R.id.btnSeleccionarHora);
        btnGuardarTarea = findViewById(R.id.btnGuardarTarea);

        actualizarTextoFecha();
        actualizarTextoHora();

        switchNotificacion.setOnCheckedChangeListener((buttonView, isChecked) ->
                layoutHoraRecordatorio.setVisibility(isChecked ? View.VISIBLE : View.GONE));
    }

    private void configurarSugerenciasMaterias() {
        // Materias sugeridas por defecto + materias que el usuario ya haya guardado antes en SQLite
        Set<String> materiasUnicas = new LinkedHashSet<>(Arrays.asList(
                "Programación", "Cálculo", "Física", "Inglés", "Bases de Datos", "General"
        ));

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT DISTINCT materia FROM tareas ORDER BY materia ASC", null);
        if (cursor.moveToFirst()) {
            do {
                String mat = cursor.getString(0);
                if (mat != null && !mat.trim().isEmpty()) {
                    materiasUnicas.add(mat.trim());
                }
            } while (cursor.moveToNext());
        }
        cursor.close();

        List<String> listaMaterias = new ArrayList<>(materiasUnicas);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, listaMaterias
        );
        actvMateria.setAdapter(adapter);
        actvMateria.setThreshold(1);
    }

    private void configurarSelectoresFechaHora() {
        etFecha.setOnClickListener(v -> {
            int anio = calendarioSeleccionado.get(Calendar.YEAR);
            int mes = calendarioSeleccionado.get(Calendar.MONTH);
            int dia = calendarioSeleccionado.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePicker = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                calendarioSeleccionado.set(Calendar.YEAR, year);
                calendarioSeleccionado.set(Calendar.MONTH, month);
                calendarioSeleccionado.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                actualizarTextoFecha();
            }, anio, mes, dia);

            datePicker.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            datePicker.show();
        });

        btnSeleccionarHora.setOnClickListener(v -> {
            int hora = calendarioSeleccionado.get(Calendar.HOUR_OF_DAY);
            int minuto = calendarioSeleccionado.get(Calendar.MINUTE);

            TimePickerDialog timePicker = new TimePickerDialog(this, (view, hourOfDay, minute) -> {
                calendarioSeleccionado.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendarioSeleccionado.set(Calendar.MINUTE, minute);
                calendarioSeleccionado.set(Calendar.SECOND, 0);
                actualizarTextoHora();
            }, hora, minuto, false);

            timePicker.show();
        });
    }

    private void actualizarTextoFecha() {
        String fechaFormateada = String.format(Locale.getDefault(), "%04d-%02d-%02d",
                calendarioSeleccionado.get(Calendar.YEAR),
                calendarioSeleccionado.get(Calendar.MONTH) + 1,
                calendarioSeleccionado.get(Calendar.DAY_OF_MONTH));
        etFecha.setText(fechaFormateada);
    }

    private void actualizarTextoHora() {
        int hora24 = calendarioSeleccionado.get(Calendar.HOUR_OF_DAY);
        int minuto = calendarioSeleccionado.get(Calendar.MINUTE);
        String amPm = (hora24 >= 12) ? "PM" : "AM";
        int hora12 = hora24 % 12;
        if (hora12 == 0) hora12 = 12;

        btnSeleccionarHora.setText(String.format(Locale.getDefault(), "%02d:%02d %s", hora12, minuto, amPm));
    }

    private void guardarTarea() {
        String titulo = etTitulo.getText() != null ? etTitulo.getText().toString().trim() : "";
        String materia = actvMateria.getText() != null ? actvMateria.getText().toString().trim() : "";
        String descripcion = etDescripcion.getText() != null ? etDescripcion.getText().toString().trim() : "";
        String fecha = etFecha.getText() != null ? etFecha.getText().toString().trim() : "";
        boolean activarNotif = switchNotificacion.isChecked();

        if (titulo.isEmpty()) {
            etTitulo.setError("Escribe el título de la tarea");
            etTitulo.requestFocus();
            return;
        }
        if (materia.isEmpty()) {
            actvMateria.setError("Selecciona o escribe una materia");
            actvMateria.requestFocus();
            return;
        }

        // Guardamos fecha + hora juntos en fecha_repaso para que se vea completo en el detalle
        String fechaYHora = fecha + " " + btnSeleccionarHora.getText().toString();

        // Usamos exactamente el constructor de Tarea.java de tu equipo
        Tarea nuevaTarea = new Tarea(
                0,
                titulo,
                materia,
                descripcion,
                fechaYHora,
                activarNotif,
                "PENDIENTE",
                null,
                null
        );

        long idGenerado = dbHelper.insertarTarea(nuevaTarea);

        if (idGenerado != -1) {
            if (activarNotif) {
                programarNotificacion((int) idGenerado, titulo, materia);
            }
            Toast.makeText(this, "Meta guardada correctamente", Toast.LENGTH_SHORT).show();
            finish(); // Regresa automáticamente a MainActivity
        } else {
            Toast.makeText(this, "Error al guardar en la base de datos", Toast.LENGTH_SHORT).show();
        }
    }

    private void programarNotificacion(int idTarea, String titulo, String materia) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(this, RecordatorioReceiver.class);
        intent.putExtra("id_tarea", idTarea);
        intent.putExtra("titulo", titulo);
        intent.putExtra("materia", materia);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                idTarea,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        long tiempoAlarma = calendarioSeleccionado.getTimeInMillis();
        // Si la hora elegida ya pasó hoy, evitamos que se dispare retroactiva o la dejamos lista
        if (tiempoAlarma <= System.currentTimeMillis()) {
            tiempoAlarma = System.currentTimeMillis() + 5000; // 5 segundos para prueba rápida si eligieron hora pasada
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                alarmManager.set(AlarmManager.RTC_WAKEUP, tiempoAlarma, pendingIntent);
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, tiempoAlarma, pendingIntent);
            }
        } catch (SecurityException e) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, tiempoAlarma, pendingIntent);
        }
    }

    private void pedirPermisoNotificacionesSiEsNecesario() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }
}
