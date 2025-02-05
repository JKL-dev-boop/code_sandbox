package com.butcher.zojcodesandbox;

import com.butcher.zojcodesandbox.model.ExecuteCodeRequest;
import com.butcher.zojcodesandbox.model.ExecuteCodeResponse;

/**
 * 代码沙箱接口定义
 * @author zhoulm54681
 * @date 2025/02/01
 */
public interface CodeSandbox {
    /**
     * 执行代码
     *
     * @param executeCodeRequest
     * @return
     */
    ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest);
}
