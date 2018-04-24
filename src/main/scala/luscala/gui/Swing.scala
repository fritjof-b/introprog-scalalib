package luscala.gui

object Swing {

  def runInSwingThread(callback: => Unit): Unit =
    javax.swing.SwingUtilities.invokeLater(() => callback)

  def apply(callback: => Unit): Unit = runInSwingThread(callback)

  def installedLookAndFeels: Vector[String] =
    javax.swing.UIManager.getInstalledLookAndFeels.toVector.map(_.getClassName)

  def findLookAndFeel(partOfName: String): Option[String] =
    installedLookAndFeels.find(_.toLowerCase contains partOfName)

  def isOS(partOfName: String): Boolean =
    scala.sys.props("os.name").toLowerCase.contains(partOfName.toLowerCase)

  private var isInit = false

  def init(): Unit = if (!isInit) {
    setPlatformSpecificLookAndFeel()
    isInit = true
  }

  def setPlatformSpecificLookAndFeel(): Unit = {
    import javax.swing.UIManager.setLookAndFeel
    if (isOS("linux")) findLookAndFeel("gtk").foreach(setLookAndFeel)
    else if (isOS("win")) findLookAndFeel("win").foreach(setLookAndFeel)
    else if (isOS("mac")) findLookAndFeel("apple").foreach(setLookAndFeel)
    else javax.swing.UIManager.setLookAndFeel(
      javax.swing.UIManager.getSystemLookAndFeelClassName()
    )
  }

  def fileDialog(buttonText: String = "Open", startDir: String = "~"): String = {
    val fs = new javax.swing.JFileChooser(new java.io.File(startDir))
    fs.showDialog(null, buttonText) match {
      case 0 => Option(fs.getSelectedFile.toString).getOrElse("")
      case _ => ""
    }
  }

  def invertColor(c: java.awt.Color): java.awt.Color =
    new java.awt.Color(255 - c.getRed, 255 - c.getGreen, 255 - c.getBlue)

  class CanvasPanel(
    val initWidth: Int,
    val initHeight: Int,
    var backgroundColor: java.awt.Color = java.awt.Color.BLACK,
    var lineColor: java.awt.Color =  java.awt.Color.WHITE,
    var lineWidth: Int = 1,
    var textSize: Int = 20
  ) extends javax.swing.JPanel {
    val img: java.awt.image.BufferedImage = java.awt.GraphicsEnvironment
      .getLocalGraphicsEnvironment
      .getDefaultScreenDevice
      .getDefaultConfiguration
      .createCompatibleImage(initWidth, initHeight, java.awt.Transparency.OPAQUE)

    setBackground(backgroundColor)
		setDoubleBuffered(true)
		setPreferredSize(new java.awt.Dimension(initWidth, initHeight))
		setMinimumSize(new java.awt.Dimension(initWidth, initHeight))
		setMaximumSize(new java.awt.Dimension(initWidth, initHeight))
		clear()

    override def paintComponent(g: java.awt.Graphics): Unit = g.drawImage(img, 0, 0, this)

  	override def imageUpdate(img: java.awt.Image, infoFlags: Int, x: Int, y: Int, width: Int, height: Int): Boolean = {
  		repaint()
      true
  	}

  	def clear(): Unit = {
  		val g = img.createGraphics()
  		g.setColor(backgroundColor)
  		g.fillRect(0, 0, initWidth, initHeight)
  		repaint()
  	}

  	def line(x1: Int, y1: Int, x2: Int, y2: Int): Unit = {
      import java.awt.BasicStroke
  		val g = img.createGraphics()
  		g.setColor(lineColor)
      val s = new BasicStroke(lineWidth.toFloat, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER)
  		g.setStroke(s)
  		g.drawLine(x1, y1, x2, y2)
  		repaint()
  	}

  	def drawString(txt: String, x: Int, y: Int): Unit = {
  		val g = img.createGraphics()
  		g.setColor(lineColor)
      g.setRenderingHint(
        java.awt.RenderingHints.KEY_TEXT_ANTIALIASING,
        java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
      val f = g.getFont
      g.setFont(new java.awt.Font(f.getName,f.getStyle,textSize))
  		g.drawString(txt, x, y)
  		repaint()
  	}
  }

  object screen {
    def device = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment.getDefaultScreenDevice

    def isSupported = device.isFullScreenSupported

    def isFullScreen = device.getFullScreenWindow != null

    def setUndecorated(w: java.awt.Window, state: Boolean): Unit =  w match {
      case f: javax.swing.JFrame =>
        f.dispose()
        f.setUndecorated(state)
        f.pack()
        f.setVisible(true)
      case _ =>
    }

    def exitFullScreen(w: java.awt.Window): Unit =
      if (isSupported) {
        device.setFullScreenWindow(null)
        setUndecorated(w, false)
      }

   def enterFullScreen(w: java.awt.Window): Unit =
     if (isSupported) {
       setUndecorated(w, true)
       device.setFullScreenWindow(w)
     }

    def toggleFullScreen(w: java.awt.Window): Unit =
      if (isFullScreen) exitFullScreen(w) else enterFullScreen(w)

    def toggleDecorations(w: java.awt.Window): Unit = w match {
      case f: javax.swing.JFrame =>
        if (f.isUndecorated) setUndecorated(f, false)
        else setUndecorated(f, true)
      case _ =>
    }
  }
}
