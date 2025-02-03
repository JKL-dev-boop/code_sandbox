package com.butcher.zojcodesandbox.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 判题信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class JudgeInfo {

    /**
     * 程序执行信息
     */
    private String message;

    /**
     * 消耗内存（KB）
     */
    private Long memory;

    /**
     * 消耗时间 （s）
     */
    private Long time;
}
