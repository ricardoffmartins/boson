package io.zink.boson.json.jsonImpl;

import io.zink.boson.bson.Boson;
import io.zink.boson.bson.bsonImpl.BosonImpl;
import io.zink.boson.bson.bsonPath.Interpreter;
import io.zink.boson.bson.bsonPath.Program;
import io.zink.boson.bson.bsonPath.TinyLanguage;
import io.zink.boson.bson.bsonValue.BsException$;
import io.zink.boson.bson.bsonValue.BsObject$;
import io.zink.boson.bson.bsonValue.BsValue;
import io.zink.boson.bson.bsonValue.Writes$;

import scala.Function1;
import scala.Option;
import scala.util.parsing.combinator.Parsers;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class BosonValidate<T> implements Boson {

    private String expression;
    private Consumer<T> validateFunction;

    public BosonValidate(String expression, Consumer<T> validateFunction) {
        this.expression = expression;
        this.validateFunction = validateFunction;
    }

    private Function1<String, BsValue> writer = (str) -> BsException$.MODULE$.apply(str);

    private BsValue callParse(BosonImpl boson, String expression){
        TinyLanguage parser = new TinyLanguage();
        try{
            Parsers.ParseResult pr = parser.parseAll(parser.program(), expression);
            if(pr.successful()){
                Interpreter interpreter = new Interpreter<>(boson, (Program) pr.get(), Option.empty());
                return interpreter.run();
            }else{
                return BsObject$.MODULE$.toBson("Failure/Error parsing!", Writes$.MODULE$.apply(writer));
            }
        }catch (RuntimeException e){
            return BsObject$.MODULE$.toBson(e.getMessage(), Writes$.MODULE$.apply(writer));
        }
    };

    @Override
    public CompletableFuture<byte[]> go(byte[] bsonByteEncoding) {
        CompletableFuture<byte[]> future =
                CompletableFuture.supplyAsync(() -> {
                    Option<byte[]> opt = Option.apply(bsonByteEncoding);
                    Option e = Option.empty();
                    BosonImpl boson = new BosonImpl(opt, e,e);
                    BsValue value = callParse(boson, expression);
                    if(value.getClass().equals(BsValue.class)){
                        validateFunction.accept((T)value);
                    }else{
                        throw new RuntimeException("BosonExtractor -> go() default case!!!");
                    }
                    return bsonByteEncoding;
                });
        return future;
    }

    @Override
    public CompletableFuture<ByteBuffer> go(ByteBuffer bsonByteBufferEncoding) {
        CompletableFuture<ByteBuffer> future =
                CompletableFuture.supplyAsync(() -> {
                    Option<ByteBuffer> opt = Option.apply(bsonByteBufferEncoding);
                    Option e = Option.empty();
                    BosonImpl boson = new BosonImpl(e,opt,e);
                    BsValue value = callParse(boson, expression);
                    if(value.getClass().equals(BsValue.class)){
                        validateFunction.accept((T)value);
                    }else{
                        throw new RuntimeException("BosonExtractor -> go() default case!!!");
                    }
                    return bsonByteBufferEncoding;
                });
        return future;
    }

    @Override
    public Boson fuse(Boson boson) {
        return null;
    }
}
