package com.roger.system.core.service;

import com.roger.common.core.domain.base.BaseService;
import com.roger.common.core.domain.base.PageInfo;
import com.roger.system.core.domain.DictDetail;
import com.roger.system.core.domain.dto.DictDetailDto;
import com.roger.system.core.domain.dto.DictDetailQueryParam;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
* @author jinjin
* @date 2020-09-24
*/
public interface DictDetailService  extends BaseService<DictDetail> {

    /**
    * 查询数据分页
    * @param query 条件
    * @param pageable 分页参数
    * @return map[totalElements, content]
    */
    PageInfo<DictDetailDto> queryAll(DictDetailQueryParam query, Pageable pageable);

    /**
    * 查询所有数据不分页
    * @param query 条件参数
    * @return List<DictDetailDto>
    */
    List<DictDetailDto> queryAll(DictDetailQueryParam query);

    /**
     * 根据字典名称获取字典详情
     * @param name 字典名称
     * @return /
     */
    List<DictDetailDto> getDictByName(String name);

    PageInfo<DictDetailDto> getDictByName(String dictName, Pageable pageable);

    boolean removeById(Long id);
    boolean removeByDictId(Long id);

    boolean updateById(DictDetailDto resources);

    boolean save(DictDetailDto resources);

}
