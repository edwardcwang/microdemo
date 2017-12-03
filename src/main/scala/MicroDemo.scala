package main

import chisel3._
import chisel3.util._

import chisel3.core.ExplicitCompileOptions.NotStrict // Issues with mem.read

class MicroDemo(val freqMHz: Int, val delayMS: Int) extends Module {
  final val CYCLES = 1000*freqMHz*delayMS

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

  io.and := io.x && io.y && io.z
  io.or := io.x || io.y || io.z
  io.xor := io.x ^ io.y ^ io.z

  val reg0 = RegInit(0.U(3.W))
  val reg1 = RegInit(2.U(3.W))
  val reg2 = RegInit(4.U(3.W))
  val ticker = Reg(init=0.U((log2Ceil(CYCLES)+1).W))
  when(ticker >= (CYCLES - 1).U) {
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

