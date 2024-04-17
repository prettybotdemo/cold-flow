package com.warm.flow.core;

import com.warm.flow.core.config.WarmFlowConfig;
import com.warm.flow.core.entity.*;
import com.warm.flow.core.handler.DataFillHandler;
import com.warm.flow.core.handler.DefaultDataFillHandler;
import com.warm.flow.core.invoker.FrameInvoker;
import com.warm.flow.core.service.*;
import com.warm.flow.core.utils.ClassUtil;
import com.warm.tools.utils.ObjectUtil;
import org.noear.snack.core.utils.StringUtil;

import java.util.function.Supplier;

/**
 * 流程定义工程
 *
 * @author warm
 */
public class FlowFactory {


    private static DefService defService = null;
    private static HisTaskService hisTaskService = null;
    private static InsService insService = null;

    private static NodeService nodeService = null;
    private static SkipService skipService = null;
    private static TaskService taskService = null;


    private static Supplier<Definition> defSupplier;
    private static Supplier<HisTask> hisTaskSupplier;
    private static Supplier<Instance> insSupplier;
    private static Supplier<Node> nodeSupplier;
    private static Supplier<Skip> skipSupplier;
    private static Supplier<Task> taskSupplier;

    private static WarmFlowConfig flowConfig;

    private static DataFillHandler dataFillHandler;

    public static void initFlowService(DefService definitionService, HisTaskService hisTaskService, InsService instanceService
            , NodeService nodeService, SkipService skipService, TaskService taskService) {
        FlowFactory.setDefService(definitionService);
        FlowFactory.setHisTaskService(hisTaskService);
        FlowFactory.setInsService(instanceService);
        FlowFactory.setNodeService(nodeService);
        FlowFactory.setSkipService(skipService);
        FlowFactory.setTaskService(taskService);
    }

    public static void setDefService(DefService defService) {
        FlowFactory.defService = defService;
    }

    public static void setHisTaskService(HisTaskService hisTaskService) {
        FlowFactory.hisTaskService = hisTaskService;
    }

    public static void setInsService(InsService insService) {
        FlowFactory.insService = insService;
    }

    public static void setNodeService(NodeService nodeService) {
        FlowFactory.nodeService = nodeService;
    }

    public static void setSkipService(SkipService skipService) {
        FlowFactory.skipService = skipService;
    }

    public static void setTaskService(TaskService taskService) {
        FlowFactory.taskService = taskService;
    }

    public static DefService defService() {
        return defService;
    }

    public static HisTaskService hisTaskService() {
        return hisTaskService;
    }

    public static InsService insService() {
        return insService;
    }

    public static NodeService nodeService() {
        return nodeService;
    }

    public static SkipService skipService() {
        return skipService;
    }

    public static TaskService taskService() {
        return taskService;
    }

    public static void setNewDef(Supplier<Definition> supplier) {
        FlowFactory.defSupplier = supplier;
    }

    public static Definition newDef() {
        return defSupplier.get();
    }

    public static void setNewHisTask(Supplier<HisTask> supplier) {
        FlowFactory.hisTaskSupplier = supplier;
    }

    public static HisTask newHisTask() {
        return hisTaskSupplier.get();
    }

    public static void setNewIns(Supplier<Instance> supplier) {
        FlowFactory.insSupplier = supplier;
    }

    public static Instance newIns() {
        return insSupplier.get();
    }

    public static void setNewNode(Supplier<Node> supplier) {
        FlowFactory.nodeSupplier = supplier;
    }

    public static Node newNode() {
        return nodeSupplier.get();
    }

    public static void setNewSkip(Supplier<Skip> supplier) {
        FlowFactory.skipSupplier = supplier;
    }

    public static Skip newSkip() {
        return skipSupplier.get();
    }

    public static void setNewTask(Supplier<Task> supplier) {
        FlowFactory.taskSupplier = supplier;
    }

    public static Task newTask() {
        return taskSupplier.get();
    }

    public static WarmFlowConfig getFlowConfig() {
        return FlowFactory.flowConfig;
    }

    public static void setFlowConfig(WarmFlowConfig flowConfig) {
        FlowFactory.flowConfig = flowConfig;
    }

    /**
     * 获取填充类
     */
    public static void setDataFillHandler(WarmFlowConfig flowConfig) throws InstantiationException, IllegalAccessException {
        DataFillHandler o = null;
        String dataFillHandlerPath = flowConfig.getDataFillHandlerPath();
        if (!StringUtil.isEmpty(dataFillHandlerPath)) {
            Class<?> clazz = ClassUtil.getClazz(dataFillHandlerPath);
            if (clazz != null) {
                o = (DataFillHandler) clazz.newInstance();
            }
        } else {
            try {
                o = FrameInvoker.getBean(DataFillHandler.class);
            } catch (Exception e) {

            }
            if (ObjectUtil.isNull(o)) {
                o = new DefaultDataFillHandler();
            }
        }
        FlowFactory.dataFillHandler = o;
    }

    /**
     * 获取填充类
     */
    public static DataFillHandler dataFillHandler() {
        return FlowFactory.dataFillHandler;
    }
}
