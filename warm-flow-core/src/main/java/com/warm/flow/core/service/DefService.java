package com.warm.flow.core.service;

import com.warm.flow.core.domain.entity.FlowDefinition;
import com.warm.mybatis.core.service.IWarmService;
import org.dom4j.Document;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * 流程定义Service接口
 *
 * @author warm
 * @date 2023-03-29
 */
public interface DefService extends IWarmService<FlowDefinition> {

    /**
     * 导入xml
     *
     * @param is
     * @throws Exception
     */
    void importXml(InputStream is) throws Exception;

    Document exportXml(Long id);

    List<FlowDefinition> queryByCodeList(List<String> flowCodeList);

    void closeFlowByCodeList(List<String> flowCodeList);

    /**
     * 校验后新增
     *
     * @param flowDefinition
     * @return
     */
    boolean checkAndSave(FlowDefinition flowDefinition);

    /**
     * 删除流程定义
     *
     * @param ids
     * @return
     */
    boolean removeDef(List<Long> ids);

    /**
     * 发布流程定义
     *
     * @param id
     * @return
     */
    boolean publish(Long id);

    /**
     * 取消发布流程定义
     *
     * @param id
     * @return
     */
    boolean unPublish(Long id);

    /**
     * 获取流程图的图片流
     *
     * @param instanceId
     * @return
     * @throws IOException
     */
    String flowChart(Long instanceId) throws IOException;
}
