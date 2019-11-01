package satd.utils

import java.io.IOException
import java.nio.file.FileVisitResult
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.SimpleFileVisitor
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicLong


fun pathSize(path: Path): Long {

    val size = AtomicLong(0)

    try {
        Files.walkFileTree(path, object : SimpleFileVisitor<Path>() {
            override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {

                size.addAndGet(attrs.size())
                return FileVisitResult.CONTINUE
            }

            override fun visitFileFailed(file: Path, exc: IOException?): FileVisitResult {

//                println("skipped: $file ($exc)")
                // Skip folders that can't be traversed
                return FileVisitResult.CONTINUE
            }

            override fun postVisitDirectory(dir: Path, exc: IOException?): FileVisitResult {

//                if (exc != null)
//                    println("had trouble traversing: $dir ($exc)")
                // Ignore errors traversing a folder
                return FileVisitResult.CONTINUE
            }
        })
    } catch (e: IOException) {
        throw AssertionError("walkFileTree will not throw IOException if the FileVisitor does not")
    }

    return size.get()
}