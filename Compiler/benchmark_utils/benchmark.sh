#!/bin/bash
../target/compiler $@
for i in 1 2 3 4 5; do START=$(date "+%s.%N"); ./a.out >> /dev/null; echo "$(date "+%s.%N")-$START"|bc; done
