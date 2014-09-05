package com.huhong.mineral.error

class MineralExpcetion(val errorCode: Int, msg: String = null, cause: Throwable = null,
  enableSuppression: Boolean = true, writableStackTrace: Boolean = true)
  extends Exception(msg, cause, enableSuppression, writableStackTrace) {

}