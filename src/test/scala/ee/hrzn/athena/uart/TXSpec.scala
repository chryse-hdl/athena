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
import org.scalatest.flatspec.AnyFlatSpec

class TXSpec extends AnyFlatSpec {
  behavior.of("TX")

  it should "transmit a byte" in {
    simulate(new TX(divisor = 3)) { c =>
      // Note that poked inputs take effect *immediately* on combinatorial
      // circuits. We want to poke as the first thing we do in any simulated
      // cycle, as if responding to the last cycle.  We do this before any
      // expects -- otherwise we might not observe comb changes correctly.
      c.reset.poke(true.B)
      c.clock.step()
      c.reset.poke(false.B)

      c.io.bits.poke("b00110101".U)
      c.io.valid.poke(true.B)

      c.pin.expect(true.B)
      c.io.ready.expect(true.B)

      c.clock.step()

      c.io.valid.poke(false.B)

      for {
        bit <- Seq(0, 1, 0, 1, 0, 1, 1, 0, 0, 1)
        i   <- 0 until 3
      } {
        c.pin.expect((bit == 1).B)
        c.clock.step()
      }
    }
  }
}
