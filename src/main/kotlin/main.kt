package main

import de.prt.gb.hardware.CPU
import de.prt.gb.hardware.GPU
import de.prt.gb.hardware.RAM
import de.prt.gb.hardware.BIOS
import de.prt.gb.hardware.CARTRIDGE
import de.prt.gb.hardware.TIMER

fun java.io.File.toShortList(): List<Short> =
  this.readBytes().map({
    it
    .toChar()
    .toInt()
    .toString(16)
    .padStart(4, '0')
    .substring(2)
    .toShort(16)
  })
fun main(args: Array<String>) {
  val bios = java.io.File(CPU::class.java.getResource("dmg_boot.bin").toURI())
  val rom = java.io.File(CPU::class.java.getResource("Tetris.gb").toURI())
  println(rom.toShortList()[0x02f1])
  BIOS.load(bios.toShortList())
  CARTRIDGE.load(rom.toShortList())
  while (true) {
    CPU.handleInterrupts()
    val time = CPU.tick()
    if (!RAM.biosMapped) {
      //println(CPU)
    }
    TIMER.tick(time)
    GPU.tick(time)
  }
}
