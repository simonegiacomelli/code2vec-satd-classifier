package satd.step2

import org.eclipse.jgit.lib.Repository

class Walker(val repo: Repository) {
    fun walk() {

        val walk = CustomRevWalk(repo)
        walk.all()
        for (child in walk.call()) {
            child.parents.forEach { parent -> walk.link(parent, child) }

        }

    }
}