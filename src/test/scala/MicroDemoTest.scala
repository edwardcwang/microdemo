package main.test

import main.MicroDemo

import Chisel.iotesters._
import chisel3.util._

class MicroDemoUnitTester(c: MicroDemo) extends PeekPokeTester(c) {
  expect(c.io.counter0, 0)
  System.out.println("%d\n".format(peek(c.io.counter0).intValue))
  for (i <- 1 to c.CYCLES) {
    step(1)
    System.out.println("%d\n".format(peek(c.io.counter0).intValue))
  }
  expect(c.io.counter0, 1)
}

class MicroDemoTest extends ChiselFlatSpec {
  val backendNames = Array[String](/*"firrtl", */"verilator")
  for ( backendName <- backendNames ) {
    "TopLevel" should s"toplevel (with ${backendName})" in {
      Driver(() => new MicroDemo(1, 1), backendName) {
        c => new MicroDemoUnitTester(c)
      } should be (true)
    }
  }
}

