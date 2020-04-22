package core

import org.joda.time.Duration

/* Simone 11/05/13 9.59 */   class Sleep {
    fun sleep(millis: Long) {
        sleepStatic(millis)
    }

    companion object {
        var instance = Sleep()
        fun sleepStatic(millis: Long) {
            try {
                Thread.sleep(millis)
            } catch (ex: InterruptedException) {
                throw RuntimeException(ex)
            }
        }

        fun sleepStatic(duration: Duration) {
            sleepStatic(duration.millis)
        }

        fun forever() {
            while (true) sleepStatic(10000)
        }
    }
}