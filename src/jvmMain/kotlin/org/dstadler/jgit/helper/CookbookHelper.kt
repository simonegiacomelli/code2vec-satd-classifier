package org.dstadler.jgit.helper

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

import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.storage.file.FileRepositoryBuilder

import java.io.File
import java.io.IOException


object CookbookHelper {

    fun openJGitCookbookRepository(): Repository {
        return FileRepositoryBuilder()
            .readEnvironment() // scan environment GIT_* variables
            .findGitDir() // scan up the file system tree
            .build()
    }

    fun createNewRepository(): Repository {
        // prepare a new folder
        val localPath = File.createTempFile("TestGitRepository", "")
        if (!localPath.delete()) {
            throw IOException("Could not delete temporary file $localPath")
        }

        // create the directory
        val repository = FileRepositoryBuilder.create(File(localPath, ".git"))
        repository.create()

        return repository
    }
}
