// Hecho por Gael
package com.journalite.journalite;

public final class Constants {

    public static final String DATE_FORMAT_ISO = "yyyy-MM-dd'T'HH:mm:ss";

    // Keys for the data passed to the reminder (Intent extras)
    public static final String EXTRA_HOMEWORK_ID = "homework_id";
    public static final String EXTRA_TITLE = "title";
    public static final String EXTRA_SUBJECT = "subject";

    // Folder (inside the app internal storage) where the evidence photos are saved
    public static final String EVIDENCE_DIR = "evidence";

    private Constants() {
    }
}
