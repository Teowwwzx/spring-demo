package com.example.springdemo.context;

import org.springframework.stereotype.Component;

/**
 * 全局事件管理器
 * 使用 volatile 关键字保证多线程环境下的可见性
 * 例如：管理员开启"双倍体力补偿"活动时，使用 volatile 保证这个开关状态对所有服务器线程立即可见
 */
@Component
public class GlobalEventManager {

    // volatile：保证所有线程能立即看到最新值
    private volatile boolean doubleEnergyEvent = false;
    private volatile boolean freeDrawEvent = false;
    private volatile String announcement = "";

    /**
     * 开启双倍体力活动
     */
    public void enableDoubleEnergyEvent() {
        this.doubleEnergyEvent = true;
        this.announcement = "🎉 双倍体力活动已开启！";
        System.out.println(">>> 全服公告: " + announcement);
    }

    /**
     * 关闭双倍体力活动
     */
    public void disableDoubleEnergyEvent() {
        this.doubleEnergyEvent = false;
        this.announcement = "";
    }

    /**
     * 开启免费绘画活动
     */
    public void enableFreeDrawEvent() {
        this.freeDrawEvent = true;
        this.announcement = "🎨 免费绘画活动已开启！不消耗体力！";
        System.out.println(">>> 全服公告: " + announcement);
    }

    /**
     * 关闭免费绘画活动
     */
    public void disableFreeDrawEvent() {
        this.freeDrawEvent = false;
        this.announcement = "";
    }

    /**
     * 检查是否有双倍体力活动
     */
    public boolean isDoubleEnergyEvent() {
        return doubleEnergyEvent;
    }

    /**
     * 检查是否有免费绘画活动
     */
    public boolean isFreeDrawEvent() {
        return freeDrawEvent;
    }

    /**
     * 获取当前公告
     */
    public String getAnnouncement() {
        return announcement;
    }

    /**
     * 设置自定义公告
     */
    public void setAnnouncement(String announcement) {
        this.announcement = announcement;
        System.out.println(">>> 全服公告: " + announcement);
    }
}
