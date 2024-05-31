package ee.hrzn.athena.uart

import chisel3._

class RXOut extends Bundle {
  val byte = Output(UInt(8.W))
  val err  = Output(Bool())
}
