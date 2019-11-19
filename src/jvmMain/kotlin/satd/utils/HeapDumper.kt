package satd.utils

import com.sun.management.HotSpotDiagnosticMXBean
import satd.step1.Folders
import java.io.IOException
import java.lang.management.ManagementFactory
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/* Simone 14/12/13 16.57 */   object HeapDumper {
    val heapdumpfile by lazy {
        val folder = Folders.heapdumps.toFile()
        folder.mkdirs()
        val format = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd--HH-mm-ss"))
        val file = folder.resolve("dump-$format.hprof").absoluteFile.toString()
        file
    }
    // This is the name of the HotSpot Diagnostic MBean
    private const val HOTSPOT_BEAN_NAME = "com.sun.management:type=HotSpotDiagnostic"
    // get the hotspot diagnostic MBean from the
// platform MBean server
    // field to store the hotspot diagnostic MBean
    @get:Synchronized
    @Volatile
    private var hotspotMBean: HotSpotDiagnosticMXBean? = null
        get() {
            if (field == null) {
                val server = ManagementFactory.getPlatformMBeanServer()
                field = try {
                    ManagementFactory.newPlatformMXBeanProxy(
                        server,
                        HOTSPOT_BEAN_NAME, HotSpotDiagnosticMXBean::class.java
                    )
                } catch (e: IOException) {
                    throw RuntimeException(e)
                }
            }
            return field
        }

    @JvmStatic
    fun dumpHeap(fileName: String?, live: Boolean) {
        try {
            hotspotMBean!!.dumpHeap(fileName, live)
        } catch (exp: Exception) {
            throw RuntimeException(exp)
        }
    }

    fun main(args: Array<String>) { // default heap dump file name
        var fileName = "c:/temp/heap.bin"
        // by default dump only the live objects
        var live = true
        when (args.size) {
            2 -> {
                live = args[1] == "true"
                fileName = args[0]
            }
            1 -> fileName = args[0]
        }
        // dump the heap
        dumpHeap(fileName, live)
    }

    fun enable(){
        enableJvmToHeapDumpOnOutOfMemory(heapdumpfile)
    }
    fun enableJvmToHeapDumpOnOutOfMemory(dumpFilename: String?) {
        hotspotMBean!!.setVMOption("HeapDumpOnOutOfMemoryError", "true")
        hotspotMBean!!.setVMOption("HeapDumpPath", dumpFilename)
    }


}