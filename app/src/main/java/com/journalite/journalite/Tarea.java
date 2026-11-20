package com.journalite.journalite;

public class Tarea {
    int idTarea;
    String titulo;
    String materia;
    String descripcion;
    String fechaRepaso;
    boolean notificacionesActivas;
    String estado;
    String rutaFoto;
    String exifFecha;

    public Tarea (
            int idTarea,
            String titulo,
            String materia,
            String descripcion,
            String fechaRepaso,
            boolean notificacionesActivas,
            String estado,
            String rutaFoto,
            String exifFecha
    ) {
        this.idTarea = idTarea;
        this.titulo = titulo;
        this.materia = materia;
        this.descripcion = descripcion;
        this.fechaRepaso = fechaRepaso;
        this.notificacionesActivas = notificacionesActivas;
        this.estado = estado;
        this.rutaFoto = rutaFoto;
        this.exifFecha = exifFecha;
    }

    public String getTitulo() {
        return this.titulo;
    }

    public String getMateria() {
        return this.materia;
    }

    public String getDescripcion() {
        return this.descripcion;
    }

    public String getFechaRepaso() {
        return this.fechaRepaso;
    }

    public boolean isNotificacionesActivas() {
        return this.notificacionesActivas;
    }

    public String getEstado() {
        return this.estado;
    }

    public String getRutaFoto() {
        return this.rutaFoto;
    }

    public String getExifFecha() {
        return this.exifFecha;
    }

    public int getIdTarea() {
        return this.idTarea;
    }
}
