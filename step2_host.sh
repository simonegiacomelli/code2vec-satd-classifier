while true; do
./gradlew step2 --args="config_$(hostname).properties"
sleep 10s
done
