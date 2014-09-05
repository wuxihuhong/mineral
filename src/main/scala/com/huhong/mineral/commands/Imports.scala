package com.huhong.mineral.commands

import com.huhong.mineral.util.SystemContext
import com.huhong.mineral.configs.IndexConfig
import java.util.Date
import com.db4o.query.Predicate
import com.huhong.mineral.util.IndexHelper
import com.huhong.mineral.util.Imports._

object Imports {
  def quit() = {

    if (SystemContext.sysInterpreter != null)
      SystemContext.sysInterpreter.close;

    sys.exit(0);
  }

  def configdb() = {
    SystemContext.configDB;
  }

  def createIndex(name: String, targetDir: String, writeThreadCount: Int = 20) = {
    try {
      IndexHelper.createIndex(name, targetDir, writeThreadCount);
    } catch {
      case e: Exception ⇒ {
        error(e);
      }
    }
  }

  def getConfig(name: String) = {
    IndexHelper.getConfig(name);
  }

  def deleteConfig(name: String) = {
    IndexHelper.deleteConfig(name);
  }

  def showidx() = IndexHelper.listConfigName;
}