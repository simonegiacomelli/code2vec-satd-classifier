package satd.step2

import satd.utils.*
import kotlin.streams.toList

fun main() {
    loglnStart("UpdateDbStats")
    logln("Starting pid: $pid")
    config.load()

    persistence.setupDatabase()

    repoRate.startStatAsync()

    val pool = forkJoinPool()

    logln("Using pool: $pool")
    pool.submit {
        RepoList
            .get()
            .also { repoRate.totRepo = it.size }
            .stream()
            .parallel()
            .map { DbRepos.updateStats(it); repoRate.spin() }
            .toList()
    }.get()

//    persistence.connection().apply {
//        sql.split("\nGO\n")
//            .forEach {
//                println("executing [$it]")
//                createStatement().apply {
//                    execute(it)
//                    close()
//                }
//            }
//
//    }
//drop table s if exists;
//create table s as
//select url, count(*) satd_count from dbsatds where accept=1 group by url

//    "select r.*, coalesce(s.satd_count,0) satd_count from dbrepos r left outer join s on r.url = s.url"
//
    //call csvwrite('~/Documents/tesi/repo-satd-stats.csv' , 'select r.*, coalesce(s.satd_count,0) satd_count from dbrepos r left outer join s on r.url = s.url')
    // call csvwrite('~/Documents/tesi/repo-satd-stats.csv' , 'select coalesce(s.url,r.url) urlc, r.*, coalesce(s.satd_count,0) satd_count from s  left outer join dbrepos r on r.url = s.url')
    repoRate.logStat()

    logln("Done")

}
