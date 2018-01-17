package de.prt.gb.hardware.mbc
interface MemoryBankController {
  fun setByteAt(addr: Int, value: Short)
  fun switchRomBank(nr: Int)
  fun switchRamBank(nr: Int)
  fun load(dat: List<Short>)
}
