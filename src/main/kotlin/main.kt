package main

import de.prt.gb.hardware.CPU
import de.prt.gb.hardware.GPU
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
  BIOS.load(bios.toShortList())
  CARTRIDGE.load(rom.toShortList())
  println(rom.toShortList().get(0x234))
//  RAM.setByteAt(0xFF40, 0b11111111)
//  CPU.setIP(0x0100)
  while (true) {
    CPU.handleInterrupts()
    val time = CPU.tick()
    TIMER.tick(time)
//    println(CPU)
    GPU.tick(time)
  }
}
