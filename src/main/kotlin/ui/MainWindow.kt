package de.prt.gb.ui
import kotlin.system.exitProcess
import javax.swing.SwingUtilities
import javax.swing.JFrame
import javax.swing.JMenu
import javax.swing.JMenuBar
import javax.swing.JMenuItem
import javax.swing.JFileChooser
import de.prt.gb.hardware.Machine

final class MainWindow(name: String) : JFrame(name) {
  private val fb = Display()
  init {
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
    setJMenuBar(MainMenu())
    add(fb)
  }
  fun showWindow() {
    SwingUtilities.invokeLater {
      setVisible(true)
    }
  }
  fun hideWindow() {
    SwingUtilities.invokeLater {
      setVisible(false)
    }
  }
}

internal class MainMenu : JMenuBar() {
  init {
    val menu = JMenu("Main")
    val openRom = JMenuItem("open rom")
    openRom.addActionListener {
      val fc = JFileChooser()
      val ret = fc.showOpenDialog(null)
      if (ret == JFileChooser.APPROVE_OPTION) {
        Machine.loadRom(fc.getSelectedFile())
      }
    }
    val start = JMenuItem("start")
    start.addActionListener {
      Machine.start()
    }
    val stop = JMenuItem("stop")
    stop.addActionListener {
      Machine.stop()
    }
    val reset = JMenuItem("reset")
    reset.addActionListener {
      Machine.reset()
    }
    val exit = JMenuItem("exit")
    exit.addActionListener {
      exitProcess(0)
    }
    menu.add(openRom)
    menu.add(start)
    menu.add(stop)
    menu.add(reset)
    menu.add(exit)
    add(menu)
  }
}
