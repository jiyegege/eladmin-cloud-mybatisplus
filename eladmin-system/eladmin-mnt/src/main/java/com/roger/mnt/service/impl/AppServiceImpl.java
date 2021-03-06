package com.roger.mnt.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.roger.common.core.domain.base.PageInfo;
import com.roger.common.security.utils.QueryHelpMybatisPlus;
import com.roger.common.core.domain.base.impl.BaseServiceImpl;
import com.roger.common.core.exception.BadRequestException;
import com.roger.common.core.utils.ConvertUtil;
import com.roger.common.core.utils.FileUtil;
import com.roger.common.core.utils.PageUtil;
import com.roger.mnt.domain.App;
import com.roger.mnt.domain.dto.AppDto;
import com.roger.mnt.domain.dto.AppQueryParam;
import com.roger.mnt.mapper.AppMapper;
import com.roger.mnt.service.AppService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
* @author jinjin
* @date 2020-09-27
*/
@Service
@AllArgsConstructor
// @CacheConfig(cacheNames = AppService.CACHE_KEY)
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class AppServiceImpl extends BaseServiceImpl<App> implements AppService {

    // private final RedisUtils redisUtils;
    private final AppMapper appMapper;

    @Override
    public PageInfo<AppDto> queryAll(AppQueryParam query, Pageable pageable) {
        IPage<App> page = PageUtil.toMybatisPage(pageable);
        IPage<App> pageList = appMapper.selectPage(page, QueryHelpMybatisPlus.getPredicate(query));
        return ConvertUtil.convertPage(pageList, AppDto.class);
    }

    @Override
    public List<AppDto> queryAll(AppQueryParam query){
        return ConvertUtil.convertList(appMapper.selectList(QueryHelpMybatisPlus.getPredicate(query)), AppDto.class);
    }

    @Override
    public App getById(Long id) {
        return appMapper.selectById(id);
    }

    @Override
    // @Cacheable(key = "'id:' + #p0")
    public AppDto findById(Long id) {
        return ConvertUtil.convert(getById(id), AppDto.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean save(App resources) {
        verification(resources);
        return appMapper.insert(resources) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateById(App resources){
        verification(resources);
        int ret = appMapper.updateById(resources);
        // delCaches(resources.id);
        return ret > 0;
    }
    private void verification(App resources){
        String opt = "/opt";
        String home = "/home";
        if (!(resources.getUploadPath().startsWith(opt) || resources.getUploadPath().startsWith(home))) {
            throw new BadRequestException("?????????????????????opt????????????home?????? ");
        }
        if (!(resources.getDeployPath().startsWith(opt) || resources.getDeployPath().startsWith(home))) {
            throw new BadRequestException("?????????????????????opt????????????home?????? ");
        }
        if (!(resources.getBackupPath().startsWith(opt) || resources.getBackupPath().startsWith(home))) {
            throw new BadRequestException("?????????????????????opt????????????home?????? ");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeByIds(Set<Long> ids){
        // delCaches(ids);
        return appMapper.deleteBatchIds(ids) > 0;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeById(Long id){
        Set<Long> set = new HashSet<>(1);
        set.add(id);
        return this.removeByIds(set);
    }

    /*
    private void delCaches(Long id) {
        redisUtils.delByKey(CACHE_KEY + "::id:", id);
    }

    private void delCaches(Set<Long> ids) {
        for (Long id: ids) {
            delCaches(id);
        }
    }*/

    @Override
    public void download(List<AppDto> all, HttpServletResponse response) throws IOException {
      List<Map<String, Object>> list = new ArrayList<>();
      for (AppDto app : all) {
        Map<String,Object> map = new LinkedHashMap<>();
              map.put("????????????", app.getName());
              map.put("????????????", app.getUploadPath());
              map.put("????????????", app.getDeployPath());
              map.put("????????????", app.getBackupPath());
              map.put("????????????", app.getPort());
              map.put("????????????", app.getStartScript());
              map.put("????????????", app.getDeployScript());
              map.put("?????????", app.getCreateBy());
              map.put("?????????", app.getUpdateBy());
              map.put("????????????", app.getCreateTime());
              map.put("????????????", app.getUpdateTime());
        list.add(map);
      }
      FileUtil.downloadExcel(list, response);
    }
}
