package de.prt.gb.hardware.mbc
import de.prt.gb.hardware.RAM
class ROM : MemoryBankController {
  override fun load(dat: List<Short>) {
    RAM.load(0, dat)
  }
  override fun setByteAt(addr: Int, value: Short) {
    when (addr) {
      in 0xA000..0xBFFF -> RAM.setByteAt(addr, value, true)
    }
  }
  override fun switchRomBank(nr: Int) {}
  override fun switchRamBank(nr: Int) {}
}
