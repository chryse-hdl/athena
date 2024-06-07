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

package ee.hrzn.athena.flashable

import ee.hrzn.chryse.build.CommandRunner._

trait PlatformFlashable {
  var romFlashBase: BigInt
  def romFlashCommand(binPath: String): Seq[String]

  def programRom(binPath: String): Unit =
    runCmd(CmdStep.Program, romFlashCommand(binPath))
}
