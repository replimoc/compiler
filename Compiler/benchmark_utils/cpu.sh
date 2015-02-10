#!/bin/bash
if [[ "$1" != "" ]]; then
	cpufreq-set -c 0 -g $1
	cpufreq-set -c 1 -g $1
	cpufreq-set -c 2 -g $1
	cpufreq-set -c 3 -g $1
fi
cpufreq-info|grep "gover"|head -2
