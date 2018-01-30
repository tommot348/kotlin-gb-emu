package de.prt.gb.ui
import javax.swing.JPanel
import javax.swing.BorderFactory
import java.awt.Dimension
import java.awt.Color
import java.awt.Graphics

internal final class Display : JPanel(), IDisplay {
  val dat = ArrayList<List<Int>>()
  init {
    setBorder(BorderFactory.createLineBorder(Color.black))
    setDoubleBuffered(true)
  }
  override fun update(lines: ArrayList<List<Int>>) {
    dat.removeAll({ true })
    dat.addAll(lines)
    repaint()
  }
  override fun getPreferredSize(): Dimension {
    return Dimension(640, 576)
  }
  override fun paintComponent(g: Graphics) {
    super.paintComponent(g)
    dat.forEachIndexed({ y, line ->
      line.forEachIndexed({ x, color ->
        when (color) {
          3 -> g.setColor(Color.BLACK)
          2 -> g.setColor(Color.DARK_GRAY)
          1 -> g.setColor(Color.LIGHT_GRAY)
          0 -> g.setColor(Color.WHITE)
        }
        g.fillRect(x * 4, y * 4, 4, 4)
      })
    })
  }
}
