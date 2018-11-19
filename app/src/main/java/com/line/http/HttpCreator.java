package com.line.http;

import android.util.Log;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Created by line on 2018/11/19.
 */

public class HttpCreator {

    private InvocationHandler invocationHandler = new InvocationHandler(){

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String methodName = method.getName();
            Log.d("http", "invoke:" + methodName);
            return null;
        }
    };

    public <T> T create(Class<T> service){
        return (T) Proxy.newProxyInstance(service.getClassLoader(), new Class<?>[]{service}, invocationHandler);
    }

}
