package main.test

import main.MemDemo

import chisel3.iotesters._
import chisel3.util._

class MemDemoUnitTester(c: MemDemo) extends PeekPokeTester(c) {
  poke(c.io.btn0.input, false)
  poke(c.io.btn1.input, false)
  poke(c.io.x, false)
  step(1)

  def advanceCounter():Unit = {
    poke(c.io.btn0.input, true)
    step(1)
    poke(c.io.btn0.input, false)
  }

  def writeValue(v:Boolean):Unit = {
    poke(c.io.x, v)
    poke(c.io.btn1.input, true)
    step(1)
    poke(c.io.btn1.input, false)
  }

  // Write a value
  for (i <- 0 to 7) {
    writeValue(i % 2 == 0)
    advanceCounter()
  }

  // Read the values back
  for (i <- 0 to 7) {
    step(1)
    expect(c.io.led3, i % 2 == 0)
    advanceCounter()
  }
}

class MemDemoTest extends ChiselFlatSpec {
  val backendNames = Array[String]("firrtl", "verilator")
  for ( backendName <- backendNames ) {
    "MemDemo" should s"write and read (with ${backendName})" in {
      Driver(() => new MemDemo(false), backendName) {
        c => new MemDemoUnitTester(c)
      } should be (true)
    }
  }
}
