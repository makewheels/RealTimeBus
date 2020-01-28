package com.eg.realtimebus.activity.location;

import lombok.Data;

/**
 * 客户端获取最近的公交距离，返回的bean
 *
 * @time 2020-01-28 20:57
 */
@Data
public class DistanceResponse {
    private boolean hasBus;
    private double distance;
}
