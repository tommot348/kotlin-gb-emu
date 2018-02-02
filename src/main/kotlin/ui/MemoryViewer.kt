package de.prt.gb.ui
import de.prt.gb.hardware.Machine
import javax.swing.JList
import javax.swing.JScrollPane

final class MemoryViewer : ShowableFrame("Memory") {
  init {
    val ram = Machine.getRamImage()
    val items = ram.map { it.toInt().toString(16).padStart(2, '0') }
    val list = JList(items.toTypedArray())
    list.setPrototypeCellValue("00 ")
    list.setLayoutOrientation(JList.HORIZONTAL_WRAP)
    val scroller = JScrollPane(list)
    add(scroller)
  }
}
