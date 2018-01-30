package de.prt.gb.hardware
import kotlin.concurrent.thread
import de.prt.gb.ui.IInput
import de.prt.gb.ui.IDisplay
import java.io.File
fun File.toShortList(): List<Short> =
  this.readBytes().map({
    it
    .toChar()
    .toInt()
    .toString(16)
    .padStart(4, '0')
    .substring(2)
    .toShort(16)
  })
object Machine {
  private var _running = false
  private var romPath = ""
  private var biosPath = ""
  var input: IInput? = null
  var display: IDisplay? = null
  private fun loadFile(file: File): List<Short> {
    CPU.PC = 0
    return (file).toShortList()
  }
  fun loadRom(file: File) {
    romPath = file.getAbsolutePath()
    CARTRIDGE.load(loadFile(file))
  }
  fun loadBios(file: File) {
    biosPath = file.getAbsolutePath()
    BIOS.load(loadFile(file))
  }
  var running: Boolean
    get() {
      return _running
    }
    private set(x: Boolean) {
      _running = x
    }
  fun reset() {
    loadRom(File(romPath))
    loadBios(File(biosPath))
    CPU.PC = 0
  }
  private fun run() {
    thread {
      while (running) {
        tick()
      }
    }
  }
  fun start() {
    running = true
    run()
  }
  fun stop() {
    running = false
  }
  fun tick() {
    CPU.handleInterrupts()
    val time = CPU.tick()
    TIMER.tick(time)
    GPU.tick(time)
  }
}

