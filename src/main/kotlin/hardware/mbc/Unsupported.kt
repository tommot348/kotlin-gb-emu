package de.prt.gb.hardware.mbc
import kotlin.system.exitProcess
class Unsupported : MemoryBankController {
  override fun load(dat: List<Short>) {
    println("Unsupported ROM format")
    exitProcess(-1)
  }
  override fun setByteAt(addr: Int, value: Short) {
    println("Unsupported ROM format")
    exitProcess(-1)
  }
  override fun switchRomBank(nr: Int) {}
  override fun switchRamBank(nr: Int) {}
}
