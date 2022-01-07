package com.roger.system.core.service;

import com.roger.common.core.domain.base.BaseService;
import com.roger.common.core.domain.base.PageInfo;
import com.roger.system.core.domain.Job;
import com.roger.system.core.domain.dto.JobDto;
import com.roger.system.core.domain.dto.JobQueryParam;
import org.springframework.data.domain.Pageable;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
* @author jinjin
* @date 2020-09-25
*/
public interface JobService  extends BaseService<Job> {

    PageInfo<JobDto> queryAll(JobQueryParam query, Pageable pageable);

    /**
    * 查询所有数据不分页
    * @param query 条件参数
    * @return List<JobDto>
    */
    List<JobDto> queryAll(JobQueryParam query);

    List<JobDto> queryAll();

    Job getById(Long id);

    /**
     * 插入一条新数据。
     */
    boolean save(Job resources);
    boolean updateById(Job resources);
    boolean removeById(Long id);
    boolean removeByIds(Set<Long> ids);

    void verification(Set<Long> ids);
    /**
    * 导出数据
    * @param all 待导出的数据
    * @param response /
    * @throws IOException /
    */
    void download(List<JobDto> all, HttpServletResponse response) throws IOException;
}
