[![DOI](https://zenodo.org/badge/227602878.svg)](https://zenodo.org/badge/latestdoi/227602878)


# *bencher* - JMH Benchmark Analysis

bencher is a tool for analysing [JMH](https://openjdk.java.net/projects/code-tools/jmh/) benchmarks.
Its features are:
* Transformation of JMH JSON files into CSV files
* Static and dynamic coverage extraction of JMH benchmarks
* Test case prioritization of benchmarks
* A command line interface for standard features and an API for more fine-granular features   





## Installation
* Java version 17 (set `$JAVA_HOME`)
* For command line interface (CLI) usage:
    * Create fat JAR with `./gradlew shadowJar`
    * Created JAR is  `build/libs/bencher-0.5.0-all.jar`, in the following referred to as `bencher.jar`
* For API usage:
    * Run `./gradlew publishToMavenLocal`
    * Use in your project with
    groupID = `ch.uzh.ifi.seal`,
    artifactID = `bencher`,
    and
    version = `0.5.0`.





## CLI Usage
We can use bencher by invoking the generated fat Jar with `java -jar bencher.jar`.
The CLI is written with the nice [picocli](https://picocli.info/) library, and comes with descriptions for all parameters.

The base command always requires setting the project name that is analyzed.
Other important arguments of the base command are:
* project name `-p`
* path to output file of the analysis `-out`
* Java package prefix of the project `-pf`
* version of the project `-pv`
* machine/instance name on which the JMH results were executed on `-i`
* trial number of JMH results (within a machine) `-t`



### JMH JSON Transformation
With bencher we can transform the JSON output files of a JMH run into a generic CSV file for further processing.

```bash
java -jar bencher.jar \
    -p myproject \
    -pv 1.0.0 \
    -i m1 \
    -t 1 \
    -out jmh_result.csv \
    trans jmhResult \
    -f jmh_result.json
```

The CSV file `jmh_result.csv` has the following structure.
For example, the performance change analysis tool [pa](https://github.com/chrstphlbr/pa) takes CSV files of this format as input.
The columns represent the following values:
1. `project` is the project name, as defined by `-p`
2. `commit` is the project version, e.g., a commit hash, as defined by `-pv`
3. `benchmark` is the name of the fully-qualified benchmark method, as extracted from the JMH result JSON
4. `params` are the JMH parameters (not the Java parameters) of the benchmark in comma-separated form.
Every parameter consists of a name and a value, separated by an equal sign (`name=value`).
Extracted from the JMH result JSON
5. `instance` is the name of the instance or machine, as defined by `-i`
6. `trial` is the number of the trial, as defined by `-t`
7. `fork` is the JMH fork number, as extracted from the JMH result JSON
8. `iteration` is the JMH iteration number with a fork, as extracted from the JMH result JSON
9. `mode` is the JMH benchmark mode, e.g, `avgt`, `thrpt`, or `sample`, as extracted from the JMH result JSON
10. `unit` is the measurement unit of the benchmark value, as extracted from the JMH result JSON
11. `value_count` is the number of invocations the `value` occurred in this iteration.
If `mode` is `sample`, every iteration can have multiple values (i.e., invocations), which are presented as a histogram.
Each histogram value corresponds to one CSV row, and the occurrences of this value is defined by `value_count`.
All other modes have a `value_count` of 1 and only a single CSV row per iteration.
Extracted from the JMH result JSON
12. `value` is the performance metric with a certain `unit`, as extracted from the JMH result JSON

Example CSV file:
```
project;commit;benchmark;params;instance;trial;fork;iteration;mode;unit;value_count;value
myproject;1.0.0;ch.uzh.ifi.seal.myproject.Bench.bench1;paramA=1,paramB=2;1;1;1;1;sample;ops/s;4;2e+07
myproject;1.0.0;ch.uzh.ifi.seal.myproject.Bench.bench1;paramA=1,paramB=2;1;1;1;1;sample;ns/op;10;210
myproject;1.0.0;ch.uzh.ifi.seal.myproject.Bench.bench1;paramA=1,paramB=2;1;1;1;1;sample;ns/op;1;220
...
myproject;1.0.0;ch.uzh.ifi.seal.myproject.Bench.bench1;paramA=1,paramB=2;1;1;1;2;sample;ns/op;1;220
...
myproject;1.0.0;ch.uzh.ifi.seal.myproject.Bench.bench1;paramA=1,paramB=2;1;1;1;3;sample;ns/op;1;210

...

myproject;1.0.0;net.laaber.myproject.Bench.bench2;;1;1;1;1;thrpt;ops/s;1;2.1e+07
```



### Benchmark Coverage

bencher generates covered methods based either on static call graphs or dynamic coverage.

The output format is text based:
```
Method:
Benchmark(clazz=ch.uzh.ifi.seal.myproject.Bench, name=bench1, params=[org.openjdk.jmh.infra.Blackhole], jmhParams=[(paramA, 1), (paramB, 2)])
Method(clazz=ch.uzh.ifi.seal.myproject.ClassA, name=m1, params=[]);1.0;2
Method(clazz=ch.uzh.ifi.seal.myproject.ClassB, name=m3, params=[]);1.0;1
Method(clazz=ch.uzh.ifi.seal.myproject.ClassC, name=<init>, params=[]);1.0;2
...
Method:
...
```

Coverage of a benchmark starts with a line `Method:`, followed by a single line that describes the benchmark (`Benchmark(...)`).
After the benchmark line, zero to n lines follow (until the next `Method:` line appears) that describe the covered methods.
Every covered method line consists of 3 elements, separated by semi-colons:
1. the method description enclosed in `Method(...)`
2. the probability that this method is covered, which is 1.0 for coverage based on dynamic information and can be lower for static call graphs, due to over-approximation of the points-to analysis
3. the coverage level at which this method is reachable

#### Dynamic Coverage

Coverage based on dynamic information can be retrieved with the following command: 

```bash
java -jar bencher.jar \
    -p myproject \
    -pf ch.uzh.ifi.seal.myproject \
    -out dynamic_coverage.txt \
    dc \
    -f project.jar \
    -inc "ch.uzh.ifi.seal.myproject,other.pkg,java.lang" \
    -covpb
    -cut METHOD
```

* The only required `dc` sub-command parameter is `-f`, specifying the benchmark JMH JAR file to analyse.
Make sure that the JMH JAR file includes all dependencies (fat JAR), otherwise the coverage extraction will not work properly.
* It is recommended to provide the `-inc` argument, which specifies which packages should be instrumented for coverage extraction.
`-inc` takes a comma-seperated list of package paths.
* `-covpb` is a boolean argument that configures whether for every parameterization of a benchmark coverage should be extracted.
If not provided, coverage is extracted for the first parameter combination, and the output contains this coverage information for *all* parameter combinations of a benchmark.
* `-cut` is the coverage unit type; i.e., method, line, or both (all); that should be extracted.
  
#### Static Coverage

Coverage based on static call graphs rely on [WALA](http://wala.sourceforge.net/wiki/index.php/Main_Page).
The following command constructs the coverage information:

```bash
java -jar bencher.jar \
    -p myproject \
    -pf ch.uzh.ifi.seal.myproject
    -out static_coverage.txt \
    sc \
    -f project.jar \
    -inc "ch.uzh.ifi.seal.myproject,other.pkg,java.lang" \
    -ro FULL \
    -wa 01CFA \
    -sep
```

* Similar to dynamic coverage, the only required argument for the `sc` sub-command is `f`, specifying the JMH JAR file.
Again, all dependencies must be contained in the JMH JAR.
* `-inc` is again recommended to specify which packages should be contained in the coverage information.
* `-wa` specifies the call graph algorithm of WALA, see CLI help for valid options
* `-ro` specifies the reflection options of WALA, see CLI help for valid options
* `-sep` is a boolean argument, specifying whether the entry points used for call graph construction of every benchmark, is the union of all benchmark entrypoints.
If not specified, bencher constructs a new call graph for every benchmark, only containing the entry points belonging to this benchmark (i.e., the benchmark method itself and its `@Setup` methods)



### Prioritization of Benchmarks

To prioritize benchmarks for execution, the bencher's sub-command `prio` is used.
A minimum command to prioritize benchmarks is the following:

```bash
java -jar bencher.jar \
    -p myproject \
    -pf ch.uzh.ifi.seal.myproject
    -out prio.csv \
    prio \
    -v1 project_v1.jar \
    -v2 project_v2.jar \
    -cov coverage.txt
```

* `-v1` and `-v2` specify the old and new version of the project between which we want to perform regression testing and prioritize the benchmarks
* `-cov` specifies the coverage file as output by `sc` or `dc`

The `prio` sub-command comes with more optional arguments:
* `-pt` specifies the prioritization strategy, i.e., `DEFAULT` JMH order, `RANDOM`, `TOTAL`, `ADDITIONAL`, or `MO_COVERAGE_OVERLAP_PERFCHANGES`
* `-pb` is a boolean argument, if specified every benchmark parameter combination is treated by the prioritization as an own benchmark
* `-pbr` is a boolean argument, if specified the parameters of a benchmark method are ranked in reverse order, starting with the largest parameter values
* `-pc` specifies the performance changes file path, which is used for the prioritization strategy `MO_COVERAGE_OVERLAP_PERFCHANGES`
* `-ppv` specifies the previous project version
* `-cas` is a boolean argument, if specified a simple change analysis between `-v1` and `-v2` is performed.
  First the benchmarks that are affected by the change are prioritized, and afterwards the unaffected benchmarks are prioritized
* `-cap` is a boolean argument, if specified a simple change analysis between `-v1` and `-v2` is performed.
  Only changed coverage information is considered for the prioritization.
* `-tb` specifies the time-budget available for testing, which is filled greedily with the strategies from `-pt`
* `-jmh` overrides the configurations extracted from the byte code, which is used for time-budget testing
* `-w` specifies a weights file, that assigns different weights to methods and considers these weights during prioritization
* `-wm` specifies how the weights from `-w` should be mapped

The output of the prioritization is a CSV file of the following columns:
1. `benchmark` is the fully-qualified benchmark method name
2. `params` are the comma-separated method parameters of the benchmark
3. `perf_params` are the JMH parameters (not the Java parameters) of the benchmark in comma-separated form.
Every parameter consists of a name and a value, separated by an equal sign (`name=value`).
4. `rank` is the rank of the benchmark.
1 is the first benchmark to execute, n is the last benchmark to execute
5. `total` is the total number of benchmarks ranked
6. `prio` is the prioritization weight assigned to the benchmark

Example prioritization CSV file:
```
benchmark;params;perf_params;rank;total;prio
ch.uzh.ifi.seal.myproject.Bench.bench1;org.openjdk.jmh.infra.Blackhole;paramA=1,paramB=2;1;4;100
ch.uzh.ifi.seal.myproject.Bench.bench3;;;2;4;10
ch.uzh.ifi.seal.myproject.Bench.bench4;;;3;4;5
ch.uzh.ifi.seal.myproject.Bench.bench2;;;4;4;1
```


## API Usage

All CLI functionality is also available through a programatic API.
One particular features that *is not* can not be accessed through the CLI, is the benchmark (`@Benchmark`) and configuration (JMH configuration annotations) parsers.
bencher implements both source code (`ch.uzh.ifi.seal.bencher.analysis.finder.jdt.JdtBenchFinder`) and byte code (`ch.uzh.ifi.seal.bencher.analysis.finder.asm.AsmBenchFinder`) parsers.
