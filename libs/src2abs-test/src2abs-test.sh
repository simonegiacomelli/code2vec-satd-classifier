#!/usr/bin/env bash
#java -jar src2abs-0.1-jar-with-dependencies.jar pair <code_granularity> <input_code_A_path> <input_code_B_path> <output_abstract_A_path> <output_abstract_B_path> <idioms_path>
java -jar src2abs-full-fat.jar pair method satd.java fixed.java abs-satd.java abs-fixed.java idioms.txt