package com.example.springdemo.context;

/**
 * ThreadLocal 用户上下文持有者
 * 每个请求线程进入后，将用户信息存入 ThreadLocal
 * 这样在后续的 Service 层就不需要层层传递 username
 */
public class UserContextHolder {

    // ThreadLocal：每个线程都有自己独立的变量副本
    private static final ThreadLocal<String> userContext = new ThreadLocal<>();

    /**
     * 设置当前线程的用户名
     */
    public static void setUser(String username) {
        userContext.set(username);
    }

    /**
     * 获取当前线程的用户名
     */
    public static String getUser() {
        return userContext.get();
    }

    /**
     * 清除当前线程的用户信息（请求结束时必须调用，防止内存泄漏）
     */
    public static void clear() {
        userContext.remove();
    }
}
