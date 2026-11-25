// Hecho por Gael
package com.journalite.journalite;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.io.IOException;

// Screen 3: shows one homework and lets the user complete it by taking an evidence photo.
// It receives the homework id in the Intent (Constants.EXTRA_HOMEWORK_ID).
public class HomeWorkDetailActivity extends AppCompatActivity {

    private AdminSQLiteOpenHelper dbHelper;
    private int homeWorkId;
    private HomeWork homeWork;

    private TextView tvStatus, tvTitle, tvSubject, tvDate, tvDescription;
    private TextView tvEvidenceDate, tvEvidenceResolution;
    private View layoutEvidencePending, layoutEvidenceDone;
    private ImageView ivEvidence;
    private MaterialButton btnTakePhoto;

    // Opens the camera app and saves the photo in the Uri we give it
    private final ActivityResultLauncher<Uri> takePictureLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> onPictureTaken(success));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_homework_detail);

        dbHelper = new AdminSQLiteOpenHelper(this);
        homeWorkId = getIntent().getIntExtra(Constants.EXTRA_HOMEWORK_ID, -1);

        bindViews();
        setupWindowInsets();

        if (loadHomeWork()) {
            showHomeWork();
        }
    }

    // Keeps the toolbar below the status bar and the content above the navigation bar
    private void setupWindowInsets() {
        View root = findViewById(R.id.mainDetail);
        View scroll = findViewById(R.id.scrollDetail);

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, 0);
            scroll.setPadding(0, 0, 0, bars.bottom);
            return insets;
        });
    }

    private void bindViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbarDetail);
        toolbar.setNavigationOnClickListener(v -> finish());
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_delete) {
                confirmDelete();
                return true;
            }
            return false;
        });

        tvStatus = findViewById(R.id.tvStatus);
        tvTitle = findViewById(R.id.tvTitle);
        tvSubject = findViewById(R.id.tvSubject);
        tvDate = findViewById(R.id.tvDate);
        tvDescription = findViewById(R.id.tvDescription);
        tvEvidenceDate = findViewById(R.id.tvEvidenceDate);
        tvEvidenceResolution = findViewById(R.id.tvEvidenceResolution);
        layoutEvidencePending = findViewById(R.id.layoutEvidencePending);
        layoutEvidenceDone = findViewById(R.id.layoutEvidenceDone);
        ivEvidence = findViewById(R.id.ivEvidence);
        btnTakePhoto = findViewById(R.id.btnTakePhoto);

        btnTakePhoto.setOnClickListener(v -> takeEvidencePhoto());
    }

    // Reads the homework from the database. If it does not exist, closes the screen.
    private boolean loadHomeWork() {
        homeWork = dbHelper.getHomeWork(homeWorkId);
        if (homeWork == null) {
            Toast.makeText(this, R.string.error_homework_not_found, Toast.LENGTH_SHORT).show();
            finish();
            return false;
        }
        return true;
    }

    private void showHomeWork() {
        tvTitle.setText(homeWork.getTitle());
        tvSubject.setText(homeWork.getSubject());
        tvDate.setText(getString(R.string.detail_target_date, DateHelper.toDisplay(homeWork.getReviewDate())));

        String description = homeWork.getDescription();
        if (description == null || description.trim().isEmpty()) {
            tvDescription.setText(R.string.detail_no_description);
        } else {
            tvDescription.setText(description);
        }

        boolean completed = homeWork.isCompleted();
        tvStatus.setText(completed ? R.string.status_completed : R.string.status_pending);
        int statusColor = ContextCompat.getColor(this,
                completed ? R.color.status_completed_green : R.color.status_pending_red);
        ViewCompat.setBackgroundTintList(tvStatus, ColorStateList.valueOf(statusColor));

        layoutEvidencePending.setVisibility(completed ? View.GONE : View.VISIBLE);
        layoutEvidenceDone.setVisibility(completed ? View.VISIBLE : View.GONE);
        if (completed) {
            showEvidence();
        }
    }

    // ---------------------------------------------------------------- Evidence photo

    private void showEvidence() {
        tvEvidenceDate.setText(getString(R.string.evidence_registered_on,
                DateHelper.toDisplay(homeWork.getPhotoDate())));

        String path = homeWork.getPhotoPath();
        File file = path == null ? null : new File(path);
        if (file == null || !file.exists()) {
            ivEvidence.setImageResource(android.R.drawable.ic_menu_report_image);
            tvEvidenceResolution.setText(R.string.photo_unavailable);
            return;
        }

        int rotation = getRotationDegrees(path);
        ivEvidence.setImageBitmap(loadScaledBitmap(path, rotation));
        tvEvidenceResolution.setText(getString(R.string.evidence_resolution, getResolutionText(path, rotation)));
    }

    // The photo is always saved with the same name, so it can be found again after the camera closes
    private File getEvidenceFile() {
        File dir = new File(getFilesDir(), Constants.EVIDENCE_DIR);
        if (!dir.exists() && !dir.mkdirs()) {
            return null;
        }
        return new File(dir, "evidence_" + homeWorkId + ".jpg");
    }

    private void takeEvidencePhoto() {
        File file = getEvidenceFile();
        try {
            if (file == null || (!file.exists() && !file.createNewFile())) {
                Toast.makeText(this, R.string.error_photo_failed, Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (IOException e) {
            Toast.makeText(this, R.string.error_photo_failed, Toast.LENGTH_SHORT).show();
            return;
        }

        Uri photoUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);
        try {
            takePictureLauncher.launch(photoUri);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.error_camera_unavailable, Toast.LENGTH_SHORT).show();
        }
    }

    private void onPictureTaken(boolean success) {
        File file = getEvidenceFile();
        if (file == null) {
            Toast.makeText(this, R.string.error_photo_failed, Toast.LENGTH_SHORT).show();
            return;
        }

        if (!success) {
            // The user closed the camera without taking a photo: remove the empty file
            file.delete();
            return;
        }
        if (!file.exists() || file.length() == 0) {
            Toast.makeText(this, R.string.error_photo_failed, Toast.LENGTH_SHORT).show();
            return;
        }

        int updated = dbHelper.updateEvidence(homeWorkId, file.getAbsolutePath(), readCaptureDate(file));
        if (updated > 0) {
            // The task is done, so its reminder is no longer needed
            ReminderScheduler.cancel(this, homeWorkId);
            vibrate();
            Toast.makeText(this, R.string.evidence_saved, Toast.LENGTH_SHORT).show();
            if (loadHomeWork()) {
                showHomeWork();
            }
        } else {
            Toast.makeText(this, R.string.error_photo_failed, Toast.LENGTH_SHORT).show();
        }
    }

    // Capture date from the photo metadata (EXIF), saved as ISO text. If missing, uses the file date.
    private String readCaptureDate(File file) {
        try {
            ExifInterface exif = new ExifInterface(file.getAbsolutePath());
            String exifDate = exif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL);
            if (exifDate == null) {
                exifDate = exif.getAttribute(ExifInterface.TAG_DATETIME);
            }
            String isoDate = DateHelper.exifToIso(exifDate);
            if (isoDate != null) {
                return isoDate;
            }
        } catch (IOException e) {
            // Falls back to the file date below
        }
        return DateHelper.millisToIso(file.lastModified());
    }

    // Short vibration to confirm that the evidence was saved
    private void vibrate() {
        Vibrator vibrator;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            VibratorManager manager = (VibratorManager) getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
            vibrator = manager != null ? manager.getDefaultVibrator() : null;
        } else {
            vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        }
        if (vibrator != null && vibrator.hasVibrator()) {
            vibrator.vibrate(VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE));
        }
    }

    // ---------------------------------------------------------------- Image helpers

    // Camera photos keep their real orientation in the metadata, so the image must be rotated when shown
    private int getRotationDegrees(String path) {
        try {
            ExifInterface exif = new ExifInterface(path);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    return 90;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    return 180;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    return 270;
                default:
                    return 0;
            }
        } catch (IOException e) {
            return 0;
        }
    }

    private BitmapFactory.Options readBounds(String path) {
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, bounds);
        return bounds;
    }

    // Loads a smaller version of the photo (about the width of the screen) to avoid running out of memory
    private Bitmap loadScaledBitmap(String path, int rotation) {
        BitmapFactory.Options bounds = readBounds(path);
        int maxSide = getResources().getDisplayMetrics().widthPixels;

        int sampleSize = 1;
        while (Math.max(bounds.outWidth, bounds.outHeight) / sampleSize > maxSide) {
            sampleSize *= 2;
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = sampleSize;
        Bitmap bitmap = BitmapFactory.decodeFile(path, options);
        if (bitmap == null || rotation == 0) {
            return bitmap;
        }

        Matrix matrix = new Matrix();
        matrix.postRotate(rotation);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    // Real size of the photo (not the reduced one), as the user sees it
    private String getResolutionText(String path, int rotation) {
        BitmapFactory.Options bounds = readBounds(path);
        int width = bounds.outWidth;
        int height = bounds.outHeight;
        if (rotation == 90 || rotation == 270) {
            int temp = width;
            width = height;
            height = temp;
        }
        return width + " x " + height + " px";
    }

    // ---------------------------------------------------------------- Delete

    private void confirmDelete() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.delete_title)
                .setMessage(R.string.delete_message)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.delete_confirm, (dialog, which) -> deleteHomeWork())
                .show();
    }

    private void deleteHomeWork() {
        String photoPath = homeWork.getPhotoPath();

        if (dbHelper.deleteHomeWork(homeWorkId) > 0) {
            // Also remove the evidence photo and the reminder of this task
            if (photoPath != null) {
                new File(photoPath).delete();
            }
            ReminderScheduler.cancel(this, homeWorkId);

            Toast.makeText(this, R.string.homework_deleted, Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, R.string.error_deleting, Toast.LENGTH_SHORT).show();
        }
    }
}
