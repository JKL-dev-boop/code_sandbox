package com.butcher.zojcodesandbox;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import cn.hutool.dfa.WordTree;
import com.butcher.zojcodesandbox.model.ExcuteCodeRequest;
import com.butcher.zojcodesandbox.model.ExcuteCodeResponse;
import com.butcher.zojcodesandbox.model.ExecuteMessage;
import com.butcher.zojcodesandbox.model.JudgeInfo;
import com.butcher.zojcodesandbox.security.DenySecurityManager;
import com.butcher.zojcodesandbox.utils.ProcessUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 代码沙箱接口实现(java 原生）
 * @author zhoulm54681
 * @date 2025/02/03
 */
public class JavaNativeCodeSandbox implements CodeSandbox {

    private static final String GLOBAL_TMP_CODE_PATH = "tmpCode";

    private static final String GLOBAL_JAVA_CLASS_NAME = "Main.java";

    private static final Long PROGRANM_MAX_TIME = 10*1000L;

    public static final List<String> BLACK_WORD_LIST = Arrays.asList("Runtime", "Process", "exec", "destroy", "exitValue", "waitFor", "getInputStream", "getErrorStream", "getOutputStream");


    public static void main(String[] args) {
        JavaNativeCodeSandbox codeSandbox = new JavaNativeCodeSandbox();
        ExcuteCodeRequest excuteCodeRequest = new ExcuteCodeRequest();
        excuteCodeRequest.setInputList(Arrays.asList("1 2", "3 4"));
        String codeStr = ResourceUtil.readStr("testCode/simpleComputeArgs/Main.java", StandardCharsets.UTF_8);
        excuteCodeRequest.setCode(codeStr);
        excuteCodeRequest.setLanguage("java");
        ExcuteCodeResponse excuteCodeResponse = codeSandbox.excuteCode(excuteCodeRequest);
        System.out.println(excuteCodeResponse);
    }

    /**
     * 执行代码
     *
     * @param excuteCodeRequest
     * @return
     */
    @Override
    public ExcuteCodeResponse excuteCode(ExcuteCodeRequest excuteCodeRequest) {
        List<String> inputList = excuteCodeRequest.getInputList();
        String code = excuteCodeRequest.getCode();
        // 代码校验，识别是否有黑名单代码
        WordTree tree = new WordTree();
        tree.addWords(BLACK_WORD_LIST);
        if (tree.isMatch(code)) {
            ExcuteCodeResponse excuteCodeResponse = new ExcuteCodeResponse();
            excuteCodeResponse.setStatus("4");
            excuteCodeResponse.setMessage("代码中包含黑名单关键字");
            excuteCodeResponse.setOutputList(new ArrayList<>());
            excuteCodeResponse.setJudgeInfo(new JudgeInfo());
            return excuteCodeResponse;
        }

        String language = excuteCodeRequest.getLanguage();

        String userDir = System.getProperty("user.dir");
        String globalCodePath = userDir + File.separator + "tmpCode";
        // 判断全局代码目录是否存在，不存在则创建
        if (!FileUtil.exist(globalCodePath)) {
            FileUtil.mkdir(globalCodePath);
        }
        // 用户代码隔离存放
        String userCodeParentPath = globalCodePath + File.separator + UUID.randomUUID().toString();
        String userCodePath = userCodeParentPath + File.separator + GLOBAL_JAVA_CLASS_NAME;
        File userCodeFile = FileUtil.writeString(code, userCodePath, StandardCharsets.UTF_8);
        // 编译代码，得到 class 文件
        String compileCommand = String.format("javac -encoding UTF-8 %s", userCodeFile.getAbsolutePath());
        StringBuilder output = new StringBuilder();
        try {
            Process compileProcess = Runtime.getRuntime().exec(compileCommand);
            // 等待编译完成 获取错误码
            ExecuteMessage compileMessages = ProcessUtils.runProcessAndGetMessages(compileProcess, "编译");
            System.out.println(compileMessages);
        } catch (IOException e) {
            return getErrorExcuteCodeResponse(e);
        }
        // 执行代码，获取输出
        List<ExecuteMessage> executeMessagesList = new ArrayList<>();
        for (String inputArgs : inputList) {
            String runCommand = String.format("java -Xmx64m -Dfile.encoding=UTF-8 -cp %s Main %s", userCodeParentPath, inputArgs);
            System.out.println(runCommand);
            try {
                Process runProcess = Runtime.getRuntime().exec(runCommand);
                // 使用线程池创建子线程，如果超时则杀死进程
                new Thread(() -> {
                    try {
                        Thread.sleep(PROGRANM_MAX_TIME);
                        if (runProcess.isAlive()) {
                            runProcess.destroy();
                            System.out.println("程序执行超时，已杀死进程");
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
                // 等待执行完成 获取错误码
                ExecuteMessage runProcessAndGetMessages = ProcessUtils.runProcessAndGetMessages(runProcess, "执行");
                executeMessagesList.add(runProcessAndGetMessages);
                System.out.println(runProcessAndGetMessages);
            } catch (IOException e) {
                return getErrorExcuteCodeResponse(e);
            }
        }
        // 整理获取输出结果
        ExcuteCodeResponse excuteCodeResponse = new ExcuteCodeResponse();
        List<String> outputList = new ArrayList<>();
        // 默认成功
        excuteCodeResponse.setStatus("1");
        Long maxTime = 0L;
        for (ExecuteMessage executeMessage : executeMessagesList) {
            String errorMessage = executeMessage.getErrorMessage();
            if (StrUtil.isNotBlank(errorMessage)) {
                excuteCodeResponse.setMessage(errorMessage);
                //执行中存在错误
                excuteCodeResponse.setStatus("3");
                break;
            }
            outputList.add(executeMessage.getMessage());
            if (executeMessage.getTime() != null && executeMessage.getTime() > maxTime) {
                maxTime = executeMessage.getTime();
            }
        }
        excuteCodeResponse.setOutputList(outputList);
        JudgeInfo judgeInfo = new JudgeInfo();
        // 需要借助第三方库实现，非常麻烦暂时不作实现
        // judgeInfo.setMemory();
        judgeInfo.setTime(maxTime);
        excuteCodeResponse.setJudgeInfo(judgeInfo);

        // 删除用户代码 防止服务器空间不足
        if (FileUtil.exist(userCodeParentPath)) {
            boolean del = FileUtil.del(userCodeParentPath);
            System.out.printf("删除用户代码%s%s%n", userCodeParentPath, del ? "成功" : "失败");
        }

        // 错误处理 提升程序健壮性
        return excuteCodeResponse;
    }

    /**
     * 当程序抛出异常时 直接抛出错误响应
     * @param e
     * @return {@link ExcuteCodeResponse }
     * @author zhoulm54681
     * @date 2025/02/03
     */
    private ExcuteCodeResponse getErrorExcuteCodeResponse(Throwable e) {
        ExcuteCodeResponse excuteCodeResponse = new ExcuteCodeResponse();
        // 表示代码沙箱执行错误，编译错误
        excuteCodeResponse.setStatus("2");
        excuteCodeResponse.setOutputList(new ArrayList<>());
        excuteCodeResponse.setMessage(e.getMessage());
        excuteCodeResponse.setJudgeInfo(new JudgeInfo());
        return excuteCodeResponse;
    }
}
