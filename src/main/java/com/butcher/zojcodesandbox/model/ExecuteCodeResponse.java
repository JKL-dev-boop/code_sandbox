package com.butcher.zojcodesandbox.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExecuteCodeResponse {

    /**
     * 输出用例
     */
    private List<String> outputList;

    /**
     * 执行信息
     */
    private String message;

    /**
     * 执行状态
     */
    private String status;

    /**
     * 判题信息
     */
    private JudgeInfo judgeInfo;

}
