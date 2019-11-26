#
task=$1
shift
./gradlew $task  --args="$1 $2 $3 $3"