package com.warm.flow.core.service.impl;

import com.warm.flow.core.FlowFactory;
import com.warm.flow.core.constant.ExceptionCons;
import com.warm.flow.core.dao.FlowInstanceDao;
import com.warm.flow.core.dto.FlowParams;
import com.warm.flow.core.entity.*;
import com.warm.flow.core.enums.FlowStatus;
import com.warm.flow.core.enums.NodeType;
import com.warm.flow.core.listener.Listener;
import com.warm.flow.core.orm.service.impl.WarmServiceImpl;
import com.warm.flow.core.service.InsService;
import com.warm.flow.core.utils.AssertUtil;
import com.warm.flow.core.utils.ListenerUtil;
import com.warm.tools.utils.*;
import org.noear.snack.ONode;

import java.util.*;

/**
 * 流程实例Service业务层处理
 *
 * @author warm
 * @date 2023-03-29
 */
public class InsServiceImpl extends WarmServiceImpl<FlowInstanceDao, Instance> implements InsService {

    @Override
    public InsService setDao(FlowInstanceDao warmDao) {
        this.warmDao = warmDao;
        return this;
    }

    @Override
    public Instance start(String businessId, FlowParams flowParams) {
        AssertUtil.isNull(flowParams.getFlowCode(), ExceptionCons.NULL_FLOW_CODE);
        AssertUtil.isTrue(StringUtils.isEmpty(businessId), ExceptionCons.NULL_BUSINESS_ID);
        // 获取已发布的流程节点
        List<Node> nodes = FlowFactory.nodeService().getByFlowCode(flowParams.getFlowCode());
        AssertUtil.isTrue(CollUtil.isEmpty(nodes), ExceptionCons.NOT_PUBLISH_NODE);
        // 获取开始节点
        Node startNode = nodes.stream().filter(t -> NodeType.isStart(t.getNodeType())).findFirst().orElse(null);
        AssertUtil.isNull(startNode, ExceptionCons.LOST_START_NODE);

        // 获取下一个节点，如果是网关节点，则重新获取后续节点
        List<Node> nextNodes = FlowFactory.taskService().getNextByCheckGateWay(flowParams, getFirstBetween(startNode));

        AssertUtil.isBlank(businessId, ExceptionCons.NULL_BUSINESS_ID);
        // 设置流程实例对象
        Instance instance = setStartInstance(nextNodes.get(0), businessId, flowParams);

        //执行开始节点 开始监听器
        ListenerUtil.executeListener(instance, startNode, Listener.LISTENER_START, flowParams);

        //判断开始结点和下一结点是否有权限监听器,有执行权限监听器node.setPermissionFlag,无走数据库的权限标识符
        ListenerUtil.executeGetNodePermission(instance, flowParams
                , StreamUtils.toArray(CollUtil.listAddToNew(nextNodes, startNode), Node[]::new));

        // 设置历史任务
        List<HisTask> hisTasks = setHisTask(nextNodes, flowParams, startNode, instance.getId());

        // 设置新增任务
        List<Task> addTasks = StreamUtils.toList(nextNodes, node -> FlowFactory.taskService()
                .addTask(node, instance, flowParams));

        // 开启流程，保存流程信息
        saveFlowInfo(instance, addTasks, hisTasks);

        // 执行结束监听器和下一节点的开始监听器
        ListenerUtil.executeListener(instance, startNode, nextNodes, flowParams);
        return instance;
    }

    @Override
    public Instance skipByInsId(Long instanceId, FlowParams flowParams) {
        AssertUtil.isTrue(StringUtils.isNotEmpty(flowParams.getMessage())
                && flowParams.getMessage().length() > 500, ExceptionCons.MSG_OVER_LENGTH);
        // 获取当前流程
        Instance instance = getById(instanceId);
        AssertUtil.isTrue(ObjectUtil.isNull(instance), ExceptionCons.NOT_FOUNT_INSTANCE);
        AssertUtil.isTrue(FlowStatus.isFinished(instance.getFlowStatus()), ExceptionCons.FLOW_FINISH);
        // 获取待办任务
        List<Task> taskList = FlowFactory.taskService().list(FlowFactory.newTask().setInstanceId(instanceId));
        AssertUtil.isTrue(CollUtil.isEmpty(taskList), ExceptionCons.NOT_FOUNT_TASK);
        AssertUtil.isTrue(taskList.size() > 1, ExceptionCons.TASK_NOT_ONE);
        Task task = taskList.get(0);
        return FlowFactory.taskService().skip(flowParams, task, instance);
    }

    @Override
    public Instance termination(Long instanceId, FlowParams flowParams) {
        // 获取当前流程
        Instance instance = getById(instanceId);
        AssertUtil.isTrue(ObjectUtil.isNull(instance), ExceptionCons.NOT_FOUNT_INSTANCE);
        AssertUtil.isTrue(FlowStatus.isFinished(instance.getFlowStatus()), ExceptionCons.FLOW_FINISH);

        // 获取待办任务
        List<Task> taskList = FlowFactory.taskService().list(FlowFactory.newTask().setInstanceId(instanceId));
        AssertUtil.isTrue(CollUtil.isEmpty(taskList), ExceptionCons.NOT_FOUNT_TASK);

        // 获取待办任务
        Task task = taskList.get(0);
        return FlowFactory.taskService().termination(instance, task, flowParams);
    }

    @Override
    public boolean remove(List<Long> instanceIds) {
        return toRemoveTask(instanceIds);
    }


    /**
     * 设置历史任务
     *
     * @param nextNodes
     * @param flowParams
     * @param startNode
     * @param instanceId
     */
    private List<HisTask> setHisTask(List<Node> nextNodes, FlowParams flowParams, Node startNode, Long instanceId) {
        Task startTask = FlowFactory.newTask()
                .setInstanceId(instanceId)
                .setTenantId(flowParams.getTenantId())
                .setDefinitionId(startNode.getDefinitionId())
                .setNodeCode(startNode.getNodeCode())
                .setNodeName(startNode.getNodeName())
                .setNodeType(startNode.getNodeType())
                .setPermissionFlag(StringUtils.isNotEmpty(startNode.getDynamicPermissionFlag())
                        ? startNode.getDynamicPermissionFlag() : startNode.getPermissionFlag())
                .setFlowStatus(FlowStatus.PASS.getKey());
        FlowFactory.dataFillHandler().idFill(startTask);
        // 开始任务转历史任务
        return FlowFactory.hisTaskService().setSkipInsHis(startTask, nextNodes, flowParams);
    }

    /**
     *
     * 开启流程，保存流程信息
     *
     * @param instance
     * @param addTasks
     * @param hisTasks
     */
    private void saveFlowInfo(Instance instance, List<Task> addTasks, List<HisTask> hisTasks) {
        FlowFactory.hisTaskService().saveBatch(hisTasks);
        FlowFactory.taskService().saveBatch(addTasks);
        save(instance);
    }

    /**
     * 设置流程实例对象
     *
     * @param firstBetweenNode
     * @param businessId
     * @return
     */
    private Instance setStartInstance(Node firstBetweenNode, String businessId
            , FlowParams flowParams) {
        Instance instance = FlowFactory.newIns();
        Date now = new Date();
        FlowFactory.dataFillHandler().idFill(instance);
        instance.setDefinitionId(firstBetweenNode.getDefinitionId());
        instance.setBusinessId(businessId);
        instance.setTenantId(flowParams.getTenantId());
        instance.setNodeType(firstBetweenNode.getNodeType());
        instance.setNodeCode(firstBetweenNode.getNodeCode());
        instance.setNodeName(firstBetweenNode.getNodeName());
        instance.setFlowStatus(FlowStatus.TOBESUBMIT.getKey());
        Map<String, Object> variable = flowParams.getVariable();
        if (MapUtil.isNotEmpty(variable)) {
            instance.setVariable(ONode.serialize(variable));
        }
        // 关联业务id,起始后面可以不用到业务id,传业务id目前来看只是为了批量创建流程的时候能创建出有区别化的流程,也是为了后期需要用到businessId。
        instance.setCreateTime(now);
        instance.setUpdateTime(now);
        instance.setCreateBy(flowParams.getCreateBy());
        instance.setExt(flowParams.getExt());
        return instance;
    }

    /**
     * 有且只能有一个开始节点
     *
     * @param startNode
     * @return
     */
    private Node getFirstBetween(Node startNode) {
        List<Skip> skips = FlowFactory.skipService().list(FlowFactory.newSkip()
                .setDefinitionId(startNode.getDefinitionId()).setNowNodeCode(startNode.getNodeCode()));
        Skip skip = skips.get(0);
        return FlowFactory.nodeService().getOne(FlowFactory.newNode().setDefinitionId(startNode.getDefinitionId())
                .setNodeCode(skip.getNextNodeCode()));
    }

    private boolean toRemoveTask(List<Long> instanceIds) {
        AssertUtil.isTrue(CollUtil.isEmpty(instanceIds), ExceptionCons.NULL_INSTANCE_ID);
        boolean success = FlowFactory.taskService().deleteByInsIds(instanceIds);
        if (success) {
            FlowFactory.hisTaskService().deleteByInsIds(instanceIds);
            return FlowFactory.insService().removeByIds(instanceIds);
        }
        return false;
    }
}
