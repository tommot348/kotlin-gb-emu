package de.prt.gb.hardware
import de.prt.gb.hardware.mbc.MBC1
import de.prt.gb.hardware.mbc.ROM
import de.prt.gb.hardware.mbc.Unsupported
import de.prt.gb.hardware.mbc.MemoryBankController

internal object CARTRIDGE {
  private var name = ""
  private var mbc: MemoryBankController? = null
  fun load(dat: List<Short>) {
    val title = dat.slice(0x0134..0x0143).map({ it.toChar() }).toString()
    name = title
    mbc = when (dat[0x0147].toInt()) {
      0 -> ROM()
      1 -> MBC1()
      2 -> MBC1()
      3 -> MBC1()
      // 5 -> "MBC2"
      // 6 -> "MBC2+BATTERY"
      8 -> ROM()
      9 -> ROM()
      // 0xF -> "MBC3+TIMER+BATTERY"
      // 0x10 -> "MBC3+TIMER+RAM+BATTERY"
      // 0x11 -> "MBC3"
      // 0x12 -> "MBC3+RAM"
      // 0x13 -> "MBC3+RAM+BATTERY"
      else -> Unsupported()
    }
    mbc?.load(dat)
  }

  fun setByteAt(addr: Int, value: Short) {
    mbc?.setByteAt(addr, value)
  }
}
internal object BIOS {
  private val bios = ArrayList<Short>()
  fun load(dat: List<Short>) {
    bios.addAll(dat)
  }
  fun getByteAt(addr: Int): Short {
    return bios[addr]
  }
}

internal object RAM {
  private val ram = Array(65536, { 0.toShort() })
  var biosMapped = true
  @Synchronized fun getByteAt(addr: Int): Short {
    return when (addr) {
      in 0..0xFF -> if (biosMapped) BIOS.getByteAt(addr) else ram[addr]
      in 0xE000..0xFDFF -> ram[addr - 0x2000]
      0xFF00 -> {
        val mode = (0b00110000 and ram[0xFF00].toInt()) shr 4
        val state = Input.getState(mode)
        state
      }
      else -> ram[addr]
    }
  }
  @Synchronized fun load(addr: Int, dat: List<Short>) {
    dat.forEachIndexed({ i: Int, d: Short -> ram[addr + i] = d })
  }
  @Synchronized fun setByteAt(addr: Int, value: Short, hardware: Boolean = false) {
    if (hardware) {
      ram[addr] = value
    } else {
      when (addr) {
        in 0..0x7FFF -> CARTRIDGE.setByteAt(addr, value)
        in 0xA000..0xBFFF -> {
          CARTRIDGE.setByteAt(addr, value)
        }
        0xFF04 -> ram[0xFF04] = 0
        0xFF44 -> ram[0xFF44] = 0
        0xFF46 -> ((value * 0x100)..((value * 0x100) + 0x9F)).forEachIndexed({ i, curr ->
          ram[0xFE00 + i] = ram[curr]
        })
        0xFF50 -> biosMapped = false
        else -> ram[addr] = value
      }
    }
  }
  fun setWordAt(addr: Int, value: Int) {
    val h = value and 0xFF00
    val l = value and 0xFF
    setByteAt(addr, h.toShort())
    setByteAt(addr + 1, l.toShort())
  }
  override fun toString() = ram.map({ it.toString(16).padStart(2, '0') }).reduceIndexed({
    i, prev, curr ->
      if ((i!= 0) && ((i + 1).rem(16) == 0)) {
        prev+'\n'+curr
      } else {
        prev + curr
      }
    })
}
