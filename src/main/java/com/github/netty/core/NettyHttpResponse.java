package com.github.netty.core;

import com.github.netty.core.constants.VersionConstants;
import com.github.netty.core.support.Recyclable;
import com.github.netty.core.support.AbstractRecycler;
import com.github.netty.core.support.Wrapper;
import com.github.netty.core.util.ReflectUtil;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * 用于兼容 netty4 与netty5
 * @author acer01
 * 2018/7/28/028
 */
public class NettyHttpResponse implements HttpResponse,Wrapper<HttpResponse>,Recyclable {

    private static final AbstractRecycler<NettyHttpResponse> RECYCLER = new AbstractRecycler<NettyHttpResponse>() {
        @Override
        protected NettyHttpResponse newInstance() {
            return new NettyHttpResponse();
        }
    };

    private HttpResponse source;
    private Class sourceClass;
    private final Object lock = new Object();

    private List<Method> getStatusMethodList;
    private List<Method> getProtocolVersionMethodList;
    private List<Method> getDecoderResultMethodList;

    private NettyHttpResponse() {

    }

    public static NettyHttpResponse newInstance(HttpResponse source) {
        Objects.requireNonNull(source);

        NettyHttpResponse instance = RECYCLER.get();
        instance.wrap(source);
        return instance;
    }

    public HttpResponseStatus getStatus() {
        if(!VersionConstants.isEnableVersionAdapter()){
            return source.getStatus();
        }
        if(getStatusMethodList == null){
            synchronized (lock) {
                if(getStatusMethodList == null) {
                    getStatusMethodList = Arrays.asList(
                            ReflectUtil.getAccessibleMethodByName(sourceClass, "getStatus",0),
                            ReflectUtil.getAccessibleMethodByName(sourceClass, "status",0)
                    );
                }
            }
        }
        return (HttpResponseStatus) ReflectUtil.invokeMethodOnce(source,getStatusMethodList);
    }

    public HttpVersion getProtocolVersion() {
        if(!VersionConstants.isEnableVersionAdapter()){
            return source.getProtocolVersion();
        }
        if(getProtocolVersionMethodList == null){
            synchronized (lock) {
                if(getProtocolVersionMethodList == null) {
                    getProtocolVersionMethodList = Arrays.asList(
                            ReflectUtil.getAccessibleMethodByName(sourceClass, "getProtocolVersion",0),
                            ReflectUtil.getAccessibleMethodByName(sourceClass, "protocolVersion",0)
                    );
                }
            }
        }
        return (HttpVersion) ReflectUtil.invokeMethodOnce(source,getProtocolVersionMethodList);
    }

    public DecoderResult getDecoderResult() {
        if(!VersionConstants.isEnableVersionAdapter()){
            return source.getDecoderResult();
        }
        if(getDecoderResultMethodList == null){
            synchronized (lock) {
                if(getDecoderResultMethodList == null) {
                    getDecoderResultMethodList = Arrays.asList(
                            ReflectUtil.getAccessibleMethodByName(sourceClass, "getDecoderResult", 0),
                            ReflectUtil.getAccessibleMethodByName(sourceClass, "decoderResult", 0)
                    );
                }
            }
        }
        return (DecoderResult) ReflectUtil.invokeMethodOnce(source,getDecoderResultMethodList);
    }

    public HttpResponseStatus status() {
        return getStatus();
    }

    public HttpVersion protocolVersion() {
        return getProtocolVersion();
    }

    public DecoderResult decoderResult() {
        return getDecoderResult();
    }

    @Override
    public HttpResponse setStatus(HttpResponseStatus status) {
        source.setStatus(status);
        return this;
    }

    @Override
    public HttpResponse setProtocolVersion(HttpVersion version) {
        source.setProtocolVersion(version);
        return this;
    }

    @Override
    public HttpHeaders headers() {
        return source.headers();
    }

    @Override
    public void setDecoderResult(DecoderResult result) {
        source.setDecoderResult(result);
    }

    @Override
    public void wrap(HttpResponse source) {
        Objects.requireNonNull(source);
        this.source = source;
        this.sourceClass = source.getClass();
    }

    @Override
    public HttpResponse unwrap() {
        return source;
    }

    @Override
    public int hashCode() {
        return source.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return source.equals(obj);
    }

    @Override
    public String toString() {
        return "NettyHttpResponse{" +
                "sourceClass=" + sourceClass +
                '}';
    }

    @Override
    public void recycle() {
        this.source = null;
        this.sourceClass = null;
        RECYCLER.recycle(this);
    }

}
