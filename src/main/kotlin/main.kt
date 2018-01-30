package main

import de.prt.gb.hardware.CPU
import de.prt.gb.hardware.GPU
import de.prt.gb.hardware.BIOS
import de.prt.gb.hardware.CARTRIDGE
import de.prt.gb.hardware.TIMER
import de.prt.gb.hardware.RAM

import de.prt.gb.ui.Display
import de.prt.gb.ui.Input

import kotlin.system.exitProcess

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
  if (args.size == 0) {
    exitProcess(-1)
  }
  val bios = java.io.File(CPU::class.java.getResource("dmg_boot.bin").toURI())
  val rom = java.io.File(args[0])
  BIOS.load(bios.toShortList())
  CARTRIDGE.load(rom.toShortList())
  val display = Display("kotlin_gb_emu")
  val input = Input()
  display.addKeyListener(input)
  GPU.setOutput(display)
  RAM.setInput(input)
  while (true) {
    try {
      CPU.handleInterrupts()
      val time = CPU.tick()
      TIMER.tick(time)
      GPU.tick(time)
    } catch (e: Exception) {
      e.printStackTrace()
      println(CPU)
      exitProcess(-1)
    }
  }
}
