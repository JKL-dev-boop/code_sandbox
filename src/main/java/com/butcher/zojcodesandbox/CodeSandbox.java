package com.butcher.zojcodesandbox;

import com.butcher.zojcodesandbox.model.ExcuteCodeRequest;
import com.butcher.zojcodesandbox.model.ExcuteCodeResponse;

/**
 * 代码沙箱接口定义
 * @author zhoulm54681
 * @date 2025/02/01
 */
public interface CodeSandbox {
    /**
     * 执行代码
     *
     * @param excuteCodeRequest
     * @return
     */
    ExcuteCodeResponse excuteCode(ExcuteCodeRequest excuteCodeRequest);
}
