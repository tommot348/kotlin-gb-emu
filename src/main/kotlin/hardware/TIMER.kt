package de.prt.gb.hardware
object TIMER {
  private var clocksTillDiv = 256
  private var clocksTillTimerReset = 1024
  private var clocksTillTimer = 1024
  private var running = false
  fun selectSpeed(nr: Int) {
    clocksTillTimerReset = when (nr and 0b11) {
      0 -> 256
      1 -> 4
      2 -> 16
      3 -> 64
      else -> -1
    }
    running = (nr and 0b100) == 0b100
  }
  fun tick(clock: Int) {
    if (running) {
      clocksTillDiv -= clock
      clocksTillTimer -= clock
    }
    if (clocksTillDiv <= 0) {
      clocksTillDiv = 256
      val div = RAM.getByteAt(0xFF04)
      if (div == 255.toShort()) {
        RAM.setByteAt(0xFF04, 0, true)
      } else {
        RAM.setByteAt(0xFF04, (div + 1).toShort(), true)
      }
    }
    if (clocksTillTimer <= 0) {
      clocksTillTimer = clocksTillTimerReset
      val timer = RAM.getByteAt(0xFF05)
      if (timer == 255.toShort()) {
        RAM.setByteAt(0xFF05, RAM.getByteAt(0xFF06), true)
        val interruptFlags = RAM.getByteAt(0xFF0F)
        RAM.setByteAt(
              0xFF0F,
              (interruptFlags.toInt() or 0b100).toShort(),
              true)
      } else {
        RAM.setByteAt(0xFF05, (timer + 1).toShort(), true)
      }
    }
  }
}
