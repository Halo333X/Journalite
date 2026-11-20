package com.journalite.journalite;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class AdminSQLiteOpenHelper extends SQLiteOpenHelper {
    public AdminSQLiteOpenHelper(Context context) {
        super(context, "journalite.db", null, 1);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE tareas (" +
                "id_tarea INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "titulo TEXT NOT NULL, " +
                "materia TEXT NOT NULL, " +
                "descripcion TEXT, " +
                "fecha_repaso TEXT NOT NULL, " +
                "notificacion_activa INTEGER DEFAULT 0, " +
                "estado TEXT NOT NULL DEFAULT 'PENDIENTE', " +
                "ruta_foto TEXT, " +
                "exif_fecha TEXT" +
                ")");
        db.execSQL("CREATE INDEX idx_tareas_materia ON tareas(materia)");
        db.execSQL("CREATE INDEX idx_tareas_estado ON tareas(estado)");
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public long insertarTarea(Tarea tarea) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues valores = new ContentValues();
        valores.put("titulo", tarea.getTitulo());
        valores.put("materia", tarea.getMateria());
        valores.put("descripcion", tarea.getDescripcion());
        valores.put("fecha_repaso", tarea.getFechaRepaso());
        valores.put("notificacion_activa", tarea.isNotificacionesActivas() ? 1 : 0);
        valores.put("estado", "PENDIENTE");

        long id = db.insert("tareas", null, valores);
        return id;
    }
}
