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
import org.eclipse.jgit.revwalk.RevWalk

import java.io.IOException

/**
 * Simple snippet which shows how to use RevWalk to iterate over objects
 */
object GetRevTreeFromObjectId {

    @Throws(IOException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        CookbookHelper.openJGitCookbookRepository().use { repository ->
            // See e.g. GetRevCommitFromObjectId for how to use a SHA-1 directly
            val head = repository.findRef("HEAD")
            println("Ref of HEAD: " + head + ": " + head.name + " - " + head.objectId.name)

            // a RevWalk allows to walk over commits based on some filtering that is defined
            RevWalk(repository).use { walk ->
                val commit = walk.parseCommit(head.objectId)
                println("Commit: $commit")

                // a commit points to a tree
                val tree = walk.parseTree(commit.tree.id)
                println("Found Tree: $tree")

                walk.dispose()
            }
        }
    }
}
