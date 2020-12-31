# ssFix

ssFix is an automated program repair technique that leverages existing code fragments from a large code repository that are syntax-related to the bug context to produce patches for bug repair.

If you use ssFix, please cite our paper:
```
@inproceedings{xin2017leveraging,
  title={Leveraging syntax-related code for automated program repair},
  author={Xin, Qi and Reiss, Steven P.},
  booktitle={Proceedings of the 32nd IEEE/ACM International Conference on Automated Software Engineering (ASE)},
  pages={660--670},
  year={2017},
  organization={IEEE}
}
```

## Running Requirements

+ Linux environment 
+ JDK 1.8
+ Apache Ant

Note that ssFix was tested on a Debian system: Debian 4.9.30 x86_64 GNU/Linux. The tested Apache Ant version was Apache Ant(TM) version 1.9.9. More recently, it was tested on a system running Ubuntu-18.04 with Apache Ant(TM) version 1.10.5 installed.

## How to Build ssFix

1. Install cocker and index your code database

Please refer to https://github.com/qixin5/cocker

2. Copy cocker.jar

In your cocker directory, run ```./makejar.sh``` to produce `cocker.jar`, then copy `cocker.jar` to XXX/ssFix/lib (use your own path to replace XXX)

3. Build ssFix

In your ssFix directory, run ```./compile```

## How to Run ssFix

1. In the script file `run` (the file is under your ssFix directory), uncomment the line that starts with `#proj_dir` (by removing `#`), change the value of `proj_dir` to the **absolute** path of your ssFix directory (if you haven't done so). **NOTE**: You might want to change the memory usage in `run` (by changing the values for `-Xms` and `-Xmx`).

2. Run the script file `run` with at least the following 8 arguments (use **absolute** paths wherever possible):
  * `-bugid`: The program id
  * `-dependjpath`: The path of the dependency jar file of the faulty program (you should create a **single** jar file for the compiled faulty program to be repaired including the test files and all the dependencies)
  * `-projdpath`: The path of the faulty program's directory
  * `-projsrcdpath`: The path of the faulty program's source directory (this is where all the source files are located)
  * `-projbuilddpath`: The path of the faulty program's source-binary directory (this is where all the binaries of the source files are located)
  * `-projtestbuilddpath`: The path of the faulty program's test-source-binary directory (this is where all the binaries of the test source files are located)
  * `-outputdpath`: The output directory (to store the generated patches)
  * `-ssfixdpath`: The directory of ssFix

The following arguments are optional:
  * `-tsuitefpath`: The file containing the full class names of all test cases, separated by semi-colons. If not used, ssFix will simply scan the test directory and create a file including all the test classes. A patched program produced by ssFix needs to pass all the test cases from all the test classes. **NOTE**: We strongly encourage you to provide this file. But if you refuse to do so (which means you want to use all the test cases from the test directory you provided), then you need to make sure that a correct program can pass all the test cases. (ssFix does not report any patched program that failed any test case.)
  * `-tpackage`: The names of the packages where a fault is likely to be located. If there are more than one name, connect them by colons. If this argument is not provided, ssFix will consider all the program packages to be suspicious.
  * `-failedtestcase`: The full class names of all failed test cases. If more than one exist, put them together connected by colons. If this argument is not provided, ssFix will test the faulty program against the test suite to figure out the failed ones.
  * `-maxfaultylines`: The maximum number of suspicious statements (ranked) to be considered for repair. Default is 30.
  * `-maxcandidates`: The maximum number of the top-retrieved, non-redundant candidate chunks to be used for repair. Default is 100. 
  * `-parallelgranularity`: The maximum number of patches that will be tested simultaneously. Default is 1. At the implementation level, we called `Executors.newFixedThreadPool(parallelgranularity)` to create an `ExecutorService` object for patch testing. For our experiments, we set this argument as 8.
  * `-faulocfpath`: The path of the result file yielded by fault localization. If provided, ssFix will simply use the current fault localization result available from the file. Otherwise, ssFix will do fault localization (mainly to call GZoltar).
  * `-faulocaddstacktrace` (NO ARGUMENT): If given, ssFix will use the stack trace information (if available) for fault localization
  * `-usesearchcache` (NO ARGUMENT): If given, ssFix will use the cached code search file (if available) to obtain candidates
  * `-deletefailedpatches` (NO ARGUMENT): If given, ssFix will delete failed patches

Note that ssFix prints the running information to screen (but saves the generated patches to the output directory you provide). You can re-direct the running information to a file, then you will get a log file.

Below is a script I used:
```
/Users/qi/ssFix/run -bugid l21 -dependjpath /Users/qi/Lang_21_buggy/all0.jar 
-projdpath /Users/qi/Lang_21_buggy -projsrcdpath /Users/qi/Lang_21_buggy/src/main/java
-projbuilddpath /Users/qi/Lang_21_buggy/target/classes
-projtestbuilddpath /Users/qi/Lang_21_buggy/target/test-classes 
-outputdpath /Users/qi/ssFix/test/rslt -ssfixdpath /Users/qi/ssFix 
-tsuitefpath /Users/qi/Lang_21_buggy/testsuite_classes -faulocaddstacktrace
-parallelgranularity 8 &> lang21_log
```

## Output

The generated patches can be found under `outputdir/bugid/patches` where `outputdir` and `bugid` are the output directory and the bug id you specified as running arguments.

Under the `patches` directory, there are directories whose names start with `f` each corresponding to a located fault (a suspicious statement). So, `f0` contains patches generated for the top-suspicious statement (with its local context), `f1` contains patches generated for the second-suspicious statement, etc.

Under the directory `patches/f*` (e.g., `patches/f0`), there are directories whose names start with `c` each corresponding to a used candidate chunk. So, `c0` contains patches generated by ssFix using the top-related candidate chunk, `c1` contains patches generated using the second-related chunk, etc.

Under the directory `patches/f*/c*` (e.g., `patches/f2/c3`), there are directories whose names start with `p` each corresponding to a patch. So, `p0` is the first generated patch (that was tested), `p1` is the second generated patch, etc. 

Under the directory `patches/f*/c*/p*` (e.g., `patches/f3/c0/p3`), you may find a single java file as the **patch** file. Using `diff` command over this file and the original, unpatched file (from the faulty program), you will notice what has been changed.

To find the patch reported by ssFix, please look at the repair log file (from the ending part). Recall that you can obtain the repair log file by re-directing ssFix's output running information to a file.


## Testing if Code Search Works

The ssFix code you run on your machine will connect to Cocker for code search
and obtain search results. To make sure that Cocker works for you,
before you actually run ssFix, please do the following for a simple code search test:
```
0. Make sure you've indexed your code database using Cocker and you've started Cocker. If you don't know how to do these, refer to https://github.com/qixin5/cocker.
1. Build ssFix (if you haven't done so).
2. cd to the test directory under your ssFix directory.
3. Untar the compressed project `Lang_21_buggy.tar.gz` by running 
`tar zxf Lang_21_buggy.tar.gz` (if you haven't done so).
4. Run `./run_lang21_codesearch`.
5. Check the result file `lang21_codesearch_rslt` under the current directory.
```
In `lang21_codesearch_rslt`, you should be able to see something similar to the following.
```
file:///home/qxin6/mycodedatabase/testgen/FlexTestGen.java,slc:83,5,0.25485426
file:///home/qxin6/mycodedatabase/testgen/MakeTestGen.java,slc:227,10,0.22162989
file:///home/qxin6/mycodedatabase/testgen/SedTestGen.java,slc:110,10,0.21562001
file:///home/qxin6/mycodedatabase/testgen/Schedule2TestGen.java,slc:57,5;slc:58,5,0.2129162
file:///home/qxin6/mycodedatabase/testgen/FlexTestGen.java,slc:90,2,0.2129162
file:///home/qxin6/mycodedatabase/testgen/ScheduleTestGen.java,slc:57,5;slc:58,5,0.2129162
file:///home/qxin6/mycodedatabase/testgen/SedTestGen.java,slc:95,3,0.2129162
file:///home/qxin6/mycodedatabase/testgen/SedTestGen.java,slc:120,2,0.2129162
file:///home/qxin6/mycodedatabase/testgen/MakeTestGen.java,slc:233,2,0.2129162
file:///home/qxin6/mycodedatabase/testgen/GzipTestGen.java,slc:96,5,0.2129162
```

## Testing if Fault Localization Works

The major part of ssFix's fault localization is done by GZoltar (version 0.1.1).
I tested it (GZoltar) fine on Debian and Ubuntu systems, but I had problems testing it on
a mac. Before you actually run ssFix, please do the following for a simple fault
localization test:
```
1. Build ssFix (if you haven't done so).
2. Set `proj_dir` as the absolute path of your ssFix directory in `run_fauloc` (under your ssFix directory).
3. cd to the test directory under your ssFix directory.
4. Untar the compressed project `Lang_21_buggy.tar.gz` by running
`tar zxf Lang_21_buggy.tar.gz` (if you haven't done so).
5. Run `./run_lang21_fauloc`.
6. Check the result file `lang21_fauloc_log` under the current directory.
```
If you find that the log file `lang21_fauloc_log` contains a line similar as
`Gzoltar Test Result Total:1827, fails: 1, GZoltar suspicious 8696`, it means
the fault localization works.

We have no control over GZoltar. If you encounter any errors,
you may consider reporting the errors to the GZoltar developers (mail@gzoltar.com).
To reproduce our experimental results, you may use our fault localization
result files available at `expt0/fauloc/rslt` under your ssFix directory
(need to untar `expt0/fauloc.tar.gz` first).
You can then run ssFix for a bug passing the path of the corresponding fault 
localization result file as an argument for `-faulocfpath`.


## Running Example

We provide an example for you to test ssFix. Please do the following.
```
0. Copy expt0/candidate/ssFix/plausible/Lang_21.java to your code database.
1. Use Cocker to index the file (refer to section "Index new files" in https://github.com/qixin5/cocker).
2. Build ssFix (if you haven't done so).
3. cd to the test directory under your ssFix directory.
4. Untar the compressed project `Lang_21_buggy.tar.gz` by running
`tar zxf Lang_21_buggy.tar.gz` (if you haven't done so).
5. Test if ssFix's code search works using the instructions shown earlier.
6. Run `./run_lang21`.
7. Wait for ssFix to finish, and check the log file `lang21_log`.
```
If you find that the log file `lang21_log` contains a line that starts with
"Found Plausible Patch at", it means the test was successful.

## Any Questions?

Please contact Qi Xin via qxin6@gatech.edu.