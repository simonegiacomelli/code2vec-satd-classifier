https://api.github.com/search/repositories?q=language:Java+topic:android+is:public+created:2019-01-01..2019-01-31&page=1&per_page=100



https://api.github.com/search/repositories?q=language:Java+topic:android+is:public&page=354&per_page=100

https://api.github.com/search/repositories?q=language:Java+topic:android+is:public+created2019-11-31..2019-12-31&page=1&per_page=100


{
  "message": "Only the first 1000 search results are available",
  "documentation_url": "https://developer.github.com/v3/search/"
}


https://stackoverflow.com/questions/37602893/github-search-limit-results#


Awesome! Thanks! In case anyone else needs this: 
https://api.github.com/search/repositories?q=language:Java+created:>=2013-04-11T00:00:00Z&order=asc


he order=asc applies on the sort field which can be stars
, forks, updated or best_match(default). 
So curl -G https://api.github.com/search/repositories --data-urlencode "q=created:>2013-04-11" --data-urlencode "order=asc" gets all repositories created after 2013-04-11 but not in the created order. We can fetch repositories within a range using q=created:time1..time2, but the results are not sorted by created time.