package com.warm.flow.core.config;

import com.warm.flow.core.FlowFactory;
import com.warm.flow.core.constant.FlowConfigCons;
import com.warm.flow.core.invoker.FrameInvoker;
import com.warm.tools.utils.ObjectUtil;
import com.warm.tools.utils.StringUtils;

/**
 * WarmFlow属性配置文件
 *
 * @author warm
 */
public class WarmFlowConfig {
    private boolean banner = true;

    private String dataFillHandlerPath;


    public boolean isBanner() {
        return banner;
    }

    public WarmFlowConfig setBanner(boolean banner) {
        this.banner = banner;
        return this;
    }

    public String getDataFillHandlerPath() {
        return dataFillHandlerPath;
    }

    public WarmFlowConfig setDataFillHandlerPath(String dataFillHandlerPath) {
        this.dataFillHandlerPath = dataFillHandlerPath;
        return this;
    }

    public static WarmFlowConfig init() throws InstantiationException, IllegalAccessException {
        WarmFlowConfig flowConfig = new WarmFlowConfig();
        String banner = FrameInvoker.getCfg(FlowConfigCons.BANNER);
        if (StringUtils.isNotEmpty(banner)) {
            flowConfig.setBanner(ObjectUtil.isStrTrue(banner));
        }
        flowConfig.setDataFillHandlerPath(FrameInvoker.getCfg(FlowConfigCons.DATAFILLHANDLEPATH));
        FlowFactory.setDataFillHandler(flowConfig);
        printBanner(flowConfig);
        return flowConfig;
    }

    private static void printBanner(WarmFlowConfig flowConfig) {
        if (flowConfig.isBanner()) {
            System.out.println("\n" +
                    "▄     ▄                      ▄▄▄▄▄▄   ▄                \n" +
                    "█  █  █  ▄▄▄    ▄ ▄▄  ▄▄▄▄▄  █        █     ▄▄▄  ▄     ▄\n" +
                    "▀ █▀█ █ ▀   █   █▀  ▀ █ █ █  █▄▄▄▄▄   █    █▀ ▀█ ▀▄ ▄ ▄▀\n" +
                    " ██ ██▀ ▄▀▀▀█   █     █ █ █  █        █    █   █  █▄█▄█\n" +
                    " █   █  ▀▄▄▀█   █     █ █ █  █        ▀▄▄  ▀█▄█▀   █ █");
        }
    }
}
