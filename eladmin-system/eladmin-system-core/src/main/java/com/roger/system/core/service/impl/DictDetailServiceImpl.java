package com.roger.system.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.roger.common.core.domain.base.PageInfo;
import com.roger.common.security.utils.QueryHelpMybatisPlus;
import com.roger.common.core.domain.base.impl.BaseServiceImpl;
import com.roger.common.core.utils.ConvertUtil;
import com.roger.common.core.utils.PageUtil;
import com.roger.common.core.utils.RedisUtils;
import com.roger.system.core.domain.Dict;
import com.roger.system.core.domain.DictDetail;
import com.roger.system.core.domain.dto.DictDetailDto;
import com.roger.system.core.domain.dto.DictDetailQueryParam;
import com.roger.system.core.mapper.DictDetailMapper;
import com.roger.system.core.mapper.DictMapper;
import com.roger.system.core.service.DictDetailService;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
* @author jinjin
* @date 2020-09-24
*/
@Service
@AllArgsConstructor
@CacheConfig(cacheNames = "dictDetail")
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class DictDetailServiceImpl extends BaseServiceImpl<DictDetail> implements DictDetailService {

    private final DictDetailMapper dictDetailMapper;
    private final DictMapper dictMapper;
    private final RedisUtils redisUtils;

    @Override
    //@Cacheable
    public PageInfo<DictDetailDto> queryAll(DictDetailQueryParam query, Pageable pageable) {
        IPage<DictDetail> page = PageUtil.toMybatisPage(pageable);
        IPage<DictDetail> pageList = dictDetailMapper.selectPage(page, QueryHelpMybatisPlus.getPredicate(query));
        return ConvertUtil.convertPage(pageList, DictDetailDto.class);
    }

    @Override
    //@Cacheable
    public List<DictDetailDto> queryAll(DictDetailQueryParam query){
        return ConvertUtil.convertList(dictDetailMapper.selectList(QueryHelpMybatisPlus.getPredicate(query)), DictDetailDto.class);
    }

    @Override
    public List<DictDetailDto> getDictByName(String dictName) {
        QueryWrapper<Dict> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(Dict::getName, dictName);
        Dict dict = dictMapper.selectOne(wrapper);
        List<DictDetailDto> ret = dictDetailMapper.getDictDetailsByDictName(dictName);
        redisUtils.set("dictDetail::dictId:"+dict.getId(), ret);
        return ret;
    }

    @Override
    public PageInfo<DictDetailDto> getDictByName(String dictName, Pageable pageable) {
        IPage<DictDetailDto> page = PageUtil.toMybatisPage(pageable, true);
        return ConvertUtil.convertPage(dictDetailMapper.getDictDetailsByDictName(dictName, page), DictDetailDto.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateById(DictDetailDto resources) {
        DictDetail detail = ConvertUtil.convert(resources, DictDetail.class);
        boolean ret = dictDetailMapper.updateById(detail) > 0;
        // 清理缓存
        delCaches(detail.getDictId());
        return ret;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean save(DictDetailDto resources){
        DictDetail detail = ConvertUtil.convert(resources, DictDetail.class);
        detail.setDictId(resources.getDict().getId());
        boolean ret = dictDetailMapper.insert(detail) > 0;
        // 清理缓存
        delCaches(detail.getDictId());
        return ret;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeById(Long id) {
        DictDetail dictDetail = dictDetailMapper.selectById(id);
        boolean ret = dictDetailMapper.deleteById(id) > 0;
        // 清理缓存
        delCaches(dictDetail.getDictId());
        return ret;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeByDictId(Long id) {
        UpdateWrapper<DictDetail> wrapper1 = new UpdateWrapper<>();
        wrapper1.lambda().eq(DictDetail::getDictId, id);
        boolean ret = dictDetailMapper.delete(wrapper1) > 0;
        delCaches(id);
        return ret;
    }

    private void delCaches(Long dictId){
        redisUtils.del("dictDetail::dictId:" + dictId);
    }
}
