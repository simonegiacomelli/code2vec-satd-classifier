package satd.fix

import satd.step2.persistence
import satd.step2.query

fun main() {
    val connection = persistence
        .connection()
    val urls =
        """https://github.com/BryanCAlcorn/cs6460project,https://github.com/danielmorozoff/bubs-parser,https://github.com/mrstig/maven""".split(
            ","
        )

    val urlsParam = urls.joinToString(",") { "'$it'" }
//    connection.createStatement().execute("update dbrepos set done=0 where url in ($urlsParam)")
//    return
    val startingPoint =
        connection.query("select created_at, id from dbrepos " +
                "where url in ($urlsParam) " +
                "order by created_at desc " +
                ""
        )
            .first()
    val ca = startingPoint[0].toString()
    val id = (startingPoint[1] as Number).toInt()

    val res = connection
        .query(
            "select id,url from dbrepos " +
                    "where id >= $id " +
                    "order by created_at " +
                    ""
        )

    res.take(10)
        .forEach {
            println(it.joinToString("\t"))
        }

}

