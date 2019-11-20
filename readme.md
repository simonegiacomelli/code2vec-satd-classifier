Useful query



SELECT * FROM DBSATDS where satd_len<50 and fixed_len < 50 order by id desc

select pattern, count(*) FROM DBSATDS 
where satd_len<50 and fixed_len < 50 
group by pattern
order by count(*) desc