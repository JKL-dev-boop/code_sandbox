package com.butcher.zojcodesandbox.utils;

import cn.hutool.core.date.StopWatch;
import com.butcher.zojcodesandbox.model.ExecuteMessage;
import com.butcher.zojcodesandbox.security.DenySecurityManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * 进程工具类
 *
 * @author zhoulm54681
 * @date 2025/02/03
 */
public class ProcessUtils {

    /**
     * 执行进程并获取执行信息
     * @author zhoulm54681
     * @date 2025/02/03
     */
    public static ExecuteMessage runProcessAndGetMessages(Process runProcess, String opName) {
//        System.setSecurityManager(new DenySecurityManager());
        ExecuteMessage executeMessage = new ExecuteMessage();
        // 等待编译完成 获取错误码
        int exitValue = 0;
        StringBuilder output = new StringBuilder();
        try {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            exitValue = runProcess.waitFor();
            if (exitValue != 0) {
                // 获取编译输出
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(runProcess.getInputStream()));
                //逐行读取输出结果
                String compileOutputLine;
                while ((compileOutputLine = bufferedReader.readLine()) != null) {
                    output.append(compileOutputLine);
                    System.out.println(compileOutputLine);
                }
                // 异常退出
                StringBuilder errorOutput = new StringBuilder();
                System.out.println(opName + "操作失败, 错误码：" + exitValue);
                // 获取编译错误输出
                BufferedReader errorBufferedReader = new BufferedReader(new InputStreamReader(runProcess.getErrorStream()));
                //逐行读取输出结果
                String compileErrorOutputLine;
                while ((compileErrorOutputLine = errorBufferedReader.readLine()) != null) {
                    errorOutput.append(compileErrorOutputLine);
                    System.out.println(compileErrorOutputLine);
                }
                executeMessage.setErrorMessage(errorOutput.toString());
            } else {
                // 正常退出
                System.out.println(opName + "成功");
                // 获取编译输出
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(runProcess.getInputStream()));
                //逐行读取输出结果
                String compileOutputLine = null;
                while ((compileOutputLine = bufferedReader.readLine()) != null) {
                    output.append(compileOutputLine);
                    System.out.println(compileOutputLine);
                }
            }
            stopWatch.stop();
            long lastTaskTimeMillis = stopWatch.getLastTaskTimeMillis();
            executeMessage.setTime(lastTaskTimeMillis);
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }

        executeMessage.setMessage(output.toString());
        executeMessage.setExitValue(exitValue);
        return executeMessage;
    }
}
