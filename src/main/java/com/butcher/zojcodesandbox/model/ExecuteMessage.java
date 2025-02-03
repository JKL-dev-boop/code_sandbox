package com.butcher.zojcodesandbox.model;

import lombok.Data;

/** 进程执行信息
 * @author zhoulm54681
 * @date 2025/02/03
 */
@Data
public class ExecuteMessage {

    private Integer exitValue;

    private String message;

    private String errorMessage;

    private Long time;

    private Long memory;
}
