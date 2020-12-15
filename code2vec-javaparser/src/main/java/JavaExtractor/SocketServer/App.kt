package JavaExtractor.SocketServer

import JavaExtractor.Common.CommandLineValues
import JavaExtractor.Common.Common
import JavaExtractor.ExtractFeaturesTask
import JavaExtractor.FeatureExtractor
import JavaExtractor.FeaturesEntities.ProgramFeatures
import JavaExtractor.FeaturesEntities.Property
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.Node
import com.github.javaparser.ast.body.MethodDeclaration
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.ServerSocket
import java.net.Socket
import java.util.*
import kotlin.concurrent.thread

private val args = CommandLineValues(
    *"--max_path_length 8 --max_path_width 2 --dir ../code2vec-satd/build-dataset/java-small/one --num_threads 10".split(
        " "
    ).toTypedArray()
)

fun main() {
    val server = ServerSocket(9999)
    println("Server is running on port ${server.localPort}")
    while (true) {
        val client = server.accept()
        thread(isDaemon = true) { ClientHandler(client).serve() }
    }
}

class ClientHandler(val client: Socket) {
    val out = DataOutputStream(client.getOutputStream())
    val inp = DataInputStream(client.getInputStream())
    fun serve() {
        client.soTimeout = 5 * 60 * 1000
        while (true) {
            val id = inp.readChunkedString()
            val code = inp.readChunkedString()
            val str = try {
                MeasureTimeMillis {
                    featuresToString(FeatureExtractor(args).extractFeatures(code))
                }.apply {
                    if (millis > 3000) println("id=$id took $millis ms")
                }.result
            } catch (ex: Exception) {
                val msg = "FAILED\t" + ex.toString().replace("\n", "\\n")
                println("$id $msg")
                msg
            }
            out.sendStringInChunk(str)
        }
    }
}

private fun featuresToString(features: ArrayList<ProgramFeatures>?) = if (features == null) {
    "FAILED\tNULL"
} else {
    val toPrint = ExtractFeaturesTask.featuresToString(features, args)
    if (toPrint.isNotEmpty()) {
        "OK\t$toPrint"
    } else "FAILED\ttoPrint.length()==0"
}

private fun DataOutputStream.sendStringInChunk(source: String) {
    val chunk = source.chunked(1024 * 32)
    writeInt(chunk.size)
    chunk.forEach { writeUTF(it) }
    flush()
}

private fun DataInputStream.readChunkedString(): String {
    val chunkCount = readInt()
    val code = (1..chunkCount).joinToString("") { readUTF() }
    return code
}

class MeasureTimeMillis<T>(block: () -> T) {
    val millis: Long
    val result: T

    init {
        val start = System.currentTimeMillis()
        result = block()
        millis = System.currentTimeMillis() - start
    }
}

public inline fun <T> measureTimeMillis2(block: () -> T): Pair<Long, T> {
    val start = System.currentTimeMillis()
    val res = block()
    return Pair(System.currentTimeMillis() - start, res)
}

class ThesisExplainMaterial {
    companion object {
        @JvmStatic
        fun main(arguments: Array<String>) {
            FeatureExtractor.upSymbol = "↑";
            FeatureExtractor.downSymbol = "↓";

            val code2 = """ 
                |void method1() {
                |  while(!done) {
                |    if(someCondition()){
                |      done = true;
                |    }
                |  }
                |}
            """.trimMargin()
            val code3 = """ 
                |String METHOD_NAME() {
                |  if(somePreCondition())
                |    while(!completed())
                |       doWork();
                |  return "ok";
                |}
            """.trimMargin()
            val code1 = """ 
                |boolean METHOD_NAME(Object target) {
                |  for (Object elem: this.elements) {
                |    if(elem.equals(target)) {
                |      return true;
                |    }
                |  }
                |  return false;
                |}
            """.trimMargin()
            val code4 = """ 
                |void METHOD_NAME(Object target) {
                |  ciao();
                |}
            """.trimMargin()
            val code = code3
            println(code)

            val featureExtractor = FeatureExtractor(args)
            val features = featureExtractor.extractFeatures(code)

            printAstPaths(features)
            //printAst(featureExtractor.compilationUnit)

        }

        private fun printAstPaths(features: ArrayList<ProgramFeatures>) {
            val paths = StringBuilder()
            for (f in features) {
                for (r in f.features) {
                    paths.append(r.source.name + ",")
                    paths.append(r.path)
                    paths.appendln("," + r.target.name)
                }
            }
            val replMap = mapOf(
                "ClassOrInterfaceType" to "Class",
                "MethodCallExpr" to "MethodCall",
                "NameExpr1" to "Name",
                "BlockStmt" to "Block",
                "IfStmt" to "If",
                "WhileStmt" to "While",
                "ExpressionStmt" to "Expression",
                "StringLiteralExpr" to "StringLiteral",
                "ReturnStmt" to "Return",
                "0" to "",
                "1" to "",
                "(" to "",
                ")" to ""
            )
            val repl = replaceText(paths.toString(),replMap)
            println(repl)
        }

        private fun replaceText(text: String, keys: Map<String, String>): String  = keys.entries.fold(text) { acc, (key, value) -> acc.replace(key, value) }


        private fun printAst(cu: CompilationUnit) {

            println("-".repeat(80))
            println("AST:")
            val methodDecl = cu.getNodesByType(MethodDeclaration::class.java).first()

            val ast = StringBuilder()
            fun recurse(node: Node) {
                //node.getUserData(Common.ChildId)
                val p = node.getUserData(Common.PropertyKey)!!

                ast.appendln("  ${p.gvName} [ label=\"${p.gvLabel}\" ] ")
                if (p.isLeaf) {
                    //additional graphviz node
                    ast.appendln("  ${p.gvLeafName} [ label=\"${p.gvLeafLabel}\" shape=\"rectangle\"] ")
                    ast.appendln("  ${p.gvName} -> ${p.gvLeafName} ")
                }
                for (c in node.childrenNodes) {
                    val cp = c.getUserData(Common.PropertyKey)!!
                    if (!cp.gvSkip) {
                        ast.appendln("  ${p.gvName} -> ${cp.gvName}")
                        recurse(c)
                    }
                }


            }
            ast.appendln("digraph G {")
            recurse(methodDecl)
            ast.appendln("}")
            println(ast)
            copyToClipboard(ast.toString())
        }

        private fun copyToClipboard(toString: String) {
            Toolkit.getDefaultToolkit()
                .systemClipboard.setContents(StringSelection(toString), null);
            println("The dot graph definition is copied into the clipboard")
            println("Dot renderer used http://graphviz.it/")
        }

        private fun latex(s: () -> String): String = s().replace("_", "\\\\_")
        val Property.gvSkip get() = name == "null" && !isLeaf
        val Property.gvName: String get() = "$id" // if (isLeaf) this.type else this.name
        val Property.gvLabel: String
            get() = latex {
                val p = this;
                when {
                    p.type.endsWith("Stmt") -> p.type.removeSuffix("Stmt")
                    p.type.endsWith("Expr") -> p.type.removeSuffix("Expr")
                    p.type == "PrimitiveType" -> "Primitive"
                    p.type == "VariableDeclaratorId" -> "VarDeclId"
                    p.type == "ClassOrInterfaceType" -> "Class"
                    else -> p.type
                }
            }

        val Property.gvLeafName: String get() = "val${id}" // if (isLeaf) this.type else this.name
        val Property.gvLeafLabel: String get() = latex { if (this.name == "METHOD_NAME") this.name else this.name }
    }

}