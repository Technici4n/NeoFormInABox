package net.neoforged.neoforminabox.utils;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class FileUtil {
    /**
     * The maximum number of tries that the system will try to atomically move a file.
     */
    private static final int MAX_TRIES = 2;

    private FileUtil() {
    }

    /**
     * Atomically moves the given source file to the given destination file.
     *
     * @param source      The source file
     * @param destination The destination file
     * @throws IOException If an I/O error occurs
     */
    @SuppressWarnings("BusyWait")

    public static void atomicMove(Path source, Path destination) throws IOException {
        if (Files.isDirectory(destination)) {
            throw new IOException("Cannot overwrite directory " + destination);
        }

        try {
            atomicMoveIfPossible(source, destination);
        } catch (AccessDeniedException ex) {
            // Sometimes because of file locking this will fail... Let's just try again and hope for the best
            // Thanks Windows!
            for (int tries = 0; true; ++tries) {
                // Pause for a bit
                try {
                    Thread.sleep(10L * tries);
                    atomicMoveIfPossible(source, destination);
                    return;
                } catch (final AccessDeniedException ex2) {
                    if (tries == MAX_TRIES - 1) {
                        throw ex;
                    }
                } catch (final InterruptedException exInterrupt) {
                    Thread.currentThread().interrupt();
                    throw ex;
                }
            }
        }
    }

    /**
     * Atomically moves the given source file to the given destination file.
     * If the atomic move is not supported, the file will be moved normally.
     *
     * @param source      The source file
     * @param destination The destination file
     * @throws IOException If an I/O error occurs
     */
    private static void atomicMoveIfPossible(final Path source, final Path destination) throws IOException {
        try {
            Files.move(source, destination, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (final AtomicMoveNotSupportedException ex) {
            Files.move(source, destination, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
