package lk.ceylontravel.config;

import java.util.Set;

public final class UploadPolicy {

    // Singleton instance
    private static UploadPolicy instance;

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
    private static final long MAX_PIXELS = 16_000_000;

    private static final Set<String> ALLOWED_TYPES =
            Set.of("image/jpeg", "image/png");

    // Private constructor prevents external object creation
    private UploadPolicy() {
    }

    // Returns the single Singleton instance
    public static UploadPolicy getInstance() {
        if (instance == null) {
            instance = new UploadPolicy();
        }       
        return instance;
    }

    // Check file type and file size
    public boolean isAllowedUpload(
            String contentType,
            long fileSize) {

        return ALLOWED_TYPES.contains(contentType)
                && fileSize > 0
                && fileSize <= MAX_FILE_SIZE;
    }

    // Check image pixel count
    public boolean isValidPixelCount(
            int width,
            int height) {

        return (long) width * height <= MAX_PIXELS;
    }
}