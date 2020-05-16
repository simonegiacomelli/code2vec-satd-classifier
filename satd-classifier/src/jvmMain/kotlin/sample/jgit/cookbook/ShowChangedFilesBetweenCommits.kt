package sample.jgit.cookbook

/*
   Copyright 2013, 2014 Dominik Stadler

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

import org.dstadler.jgit.helper.CookbookHelper
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.errors.GitAPIException
import org.eclipse.jgit.treewalk.CanonicalTreeParser

import java.io.IOException


/**
 * Snippet which shows how to show diffs between two commits.
 *
 * @author dominik.stadler at gmx.at
 */
object ShowChangedFilesBetweenCommits {

    @Throws(IOException::class, GitAPIException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        CookbookHelper.openJGitCookbookRepository().use { repository ->
            // The {tree} will return the underlying tree-id instead of the commit-id itself!
            // For a description of what the carets do see e.g. http://www.paulboxley.com/blog/2011/06/git-caret-and-tilde
            // This means we are selecting the parent of the parent of the parent of the parent of current HEAD and
            // take the tree-ish of it
            val oldHead = repository.resolve("HEAD^^{tree}")
            val head = repository.resolve("HEAD^{tree}")

            println("Printing diff between tree: $oldHead and $head")

            // prepare the two iterators to compute the diff between
            repository.newObjectReader().use { reader ->
                val oldTreeIter = CanonicalTreeParser()
                oldTreeIter.reset(reader, oldHead)
                val newTreeIter = CanonicalTreeParser()
                newTreeIter.reset(reader, head)

                // finally get the list of changed files
                Git(repository).use { git ->
                    val diffs = git.diff()
                        .setNewTree(newTreeIter)
                        .setOldTree(oldTreeIter)
                        .call()
                    for (entry in diffs) {
                        println("Entry: $entry")
                    }
                }
            }
        }

        println("Done")
    }
}
