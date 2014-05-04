mkdir tmp

git clone https://github.com/Fitbit/fitbit4j.git
cd fitbit4j
mvn clean install
cd ..

rmdir /qs tmp
