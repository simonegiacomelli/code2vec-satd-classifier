package sample.jgit.customized

/*
   Copyright 2016 Dominik Stadler

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

import org.eclipse.jgit.api.errors.JGitInternalException
import org.eclipse.jgit.api.errors.NoHeadException
import org.eclipse.jgit.errors.IncorrectObjectTypeException
import org.eclipse.jgit.errors.MissingObjectException
import org.eclipse.jgit.internal.JGitText
import org.eclipse.jgit.lib.AnyObjectId
import org.eclipse.jgit.lib.Constants
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.revwalk.RevSort
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import java.io.IOException
import java.text.MessageFormat


/**
 * Simple snippet which shows how to use RevWalk to iterate over all commits
 * across all branches/tags/remotes in the given repository
 *
 * See the original discussion at http://stackoverflow.com/a/40803945/411846
 */
object WalkSourceNodes {

    @JvmStatic
    fun main(args: Array<String>) {
        val guineaPig = gp1()
        guineaPig.rebuild()
        val git = guineaPig.git
//        val git = Git.open(Folders.repos.resolve("elastic_elasticsearch").toFile())
        val sources = git.use { git ->
            git.log().all().call().filter { it.parentCount == 0 }
        }

        println("setting SatdRevWalk")
        val walk = CustomRevWalk(git.repository)
        walk.all()
        for ( commit in walk.call()){
            println("Commit: $commit ${commit.fullMessage}")
        }
        println("-----walking all refs:")
        FileRepositoryBuilder()
            .setGitDir(git.repository.directory)
            .readEnvironment()
            .findGitDir() // scan up the file system tree
            .build().use { repository ->
                // get a list of all known heads, tags, remotes, ...

                println("All sources ${sources.size}:")
                sources.forEach {
                    val ref = it!!
                    println("  ${ref.name}")
                }
                // a RevWalk allows to walk over commits based on some filtering that is defined

                RevWalk(repository).use { revWalk ->
                    //                    for (ref in sources) {
//                        revWalk.markStart(revWalk.parseCommit(ref.toObjectId()))
//                    }
//                    println("Walking all commits starting with " + sources.size + " refs: " + sources)
                    revWalk.sort(RevSort.REVERSE)
//                    revWalk.revSort.add(RevSort.REVERSE)

                    val head = repository.findRef("HEAD")
                    revWalk.markStart(revWalk.parseCommit(head.objectId))
                    var count = 0
                    for (commit in revWalk) {
                        println("Commit: $commit ${commit.fullMessage}")
                        count++
                    }
                    println("Had $count commits")
                }
            }
    }
}

class SatdCommit(id: AnyObjectId) : RevCommit(id) {

}

class CustomRevWalk(val repo: Repository) : RevWalk(repo) {
    override fun createCommit(id: AnyObjectId?): RevCommit {
        return SatdCommit(id!!)
    }

    var startSpecified = false

    fun all() {
        for (refi in repo.refDatabase.refs) {
            val ref = if (!refi.isPeeled) repo.refDatabase.peel(refi) else refi

            var objectId: ObjectId? = ref.getPeeledObjectId()
            if (objectId == null)
                objectId = ref.getObjectId()
            var commit: RevCommit? = null
            try {
                commit = parseCommit(objectId)
            } catch (e: MissingObjectException) {
                // ignore as traversal starting point:
                // - the ref points to an object that does not exist
                // - the ref points to an object that is not a commit (e.g. a
                // tree or a blob)
            } catch (e: IncorrectObjectTypeException) {
            }

            if (commit != null)
                add(commit)
        }

    }

    fun call(): Iterable<RevCommit> {


        if (!startSpecified) {
            try {
                val headId = repo.resolve(Constants.HEAD)
                    ?: throw NoHeadException(
                        JGitText.get().noHEADExistsAndNoExplicitStartingRevisionWasSpecified
                    )
                add(headId)
            } catch (e: IOException) {
                // all exceptions thrown by add() shouldn't occur and represent
                // severe low-level exception which are therefore wrapped
                throw JGitInternalException(
                    JGitText.get().anExceptionOccurredWhileTryingToAddTheIdOfHEAD,
                    e
                )
            }

        }

        if (this.revFilter != null) {
            setRevFilter(this.revFilter)
        }
        return this

    }

    @Throws(MissingObjectException::class, IncorrectObjectTypeException::class)
    fun add(start: AnyObjectId) {
        add(true, start)
    }

    @Throws(MissingObjectException::class, IncorrectObjectTypeException::class, JGitInternalException::class)
    private fun add(include: Boolean, start: AnyObjectId) {

        try {
            if (include) {
                markStart(lookupCommit(start))
                startSpecified = true
            } else
                markUninteresting(lookupCommit(start))

        } catch (e: MissingObjectException) {
            throw e
        } catch (e: IncorrectObjectTypeException) {
            throw e
        } catch (e: IOException) {
            throw JGitInternalException(
                MessageFormat.format(
                    JGitText.get().exceptionOccurredDuringAddingOfOptionToALogCommand, start
                ), e
            )
        }

    }

}