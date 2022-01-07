package com.roger.system.core.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.roger.common.core.domain.base.CommonMapper;
import com.roger.system.core.domain.DictDetail;
import com.roger.system.core.domain.dto.DictDetailDto;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
* @author jinjin
* @date 2020-09-24
*/
@Repository
public interface DictDetailMapper extends CommonMapper<DictDetail> {

    List<DictDetailDto> getDictDetailsByDictName(@Param("dictName") String dictName);
    IPage<DictDetailDto> getDictDetailsByDictName(@Param("dictName") String dictName, IPage<DictDetailDto> page);
}
