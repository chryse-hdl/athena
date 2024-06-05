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

class RX(private val divisor: BigInt) extends Module {
  val io  = IO(Decoupled(new RXOut))
  val pin = IO(Input(Bool()))

  private val syncedPin = RegNext(RegNext(pin, true.B), true.B)

  object State extends ChiselEnum {
    val sIdle, sRx, sFinish = Value
  }
  private val state = RegInit(State.sIdle)

  private val timerReg   = RegInit(0.U(unsignedBitLength(divisor - 1).W))
  private val counterReg = RegInit(0.U(unsignedBitLength(9).W))
  private val shiftReg   = RegInit(0.U(10.W))

  // |_s_|_1_|_2_|_3_|_4_|_5_|_6_|_7_|_8_|_S
  // ^-- counterReg := 9, state := sRx
  //   ^-- timer hits 0 for the first time, counterReg 9->8
  //       ^-- timer hits 0, counterReg 8->7
  //                                       ^-- 0->-1. Finish @ half STOP bit.

  io.noenq()

  switch(state) {
    is(State.sIdle) {
      when(!syncedPin) {
        timerReg   := (divisor >> 1).U
        counterReg := 9.U
        state      := State.sRx
      }
    }
    is(State.sRx) {
      timerReg := timerReg - 1.U
      when(timerReg === 0.U) {
        timerReg   := (divisor - 1).U
        counterReg := counterReg - 1.U
        // LSB first.
        shiftReg := syncedPin ## shiftReg(9, 1)

        when(counterReg === 0.U) {
          state := State.sFinish
        }
      }
    }
    is(State.sFinish) {
      val rxout = Wire(new RXOut())
      rxout.byte := shiftReg(8, 1)
      // START high or STOP low.
      rxout.err := shiftReg(0) | ~shiftReg(9)
      io.enq(rxout)
      state := State.sIdle
    }
  }
}
