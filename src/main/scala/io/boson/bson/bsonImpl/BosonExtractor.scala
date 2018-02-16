package io.boson.bson.bsonImpl

import java.nio.ByteBuffer
import java.util.concurrent.CompletableFuture
import io.boson.bson
import io.boson.bson.bsonValue
import io.boson.bson.bsonPath.{Interpreter, Program, TinyLanguage}
import io.boson.bson.bsonValue.BsValue


class BosonExtractor[T](expression: String, extractFunction: java.util.function.Consumer[T]) extends bson.Boson {

  private def callParse(boson: BosonImpl, expression: String): io.boson.bson.bsonValue.BsValue = {
    val parser = new TinyLanguage
    try {
      parser.parseAll(parser.program, expression) match {
        case parser.Success(r, _) =>
          new Interpreter(boson, r.asInstanceOf[Program]).run()
        case parser.Error(msg, _) => bsonValue.BsObject.toBson(msg)
        case parser.Failure(msg, _) => bsonValue.BsObject.toBson(msg)
      }
    } catch {
      case e: RuntimeException =>
        bsonValue.BsObject.toBson(e.getMessage)
    }
  }

  override def go(bsonByteEncoding: Array[Byte]): CompletableFuture[Array[Byte]] = {
    val future: CompletableFuture[Array[Byte]] =
      CompletableFuture.supplyAsync(() => {
        val boson: io.boson.bson.bsonImpl.BosonImpl = new BosonImpl(byteArray = Option(bsonByteEncoding))
        callParse(boson, expression) match {
          case (res: BsValue) =>
            extractFunction.accept(res.asInstanceOf[T])
          case _ => throw new RuntimeException("BosonExtractor -> go() default case!!!")
        }
        bsonByteEncoding
      })
    future
  }

  override def go(bsonByteBufferEncoding: ByteBuffer): CompletableFuture[ByteBuffer] = {
    val future: CompletableFuture[ByteBuffer] =
      CompletableFuture.supplyAsync(() => {
        val boson: io.boson.bson.bsonImpl.BosonImpl = new BosonImpl(javaByteBuf = Option(bsonByteBufferEncoding))
          callParse(boson,expression) match {
            case (res: BsValue) =>
              extractFunction.accept(res.asInstanceOf[T])
            case _ => throw new RuntimeException("BosonExtractor -> go() default case!!!")
          }
        bsonByteBufferEncoding
      })
    future
  }

  override def fuse(boson: bson.Boson): bson.Boson = new BosonFuse(this,boson)



}
