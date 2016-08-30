#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

cd $DIR

while [[ $# -gt 1 ]]
do
key="$1"

case $key in
    -b|--bomsWithHigherPrecedence)
    PLUGIN_PARAMS="-DbomsWithHigherPrecedence=$2"
    shift
    ;;
    -bt|--bootVersion)
    PLUGIN_PARAMS="$PLUGIN_PARAMS -DbootVersion=$2"
    shift # past argument
    ;;
    *)
        # unknown option
    ;;
esac
shift
done

#execute mvn plugin with app generation
if [ -n "$PLUGIN_PARAMS" ]; then
    ./mvnw clean install scs:generate-app -pl :spring-cloud-stream-app-generator $PLUGIN_PARAMS
else
    ./mvnw clean install scs:generate-app -pl :spring-cloud-stream-app-generator
fi