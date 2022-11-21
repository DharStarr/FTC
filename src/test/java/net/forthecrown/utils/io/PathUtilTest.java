package net.forthecrown.utils.io;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class PathUtilTest {

    @Test
    void findAllFiles() {
        Path workingDir = new File("")
                .toPath()
                .toAbsolutePath()
                .resolve("src")
                .resolve("main")
                .resolve("java")
                .resolve("net")
                .resolve("forthecrown");

        var result = PathUtil.findAllFiles(workingDir, false, false);

        assertDoesNotThrow(() -> {
            var results = result.getOrThrow(true, s -> {
                throw new RuntimeException(s);
            });

            System.out.printf("%s results!\n", results.size());
            results.forEach(System.out::println);
        });
    }

    @Test
    void archive() {
        Path dir = new File("")
                .toPath()
                .toAbsolutePath()
                .resolve("src")
                .resolve("test");

        Path output = new File("")
                .toPath()
                .toAbsolutePath()
                .resolve("test_output")
                .resolve("PathUtilTest_archive.zip");

        assertDoesNotThrow(() -> {
            PathUtil.archive(dir, output);
        });
    }
}