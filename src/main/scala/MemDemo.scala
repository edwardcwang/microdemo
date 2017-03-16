package main

import chisel3._
import chisel3.util._

object EdgeDetector {
  // Set bothEdges to true to detect both edges.
  def apply(sig:Bool, bothEdges:Boolean = false):Bool = {
    val prev = RegNext(sig)
    if (bothEdges) {
      prev =/= sig
    } else {
      !prev && sig
    }
  }
}

object Debouncer {
  def apply(sig:Bool) : Bool = {
    val m = Module(new Debouncer(1))
    m.io.in(0) := sig
    m.io.out(0)
  }
}

// n-bit debouncer.
// In the samples window, must have >= percent_stable*samples samples
// to remain high.
class Debouncer(n: Int, samples: Int = 60000, percent_stable: Float = 0.8f) extends Module {
  val stable_samples : Int = (percent_stable * samples.toFloat).toInt
  val io = IO(new Bundle {
    val in  = Input(Vec(n, Bool()))
    val out  = Output(Vec(n, Bool()))
  })
  val out_reg = Reg(init = Vec.fill(n){Bool(false)})
  io.out <> out_reg

  val num_ones = Reg(init = Vec.fill(n)(UInt(0, width=32)))
  val counter = Counter(samples)

  when(Bool(true)) {
    for (i <- 0 to (n-1)) {
      val num_ones_next = Mux(io.in(i), num_ones(i) + UInt(1), num_ones(i))
      when(counter.value === UInt(samples-1)) {
        out_reg(i) := Mux(num_ones(i) >= UInt(stable_samples), Bool(true), Bool(false))
      }
      num_ones(i) := Mux(counter.value === UInt(samples-1), UInt(0), num_ones_next)
    }
    counter.inc()
  }

  def getN(): Int = { n }
}

class TristateIO extends Bundle {
	val input = Input(Bool()) // "_o" in the IOBUF (pad->FPGA)
	val output = Output(Bool()) // "_i" in the IOBUF (pad<-FPGA)
	val output_enable = Output(Bool()) // "_oe" in the IOBUF (set to true to push data pad<-FPGA)
}

class MemDemo(val debounce: Boolean = true) extends Module {
  val io = IO(new Bundle {
    val x = Input(Bool())
    val y = Input(Bool())
    val z = Input(Bool())
    val btn0 = new TristateIO()
    val btn1 = new TristateIO()
    val and = Output(Bool())
    val or = Output(Bool())
    val xor = Output(Bool())
    val led3 = Output(Bool())
    val counter0 = Output(UInt(width=3))
    val counter1 = Output(UInt(width=3))
    val counter2 = Output(UInt(width=3))
  })

  // Configure the two buttons as inputs.
  io.btn0.output_enable := false.B
  io.btn1.output_enable := false.B

  // Choose whether to use a debouncer or not.
  // If not debouncing, use a dummy function which just passes the signal
  // through.
  val debouncer = if (debounce) ((s:Bool) => Debouncer(s)) else ((s:Bool) => s)

  // Edge detect the buttons and debounce the inputs as needed.
  val btn0 = EdgeDetector(debouncer(io.btn0.input))
  val btn1 = EdgeDetector(debouncer(io.btn1.input))
  val x = debouncer(io.x)
  val y = debouncer(io.y)
  val z = debouncer(io.z)

  // Create a counter for the button to increment.
  val counter = Counter(8)
  when(btn1) {
    counter.inc()
  }

  // Display the value of the counter on the LEDs.
  io.and := counter.value(0)
  io.or := counter.value(1)
  io.xor := counter.value(2)

  // Set up the wires for accessing the memory.
  val addr = Wire(init=0.U(11.W))
  val dataIn = Wire(UInt(32.W))
  val dataOut = Wire(UInt(32.W))
  val write = btn0
  val read = true.B
  addr := counter.value
  dataIn := x

  // Instantiate the synchronous memory.
  val mem = SyncReadMem(2048, UInt(32.W))
  when (write) { mem.write(addr, dataIn) }
  .otherwise { dataOut := mem.read(addr, read) }

  // Wire up the last LED to the data being read.
  io.led3 <> dataOut(0)

  // Also show the address wire as well.
  io.counter0 := addr(2, 0)
}
