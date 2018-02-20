package io.zink.boson.json.jsonImpl;



import io.zink.boson.bson.Boson;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

public class BosonFuse implements Boson {
    private Boson first;
    private Boson second;

    public BosonFuse(Boson first, Boson second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public CompletableFuture<byte[]> go(byte[] bsonByteEncoding) {
        CompletableFuture<byte[]> future =
                CompletableFuture.supplyAsync(() -> {
                   CompletableFuture<byte[]> firstFuture = first.go(bsonByteEncoding);
                   return second.go(firstFuture.join()).join();
                });
        return future;
    }

    @Override
    public CompletableFuture<ByteBuffer> go(ByteBuffer bsonByteBufferEncoding) {
        CompletableFuture<ByteBuffer> future =
                CompletableFuture.supplyAsync(() -> {
                    CompletableFuture<ByteBuffer> firstFuture = first.go(bsonByteBufferEncoding);
                    return second.go(firstFuture.join()).join();
                });
        return future;
    }

    @Override
    public Boson fuse(Boson boson) {
        return new BosonFuse(this,boson);
    }
}
