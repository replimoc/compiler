#!/bin/bash
cat $1 |grep -v "^[[:space:]]#"|wc -l
