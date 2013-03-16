import java.io.File

import widebase.ui.toolkit.runtime.ConfigLike

new ConfigLike {

  val app = "val app = new widebase.ui.ide.App"

  val plugins = Array[File](
    new File("plugin/widebase.ui.ide.cli/Launch.scala"),
    new File("plugin/widebase.ui.ide.editor/Launch.scala"))

}

