#!/bin/sh

. $(dirname $0)/env.sh

JAVA_HOME=$JAVA_HOME ant build
