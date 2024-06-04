package ee.hrzn.athena.flashable

import ee.hrzn.chryse.tasks.BaseTask

trait PlatformFlashable {
  var romFlashBase: BigInt
  def romFlashCommand(binPath: String): Seq[String]

  def programROM(binPath: String): Unit =
    programROMImpl(binPath)

  object programROMImpl extends BaseTask {
    def apply(binPath: String): Unit =
      runCmd(CmdStepProgram, romFlashCommand(binPath))
  }
}
