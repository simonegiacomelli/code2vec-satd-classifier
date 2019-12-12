package satd.step2

import satd.utils.config
import satd.utils.logln
import java.util.concurrent.ForkJoinPool

//maybe using System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "10");
//will save some code?

fun forkJoinPool(): ForkJoinPool {
    val parallelism = config.thread_count.toIntOrNull() ?: ForkJoinPool.commonPool().parallelism
    logln("forkJoinPool parallelism: $parallelism")
    val pool = ForkJoinPool(
        parallelism,
        { pool: ForkJoinPool? ->
            val w =
                ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool)
            w.name = "w" + w.poolIndex.toString().padStart(2, '0')
            w
        }, null, false
    )
    return pool
}