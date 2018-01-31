package de.prt.gb.ui
import de.prt.gb.hardware.Machine
import java.awt.Graphics
import kotlin.concurrent.timer

final class MemoryViewer : ShowableFrame("Memory") {
  init {
    timer(period=1000L) {
      println("repaint Memory")
      repaint()
    }
  }
  override fun paint(g: Graphics) {
    val pc = Machine.getPC()
    val ram = Machine.getRamImage()
    ram.forEachIndexed { i, num ->
      g.drawString(num.toInt().toString(16).padStart(2, '0'), (i % 16) * 20, (i / 16) * 20)
    }
  }
}
