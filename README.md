# Journalite
#### Diario de Tareas y Evidencias Fotográficas
<img src="./logo.png" width="50%">

### Integrantes de equipo

- Alina Valeria Franco Gutiérrez
- Gael Orian Acevedo Miralles
- Paola Castillo Pérez

**Materia:** Programación de Dispositivos Móviles  
**Lenguaje / Plataforma:** Java — Android Studio (SDK 26+)

### Descripción del proyecto
**Journalite** es una aplicación móvil para Android pensada para estudiantes universitarios que quieren organizar mejor sus actividades de estudio por cuenta propia. A diferencia de plataformas como Canvas, que se utilizan principalmente para revisar tareas y entregas de los profesores, Journalite permite llevar un registro de repasos, metas personales y apuntes fotografiados en clase, organizados por materia para que no se pierdan entre las demás fotos del celular.

Una de las características principales de la aplicación es que no permite marcar una tarea como completada sin antes tomar una fotografía como evidencia. Además, utiliza los metadatos EXIF de la imagen para obtener la fecha y hora en que fue tomada, y confirma que la evidencia se guardó correctamente mediante una vibración corta del dispositivo.

---

### Funcionalidades y tecnologías móviles integradas
-**Almacenamiento local (SQLite):** La información se guarda directamente en el dispositivo, por lo que la aplicación puede utilizarse sin conexión a internet. Se utiliza SQLiteOpenHelper para administrar la base de datos journalite.db, donde se pueden consultar las tareas y filtrarlas por materia o estado (PENDIENTE / COMPLETADA).

-**Cámara y almacenamiento:** Permite tomar fotografías desde la aplicación y guardar las evidencias en una carpeta propia de Journalite.

-**Lectura de metadatos:** Obtiene automáticamente la fecha y hora de captura de la fotografía mediante TAG_DATETIME.

-**Recordatorios:** Permite programar notificaciones para recordar al estudiante cuándo debe repasar sus apuntes o realizar alguna actividad.

-**Confirmación por vibración:** El dispositivo emite una vibración corta cuando la evidencia se registra correctamente.

---

### Estructura del repositorio
- `app/` — Contiene el código fuente desarrollado en Java, los archivos XML de las pantallas y los recursos utilizados en Android Studio.
- `Documentacion/` — Incluye los documentos en PDF, el script de la base de datos (journalite_db.sql) y las capturas de pantalla y diagramas del proyecto.

### Link de documentos

- Modelo de base de datos: https://docs.google.com/document/d/1NdfaklhSnLf6QGdEKMffGmvt-6i2Wvv4ryUd4UJSz78/edit?usp=sharing
- Documento de requerimientos: https://docs.google.com/document/d/1oiLijUn2eX258Dmev_APnWs05JZKa2Zxhb9ouFbIxSc/edit?tab=t.0
- Prototipo de interfaz: https://docs.google.com/document/d/1t6jKIAFSJujWVwd_Ts9wnUcMe9BUgD6Vm0-84zLo6sI/edit?tab=t.0
