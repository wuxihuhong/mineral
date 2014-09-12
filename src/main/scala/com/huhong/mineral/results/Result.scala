package com.huhong.mineral.results

import scala.beans.BeanProperty
import org.apache.lucene.document.Document
import net.sf.cglib.beans.BeanGenerator
import scala.collection.JavaConverters._
import scala.collection.JavaConversions._
import net.sf.cglib.beans.BeanMap
import org.apache.lucene.index.IndexableFieldType
import org.apache.lucene.index.FieldInfo

@serializable
trait DynamicResult {

  @BeanProperty
  var score: Float = _;

}

object Result {

  
  def generateBean(doc: Document, score: Float): Result = {
    val generator = new BeanGenerator;

    generator.setSuperclass(classOf[Result]);

    doc.getFields() foreach { f ⇒
      {
        f.fieldType()
        generator.addProperty(f.name(), classOf[String]);

      }
    };

    val ret = generator.create().asInstanceOf[Result];

    ret.score = score;
    val bm = BeanMap.create(ret);
    doc.getFields() foreach { f ⇒
      {
        bm.put(f.name(), f.stringValue())

      }
    };

    ret;
  }
}

class Result extends DynamicResult {

}
