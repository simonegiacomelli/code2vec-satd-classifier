package satd.step2

import java.io.File
import java.util.*

class DatasetInfo(
    val trainCount: Int,
    val testCount: Int,
    val validationCount: Int,
    val where: String
) {

    val satdIds = mutableMapOf<String, MutableList<Long>>()
    fun addSatdId(folder: String, satdId: Long) {
        satdIds.getOrPut(folder) { mutableListOf() }.add(satdId)
    }

    fun saveTo(file: File) {
        file.writeText(
            "#DbSatd rows:\n" +
                    "$trai=$trainCount\n" +
                    "$test=$testCount\n" +
                    "$vali=$validationCount\n" +
                    "$wher=${where.replace("\n", "\\n")}\n" +
                    "${ids(Partitions.training)}\n" +
                    "${ids(Partitions.test)}\n" +
                    "${ids(Partitions.validation)}\n"
        )
    }

    fun satdIdsToString(type: String): String = satdIds[type].orEmpty().joinToString(separator)

    private fun ids(type: String): String = "${getKey(type)}=${satdIdsToString(type)}"

    companion object {
        private fun getKey(type: String): String = "$type-SatdIds"
        val separator = ","

        val trai = "trainCount"
        val test = "testCount"
        val vali = "validationCount"
        val wher = "where"
        internal fun fromPartitions(p: Partitions, where: String): DatasetInfo =
            DatasetInfo(p.trainCount, p.testCount, p.validationCount, where)

        fun loadFrom(file: File): DatasetInfo {
            val p = file.bufferedReader().use { Properties().apply { load(it) } }
            fun s(name: String) = p.getProperty(name).orEmpty()
            fun i(name: String) = s(name).toInt()
            val ds = DatasetInfo(i(trai), i(test), i(vali), s(wher))
            Partitions.all.forEach { folder ->
                val ids = s(getKey(folder))
                ids.split(separator).forEach { ds.addSatdId(folder, it.toLong()) }
            }
            return ds
        }
    }


}