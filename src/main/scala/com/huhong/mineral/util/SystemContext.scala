package com.huhong.mineral.util

import scala.tools.nsc.interpreter.ILoop
import com.db4o.ObjectContainer

object SystemContext {
  var sysInterpreter: ILoop = _;

  var configDB: ObjectContainer = _;
  
  
  

}