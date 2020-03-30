package sample

import edu.wm.cs.src2abs.AbstractorManager
import edu.wm.cs.src2abs.main.AbstractorMain

fun main() {
    AbstractorMain.main(emptyArray())
//     fun abstractCodePair(args: Array<String>) {
//        if (args.size < 7) {
//            AbstractorMain.printIllegalArgumentError("Not enough arguments!")
//        }
//        val granularity = args[1]
//        val inputCodePath1 = args[2]
//        val inputCodePath2 = args[3]
//        val outputCodePath1 = args[4]
//        val outputCodePath2 = args[5]
//        val idiomsFilePath = args[6]
//        val codeGranularity = AbstractorMain.getCodeGranularity(granularity)
//        val abstractor = AbstractorManager()
//        abstractor.abstractCodePair(
//            codeGranularity,
//            inputCodePath1,
//            inputCodePath2,
//            outputCodePath1,
//            outputCodePath2,
//            idiomsFilePath
//        )
//    }
}