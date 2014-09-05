package com.huhong.mineral.index

import akka.actor.Actor
import com.huhong.mineral.messages._
trait IndexHandler {
  this: Actor ⇒

}

class DefaultIndexHandler extends IndexHandler with Actor {
  def receive = {
    case doc: Document ⇒ {

    }

  }
}

