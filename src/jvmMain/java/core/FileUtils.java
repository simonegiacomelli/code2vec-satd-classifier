package core;

/* Simone 04/12/13 11.59 */

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

public class FileUtils {
    public static void write(String file, CharSequence data) {
        File file1 = new File(file);
        write(file1, data);
    }

    public static void write(String file, byte[] data) {
        File file1 = new File(file);
        write(file1, data);
    }

    public static void write(Path path, CharSequence data) {
        write(path.toFile(), data);
    }

    public static void write(Path path, byte[] data) {
        write(path.toFile(), data);
    }

    public static void append(Path path, CharSequence data) {
        append(path.toFile(), data);
    }

    public static void append(String file, CharSequence data) {
        File file1 = new File(file);
        append(file1, data);
    }

    public static void append(File file, CharSequence data) {
        try {
            org.apache.commons.io.FileUtils.write(file, data, Charset.defaultCharset(), true);
        } catch (IOException e) {
            throw new RuntimeIOException(e);
        }
    }

    public static void append(File file, byte[] data) {
        try {
            try (FileOutputStream fos = new FileOutputStream(file, true)) {
                fos.write(data);
            }
        } catch (IOException e) {
            throw new RuntimeIOException(e);
        }
    }

    public static void append(String path, byte[] data) {
        try {
            try (FileOutputStream fos = new FileOutputStream(new File(path), true)) {
                fos.write(data);
            }
        } catch (IOException e) {
            throw new RuntimeIOException(e);
        }
    }

    public static void write(File file, CharSequence data) {
        try {
            org.apache.commons.io.FileUtils.write(file, data, (String) null, false);
        } catch (IOException e) {
            throw new RuntimeIOException(e);
        }
    }

    public static void write(File file, byte[] data) {
        try {
            FileOutputStream fos = new FileOutputStream(file);
            try {
                fos.write(data);
            } finally {
                fos.close();
            }
        } catch (IOException e) {
            throw new RuntimeIOException(e);
        }
    }

    public static String read(File file) {
        try {
            return org.apache.commons.io.FileUtils.readFileToString(file, Charset.defaultCharset());
        } catch (IOException e) {
            throw new RuntimeIOException(e);
        }
    }

    public static String read(String path) {
        return read(new File(path));
    }

    public static List<String> readLines(String path) {
        return readLines(new File(path));
    }

    public static List<String> readLines(File file) {
        try {
            return org.apache.commons.io.FileUtils.readLines(file);
        } catch (IOException e) {
            throw new RuntimeIOException(e);
        }
    }

    public static List<String> readLines(Path path) {
        return readLines(path.toFile());
    }

    public static String read(Path path) {
        try {
            return org.apache.commons.io.FileUtils.readFileToString(path.toFile(), Charset.defaultCharset());
        } catch (IOException e) {
            throw new RuntimeIOException(e);
        }
    }

    public static void writeLines(File file, Collection<?> lines) {
        try {
            org.apache.commons.io.FileUtils.writeLines(file, lines);
        } catch (IOException e) {
            throw new RuntimeIOException(e);
        }
    }


    public static FileOutputStream newFileOutputStream(File file) {
        try {
            return new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeIOException(e);
        }
    }

    public static FileInputStream newFileInputStream(File file) {
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeIOException(e);
        }
    }

    public static Path move(Path source, Path target, CopyOption... options) {
        try {
            return Files.move(source, target, options);
        } catch (IOException e) {
            throw new RuntimeIOException(e);
        }
    }

    public static void delete(File file) {
        if (file.exists())
            file.delete();
    }

    public static boolean createNewFile(File file) {
        try {
            return file.createNewFile();
        } catch (IOException e) {
            throw new RuntimeIOException(e);
        }
    }

    public static byte[] readBytes(File file) {
        try {
            return org.apache.commons.io.FileUtils.readFileToByteArray(file);
        } catch (IOException e) {
            throw new RuntimeIOException(e);
        }
    }

    public static byte[] readBytes(Path path) {
        return readBytes(path.toFile());
    }

    public static byte[] readBytes(String path) {
        return readBytes(new File(path));
    }

    public static void delete(String path) {
        delete(new File(path));
    }
}
