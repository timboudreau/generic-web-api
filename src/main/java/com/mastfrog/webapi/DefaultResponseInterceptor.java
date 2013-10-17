package com.mastfrog.webapi;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.CharsetUtil;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;

/**
 *
 * @author Tim Boudreau
 */
class DefaultResponseInterceptor extends Interpreter {

    private final ObjectMapper mapper;

    DefaultResponseInterceptor(ObjectMapper mapper) {
        this.mapper = mapper;
    }
    
    private byte[] toBytes(ByteBuf bb) {
        bb.resetReaderIndex();
        byte[] b = new byte[bb.readableBytes()];
        bb.readBytes(b);
        bb.resetReaderIndex();
        return b;
    }
    
    @Override
    protected <T> T interpret(HttpResponseStatus status, HttpHeaders headers, ByteBuf contents, Class<T> as) throws Exception {
        if (as == Void.class) {
            return null;
        }
        contents.resetReaderIndex();
        if (as == String.class || as == CharSequence.class) {
            return as.cast(new String(toBytes(contents), CharsetUtil.UTF_8));
        } else if (byte[].class == as) {
            return as.cast(toBytes(contents));
        } else if (ByteBuf.class == as) {
            return as.cast(contents);
        } else if (as == InputStream.class) {
            return as.cast(new ByteBufInputStream(contents));
        } else if (as == Image.class || as == BufferedImage.class) {
            try (InputStream in = new ByteBufInputStream(contents)) {
                return as.cast(ImageIO.read(in));
            }
        }
        try {
            ByteBufInputStream in = new ByteBufInputStream(contents);
            return mapper.readValue(in, as);
        } catch (JsonMappingException | JsonParseException ex) {
            contents.resetReaderIndex();
            String s = new String(toBytes(contents), CharsetUtil.UTF_8);
            throw new IOException("Bad JSON trying to deserialize " + as + " '" +s + "'", ex);
        }
    }
}
