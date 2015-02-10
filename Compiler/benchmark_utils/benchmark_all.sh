#!/bin/bash
if [[ "$(cpufreq-info|grep performance|grep -v powersave|wc -l)" == "0" ]]; then
	echo "CPU is not in performance mode"
	exit 1
fi
if [[ "$(ps -ef|grep executable|grep -v grep|wc -l)" != "0" ]]; then
	echo "An test executable is still running."
	exit 2
fi
cd ../build
ant clean
ant build
cd -
echo "Benchmark for $(git rev-parse --verify HEAD) at $(date)"
for i in ../testdata/mj-test/run/fannkuch.mj ../testdata/mj-test/run/Pi.mj ../testdata/mj-test/run/MMul.mj ../testdata/mj-test/run/mandelbrot-mutable.mj ../testdata/benchmark/benchmark1.java ../testdata/benchmark/benchmark2.java ../testdata/benchmark/benchmark3.java; do
	echo "### $i"
	echo "Our backend:"
	./benchmark.sh $i
	echo "Firm backend:"
	./benchmark.sh --compile-firm $i
done
