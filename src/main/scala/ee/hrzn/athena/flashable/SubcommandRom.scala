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

import ee.hrzn.chryse.ChryseApp
import ee.hrzn.chryse.ChryseSubcommand
import ee.hrzn.chryse.build.writePath
import ee.hrzn.chryse.platform.cxxrtl.CxxrtlPlatform

import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.Path

abstract class SubcommandRom(chryse: ChryseApp)
    extends ChryseSubcommand("rom") {
  banner("Build the ROM image, and optionally program it.")
  val program =
    if (chryse.targetPlatforms.length > 1)
      choice(
        chryse.targetPlatforms.map(_.id),
        name = "program",
        argName = "board",
        descr = s"Program the ROM onto the board.", // + " Choices: ..."
      )
    else
      opt[Boolean](descr =
        s"Program the ROM onto ${chryse.targetPlatforms(0).id}",
      )

  def romContent: Array[Byte]

  private def generate(): String = {
    val buildDir = "build"

    Files.createDirectories(Path.of(buildDir))

    val content = romContent
    val binPath = s"$buildDir/rom.bin"
    val fos     = new FileOutputStream(binPath)
    fos.write(content, 0, content.length)
    fos.close()
    println(s"wrote $binPath")

    binPath
  }

  def generateCxxrtl(platform: CxxrtlPlatform): Unit = {
    val romFlashBase = platform.asInstanceOf[PlatformFlashable].romFlashBase
    val content      = romContent
    writePath(s"${platform.buildDir}/rom.cc") { wr =>
      wr.print("const uint8_t spi_flash_content[] = \"");
      wr.print(content.map(b => f"\\$b%03o").mkString)
      wr.println("\";");
      wr.println(f"const uint32_t spi_flash_base = 0x$romFlashBase%x;");
      wr.println(f"const uint32_t spi_flash_length = 0x${content.length}%x;");
    }
  }

  def execute() = {
    val binPath = generate()

    if (program.isDefined) {
      val platform =
        if (chryse.targetPlatforms.length > 1)
          chryse.targetPlatforms
            .find(_.id == program().asInstanceOf[String])
            .get
        else
          chryse.targetPlatforms(0)

      platform.asInstanceOf[PlatformFlashable].programRom(binPath)
    }
  }
}
