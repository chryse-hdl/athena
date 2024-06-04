package ee.hrzn.athena.flashable

import ee.hrzn.chryse.ChryseApp
import ee.hrzn.chryse.ChryseSubcommand
import ee.hrzn.chryse.platform.cxxrtl.CXXRTLPlatform
import ee.hrzn.chryse.tasks.BaseTask

import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.Paths

abstract class SubcommandRom(chryse: ChryseApp)
    extends ChryseSubcommand("rom")
    with BaseTask {
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
    Files.createDirectories(Paths.get(buildDir))

    val content = romContent
    val binPath = s"$buildDir/rom.bin"
    val fos     = new FileOutputStream(binPath)
    fos.write(content, 0, content.length)
    fos.close()
    println(s"wrote $binPath")

    binPath
  }

  def generateCxxrtl(platform: CXXRTLPlatform): Unit = {
    val romFlashBase = platform.asInstanceOf[PlatformFlashable].romFlashBase
    val content      = romContent
    writePath(s"$buildDir/rom.cc") { wr =>
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

      platform.asInstanceOf[PlatformFlashable].programROM(binPath)
    }
  }
}
