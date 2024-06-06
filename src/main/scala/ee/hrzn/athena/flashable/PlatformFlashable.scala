package ee.hrzn.athena.flashable

import ee.hrzn.chryse.tasks.BaseTask

trait PlatformFlashable {
  var romFlashBase: BigInt
  def romFlashCommand(binPath: String): Seq[String]

  def programRom(binPath: String): Unit =
    programRomImpl(binPath)

  object programRomImpl extends BaseTask {
    def apply(binPath: String): Unit =
      runCmd(CmdStepProgram, romFlashCommand(binPath))
  }
}
