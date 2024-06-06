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
import ee.hrzn.chryse.platform.Platform

class UartIO extends Bundle {
  val tx = Flipped(Decoupled(UInt(8.W)))
  val rx = Decoupled(new RXOut)
}

class Uart(
    val baud: Int = 9600,
    val bufferLength: Int = 16,
    useSyncReadMem: Boolean = true,
)(implicit
    platform: Platform,
) extends Module {
  private val divisor = platform.clockHz / baud

  val io = IO(new UartIO)
  val pins = IO(new Bundle {
    val rx = Input(Bool())
    val tx = Output(Bool())
  })

  private val rx = Module(new RX(divisor))
  io.rx :<>= Queue(rx.io, bufferLength, useSyncReadMem = useSyncReadMem)
  rx.pin := pins.rx

  private val tx = Module(new TX(divisor))
  tx.io :<>= Queue.irrevocable(
    io.tx,
    bufferLength,
    useSyncReadMem = useSyncReadMem,
  )
  pins.tx := tx.pin
}
