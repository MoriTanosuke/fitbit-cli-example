#/bin/sh
pushd /tmp
git clone https://github.com/Fitbit/fitbit4j.git
pushd fitbit4j
mvn clean install
popd
rm -rf fitbit4j
popd
