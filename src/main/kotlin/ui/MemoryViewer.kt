package de.prt.gb.ui
import de.prt.gb.hardware.Machine
import java.awt.Graphics
import java.awt.GridLayout
import java.awt.Dimension
import javax.swing.JPanel
import javax.swing.JList
import javax.swing.JScrollPane
import kotlin.concurrent.thread

private final class MemoryDisplay : JPanel() {
  val layout = GridLayout(0, 16)
  init {
    setLayout(layout)
    thread {
      update()
    }
  }
  fun update() {
    val pc = Machine.getPC()
    val ram = Machine.getRamImage()
    val tfs = ram.map{ num ->
      (num.toInt().toString(16).padStart(2, '0'))
    }
    add(JList(tfs.toTypedArray()))
  }
}

final class MemoryViewer : ShowableFrame("Memory") {
  init {
    val scroller = JScrollPane(MemoryDisplay())
    add(scroller)
  }
}
