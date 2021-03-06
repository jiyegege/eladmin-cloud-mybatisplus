package com.roger.system.core.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.roger.common.core.domain.base.PageInfo;
import com.roger.common.security.utils.QueryHelpMybatisPlus;
import com.roger.common.core.domain.base.impl.BaseServiceImpl;
import com.roger.common.core.utils.ConvertUtil;
import com.roger.common.core.utils.FileUtil;
import com.roger.common.core.utils.PageUtil;
import com.roger.common.core.utils.RedisUtils;
import com.roger.system.core.domain.Dict;
import com.roger.system.core.domain.dto.DictDto;
import com.roger.system.core.domain.dto.DictQueryParam;
import com.roger.system.core.mapper.DictMapper;
import com.roger.system.core.service.DictDetailService;
import com.roger.system.core.service.DictService;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
* @author jinjin
* @date 2020-09-24
*/
@Service
@AllArgsConstructor
@CacheConfig(cacheNames = DictServiceImpl.CACHE_KEY)
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class DictServiceImpl extends BaseServiceImpl<Dict> implements DictService {

    public static final String CACHE_KEY = "${changeClassName}";
    private final RedisUtils redisUtils;
    private final DictMapper dictMapper;
    private final DictDetailService detailService;

    @Override
    //@Cacheable
    public PageInfo<DictDto> queryAll(DictQueryParam query, Pageable pageable) {
        IPage<Dict> page = PageUtil.toMybatisPage(pageable);
        IPage<Dict> pageList = dictMapper.selectPage(page, QueryHelpMybatisPlus.getPredicate(query));
        return ConvertUtil.convertPage(pageList, DictDto.class);
    }

    @Override
    //@Cacheable
    public List<DictDto> queryAll(DictQueryParam query){
        return ConvertUtil.convertList(dictMapper.selectList(QueryHelpMybatisPlus.getPredicate(query)), DictDto.class);
    }

    @Override
    public Dict getById(Long id) {
        return dictMapper.selectById(id);
    }

    @Override
    @Cacheable(key = "'id:' + #p0")
    public DictDto findById(Long id) {
        return ConvertUtil.convert(getById(id), DictDto.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean save(Dict resources) {
        return dictMapper.insert(resources) > 0;
    }

    @Override
    // @CacheEvict(allEntries = true)
    @CacheEvict(key = "'id:' + #p0.id")
    @Transactional(rollbackFor = Exception.class)
    public boolean updateById(Dict resources){
        return dictMapper.updateById(resources) > 0;
    }

    @Override
    @CacheEvict(allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public boolean removeByIds(Set<Long> ids){
        List<Dict> dicts = dictMapper.selectBatchIds(ids);
        boolean ret = dictMapper.deleteBatchIds(ids) > 0;
        for (Dict dict : dicts) {
            detailService.removeByDictId(dict.getId());
            delCaches(dict);
        }
        return ret;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeById(Long id){
        Set<Long> ids = new HashSet<>(1);
        ids.add(id);
        return removeByIds(ids);
    }
    @Override
    public void download(List<DictDto> all, HttpServletResponse response) throws IOException {
      List<Map<String, Object>> list = new ArrayList<>();
      for (DictDto dict : all) {
        Map<String,Object> map = new LinkedHashMap<>();
              map.put("????????????", dict.getName());
              map.put("??????", dict.getDescription());
              map.put("?????????", dict.getCreateBy());
              map.put("?????????", dict.getUpdateBy());
              map.put("????????????", dict.getCreateTime());
              map.put("????????????", dict.getUpdateTime());
        list.add(map);
      }
      FileUtil.downloadExcel(list, response);
    }

    private void delCaches(Dict dict){
        redisUtils.del("dictDetail::name:" + dict.getName());
        redisUtils.del("dictDetail::dictId:" + dict.getId());
        redisUtils.del("dict::id:" + dict.getId());
    }
}