package main

import de.prt.gb.CPU

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
  val cpu = CPU()
  val bios = java.io.File(CPU::class.java.getResource("dmg_boot.bin").toURI())
  val rom = java.io.File(CPU::class.java.getResource("Tetris.gb").toURI())
  cpu.ram.load(0, rom.toShortList())
  cpu.ram.load(0, bios.toShortList())
  while (true) {
    if (cpu.PC > 0x1d) {
      println(cpu)
      readLine()
    }
    cpu.tick()
  }
}
