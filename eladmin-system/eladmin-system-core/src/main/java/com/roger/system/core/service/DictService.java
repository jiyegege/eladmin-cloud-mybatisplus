package com.roger.system.core.service;

import com.roger.common.core.domain.base.BaseService;
import com.roger.common.core.domain.base.PageInfo;
import com.roger.system.core.domain.Dict;
import com.roger.system.core.domain.dto.DictDto;
import com.roger.system.core.domain.dto.DictQueryParam;
import org.springframework.data.domain.Pageable;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
* @author jinjin
* @date 2020-09-24
*/
public interface DictService  extends BaseService<Dict> {

    /**
    * 查询数据分页
    * @param query 条件
    * @param pageable 分页参数
    * @return map[totalElements, content]
    */
    PageInfo<DictDto> queryAll(DictQueryParam query, Pageable pageable);

    /**
    * 查询所有数据不分页
    * @param query 条件参数
    * @return List<DictDto>
    */
    List<DictDto> queryAll(DictQueryParam query);

    Dict getById(Long id);
    DictDto findById(Long id);
    boolean save(Dict resources);
    boolean updateById(Dict resources);
    boolean removeById(Long id);
    boolean removeByIds(Set<Long> ids);

    /**
    * 导出数据
    * @param all 待导出的数据
    * @param response /
    * @throws IOException /
    */
    void download(List<DictDto> all, HttpServletResponse response) throws IOException;
}
