package de.prt.gb.ui
import kotlin.system.exitProcess
import javax.swing.JFrame
import javax.swing.JMenu
import javax.swing.JMenuBar
import javax.swing.JMenuItem
import javax.swing.JFileChooser
import de.prt.gb.hardware.Machine

final class MainWindow(name: String) : ShowableFrame(name) {
  init {
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
    setJMenuBar(MainMenu())
  }
}

internal class MainMenu : JMenuBar() {
  init {
    val menu = JMenu("Main")
    val debugMenu = JMenu("Debug")
    val openBios = JMenuItem("open bios")
    openBios.addActionListener {
      val fc = JFileChooser()
      val ret = fc.showOpenDialog(null)
      if (ret == JFileChooser.APPROVE_OPTION) {
        Machine.loadBios(fc.getSelectedFile())
      }
    }
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
    val showMemory = JMenuItem("Show Memory")
    showMemory.addActionListener {
      val mem = MemoryViewer()
      mem.showWindow()
    }
    menu.add(openRom)
    menu.add(openBios)
    menu.add(start)
    menu.add(stop)
    menu.add(reset)
    menu.add(exit)
    debugMenu.add(showMemory)
    add(menu)
    add(debugMenu)
  }
}
