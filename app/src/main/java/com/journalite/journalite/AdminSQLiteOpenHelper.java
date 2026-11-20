package com.journalite.journalite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
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
        db.execSQL("DROP TABLE IF EXISTS tareas");
        onCreate(db);
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

    public Tarea obtenerTarea(int id) {
        SQLiteDatabase db = getReadableDatabase();
        Tarea tarea = null;
        String[] columnas = {"id_tarea", "titulo", "materia", "descripcion", "fecha_repaso", "notificacion_activa", "estado", "ruta_foto", "exif_fecha"};
        String[] parametros = {String.valueOf(id)};
        Cursor cursor = db.query("tareas", columnas, "id_tarea = ?", parametros, null, null, null);
        if (cursor.moveToFirst()) {
            tarea = new Tarea(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getInt(5) == 1,
                    cursor.getString(6),
                    cursor.getString(7),
                    cursor.getString(8)
            );
        }
        cursor.close();
        return tarea;
    }

    public int actualizarEvidencia(int id, String rutaFoto, String exifFecha) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues valores = new ContentValues();
        valores.put("ruta_foto", rutaFoto);
        valores.put("exif_fecha", exifFecha);
        valores.put("estado", "COMPLETADA");
        return db.update("tareas", valores, "id_tarea = ?", new String[]{String.valueOf(id)});
    }

    public int eliminarTarea(int id) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete("tareas", "id_tarea = ?", new String[]{String.valueOf(id)});
    }
}
