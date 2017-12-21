package main

import de.prt.gb.CPU
import de.prt.gb.RAM
import de.prt.gb.GPU
//import de.prt.gb.Display

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
  RAM.load(0, rom.toShortList())
  RAM.load(0, bios.toShortList())
//  val disp = Display()
//  disp.showWindow()
  while (true) {
    val time = CPU.tick()
    GPU.tick(time)
  }
}
