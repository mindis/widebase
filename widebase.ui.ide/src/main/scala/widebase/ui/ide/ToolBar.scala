package widebase.ui.ide

import javax.swing.ImageIcon

import scala.swing.Button

import widebase.ui.toolkit.Action

/** Tool bar of app frame.
 * 
 * @author myst3r10n
 */
class ToolBar(frame: Frame) extends widebase.ui.toolkit.ToolBar {

  this += "Edit" -> new Button(
    new Action("") {

      def apply {

        frame.viewPane.add

      }

      icon = new ImageIcon(getClass.getResource("/icon/window-new.png"))

    }
  )
}
