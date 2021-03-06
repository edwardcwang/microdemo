package main

import chisel3._

class MicroDemoTop extends Module {
  final val FREQ_MHZ = 65
  final val TIME_DELAY_MS = 200

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
    val counter0 = Output(UInt(3.W))
    val counter1 = Output(UInt(3.W))
    val counter2 = Output(UInt(3.W))
  })

  val microdemo = Module(new MicroDemo(FREQ_MHZ, TIME_DELAY_MS))
  microdemo.io <> io
}
