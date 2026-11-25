// Hecho por Gael
package com.journalite.journalite;

public class HomeWork {
    private final int idHomeWork;
    private final String title;
    private final String subject;
    private final String description;
    private final String reviewDate;
    private final boolean notifications;
    private final String status;
    private final String photoPath;
    private final String photoDate;
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_COMPLETED = "COMPLETED";

    public HomeWork (
            int idHomeWork,
            String title,
            String subject,
            String description,
            String reviewDate,
            boolean notifications,
            String status,
            String photoPath,
            String photoDate
    ) {
        this.idHomeWork = idHomeWork;
        this.title = title;
        this.subject = subject;
        this.description = description;
        this.reviewDate = reviewDate;
        this.notifications = notifications;
        this.status = status;
        this.photoPath = photoPath;
        this.photoDate = photoDate;
    }

    public String getTitle() {
        return this.title;
    }

    public String getSubject() {
        return this.subject;
    }

    public String getDescription() {
        return this.description;
    }

    public String getReviewDate() {
        return this.reviewDate;
    }

    public boolean hasNotifications() {
        return this.notifications;
    }

    public String getStatus() {
        return this.status;
    }

    public String getPhotoPath() {
        return this.photoPath;
    }

    public String getPhotoDate() {
        return this.photoDate;
    }

    public boolean isCompleted() {
        return STATUS_COMPLETED.equals(this.status);
    }

    public int getHomeWorkId() {
        return this.idHomeWork;
    }
}
