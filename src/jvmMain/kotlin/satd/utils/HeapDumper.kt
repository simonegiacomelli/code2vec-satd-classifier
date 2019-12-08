package satd.utils

import com.sun.management.HotSpotDiagnosticMXBean
import java.lang.management.ManagementFactory

/* Simone 14/12/13 16.57 */
object HeapDumper {
    val heapdumpfile by lazy {
        val folder = Folders.heapdumps.toFile()
        folder.mkdirs()
        val format = dateTimeToStr()
        val file = folder.resolve("dump-$format.hprof").absoluteFile.toString()
        file
    }

    // This is the name of the HotSpot Diagnostic MBean
    private const val HOTSPOT_BEAN_NAME = "com.sun.management:type=HotSpotDiagnostic"
    // get the hotspot diagnostic MBean from the
// platform MBean server
    // field to store the hotspot diagnostic MBean

    private val hotspotMBean: HotSpotDiagnosticMXBean
            by lazy(LazyThreadSafetyMode.NONE) {
                ManagementFactory
                    .newPlatformMXBeanProxy(
                        ManagementFactory.getPlatformMBeanServer()
                        , HOTSPOT_BEAN_NAME
                        , HotSpotDiagnosticMXBean::class.java
                    )
            }

    fun dumpHeap(fileName: String?, live: Boolean) {
        hotspotMBean.dumpHeap(fileName, live)
        println("Heap dump done")
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

    fun enable() {
        enableJvmToHeapDumpOnOutOfMemory(heapdumpfile)
    }

    private fun enableJvmToHeapDumpOnOutOfMemory(dumpFilename: String?) {
        hotspotMBean.setVMOption("HeapDumpOnOutOfMemoryError", "true")
        hotspotMBean.setVMOption("HeapDumpPath", dumpFilename)
    }


}