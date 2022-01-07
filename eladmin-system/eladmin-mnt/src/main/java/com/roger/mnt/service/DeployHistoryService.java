package com.roger.mnt.service;

import com.roger.common.core.domain.base.BaseService;
import com.roger.common.core.domain.base.PageInfo;
import com.roger.mnt.domain.DeployHistory;
import com.roger.mnt.domain.dto.DeployHistoryDto;
import com.roger.mnt.domain.query.DeployHistoryQueryParam;
import org.springframework.data.domain.Pageable;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
* @author jinjin
* @date 2020-09-27
*/
public interface DeployHistoryService  extends BaseService<DeployHistory> {

    static final String CACHE_KEY = "deployHistory";

    /**
    * 查询数据分页
    * @param query 条件
    * @param pageable 分页参数
    * @return PageInfo<DeployHistoryDto>
    */
    PageInfo<DeployHistoryDto> queryAll(DeployHistoryQueryParam query, Pageable pageable);

    /**
    * 查询所有数据不分页
    * @param query 条件参数
    * @return List<DeployHistoryDto>
    */
    List<DeployHistoryDto> queryAll(DeployHistoryQueryParam query);

    DeployHistory getById(Long id);
    DeployHistoryDto findById(Long id);

    /**
     * 插入一条新数据。
     */
    boolean save(DeployHistory resources);
    boolean updateById(DeployHistory resources);
    boolean removeById(String id);
    boolean removeByIds(Set<String> ids);

    /**
    * 导出数据
    * @param all 待导出的数据
    * @param response /
    * @throws IOException /
    */
    void download(List<DeployHistoryDto> all, HttpServletResponse response) throws IOException;
}
