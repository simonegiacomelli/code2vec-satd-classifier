package satd.step2

import kotlin.test.Test
import kotlin.test.assertEquals

class DatasetInfoTest {

    @Test
    fun `saveTo and loadFrom should leave info intact`() {
        val file = createTempFile()
        val where = "row1=one\nrow2=two\nrow3=three\tyeah\nrow4=four"
        val sequence = (4L..9L)
        DatasetInfo(1, 2, 3, where)
            .apply {
                val i = sequence.iterator()
                Partitions.all.forEach {folder->
                    addSatdId(folder, i.next())
                    addSatdId(folder, i.next())
                }
            }
            .saveTo(file)
        println(file.readText())
        val r = DatasetInfo.loadFrom(file)
        assertEquals(1, r.trainCount)
        assertEquals(2, r.testCount)
        assertEquals(3, r.validationCount)
        assertEquals(where, r.where)
        val i = sequence.iterator()
        Partitions.all.forEach {folder->
            assertEquals(r.satdIds[folder].orEmpty().toSet(), setOf(i.nextLong(), i.nextLong()))
        }
    }
}