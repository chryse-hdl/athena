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
import chisel3.simulator.EphemeralSimulator._
import ee.hrzn.chryse.platform.Platform
import org.scalatest.flatspec.AnyFlatSpec

class UartSpec extends AnyFlatSpec {
  behavior.of("Uart")

  // These tests are *really* ugly, but they work for now. Need more clarity.

  implicit val platform: Platform = new Platform {
    val id      = "uartspec"
    val clockHz = 3
  }

  it should "receive a byte" in {
    simulate(new Uart(baud = 1)) { c =>
      c.reset.poke(true.B)
      c.clock.step()
      c.reset.poke(false.B)

      // Assert START and hold for one bit.
      c.pins.rx.poke(false.B)

      c.io.rx.valid.expect(false.B)

      c.clock.step(3)

      // Generate a byte and play it out. Ensure we remain not `rdy`.
      val input = (new scala.util.Random).nextInt(256)
      for {
        bitIx <- 0 until 8
        i     <- 0 until 3
      } {
        c.pins.rx.poke(((input & (1 << bitIx)) != 0).B)
        c.io.rx.valid.expect(false.B)
        c.clock.step()
      }

      // Assert STOP and hold for one bit; wait for sync and processing delay (?).
      c.pins.rx.poke(true.B)

      for { i <- 0 until 6 } {
        c.io.rx.valid.expect(false.B, s"step $i")
        c.clock.step()
      }

      // Check received OK.
      c.io.rx.ready.poke(true.B)

      c.io.rx.valid.expect(true.B)
      c.io.rx.bits.byte.expect(input)

      // Ensure we can move to the next byte.
      c.clock.step()
      c.io.rx.ready.poke(false.B)
      c.io.rx.valid.expect(false.B)
    }
  }

  it should "transmit a byte" in {
    simulate(new Uart(baud = 1)) { c =>
      c.reset.poke(true.B)
      c.clock.step()
      c.reset.poke(false.B)

      // Generate a byte and request it to be sent.
      val input = (new scala.util.Random).nextInt(256)
      c.io.tx.bits.poke(input.U)
      c.io.tx.valid.poke(true.B)

      c.pins.tx.expect(true.B)

      c.clock.step()
      c.io.tx.valid.poke(false.B)

      c.pins.tx.expect(true.B)

      // Watch START.
      for { i <- 0 until 3 } {
        c.clock.step()
        c.pins.tx.expect(false.B)
      }

      // Check for each bit in turn.
      for {
        bitIx <- 0 until 8
        i     <- 0 until 3
      } {
        c.clock.step()
        c.pins.tx.expect(((input & (1 << bitIx)) != 0).B)
      }

      // Watch STOP.
      for { i <- 0 until 3 } {
        c.clock.step()
        c.pins.tx.expect(true.B)
      }
    }
  }
}
