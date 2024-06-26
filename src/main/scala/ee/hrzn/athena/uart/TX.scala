/* Copyright © 2024 Asherah Connor.
 *
 * This file is part of Athena.
 *
 * Athena is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * Athena is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Athena. If not, see <https://www.gnu.org/licenses/>.
 */

package ee.hrzn.athena.uart

import chisel3._
import chisel3.util._

class TX(private val divisor: BigInt) extends Module {
  val io  = IO(Flipped(Irrevocable(UInt(8.W))))
  val pin = IO(Output(Bool()))

  object State extends ChiselEnum {
    val sIdle, sTx = Value
  }
  private val state = RegInit(State.sIdle)

  private val timerReg   = RegInit(0.U(unsignedBitLength(divisor - 1).W))
  private val counterReg = RegInit(0.U(unsignedBitLength(9).W))
  private val shiftReg   = RegInit(0.U(10.W))

  pin := true.B
  io.nodeq()

  switch(state) {
    is(State.sIdle) {
      val bits = io.deq()
      when(io.fire) {
        timerReg   := (divisor - 1).U
        counterReg := 9.U
        shiftReg   := 1.U(1.W) ## bits ## 0.U(1.W)
        state      := State.sTx
      }
    }
    is(State.sTx) {
      pin      := shiftReg(0)
      timerReg := timerReg - 1.U
      when(timerReg === 0.U) {
        timerReg   := (divisor - 1).U
        counterReg := counterReg - 1.U
        shiftReg   := 0.U(1.W) ## shiftReg(9, 1)
        when(counterReg === 0.U) {
          state := State.sIdle
        }
      }
    }
  }
}
