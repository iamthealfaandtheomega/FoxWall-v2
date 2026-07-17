package thezowi.foxwall.utils;

import java.io.*;
import java.nio.file.*;

// Just a class for show progress when downloading a file.
// This was little hard to do (for me in that time)
public class DownloadUtils {

    private static final int BUFFER_SIZE = 65536;
    private static final long REPORT_INTERVAL_MS = 500;

    public static void copyWithProgress(InputStream input, Path target, long totalSize, String type) throws IOException {
        Path temp = target.resolveSibling("."+target.getFileName()+".downloading");

        try (OutputStream output = new BufferedOutputStream(
                Files.newOutputStream(temp, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING), BUFFER_SIZE)) {
		            byte[] buffer = new byte[BUFFER_SIZE];
		            long totalRead = 0;
		            long lastReported = 0;
		            long lastTime = System.currentTimeMillis();
		            int read;
		
		            while ((read = input.read(buffer)) != -1) {
		                output.write(buffer, 0, read);
		                totalRead += read;
		
		                long now = System.currentTimeMillis();
		                long elapsed = now - lastTime;
		
		                if (elapsed >= REPORT_INTERVAL_MS) {
		                    long deltaBytes = totalRead - lastReported;
		                    final String speed = formatSpeed(deltaBytes, elapsed);
		                    final long totalReadFinal = totalRead;
		                    
		                    if (totalSize > 0) {
		                        int percent = (int) ((totalRead * 100) / totalSize);
		                        SharedFunctions.logger.info(() -> String.format("[%s] Downloading: %dKB / %dKB (%d%%) - [%s≈]", type, totalReadFinal / 1024, totalSize / 1024, percent, speed));
		                    } else {
		                        SharedFunctions.logger.info(() -> String.format("[%s] Downloading: %dKB downloaded... [%s≈]", type, totalReadFinal / 1024, speed));
		                    }
		
		                    lastReported = totalRead;
		                    lastTime = now;
		                }
		            }
		
		            output.flush();
        		}

        Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);
    }

    private static String formatSpeed(long bytes, long milliseconds) {
        if (milliseconds == 0) return "0B/s";
        double seconds = milliseconds / 1000.0;
        double bytesPerSecond = bytes / seconds;

        if (bytesPerSecond >= 1024 * 1024) {
            return String.format("%.2fMB/s", bytesPerSecond / (1024 * 1024));
        } else if (bytesPerSecond >= 1024) {
            return String.format("%.2fKB/s", bytesPerSecond / 1024);
        } else {
            return String.format("%.0fB/s", bytesPerSecond);
        }
    }
}