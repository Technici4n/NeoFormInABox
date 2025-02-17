package net.neoforged.neoforminabox.actions;

import net.neoforged.neoforminabox.cli.FileHashService;
import org.jetbrains.annotations.Nullable;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Inject content from a directory on disk.
 *
 * @see InjectZipContentAction
 */
public record InjectFromDirectorySource(Path folder) implements InjectSource {
    @Override
    public String getCacheKey(FileHashService fileHashService) throws IOException {
        return fileHashService.getHashValue(folder);
    }

    @Override
    public byte @Nullable [] tryReadFile(String path) throws IOException {
        var file = folder.resolve(path);
        try {
            return Files.readAllBytes(file);
        } catch (FileNotFoundException ignored) {
            return null;
        }
    }

    @Override
    public void copyTo(ZipOutputStream out) throws IOException {
        try (var stream = Files.walk(folder).sorted()) {
            stream.forEach(path -> {
                var outputPath = folder.relativize(path).toString().replace('\\', '/');
                try {
                    out.putNextEntry(new ZipEntry(outputPath));
                    Files.copy(path, out);
                    out.closeEntry();
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        }

    }
}
