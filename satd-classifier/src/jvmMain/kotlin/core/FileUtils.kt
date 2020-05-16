package core

import org.apache.commons.io.FileUtils
import java.io.*
import java.nio.charset.Charset
import java.nio.file.CopyOption
import java.nio.file.Files
import java.nio.file.Path

/* Simone 04/12/13 11.59 */   object FileUtils {
    fun write(file: String?, data: CharSequence?) {
        val file1 = File(file)
        write(file1, data)
    }

    fun write(file: String?, data: ByteArray?) {
        val file1 = File(file)
        write(file1, data)
    }

    fun write(path: Path, data: CharSequence?) {
        write(path.toFile(), data)
    }

    fun write(path: Path, data: ByteArray?) {
        write(path.toFile(), data)
    }

    fun append(path: Path, data: CharSequence?) {
        append(path.toFile(), data)
    }

    fun append(file: String?, data: CharSequence?) {
        val file1 = File(file)
        append(file1, data)
    }

    fun append(file: File?, data: CharSequence?) {
        try {
            FileUtils.write(file, data, Charset.defaultCharset(), true)
        } catch (e: IOException) {
            throw RuntimeIOException(e)
        }
    }

    fun append(file: File?, data: ByteArray?) {
        try {
            FileOutputStream(file, true).use { fos -> fos.write(data) }
        } catch (e: IOException) {
            throw RuntimeIOException(e)
        }
    }

    fun append(path: String?, data: ByteArray?) {
        try {
            FileOutputStream(File(path), true).use { fos -> fos.write(data) }
        } catch (e: IOException) {
            throw RuntimeIOException(e)
        }
    }

    fun write(file: File?, data: CharSequence?) {
        try {
            FileUtils.write(file, data, null as String?, false)
        } catch (e: IOException) {
            throw RuntimeIOException(e)
        }
    }

    fun write(file: File?, data: ByteArray?) {
        try {
            val fos = FileOutputStream(file)
            try {
                fos.write(data)
            } finally {
                fos.close()
            }
        } catch (e: IOException) {
            throw RuntimeIOException(e)
        }
    }

    fun read(file: File?): String {
        return try {
            FileUtils.readFileToString(file, Charset.defaultCharset())
        } catch (e: IOException) {
            throw RuntimeIOException(e)
        }
    }

    fun read(path: String?): String {
        return read(File(path))
    }

    fun readLines(path: String?): List<String> {
        return readLines(File(path))
    }

    fun readLines(file: File?): List<String> {
        return try {
            FileUtils.readLines(file)
        } catch (e: IOException) {
            throw RuntimeIOException(e)
        }
    }

    fun readLines(path: Path): List<String> {
        return readLines(path.toFile())
    }

    fun read(path: Path): String {
        return try {
            FileUtils.readFileToString(path.toFile(), Charset.defaultCharset())
        } catch (e: IOException) {
            throw RuntimeIOException(e)
        }
    }

    fun writeLines(file: File?, lines: Collection<*>?) {
        try {
            FileUtils.writeLines(file, lines)
        } catch (e: IOException) {
            throw RuntimeIOException(e)
        }
    }

    fun newFileOutputStream(file: File?): FileOutputStream {
        return try {
            FileOutputStream(file)
        } catch (e: FileNotFoundException) {
            throw RuntimeIOException(e)
        }
    }

    fun newFileInputStream(file: File?): FileInputStream {
        return try {
            FileInputStream(file)
        } catch (e: FileNotFoundException) {
            throw RuntimeIOException(e)
        }
    }

    fun move(
        source: Path?,
        target: Path?,
        vararg options: CopyOption?
    ): Path {
        return try {
            Files.move(source, target, *options)
        } catch (e: IOException) {
            throw RuntimeIOException(e)
        }
    }

    fun delete(file: File) {
        if (file.exists()) file.delete()
    }

    fun createNewFile(file: File): Boolean {
        return try {
            file.createNewFile()
        } catch (e: IOException) {
            throw RuntimeIOException(e)
        }
    }

    fun readBytes(file: File?): ByteArray {
        return try {
            FileUtils.readFileToByteArray(file)
        } catch (e: IOException) {
            throw RuntimeIOException(e)
        }
    }

    fun readBytes(path: Path): ByteArray {
        return readBytes(path.toFile())
    }

    fun readBytes(path: String?): ByteArray {
        return readBytes(File(path))
    }

    fun delete(path: String?) {
        delete(File(path))
    }
}