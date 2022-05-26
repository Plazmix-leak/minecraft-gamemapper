package org.stonlexx.minecraft.gamemapper.utility;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@SuppressWarnings("all")
@UtilityClass
public final class Directories {

    @SneakyThrows
    public void createIfNotExists(@NonNull File directory) {

        if (!Files.exists(directory.toPath())) {
            Files.createDirectories(directory.toPath());
        }
    }

    public void clearDirectory(@NonNull File directory, boolean delete) {
        if (!directory.exists()) {
            return;
        }

        if (directory.isDirectory()) {
            for (File directoryFile : directory.listFiles()) {

                if (directoryFile.isDirectory()) {
                    clearDirectory(directoryFile, true);

                    continue;
                }

                directoryFile.delete();
            }
        }

        if (delete) {
            directory.delete();
        }
    }

    public void copyDirectory(@NonNull Path source, @NonNull Path target, String... ignoreFilesPrefix) throws IOException {
        if (Files.isDirectory(source)) {

            if (!Files.exists(target)) {
                Files.createDirectories(target);
            }

            for (File directoryFile : source.toFile().listFiles()) {
                copyDirectory(directoryFile.toPath(), target.resolve(directoryFile.getName()));
            }

        } else {

            for (String ignore : ignoreFilesPrefix) {
                if (source.toFile().getName().startsWith(ignore)) {
                    return;
                }
            }

            Files.deleteIfExists(target);
            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

}
