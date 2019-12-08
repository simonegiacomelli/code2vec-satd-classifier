package satd.step2

import satd.utils.config
import java.util.concurrent.ForkJoinPool

fun forkJoinPool(): ForkJoinPool {
    val pool = ForkJoinPool(
        config.thread_count.toIntOrNull() ?: 10,
        { pool: ForkJoinPool? ->
            val w =
                ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool)
            w.name = "w" + w.poolIndex.toString().padStart(2, '0')
            w
        }, null, false
    )
    return pool
}