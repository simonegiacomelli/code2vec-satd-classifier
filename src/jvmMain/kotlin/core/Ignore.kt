package core

import org.slf4j.LoggerFactory

/* Simone 04/09/13 11.30 */   object Ignore {
    val log = LoggerFactory.getLogger(Ignore::class.java)
    fun exception(runnable: RunnableEx) {
        try {
            runnable.run()
        } catch (ex: Exception) {
            log.error("", ex)
        }
    }

    @JvmStatic
    fun exception(runnable: Runnable) {
        try {
            runnable.run()
        } catch (ex: Exception) {
            log.error("", ex)
        }
    }

    @JvmStatic
    fun exception(runnable: () -> Unit) {
        try {
            runnable()
        } catch (ex: Exception) {
            log.error("", ex)
        }
    }
}