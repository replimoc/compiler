#!/bin/bash

PROJECT_HOME=`dirname "$SCRIPT_LOCATION"`

CLASSPATH="$PROJECT_HOME/lib/commons-cli-1.2.jar"
CLASSPATH+=":$PROJECT_HOME/target/Compiler.jar"
export CLASSPATH

java compiler.main.CompilerApp $1 $2