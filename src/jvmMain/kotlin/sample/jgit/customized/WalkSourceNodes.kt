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

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import satd.step1.Folders
import satd.step1.Repo


/**
 * Simple snippet which shows how to use RevWalk to iterate over all commits
 * across all branches/tags/remotes in the given repository
 *
 * See the original discussion at http://stackoverflow.com/a/40803945/411846
 */
object WalkSourceNodes {

    @JvmStatic
    fun main(args: Array<String>) {
        val guineaPig = gp_three_sink_dag()
        guineaPig.rebuild()
        val git = guineaPig.git
//        val git = Git.open(Folders.repos.resolve("elastic_elasticsearch").toFile())
        val sources = git.use { git ->
            git.log().all().call().filter { it.parentCount == 0 }
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
                    for (ref in sources) {
                        revWalk.markStart(revWalk.parseCommit(ref.toObjectId()))
                    }
                    println("Walking all commits starting with " + sources.size + " refs: " + sources)
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
