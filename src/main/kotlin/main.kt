package main

import de.prt.gb.hardware.Machine

import de.prt.gb.ui.Display
import de.prt.gb.ui.MainWindow
import de.prt.gb.ui.Input

fun main(args: Array<String>) {
  val mainWindow = MainWindow("kotlin_gb_emu")
  val display = Display()
  mainWindow.add(display)
  mainWindow.showWindow()
  val input = Input()
  mainWindow.addKeyListener(input)
  Machine.input = input
  Machine.display = display
}
