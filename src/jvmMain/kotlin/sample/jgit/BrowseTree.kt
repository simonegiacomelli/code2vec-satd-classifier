package sample.jgit

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
import org.eclipse.jgit.lib.Constants
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.lib.ObjectLoader
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.treewalk.TreeWalk

import java.io.IOException

/**
 * Simple snippet which shows how to use RevWalk to iterate over items in a file-tree
 *
 * @author dominik.stadler at gmx.at
 */
object BrowseTree {

    @Throws(IOException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        CookbookHelper.openJGitCookbookRepository().use { repository ->
            val revId = repository.resolve(Constants.HEAD)
            TreeWalk(repository).use { treeWalk ->
                RevWalk(repository).use { revWalk ->
                    treeWalk.addTree(revWalk.parseTree(revId))

                    while (treeWalk.next()) {
                        println("---------------------------")
                        System.out.append("name: ").println(treeWalk.nameString)
                        System.out.append("path: ").println(treeWalk.pathString)

                        val loader = repository.open(treeWalk.getObjectId(0))

                        System.out.append("directory: ").println(loader.type == Constants.OBJ_TREE)
                        System.out.append("size: ").println(loader.size)
                    }
                }
            }
        }
    }
}
