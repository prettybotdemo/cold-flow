package com.warm.flow.core.constant;

/**
 * @author minliuhua
 * @description: 工作流中用到的一些常量
 * @date: 2023/3/30 14:05
 */
public class ExceptionCons {

    public static final String SAME_CONDITION_VALUE = "中间节点，同一个节点不能有相同跳转类型，跳转同一个目标节点!";

    public static final String SAME_CONDITION_NODE = "互斥网关，同一个节点不能有相同跳转条件，跳转同一个目标节点!";

    public static final String SAME_DEST_NODE = "并行网关，同一个节点不能跳转同一个目标节点!";

    public static final String GATEWAY_NOT_CONNECT = "网关节点不可直连!";

    public static final String BETWEEN_REJECT_GATEWAY = "中间节点不可驳回到网关节点!";

    public static final String MUL_START_NODE = "开始节点不能超过1个!";

    public static final String MUL_SKIP_BETWEEN = "不可同时通过或者驳回到多个中间节点，必须先流转到网关节点!";

    public static final String MUL_START_SKIP = "节点流转条件不能超过1个!";

    public static final String MUST_CONDITIONVALUE_NODE = "互斥网关必须传递条件!";

    public static final String ALREADY_EXIST = "流程已经存在,请通过创建新版本的流程对该流程进行更新!";

    public static final String LOST_START_NODE = "流程缺少开始节点!";

    public static final String LOST_NODE_CODE = "节点编码缺失";

    public static final String SAME_NODE_CODE = "同一流程中节点编码重复!";

    public static final String NULL_DEST_NODE = "无法跳转到节点,请检查跳转类型和当前用户权限是否匹配!";

    public static final String NULL_CONDITIONVALUE_NODE = "无法跳转到节点,请检查跳转类型和跳转条件是否匹配!";

    public static final String NULL_CONDITIONVALUE = "跳转条件不能为空!";

    public static final String NULL_EXPRESSION_STRATEGY = "条件表达式不能为空!";

    public static final String FRIST_FORBID_BACK = "禁止驳回到第一个节点";

    public static final String NULL_ROLE_NODE = "无法跳转到该节点,请检查当前用户是否有权限!";

    public static final String LOST_DEST_NODE = "目标节点为空!";

    public static final String NULL_NODE_CODE = "目标节点不存在!";

    public static final String NULL_BUSINESS_ID = "业务id为空!";

    public static final String NULL_FLOW_CODE = "流程编码缺失!";

    public static final String NOT_FOUNT_INSTANCE_ID = "流程实例id为空!";

    public static final String NOT_FOUNT_INSTANCE = "流程实例获取失败!";

    public static final String NULL_INSTANCE_ID = "流程实例id不能为空!";

    public static final String NOT_FOUNT_TASK = "待办任务获取失败!";

    public static final String TASK_NOT_ONE = "不能同时跳转多个代办任务!";

    public static final String NOT_DEFINITION_ID = "流程定义id不能为空!";

    public static final String NOT_NODE_DATA = "流程节点数据缺失!";

    public static final String SKIP_ANY_NODE = "该节点未配置可退回到任意节点!";

    public static final String NOT_PUBLISH_NODE = "不存在已发布的流程定义!";

    public static final String NOT_PUBLISH_TASK = "存在正在执行的代办任务，不可取消!";

    public static final String MSG_OVER_LENGTH = "意见长度过长!";

    public static final String CONDITION_ERR = "条件判断异常！";

}
