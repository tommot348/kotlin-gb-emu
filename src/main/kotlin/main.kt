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
  val rom = java.io.File(CPU::class.java.getResource("dmg_boot.bin").toURI())
  println(rom.readBytes().map({ it.toChar().toInt().toString(16).padStart(4, '0').substring(2) }).reduce({
    a, b -> a + b
  }))
  cpu.ram.load(0, rom.toShortList())
  cpu.ram.load(0xC000, rom.toShortList())
  while (true) {
    println(cpu)
    readLine()
    cpu.tick()
  }
}
