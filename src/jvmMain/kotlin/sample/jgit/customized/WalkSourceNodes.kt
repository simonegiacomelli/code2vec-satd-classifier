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

import satd.step2.CustomRevWalk


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


        println("setting SatdRevWalk")
        val walk = CustomRevWalk(git.repository)
        walk.all()
        for (commit in walk.call()) {
            if(commit.parentCount==0)
                println("walk $commit has no parent")
            else
                println("walk $commit")
            commit.parents.forEach {
                walk.link(it, commit);
            }
           // println("Commit: $commit ${commit.fullMessage}")
        }

        println("Sink count: ${walk.commits.values.filter { it.parents.size == 0 }.size}")
        println("Source count: ${walk.commits.values.filter { it.childs.size == 0 }.size}")

    }
}

