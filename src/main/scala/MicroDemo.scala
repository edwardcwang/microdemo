package main

import chisel3._
import chisel3.util._

class MicroDemo(val freqMHz: Int, val delayMS: Int) extends Module {
  final val CYCLES = 1000*freqMHz*delayMS

  val io = IO(new Bundle {
    val x = Input(Bool())
    val y = Input(Bool())
    val z = Input(Bool())
    val and = Output(Bool())
    val or = Output(Bool())
    val xor = Output(Bool())
    val counter0 = Output(UInt(width=3))
    val counter1 = Output(UInt(width=3))
    val counter2 = Output(UInt(width=3))
  })

  io.and := io.x && io.y && io.z
  io.or := io.x || io.y || io.z
  io.xor := io.x ^ io.y ^ io.z

  val reg0 = Reg(init=0.U(3.W))
  val reg1 = Reg(init=2.U(3.W))
  val reg2 = Reg(init=4.U(3.W))
  val ticker = Reg(init=UInt(0, width=log2Ceil(CYCLES)+1))
  when(ticker >= UInt(CYCLES - 1)) {
    reg0 := reg0 + 1.U
    reg1 := reg1 + 1.U
    reg2 := reg2 + 1.U
    ticker := 0.U
  } .otherwise {
    ticker := ticker + 1.U
  }

  io.counter0 := reg0
  io.counter1 := reg1
  io.counter2 := reg2
}

