package com.example.springdemo.controller;

import com.example.springdemo.context.GlobalEventManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 全局事件控制器（管理员功能）
 */
@RestController
@RequestMapping("/api/event")
public class EventController {

    @Autowired
    private GlobalEventManager globalEventManager;

    /**
     * 开启双倍体力活动
     */
    @PostMapping("/double-energy/enable")
    public String enableDoubleEnergy() {
        globalEventManager.enableDoubleEnergyEvent();
        return "双倍体力活动已开启！";
    }

    /**
     * 关闭双倍体力活动
     */
    @PostMapping("/double-energy/disable")
    public String disableDoubleEnergy() {
        globalEventManager.disableDoubleEnergyEvent();
        return "双倍体力活动已关闭！";
    }

    /**
     * 开启免费绘画活动
     */
    @PostMapping("/free-draw/enable")
    public String enableFreeDraw() {
        globalEventManager.enableFreeDrawEvent();
        return "免费绘画活动已开启！";
    }

    /**
     * 关闭免费绘画活动
     */
    @PostMapping("/free-draw/disable")
    public String disableFreeDraw() {
        globalEventManager.disableFreeDrawEvent();
        return "免费绘画活动已关闭！";
    }

    /**
     * 设置自定义公告
     */
    @PostMapping("/announcement")
    public String setAnnouncement(@RequestParam String message) {
        globalEventManager.setAnnouncement(message);
        return "公告已发布：" + message;
    }

    /**
     * 获取当前活动状态
     */
    @GetMapping("/status")
    public Map<String, Object> getEventStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("doubleEnergyEvent", globalEventManager.isDoubleEnergyEvent());
        status.put("freeDrawEvent", globalEventManager.isFreeDrawEvent());
        status.put("announcement", globalEventManager.getAnnouncement());
        return status;
    }
}
