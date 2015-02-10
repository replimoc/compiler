#!/bin/bash
START=$(date "+%s.%N");  $@ >> /dev/null; echo "$(date "+%s.%N")-$START"|bc;
