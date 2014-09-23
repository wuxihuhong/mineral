package com.huhong.mineral.util

import com.huhong.mineral.error.MineralExpcetion
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import java.io.ByteArrayInputStream
import org.apache.commons.io.IOUtils
import java.io.ByteArrayOutputStream
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.lucene.document.Document
import java.io.StringWriter
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.json.UTF8JsonGenerator
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import org.apache.lucene.index.IndexableField
import java.io.OutputStream
import com.fasterxml.jackson.core.JsonEncoding
import com.fasterxml.jackson.databind.SerializationFeature
import com.cedarsoftware.util.io.JsonWriter
import org.apache.solr.common.SolrDocument

object Imports {

  private val objectMapper = new ObjectMapper;
  val rootObjectMapper = new ObjectMapper;

  def sleep(millis: Long) = {
    Thread.sleep(millis);
  }

  def pause()={
    System.in.read();
  }
  def error(e: Exception) = System.err.println(e.getMessage());

  def printlnjson(json: String) = {
    println(JsonWriter.formatJson(json));
  }
  @throws(classOf[MineralExpcetion])
  def throws(errorCode: Int, msg: String = null, cause: Throwable = null,
    enableSuppression: Boolean = true, writableStackTrace: Boolean = true) = {
    throw new MineralExpcetion(errorCode, msg, cause, enableSuppression, writableStackTrace);
  }

  def bytesToJson(buff: Array[Byte]): String = {

    val bis = new ByteArrayInputStream(buff);
    val gzipin = new GzipCompressorInputStream(bis);
    val bos = new ByteArrayOutputStream;
    IOUtils.copy(gzipin, bos);
    val bs = bos.toByteArray();

    bos.close();
    bis.close();
    gzipin.close();
    new String(bs);
  }

  private def getFieldValue(f: IndexableField): Any = {
    var ret: Any = f.stringValue();
    if (ret == null) {
      ret = f.numericValue();
    }
    if (ret == null) {
      ret = f.binaryValue();
    }
    if (ret == null) {
      val reader = f.readerValue();
      if (reader != null) {
        val bos = new ByteArrayOutputStream;
        IOUtils.copy(reader, bos);
        ret = bos.toByteArray();
        bos.close();
      }
    }

    ret;
  }
  def docsToJson(docs: Array[(Document, Float)], out: OutputStream, hits: Long, consuming: Long): Unit = {
    val sw = new StringWriter;
    val gen = objectMapper.getJsonFactory().createGenerator(out, JsonEncoding.UTF8);
    gen.writeStartObject();
    gen.writeNumberField("hits", hits);
    gen.writeNumberField("consuming", consuming);
    if (docs == null) {
      gen.writeNull();
    } else {
      gen.writeArrayFieldStart("datas")

      docs.foreach(doc ⇒ {

        gen.writeStartObject();
        gen.writeNumberField("score", doc._2);
        doc._1.getFields().foreach(f ⇒ {
          val value = getFieldValue(f);
          gen.writeObjectField(f.name(), value);
        })

        gen.writeEndObject();

      })
      gen.writeEndArray();

    }
    gen.writeEndObject();
    gen.flush();
    gen.close;
  }
  
  
}