package com.warm.flow.core.service.impl;

import com.warm.flow.core.FlowFactory;
import com.warm.flow.core.chart.*;
import com.warm.flow.core.constant.ExceptionCons;
import com.warm.flow.core.domain.dto.FlowCombine;
import com.warm.flow.core.domain.entity.*;
import com.warm.flow.core.enums.FlowStatus;
import com.warm.flow.core.enums.NodeType;
import com.warm.flow.core.enums.PublishStatus;
import com.warm.flow.core.enums.SkipType;
import com.warm.flow.core.exception.FlowException;
import com.warm.flow.core.mapper.FlowDefinitionMapper;
import com.warm.flow.core.service.DefService;
import com.warm.flow.core.utils.AssertUtil;
import com.warm.flow.core.utils.FlowConfigUtil;
import com.warm.mybatis.core.service.impl.WarmServiceImpl;
import com.warm.tools.utils.Base64;
import com.warm.tools.utils.*;
import org.dom4j.Document;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 流程定义Service业务层处理
 *
 * @author warm
 * @date 2023-03-29
 */
public class DefServiceImpl extends WarmServiceImpl<FlowDefinitionMapper, FlowDefinition> implements DefService {

    @Override
    public Class<FlowDefinitionMapper> getMapperClass() {
        return FlowDefinitionMapper.class;
    }

    @Override
    public void importXml(InputStream is) throws Exception {
        if (ObjectUtil.isNull(is)) {
            return;
        }
        FlowCombine combine = FlowConfigUtil.readConfig(is);
        // 流程定义
        FlowDefinition definition = combine.getDefinition();
        // 所有的流程节点
        List<FlowNode> allNodes = combine.getAllNodes();
        // 所有的流程连线
        List<FlowSkip> allSkips = combine.getAllSkips();
        // 根据不同策略进行新增或者更新
        updateFlow(definition, allNodes, allSkips);
    }

    @Override
    public void saveXml(FlowDefinition def) throws Exception {
        if (StringUtils.isEmpty(def.getXmlString())) {
            FlowFactory.nodeService().remove(new FlowNode().setDefinitionId(def.getId()));
            FlowFactory.skipService().remove(new FlowSkip().setDefinitionId(def.getId()));
            return;
        }
        FlowCombine combine = FlowConfigUtil.readConfig(new ByteArrayInputStream(def.getXmlString()
                .getBytes(StandardCharsets.UTF_8)));
        // 所有的流程节点
        List<FlowNode> allNodes = combine.getAllNodes();
        // 所有的流程连线
        List<FlowSkip> allSkips = combine.getAllSkips();
        FlowFactory.nodeService().remove(new FlowNode().setDefinitionId(def.getId()));
        FlowFactory.skipService().remove(new FlowSkip().setDefinitionId(def.getId()));
        allNodes.forEach(node -> node.setDefinitionId(def.getId()));
        allSkips.forEach(skip -> skip.setDefinitionId(def.getId()));
        FlowFactory.nodeService().saveBatch(allNodes);
        FlowFactory.skipService().saveBatch(allSkips);
    }

    @Override
    public Document exportXml(Long id) {
        FlowDefinition definition = getAllDataDefinition(id);
        return FlowConfigUtil.createDocument(definition);
    }

    @Override
    public String xmlString(Long id) {
        FlowDefinition definition = getAllDataDefinition(id);
        Document document = FlowConfigUtil.createDocument(definition);
        return document.asXML();
    }

    public FlowDefinition getAllDataDefinition(Long id) {
        FlowDefinition definition = getMapper().selectById(id);
        FlowNode node = new FlowNode();
        node.setDefinitionId(id);
        List<FlowNode> nodeList = FlowFactory.nodeService().list(node);
        definition.setNodeList(nodeList);
        FlowSkip flowSkip = new FlowSkip();
        flowSkip.setDefinitionId(id);
        List<FlowSkip> flowSkips = FlowFactory.skipService().list(flowSkip);
        Map<Long, List<FlowSkip>> flowSkipMap = flowSkips.stream()
                .collect(Collectors.groupingBy(FlowSkip::getNodeId));
        nodeList.forEach(flowNode -> flowNode.setSkipList(flowSkipMap.get(flowNode.getId())));

        return definition;
    }

    /**
     * 每次只做新增操作,保证新增的flowCode+version是唯一的
     *
     * @param definition
     * @param allNodes
     * @param allSkips
     */
    private void updateFlow(FlowDefinition definition, List<FlowNode> allNodes, List<FlowSkip> allSkips) {
        List<String> flowCodeList = Collections.singletonList(definition.getFlowCode());
        List<FlowDefinition> flowDefinitions = getMapper().queryByCodeList(flowCodeList);
        for (int j = 0; j < flowDefinitions.size(); j++) {
            FlowDefinition beforeDefinition = flowDefinitions.get(j);
            if (definition.getFlowCode().equals(beforeDefinition.getFlowCode()) && definition.getVersion().equals(beforeDefinition.getVersion())) {
                throw new FlowException(definition.getFlowCode() + "(" + definition.getVersion() + ")" + ExceptionCons.ALREADY_EXIST);
            }
        }
        FlowFactory.defService().save(definition);
        FlowFactory.nodeService().saveBatch(allNodes);
        FlowFactory.skipService().saveBatch(allSkips);
    }

    @Override
    public List<FlowDefinition> queryByCodeList(List<String> flowCodeList) {
        return getMapper().queryByCodeList(flowCodeList);
    }

    @Override
    public void closeFlowByCodeList(List<String> flowCodeList) {
        getMapper().closeFlowByCodeList(flowCodeList);
    }

    @Override
    public boolean checkAndSave(FlowDefinition flowDefinition) {
        List<String> flowCodeList = Collections.singletonList(flowDefinition.getFlowCode());
        List<FlowDefinition> flowDefinitions = queryByCodeList(flowCodeList);
        for (FlowDefinition beforeDefinition : flowDefinitions) {
            if (flowDefinition.getFlowCode().equals(beforeDefinition.getFlowCode()) && flowDefinition.getVersion().equals(beforeDefinition.getVersion())) {
                throw new FlowException(flowDefinition.getFlowCode() + "(" + flowDefinition.getVersion() + ")" + ExceptionCons.ALREADY_EXIST);
            }
        }
        return save(flowDefinition);
    }

    /**
     * 删除流程定义
     *
     * @param ids
     */
    @Override
    public boolean removeDef(List<Long> ids) {
        getMapper().deleteNodeByDefIds(ids);
        getMapper().deleteSkipByDefIds(ids);
        return removeByIds(ids);
    }

    @Override
    public boolean publish(Long id) {
        FlowDefinition definition = getById(id);
        List<String> flowCodeList = Collections.singletonList(definition.getFlowCode());
        // 把之前的流程定义改为已失效
        closeFlowByCodeList(flowCodeList);

        FlowDefinition flowDefinition = new FlowDefinition();
        flowDefinition.setId(id);
        flowDefinition.setIsPublish(PublishStatus.PUBLISHED.getKey());
        return updateById(flowDefinition);
    }

    @Override
    public boolean unPublish(Long id) {
        List<FlowTask> flowTasks = FlowFactory.taskService().list(new FlowTask().setDefinitionId(id));
        AssertUtil.isTrue(CollUtil.isNotEmpty(flowTasks), ExceptionCons.NOT_PUBLISH_TASK);
        FlowDefinition flowDefinition = new FlowDefinition();
        flowDefinition.setId(id);
        flowDefinition.setIsPublish(PublishStatus.UNPUBLISHED.getKey());
        return updateById(flowDefinition);
    }

    @Override
    public String flowChart(Long instanceId) throws IOException {
        int width = 1600, height = 800;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        // 获取图形上下文,graphics想象成一个画笔
        Graphics2D graphics = image.createGraphics();
        // 消除线条锯齿
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // 对指定的矩形区域填充颜色
        graphics.setColor(Color.WHITE);    // GREEN:绿色；  红色：RED;   灰色：GRAY
        graphics.fillRect(0, 0, width, height);

        FlowChartChain flowChartChain = new FlowChartChain();
        FlowInstance instance = FlowFactory.insService().getById(instanceId);
        Map<String, Color> colorMap = new HashMap<>();
        addNodeChart(colorMap, instance, flowChartChain);
        addSkipChart(colorMap, instance, flowChartChain);
        flowChartChain.draw(graphics);

        graphics.setPaintMode();
        graphics.translate(400, 600);
        graphics.dispose();// 释放此图形的上下文并释放它所使用的所有系统资源

        ByteArrayOutputStream os = new ByteArrayOutputStream();

        ImageIO.write(image, "jpg", os);
        return Base64.encode(os.toByteArray());

    }

    public static BufferedImage zoomImage(BufferedImage originalImage, int newWidth, int newHeight) {
        BufferedImage zoomedImage = new BufferedImage(newWidth, newHeight, originalImage.getType());
        Graphics2D g = zoomedImage.createGraphics();
        g.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
        g.dispose();
        return zoomedImage;
    }

    /**
     * 添加跳转流程图
     *
     * @param colorMap
     * @param instance
     * @param flowChartChain
     */
    private void addSkipChart(Map<String, Color> colorMap, FlowInstance instance, FlowChartChain flowChartChain) {
        List<FlowSkip> skipList = FlowFactory.skipService().list(new FlowSkip().setDefinitionId(instance.getDefinitionId()));
        for (FlowSkip flowSkip : skipList) {
            if (StringUtils.isNotEmpty(flowSkip.getCoordinate())) {
                String[] coordinateSplit = flowSkip.getCoordinate().split("\\|");
                String[] skipSplit = coordinateSplit[0].split(";");
                int[] skipX = new int[skipSplit.length];
                int[] skipY = new int[skipSplit.length];
                TextChart textChart = null;
                if (coordinateSplit.length > 1) {
                    String[] textSplit = coordinateSplit[1].split(",");
                    int textX = Integer.parseInt(textSplit[0].split("\\.")[0]);
                    int textY = Integer.parseInt(textSplit[1].split("\\.")[0]);
                    textChart = new TextChart(textX, textY, flowSkip.getSkipName());
                }

                for (int i = 0; i < skipSplit.length; i++) {
                    skipX[i] = Integer.parseInt(skipSplit[i].split(",")[0].split("\\.")[0]);
                    skipY[i] = Integer.parseInt(skipSplit[i].split(",")[1].split("\\.")[0]);
                }
                Color c = colorGet(colorMap, "skip:" + flowSkip.getId().toString());
                flowChartChain.addFlowChart(new SkipChart(skipX, skipY, c, textChart));
            }
        }
    }

    /**
     * 添加节点流程图
     *
     * @param instance
     * @param flowChartChain
     */
    private void addNodeChart(Map<String, Color> colorMap, FlowInstance instance, FlowChartChain flowChartChain) {
        List<FlowNode> nodeList = FlowFactory.nodeService().list(new FlowNode().setDefinitionId(instance.getDefinitionId()));
        List<FlowSkip> allSkips = FlowFactory.skipService().list(new FlowSkip()
                .setDefinitionId(instance.getDefinitionId()).setSkipType(SkipType.PASS.getKey()));
        // 流程图渲染，过滤掉当前任务后的节点
        List<FlowNode> needChartNodes = filterNodes(instance, allSkips, nodeList);
        setColorMap(colorMap, instance, allSkips, needChartNodes);
        for (FlowNode flowNode : nodeList) {
            if (StringUtils.isNotEmpty(flowNode.getCoordinate())) {
                String[] coordinateSplit = flowNode.getCoordinate().split("\\|");
                String[] nodeSplit = coordinateSplit[0].split(",");
                int nodeX = Integer.parseInt(nodeSplit[0].split("\\.")[0]);
                int nodeY = Integer.parseInt(nodeSplit[1].split("\\.")[0]);
                TextChart textChart = null;
                if (coordinateSplit.length > 1) {
                    String[] textSplit = coordinateSplit[1].split(",");
                    int textX = Integer.parseInt(textSplit[0].split("\\.")[0]);
                    int textY = Integer.parseInt(textSplit[1].split("\\.")[0]);
                    textChart = new TextChart(textX, textY, flowNode.getNodeName());
                }
                Color c = colorGet(colorMap, "node:" + flowNode.getNodeCode());
                if (NodeType.isStart(flowNode.getNodeType())) {
                    flowChartChain.addFlowChart(new OvalChart(nodeX, nodeY, Color.GREEN, textChart));
                } else if (NodeType.isBetween(flowNode.getNodeType())) {
                    flowChartChain.addFlowChart(new BetweenChart(nodeX, nodeY, c, textChart));
                }  else if (NodeType.isGateWaySerial(flowNode.getNodeType())) {
                    flowChartChain.addFlowChart(new SerialChart(nodeX, nodeY, c));
                }  else if (NodeType.isGateWayParallel(flowNode.getNodeType())) {
                    flowChartChain.addFlowChart(new ParallelChart(nodeX, nodeY, c));
                } else if (NodeType.isEnd(flowNode.getNodeType())) {
                    flowChartChain.addFlowChart(new OvalChart(nodeX, nodeY,  c, textChart));
                }
            }
        }
    }

    /**
     * 流程图渲染，过滤掉当前任务后的节点
     * @param instance
     * @param allSkips
     * @param nodeList
     * @return
     */
    private List<FlowNode> filterNodes(FlowInstance instance, List<FlowSkip> allSkips, List<FlowNode> nodeList) {
        List<String> allLastNode = new ArrayList<>();
        List<FlowTask> curTasks = FlowFactory.taskService().getByInsId(instance.getId());
        Map<String, List<FlowSkip>> skipLastMap = StreamUtils.groupByKey(allSkips, FlowSkip::getNextNodeCode);
        for (FlowTask curTask : curTasks) {
            allLastNode.add(curTask.getNodeCode());
            List<FlowSkip> lastSkips = skipLastMap.get(curTask.getNodeCode());
            getAllLastNode(lastSkips, allLastNode, skipLastMap);
        }
        return StreamUtils.filter(nodeList, node -> allLastNode.contains(node.getNodeCode()));
    }

    /**
     *
     * 获取代办任务节点前的所有节点
     * @param lastSkips
     * @param allLastNode
     */
    private void getAllLastNode(List<FlowSkip> lastSkips, List<String> allLastNode, Map<String, List<FlowSkip>> skipMap) {
        if (CollUtil.isNotEmpty(lastSkips)) {
            for (FlowSkip lastSkip : lastSkips) {
                allLastNode.add(lastSkip.getNowNodeCode());
                List<FlowSkip> lastLastSkips = skipMap.get(lastSkip.getNowNodeCode());
                getAllLastNode(lastLastSkips, allLastNode,skipMap);
            }
        }
    }

    /**
     * 设置节点和跳转对应的颜色
     *
     * @param colorMap
     * @param instance
     * @param allSkips
     * @param nodeList
     * @return
     */
    public void setColorMap(Map<String, Color> colorMap, FlowInstance instance, List<FlowSkip> allSkips
        , List<FlowNode> nodeList) {
        final Color color = new Color(255, 145, 158);
        Map<String, List<FlowSkip>> skipLastMap = StreamUtils.groupByKey(allSkips, FlowSkip::getNextNodeCode);
        Map<String, List<FlowSkip>> skipNextMap = StreamUtils.groupByKey(allSkips, FlowSkip::getNowNodeCode);
        for (FlowNode flowNode : nodeList) {
            List<FlowSkip> oneNextSkips = skipNextMap.get(flowNode.getNodeCode());
            if (NodeType.isStart(flowNode.getNodeType())) {
                colorPut(colorMap, "node:" + flowNode.getNodeCode(), Color.GREEN);
                if (CollUtil.isNotEmpty(oneNextSkips)) {
                    oneNextSkips.forEach(oneNextSkip -> colorPut(colorMap, "skip:" + oneNextSkip.getId().toString(), Color.GREEN));
                }
                continue;
            }
            if (NodeType.isEnd(flowNode.getNodeType()) && FlowStatus.FINISHED.getKey().equals(instance.getFlowStatus())) {
                colorPut(colorMap, "node:" + flowNode.getNodeCode(), Color.GREEN);
                continue;
            }
            if (NodeType.isGateWay(flowNode.getNodeType())) {
                continue;
            }
            FlowTask flowTask = FlowFactory.taskService()
                    .getOne(new FlowTask().setNodeCode(flowNode.getNodeCode()).setInstanceId(instance.getId()));
            List<FlowSkip> oneLastSkips = skipLastMap.get(flowNode.getNodeCode());
            FlowHisTask curHisTask = CollUtil.getOne(FlowFactory.hisTaskService()
                    .getNoReject(flowNode.getNodeCode(), instance.getId()));

            if (CollUtil.isNotEmpty(oneLastSkips)) {
                for (FlowSkip oneLastSkip : oneLastSkips) {
                    if (NodeType.isStart(oneLastSkip.getNowNodeType()) && flowTask == null) {
                        colorPut(colorMap, "node:" + flowNode.getNodeCode(), Color.GREEN);
                        colorPut(colorMap, "skip:" + oneLastSkip.getId().toString(), Color.GREEN);
                        oneNextSkips.forEach(oneNextSkip -> colorPut(colorMap, "skip:" + oneNextSkip.getId().toString(), Color.GREEN));
                    } else if (NodeType.isGateWay(oneLastSkip.getNowNodeType())) {
                        // 如果前置节点是网关，那网关前任意一个任务完成就算完成
                        List<FlowSkip> twoLastSkips = skipLastMap.get(oneLastSkip.getNowNodeCode());
                        for (FlowSkip twoLastSkip : twoLastSkips) {
                            FlowHisTask twoLastHisTask = CollUtil.getOne(FlowFactory.hisTaskService()
                                    .getNoReject(twoLastSkip.getNowNodeCode(), instance.getId()));
                            Color c;
                            // 前前置节点完成时间是否早于前置节点，如果是串行网关，那前前置节点必须只有一个完成，如果是并行网关都要完成
                            if (flowTask != null) {
                                c = color;
                                colorPut(colorMap, "skip:" + oneLastSkip.getId().toString(), Color.GREEN);
                            } else  {
                                if (curHisTask != null && ObjectUtil.isNotNull(twoLastHisTask) && twoLastHisTask.getCreateTime()
                                        .before(curHisTask.getCreateTime())) {
                                    c = Color.GREEN;
                                } else {
                                    c = Color.BLACK;
                                }
                                colorPut(colorMap, "skip:" + oneLastSkip.getId().toString(), c);
                            }
                            colorPut(colorMap, "node:" + flowNode.getNodeCode(), c);
                            setNextColorMap(colorMap, oneNextSkips, c, skipNextMap);
                        }
                    } else {
                        FlowHisTask twoLastHisTask = CollUtil.getOne(FlowFactory.hisTaskService()
                                .getNoReject(oneLastSkip.getNowNodeCode(), instance.getId()));
                        Color c;
                        // 前前置节点完成时间是否早于前置节点，如果是串行网关，那前前置节点必须只有一个完成，如果是并行网关都要完成
                        if (flowTask != null) {
                            c = color;
                        } else if (curHisTask != null && ObjectUtil.isNotNull(twoLastHisTask) && twoLastHisTask.getCreateTime()
                                .before(curHisTask.getCreateTime())) {
                            c = Color.GREEN;
                        } else {
                            c = Color.BLACK;
                        }
                        colorPut(colorMap, "node:" + flowNode.getNodeCode(), c);
                        colorPut(colorMap, "skip:" + oneLastSkip.getId().toString(), c);
                        setNextColorMap(colorMap, oneNextSkips, c, skipNextMap);
                    }
                }
            }
        }
    }

    /**
     * 设置下个节点的颜色
     * @param colorMap
     * @param oneNextSkips
     * @param c
     * @param skipNextMap
     */
    private void setNextColorMap(Map<String, Color> colorMap, List<FlowSkip> oneNextSkips, Color c, Map<String, List<FlowSkip>> skipNextMap) {
        if (CollUtil.isNotEmpty(oneNextSkips)) {
            oneNextSkips.forEach(oneNextSkip -> {
                colorPut(colorMap, "skip:" + oneNextSkip.getId().toString(), c);
                if (NodeType.isGateWay(oneNextSkip.getNextNodeType()) && (c == Color.GREEN || c == Color.BLACK)) {
                    colorPut(colorMap, "node:" + oneNextSkip.getNextNodeCode(), c);
                }
            });
        }
    }

    /**
     * 优先绿色
     * @param colorMap
     * @param key
     * @param c
     */
    private void colorPut(Map<String, Color> colorMap, String key, Color c) {
        Color color = colorMap.get(key);
        if (c == Color.GREEN) {
            colorMap.put(key, c);
        } else if (color == null || color == Color.BLACK) {
            colorMap.put(key, c);
        }
    }

    private Color colorGet(Map<String, Color> colorMap, String key) {
        Color color = colorMap.get(key);
        if (color == null) {
            color = Color.BLACK;
        }
        return color;
    }

}
