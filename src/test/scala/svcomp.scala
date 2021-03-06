package analysis

import Util._
import scala.io.Source

abstract class SVCompSuite extends FileDiffSuite {

  // TODO: make path configurable
  val sv_bench_root = {
    val lines = Source.fromFile("application.conf").getLines().toList
    def pathValid(s: String) = new java.io.File(s + "sv-benchmarks").exists
    (lines find pathValid getOrElse (sys.error(s"""sv benchmark root not found in search path:\n\t- ${lines mkString "\n\t -" }"""))) + "sv-benchmarks/c/"
  }

  val file_pat_re = """(.+)/\*_(true|false)-(.*)\*(.+)""".r // note: inefficent match, may cause heavy backtracking!
  val exact_file_pat_re = """(.+)/.*_(true|false)-(.*)\.(.+)""".r // note: inefficent match, may cause heavy backtracking!

  val out_prefix = "test-out/sv-bench/"


  val controlFlow = Array(
    "ntdrivers-simplified/*_false-unreach-call*.cil.c",
    "ntdrivers-simplified/*_true-unreach-call*.cil.c",
    "ssh-simplified/*_false-unreach-call*.cil.c",
    "ssh-simplified/*_true-unreach-call*.cil.c",
    "locks/*_false-unreach-call*.c",
    "locks/*_true-unreach-call*.c")

  val loops = Array(
    // "loops/*_false-unreach-call*.i",
    // "loops/*_true-unreach-call*.i",
    // "loop-acceleration/*_false-unreach-call*.i",
    // "loop-acceleration/*_true-unreach-call*.i",
    "loop-invgen/*_false-unreach-call*.i",
    "loop-invgen/*_true-unreach-call*.i",
    "loop-lit/*_true-unreach-call*.c.i" // ,
    // "loop-new/*_true-unreach-call*.i"
    )

  val recursive = Array(
  "recursive-simple/sum_25x0_false-unreach-call.c",
  "recursive-simple/afterrec_false-unreach-call.c",
  "recursive-simple/sum_15x0_false-unreach-call.c",
  "recursive-simple/fibo_2calls_2_false-unreach-call.c",
  // "recursive-simple/id_b3_o2_false-unreach-call.c", // random values
  "recursive-simple/fibo_5_false-unreach-call.c",
  // "recursive-simple/sum_non_false-unreach-call.c",  // random values
  "recursive-simple/fibo_2calls_4_false-unreach-call.c",
  // "recursive-simple/id_o100_false-unreach-call.c",  // random values
  // "recursive-simple/id_o3_false-unreach-call.c",    // random values
  "recursive-simple/sum_10x0_false-unreach-call.c",
  "recursive-simple/sum_20x0_false-unreach-call.c",
  "recursive-simple/id_i20_o20_false-unreach-call.c",
  // "recursive-simple/fibo_25_false-unreach-call.c", // fibo(25) OoM
  // "recursive-simple/sum_non_eq_false-unreach-call.c",
  "recursive-simple/fibo_2calls_5_false-unreach-call.c",
  "recursive-simple/afterrec_2calls_false-unreach-call.c",
  // "recursive-simple/id_o200_false-unreach-call.c",
  // "recursive-simple/id_o1000_false-unreach-call.c",
  "recursive-simple/id_i10_o10_false-unreach-call.c",
  "recursive-simple/id2_i5_o5_false-unreach-call.c",
  "recursive-simple/id_i5_o5_false-unreach-call.c",
  "recursive-simple/fibo_10_false-unreach-call.c",
  "recursive-simple/sum_2x3_false-unreach-call.c",
  "recursive-simple/fibo_15_false-unreach-call.c",
  "recursive-simple/fibo_2calls_15_false-unreach-call.c",
  "recursive-simple/id_i15_o15_false-unreach-call.c",
  "recursive-simple/id_i25_o25_false-unreach-call.c",
  // "recursive-simple/id_o10_false-unreach-call.c",
  "recursive-simple/fibo_2calls_10_false-unreach-call.c",
  // "recursive-simple/id_o20_false-unreach-call.c",
  // "recursive-simple/id2_b3_o2_false-unreach-call.c",
  "recursive-simple/fibo_2calls_20_false-unreach-call.c",
  // "recursive-simple/fibo_2calls_25_false-unreach-call.c",
  "recursive-simple/fibo_2calls_6_false-unreach-call.c",
  "recursive-simple/fibo_7_false-unreach-call.c",
  "recursive-simple/fibo_20_false-unreach-call.c",
  "recursive-simple/fibo_2calls_8_false-unreach-call.c",
  "recursive-simple/id_i25_o25_true-unreach-call.c",
  // "recursive-simple/id_b2_o3_true-unreach-call.c",
  "recursive-simple/afterrec_true-unreach-call.c",
  "recursive-simple/id_i10_o10_true-unreach-call.c",
  "recursive-simple/id2_i5_o5_true-unreach-call.c",
  "recursive-simple/id_i5_o5_true-unreach-call.c",
  // "recursive-simple/id_b3_o5_true-unreach-call.c",
  "recursive-simple/fibo_15_true-unreach-call.c",
  "recursive-simple/fibo_2calls_4_true-unreach-call.c",
  "recursive-simple/fibo_2calls_8_true-unreach-call.c",
  "recursive-simple/fibo_2calls_10_true-unreach-call.c",
  // "recursive-simple/id2_b5_o10_true-unreach-call.c",
  // "recursive-simple/id2_b3_o5_true-unreach-call.c",
  "recursive-simple/id_i20_o20_true-unreach-call.c",
  "recursive-simple/fibo_2calls_6_true-unreach-call.c",
  "recursive-simple/sum_2x3_true-unreach-call.c",
  // "recursive-simple/id_b5_o10_true-unreach-call.c",
  // "recursive-simple/sum_non_true-unreach-call.c",
  "recursive-simple/fibo_2calls_5_true-unreach-call.c",
  // "recursive-simple/sum_non_eq_true-unreach-call.c",
  "recursive-simple/fibo_7_true-unreach-call.c",
  "recursive-simple/fibo_10_true-unreach-call.c",
  // "recursive-simple/fibo_2calls_25_true-unreach-call.c",
  "recursive-simple/sum_15x0_true-unreach-call.c",
  "recursive-simple/fibo_5_true-unreach-call.c",
  // "recursive-simple/id2_b2_o3_true-unreach-call.c",
  "recursive-simple/sum_25x0_true-unreach-call.c",
  "recursive-simple/fibo_2calls_15_true-unreach-call.c",
  // "recursive-simple/fibo_25_true-unreach-call.c",
  "recursive-simple/sum_20x0_true-unreach-call.c",
  "recursive-simple/fibo_2calls_2_true-unreach-call.c",
  "recursive-simple/fibo_2calls_20_true-unreach-call.c",
  "recursive-simple/sum_10x0_true-unreach-call.c",
  "recursive-simple/fibo_20_true-unreach-call.c",
  "recursive-simple/afterrec_2calls_true-unreach-call.c",
  "recursive-simple/id_i15_o15_true-unreach-call.c"

    )
  val successAsIs = Array(
    "loop-invgen/nested6_true-unreach-call.i",
    "loop-invgen/up_true-unreach-call.i",
    "loop-invgen/NetBSD_loop_true-unreach-call.i",
    "loop-invgen/MADWiFi-encode_ie_ok_true-unreach-call.i",
    "loop-invgen/down_true-unreach-call.i",
    "loop-invgen/seq_true-unreach-call.i",
    "loop-invgen/nest-if3_true-unreach-call.i", // this one is pretty cool
    "loop-lit/gj2007b_true-unreach-call.c.i",
    "loop-lit/gj2007_true-unreach-call.c.i",
    "loop-lit/jm2006_variant_true-unreach-call.c.i",
    "loop-lit/jm2006_true-unreach-call.c.i",
    "loop-lit/cggmp2005_variant_true-unreach-call.c.i",
    "loop-lit/css2003_true-unreach-call.c.i",
    "loop-lit/hhk2008_true-unreach-call.c.i",
    "loop-lit/cggmp2005_true-unreach-call.c.i",
    "loop-lit/bhmr2007_true-unreach-call.c.i",
    "loop-invgen/large_const_true-unreach-call.i",
    "loop-lit/afnp2014_true-unreach-call.c.i",
    "loop-lit/mcmillan2006_true-unreach-call.c.i",
    "loop-lit/gr2006_true-unreach-call.c.i",
    "loop-invgen/fragtest_simple_true-unreach-call.i", // not sure it is correct
    "loop-invgen/id_trans_false-unreach-call.i",
    "loop-lit/ddlm2013_true-unreach-call.c.i",
    "loop-invgen/nested9_true-unreach-call.i",
    "loop-invgen/half_2_true-unreach-call.i",
    "loop-invgen/id_build_true-unreach-call.i"
  )

  val successModified = Array(
    "loop-invgen/sendmail-close-angle_true-unreach-call.i"
  )

  val success = successAsIs ++ successModified

  val sndDegree_condition = Array(
    "loop-lit/gsv2008_true-unreach-call.c.i" // complicated logic
    )

  val incorrect = Array(
    "loop-lit/cggmp2005b_true-unreach-call.c.i" // hit recursion
    )

  val simplification_missing = Array(
    "loop-invgen/string_concat-noarr_true-unreach-call.i"  // random, infinite loop?
  )

  val break_missing = Array(
    "loop-invgen/apache-escape-absolute_true-unreach-call.i", // != makes the analysis difficult
    "loop-invgen/heapsort_true-unreach-call.i", // heap sort -- successive div
    "loop-invgen/apache-get-tag_true-unreach-call.i" // complex control flow
  )

  val toolong = Array(
    "loop-invgen/SpamAssassin-loop_true-unreach-call.i"
  )

  val handle = (success ++ simplification_missing ++ break_missing ++ toolong).toSet

  def extractAll(patterns: Array[String]) = {
    var tot = 0
    for (p <- patterns) {
      val file_pat_re(dir,outcome,prop,suffix) = p
      val search = "_"+outcome+"-"+prop
      val df = new java.io.File(sv_bench_root+"/"+dir)
      assert(df.isDirectory)
      val files = df.listFiles
      for (f <- files) {
        val name = f.getName
        if (name.contains(search) && name.endsWith(suffix) && !handle(dir + "/" + name)) {
          tot += 1
          testOne(dir, name, Map(prop -> outcome.toBoolean))
        }
      }
    }
    println(s"Run $tot test (${patterns mkString(", ")})")
  }

  def runAll(files: Array[String]) = {
    for (p <- files) {
      val exact_file_pat_re(dir,outcome,prop,suffix) = p
      val f = new java.io.File(sv_bench_root+"/"+p)
      val name = f.getName
      testOne(dir, name, Map(prop -> outcome.toBoolean))

    }
  }

  def testOne(dir: String, file: String, props: Map[String,Boolean]) = {
    val key = dir+"/"+file
    test(key) { withOutFileChecked(out_prefix+key) {
      var res: String = "ERROR"
      object Runner extends CFGtoEngine {
        import IRD._
        def run = {
          println("// # "+key)
          val parsed = parseCFile(sv_bench_root+"/"+dir+"/"+file)
          println("// # literal source")
          println(readFile(sv_bench_root+"/"+key))

          withOutFileDiscarded("useless") {
            val cfgs = fileToCFG(parsed)
            val (args, cfg) = cfgs("main")
            val store = evalCFG(args, cfg, funcs = cfgs)

            res = termToString(store)

            val valid = (store match {
              case GConst(m: Map[GVal,GVal]) => m.get(GConst("valid"))
              case Def(DMap(m)) => m.get(GConst("valid"))
            }).getOrElse(GError)

            assert(if (props.values.head) valid == GConst(1) else valid != GConst(1), s"wanted ${props.values.head} -- got ${termToString(valid)} (${termToString(store)})")
          }
        }
      }
      Runner.run

      println("\n\nFinal store:")
      println(res)
    }}
  }

}


// class SVCompControlFlow extends SVCompSuite {
//     extractAll(controlFlow)
// }

class SVCompLoops extends SVCompSuite {
  extractAll(loops)
}

class SVCompRecursive extends SVCompSuite {
  runAll(recursive)
}


class SVCompRunSuccess extends SVCompSuite {
  runAll(success)
}

/*
------> see https://sv-comp.sosy-lab.org

## Definitions

The following definitions are taken from the SV-COMP report
[2016](https://www.sosy-lab.org/~dbeyer/Publications/2016-TACAS.Reliable_and_Reproducible_Competition_Results_with_BenchExec_and_Witnesses.pdf) (and previous years).

A *verification task* consists of
- a program,
- a specification (set of properties), and
- parameters.

A *category* is a set of verification tasks.

A *sub-category* is a set of verification tasks that consist of the same
specification and the same parameters.
A sub-category <sub-category> is defined by the following three files:
- <sub-category>.set contains patterns that specify the set of programs,
- <sub-category>.prp contains the specification, and
- <sub-category>.cfg contains the parameters (and a description of the sub-category).

A *verification run* is
- a non-interactive execution
- of one verification tool
- on one verification task
- under specific resource constraints
in order to check whether the following statement is correct:
"The program satisfies the specification."

A *verification result* is a triple (ANSWER, WITNESS, TIME) with
- ANSWER is an element from {TRUE, FALSE, UNKNOWN},
- WITNESS is a violation witness or correctness witness that supports validation of the (untrusted) answer, and
- TIME is the CPU time that the verification run has consumed (in practice, also other resource measurement values are reported).

## Name Convention for Programs

A program file should be named as follows:
- the original file name or short title of the program is given at the beginning,
- for each property against which the program is to be verified,
  the string `_false-<property>` or `_true-<property>` is included, according to the expected verification answer, and
- the filename ends with ending .c for not pre-processed files and .i for pre-processed files.

For example, the program `minepump_spec5_product61_true-unreach-call_false-termination.cil.c`
is expected to satisfy property `unreach-call` and to violate property `termination`.

There are some old programs that have ending .c although they are pre-processed.

## Specification Properties

For SV-COMP, the [rules page](http://sv-comp.sosy-lab.org/2017/rules.php) explains all currently supported properties:
  - [unreach-call](https://raw.githubusercontent.com/sosy-lab/sv-benchmarks/master/c/PropertyUnreachCall.prp):
    A certain function call must not be reachable in the program.
  - [valid-memsafety, valid-deref, valid-free, valid-memtrack](https://raw.githubusercontent.com/sosy-lab/sv-benchmarks/master/c/PropertyMemSafety.prp):
    A certain memory safety property must hold in the program.
    "memsafety" is the conjunction the other three properties.
  - [no-overflow](https://raw.githubusercontent.com/sosy-lab/sv-benchmarks/master/c/PropertyOverflow.prp):
    A certain kind of undefined behavior (overflows of signed ints) must not be present in the program.
  - [termination](https://raw.githubusercontent.com/sosy-lab/sv-benchmarks/master/c/PropertyTermination.prp):
    The program must terminate on all execution paths.

# Benchmark Verification Tasks

## 5. Integers and Control Flow

This category consists of the following sets of verification tasks. Each of these sets is an own subcategory.

ControlFlow

This set contains programs depend mostly on the control-flow structure and integer variables. There is no particular focus on pointers, data structures, and concurrency.

ntdrivers-simplified/*_false-unreach-call*.cil.c
ntdrivers-simplified/*_true-unreach-call*.cil.c
ssh-simplified/*_false-unreach-call*.cil.c
ssh-simplified/*_true-unreach-call*.cil.c
locks/*_false-unreach-call*.c
locks/*_true-unreach-call*.c
The programs were taken from the source code repositories of the tools BLAST and CPAchecker.

Memory Model: Precise
Architecture: 32 bit

Simple

The problems in this set use programs and properties that depend mostly on control-flow structure and integer variables. There is no particular focus on pointers, data structures, and concurrency.

ntdrivers/*_false-unreach-call*.i.cil.c
ntdrivers/*_true-unreach-call*.i.cil.c
ssh/*_false-unreach-call*.i.cil.c
ssh/*_true-unreach-call*.i.cil.c
Memory Model: Simple
Architecture: 32 bit

ECA (Event-Condition-Action Systems)

This set contains programs that represent event-condition-action systems.

eca-rers2012/*_false-unreach-call*.c
eca-rers2012/*_true-unreach-call*.c
Memory Model: Precise
Architecture: 32 bit

Loops

This set contains verification tasks for which loop analysis is necessary are contained:

loops/*_false-unreach-call*.i
loops/*_true-unreach-call*.i
loop-acceleration/*_false-unreach-call*.i
loop-acceleration/*_true-unreach-call*.i
loop-invgen/*_false-unreach-call*.i
loop-invgen/*_true-unreach-call*.i
loop-lit/*_true-unreach-call*.c.i
loop-new/*_true-unreach-call*.i
Memory Model: Precise
Architecture: 32 bit

Recursive

This set contains several verification tasks for which recursive analysis is necessary:

recursive/*_false-unreach-call*.c
recursive/*_true-unreach-call*.c
recursive-simple/*_false-unreach-call*.c
recursive-simple/*_true-unreach-call*.c
The benchmark set was provided by the Ultimate project.
Description: recursive

Memory Model: Precise
Architecture: 32 bit

ProductLines

This set of verification tasks represents 'products' and 'product simulators' that are derived using different configurations of product lines:

product-lines/elevator*_false-unreach-call.*.c
product-lines/elevator*_true-unreach-call.*.c
product-lines/email*_false-unreach-call.*.c
product-lines/email*_true-unreach-call.*.c
product-lines/minepump*_false-unreach-call.*.c
product-lines/minepump*_true-unreach-call.*.c
The directories 'ntdriver', 'ssh', and 'locks' were taken from the source code repositories of the tools BLAST and CPAchecker. The ECA programs were taken from the RERS Challenge 2012. The directory 'loops' was provided by the ESBMC project. The products were contributed by the SPLverifier project.
Descriptions: eca, loops, product-lines

Memory Model: Precise
Architecture: 32 bit

Sequentialized

This set contains sequentialized concurrent problems that were derived from SystemC programs. The programs were transformed to pure C programs by incorporating the scheduler into the C code.

systemc/*_false-unreach-call*.cil.c
systemc/*_true-unreach-call*.cil.c
seq-mthreaded/*_false-unreach-call.*.c
seq-mthreaded/*_true-unreach-call.*.c
seq-pthread/*_false-unreach-call*.i
seq-pthread/*_true-unreach-call*.i
The SystemC benchmarks were provided by the SyCMC project. The other sequentialized programs are from the projects CSeq and HCCPS.
Descriptions: systemc, seq-mthreaded, seq-pthread

Memory Model: Precise
Architecture: 32 bit


*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/ // annoying formatting ...
