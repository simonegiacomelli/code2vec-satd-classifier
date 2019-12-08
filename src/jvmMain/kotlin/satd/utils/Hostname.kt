package satd.utils

import java.net.InetAddress
import java.net.UnknownHostException


val hostname: String by lazy { hostname() }

private fun hostname(): String {
    val env = System.getenv()
    var hostname =
        if (env.containsKey("COMPUTERNAME")) env["COMPUTERNAME"] else if (env.containsKey("HOSTNAME")) env["HOSTNAME"] else ""
    hostname = hostname.orEmpty()

    if (hostname != "") return hostname

    try {
        val addr: InetAddress = InetAddress.getLocalHost()
        return addr.hostName
    } catch (ex: UnknownHostException) {

    }

    Runtime.getRuntime().exec("hostname").inputStream.use {
        return it.bufferedReader().readLine().orEmpty()
    }
}