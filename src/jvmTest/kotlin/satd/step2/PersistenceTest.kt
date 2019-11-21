package satd.step2

import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import satd.step1.Folders
import java.nio.file.Path
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.test.*

class PersistenceTest {
    companion object {
        var counter = 0L
        val folder = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd--HH-mm-ss"))!!
    }

    @BeforeTest
    fun setUp() {
    }

    @Test
    fun `duplicate hash_code should be silently ignored (unique index violation)`() {
        val p = Persistence(newTempFolder())
        p.setupDatabase()
        ignoreDuplcatesInvoke("hash1")
        ignoreDuplcatesInvoke("hash2")
        ignoreDuplcatesInvoke("hash1")
//        p.showInBrowser()
    }

    @Test
    fun `reasonable error should NOT be ignored`() {
        val p = Persistence(newTempFolder())
        p.setupDatabase()
        ignoreDuplcatesInvoke("hash1", "commit1")
        assertFails("the value should be too big to be accepted") {
            ignoreDuplcatesInvoke("hash2", "x".repeat(20000))
        }
    }

    private fun ignoreDuplcatesInvoke(code_hash: String, commitId: String = "commitid") {
        ignoreDuplicates {
            transaction {
                DbSatds.insert {
                    it[this.repo] = "repo2"
                    it[this.commit] = commitId
                    it[this.old] = "bodyold"
                    it[this.new] = "bodynew"
                    it[this.pattern] = "fixme"
                    it[this.old_len] = 1
                    it[this.new_len] = 2
                    it[this.commit_message] = "commit message1"

                    it[this.old_clean] = "old clean"
                    it[this.new_clean] = "new clean"
                    it[this.old_clean_len] = 3
                    it[this.new_clean_len] = 4
                    it[this.clean_diff_ratio] = 0.2
                    it[this.code_hash] = code_hash
                }
            }
        }
    }

    private fun newTempFolder(): Path {
        counter++;
        return Folders.temp.resolve("test/dup/$folder/$counter").apply { toFile().mkdirs() }
    }
}