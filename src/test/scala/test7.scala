package analysis

class TestAnalysis7 extends RunAndCheckSuite with SimpleFrontend {

  val prefix = "test-out/test-analysis-7"

  // modify stuff after a loop

  testProg("C1") { //test6
      Block(List(
        Assign("i", Const(0)),
        Assign("z", New("A")),
        Assign("x", Ref("z")),
        Assign("y", New("B")),
        While(Less(Ref("i"),Const(100)), Block(List(
          Put(Ref("y"), Const("head"), Ref("i")),
          Put(Ref("y"), Const("tail"), Ref("x")),
          Assign("x", Ref("y")),
          Assign("i", Plus(Ref("i"), Const(1)))
        ))),
        Put(Ref("y"), Const("tail"), Ref("z")),
        Put(Ref("y"), Const("head"), Const(7))
      ))
    } {
      """
        Map(
          "&i" -> Map("val" -> 100),
          "B"  -> Map("top" -> Map("head" -> 7, "tail" -> (A,top))),
          "A"  -> Map("top" -> Map()),
          "&x" -> Map("val" -> (B,top)),
          "&z" -> Map("val" -> (A,top)),
          "&y" -> Map("val" -> (B,top))
        )
      """
    }

  // strong update for if

  testProg("D1") { //test7
      Block(List(
        Assign("x", New("A")),
        If(Direct(vref("input")),
          Block(List(
            Put(Ref("x"), Const("a"), New("B")),
            Put(Get(Ref("x"), Const("a")), Const("foo"), Const(5))
          )),
          Block(List(
            Put(Ref("x"), Const("a"), New("C")),
            Put(Get(Ref("x"), Const("a")), Const("bar"), Const(5))
          ))
        ),
        Assign("foo", Get(Get(Ref("x"), Const("a")), Const("foo"))),
        Assign("bar", Get(Get(Ref("x"), Const("a")), Const("bar")))
      ))
    }{
      """
        Map(
          "B"  -> Map("top" -> Map("foo" -> 5)),
          "A"  -> Map("top" -> Map("a" -> (B,top))),
          "&x" -> Map("val" -> (A,top)),
          "&bar" -> Map("val" -> <error>),
          "&foo" -> Map("val" -> 5)
        )
      """
    }
  testProg("D2") { //test8
      Block(List(
        Assign("x", New("A")),
        Put(Ref("x"), Const("a"), New("A2")),
        Put(Get(Ref("x"), Const("a")), Const("baz"), Const(3)),
        If(Direct(vref("input")),
          Block(List(
            Put(Ref("x"), Const("a"), New("B")), // strong update, overwrite
            Put(Get(Ref("x"), Const("a")), Const("foo"), Const(5))
          )),
          Block(List(
            Put(Ref("x"), Const("a"), New("C")), // strong update, overwrite
            Put(Get(Ref("x"), Const("a")), Const("bar"), Const(5))
          ))
        ),
        Put(Get(Ref("x"), Const("a")), Const("bar"), Const(7)), // this is not a strong update, because 1.a may be one of two allocs
        Assign("xbar", Get(Get(Ref("x"), Const("a")), Const("bar"))) // should still yield 7!
      ))
    }{
      """
        Map(
          "B"  -> Map("top" -> Map("foo" -> 5, "bar" -> 7)),
          "A2" -> Map("top" -> Map("baz" -> 3)),
          "A"  -> Map("top" -> Map("a" -> (B,top))),
          "&x" -> Map("val" -> (A,top)),
          "&xbar" -> Map("val" -> 7)
        )
      """
    }

  // update stuff allocated in a loop

  testProg("E1") { //test9
    If(Less(Direct(vref("COUNT")),Const(0)),
      Block(Nil),
      Block(List(
        Assign("i", Const(0)),
        Assign("x", New("X")),
        Put(Ref("x"), Const("a"), New("A")),
        Put(Get(Ref("x"), Const("a")), Const("baz"), Const(3)),
        While(Less(Ref("i"),Direct(vref("COUNT"))),
          Block(List(
            Put(Ref("x"), Const("a"), New("B")), // strong update, overwrite
            Put(Get(Ref("x"), Const("a")), Const("foo"), Const(5)),
            Assign("i", Plus(Ref("i"),Const(1)))
          ))
        ),
        Put(Get(Ref("x"), Const("a")), Const("bar"), Const(7)), // this is not a strong update, because 1.a may be one of two allocs
        Assign("xbar", Get(Get(Ref("x"), Const("a")), Const("bar"))) // should still yield 7!
      )))
    } {
      """
      if(COUNT < 0) Map() else
      Map(
        "&i" -> Map("val" -> if (0 < "COUNT") "COUNT" else 0),
        "B"  -> if(0 < COUNT) Map("top" ->
          if (1 < "COUNT")
            collect("COUNT") { x15_B_top_x16 => Map("foo" -> 5) }
            + ("COUNT" + -1 -> Map("foo" -> 5, "baz" -> "nil", "bar" -> 7))
          else
            collect("COUNT") { x15_B_top_x16 => Map("foo" -> 5) }
        ) else nil,
        "X"  -> Map("top" -> Map("a" ->
          if(0<COUNT){ if (1 < "COUNT")
            ("B",("top","COUNT" + -1))
          else
            (A,top) } else (A,top)
        )),
        "A"  -> Map("top" -> Map("baz" -> 3, "foo" -> "nil", "bar" -> if(0<COUNT){ if (1 < "COUNT") "nil" else 7 } else 7)),
        "&x" -> Map("val" -> (X,top)),
        "&xbar" -> Map("val" -> 7)
      )
      """
/*
      Note: above is more complicated that we'd like. should optimize (at least) to:
      """
      Map(
        "&i" -> Map("val" -> "COUNT"),
        "B"  -> Map("top" ->
          if (1 < "COUNT")
            collect("COUNT") { x14_B_top_x15 => Map("foo" -> 5) }
            + ("COUNT" + -1 -> Map("foo" -> 5, "baz" -> "nil", "bar" -> 7))
          else
            collect("COUNT") { x14_B_top_x15 => Map("foo" -> 5) }
        ),
        "X"  -> Map("top" -> Map("a" ->
          if (1 < "COUNT")
            ("B",("top","COUNT" + -1))
          else
            (A,top)
        )),
        "A"  -> Map("top" -> Map("baz" -> 3, "foo" -> "nil", "bar" -> if (1 < "COUNT") "nil" else 7)),
        "&x" -> Map("val" -> (X,top)),
        "&xbar" -> Map("val" -> 7)
      )
      """
*/    }


  // factorial: direct
  testProg("F1") {
      Block(List(
        Assign("n", Direct(vref("N"))),
        Assign("i", Const(0)),
        Assign("r", Const(1)),
        While(Less(Ref("i"),Ref("n")),
          Block(List(
            Assign("i", Plus(Ref("i"),Const(1))),
            Assign("r", Times(Ref("r"),Ref("i")))
          ))
        )
      ))
    } {
      """
        val x7_&r_val = { x8 => if (0 < x8) x7_&r_val(x8 + -1) * x8 else 1 }
        Map(
          "&n" -> Map("val" -> "N"),
          "&i" -> Map("val" -> if (0 < "N") "N" else 0),
          "&r" -> Map("val" -> x7_&r_val("N"))
        )
      """
      // Note: formula for &i should be optimized if we assumed that N >= 0
    }
}

// (to try: fac, first as while loop, then as recursive
// function with defunctionalized continuations in store)


/*

McCarthy's 91 program:

MC(n)= if (n>100) n-10 else MC(MC(n + 11)) // n ≤ 100

equivalent to:

MC(n)= (n>100) n-10 else 91

non-recursive version:

 int mccarthy(int n)
 {
     int c;
     for (c = 1; c != 0; ) {
         if (n > 100) {
             n = n - 10;
             c--;
         } else {
             n = n + 11;
             c++;
         }
     }
     return n;
 }


*/
