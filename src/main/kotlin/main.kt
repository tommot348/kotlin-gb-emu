package main

import de.prt.gb.hardware.CPU
import de.prt.gb.hardware.RAM
import de.prt.gb.hardware.GPU

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
//  RAM.setByteAt(0xFF40, 0b11111111)
//  CPU.setIP(0x0100)
  while (true) {
    val beforeInt = System.nanoTime()
    CPU.handleInterrupts()
    val afterInt = System.nanoTime()
    val beforeCPU = System.nanoTime()
    val time = CPU.tick()
    val afterCPU = System.nanoTime()
    //println(CPU)
    val beforeGPU = System.nanoTime()
    GPU.tick(time)
//    if (beforeGPU % 100 == 0L) Thread.sleep(1)
    val afterGPU = System.nanoTime()
//    println("Int: ${afterInt - beforeInt}\nCPU: ${afterCPU - beforeCPU}\nGPU: ${afterGPU - beforeGPU}")
  }
}
