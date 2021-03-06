package com.roger.quartz.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.roger.common.core.domain.base.PageInfo;
import com.roger.common.core.domain.base.impl.BaseServiceImpl;
import com.roger.common.core.utils.FileUtil;
import com.roger.common.core.utils.PageUtil;
import com.roger.common.core.utils.RedisUtils;
import com.roger.common.security.utils.QueryHelpMybatisPlus;
import com.roger.quartz.domain.QuartzJob;
import com.roger.quartz.domain.QuartzLog;
import com.roger.quartz.domain.query.QuartzJobQueryParam;
import com.roger.quartz.domain.query.QuartzLogQueryParam;
import com.roger.quartz.mapper.QuartzJobMapper;
import com.roger.quartz.mapper.QuartzLogMapper;
import com.roger.quartz.service.QuartzJobService;
import com.roger.quartz.utils.QuartzManage;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
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
// @CacheConfig(cacheNames = QuartzJobService.CACHE_KEY)
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class QuartzJobServiceImpl extends BaseServiceImpl<QuartzJob> implements QuartzJobService {

    private final QuartzManage quartzManage;
    private final QuartzJobMapper jobMapper;
    private final QuartzLogMapper logMapper;
    private final RedisUtils redisUtils;

    @Override
    public PageInfo<QuartzJob> queryAll(QuartzJobQueryParam criteria, Pageable pageable){
        IPage<QuartzJob> page = PageUtil.toMybatisPage(pageable);
        IPage<QuartzJob> pageList = jobMapper.selectPage(page, QueryHelpMybatisPlus.getPredicate(criteria));
        PageInfo<QuartzJob> pageInfo = new PageInfo<>();
        pageInfo.setContent(pageList.getRecords());
        pageInfo.setTotalElements(pageList.getTotal());
        return pageInfo;
    }

    @Override
    public List<QuartzJob> queryAll(QuartzJobQueryParam criteria) {
        return jobMapper.selectList(QueryHelpMybatisPlus.getPredicate(criteria));
    }

    @Override
    public PageInfo<QuartzLog> queryAllLog(QuartzLogQueryParam criteria, Pageable pageable){
        IPage<QuartzLog> page = PageUtil.toMybatisPage(pageable);
        IPage<QuartzLog> pageList = logMapper.selectPage(page, QueryHelpMybatisPlus.getPredicate(criteria));
        PageInfo<QuartzLog> pageInfo = new PageInfo<>();
        pageInfo.setContent(pageList.getRecords());
        pageInfo.setTotalElements(pageList.getTotal());
        return pageInfo;
    }
    @Override
    public List<QuartzLog> queryAllLog(QuartzLogQueryParam criteria) {
        return logMapper.selectList(QueryHelpMybatisPlus.getPredicate(criteria));
    }


    @Override
    // @Cacheable(key = "'id:' + #p0")
    public QuartzJob findById(Long id) {
        return jobMapper.selectById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean save(QuartzJob resources) {
        return jobMapper.insert(resources) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateById(QuartzJob resources){
        boolean ret = jobMapper.updateById(resources) > 0;
        // delCaches(resources.id);
        return ret;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeByIds(Set<Long> ids){
        // delCaches(ids);
        return jobMapper.deleteBatchIds(ids) > 0;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeById(Long id){
        Set<Long> set = new HashSet<>(1);
        set.add(id);
        return this.removeByIds(set);
    }

    @Override
    public void updateIsPause(QuartzJob quartzJob) {
        if (quartzJob.getIsPause()) {
            quartzManage.resumeJob(quartzJob);
            quartzJob.setIsPause(false);
        } else {
            quartzManage.pauseJob(quartzJob);
            quartzJob.setIsPause(true);
        }
        this.updateById(quartzJob);
    }

    @Override
    public void execution(QuartzJob quartzJob) {
        quartzManage.runJobNow(quartzJob);
    }

    @Async
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void executionSubJob(String[] tasks) throws InterruptedException {
        for (String id : tasks) {
            QuartzJob quartzJob = findById(Long.parseLong(id));
            // ????????????
            String uuid = IdUtil.simpleUUID();
            quartzJob.setUuid(uuid);
            // ????????????
            execution(quartzJob);
            // ????????????????????????????????????????????????????????????????????????
            Boolean result = (Boolean) redisUtils.get(uuid);
            while (result == null) {
                // ??????5???????????????????????????????????????
                Thread.sleep(5000);
                result = (Boolean) redisUtils.get(uuid);
            }
            if(!result){
                redisUtils.del(uuid);
                break;
            }
        }
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
    public void download(List<QuartzJob> all, HttpServletResponse response) throws IOException {
      List<Map<String, Object>> list = new ArrayList<>();
      for (QuartzJob quartzJob : all) {
        Map<String,Object> map = new LinkedHashMap<>();
              map.put("Spring Bean??????", quartzJob.getBeanName());
              map.put("cron ?????????", quartzJob.getCronExpression());
              map.put("?????????1?????????0??????", quartzJob.getIsPause());
              map.put("????????????", quartzJob.getJobName());
              map.put("????????????", quartzJob.getMethodName());
              map.put("??????", quartzJob.getParams());
              map.put("??????", quartzJob.getDescription());
              map.put("?????????", quartzJob.getPersonInCharge());
              map.put("????????????", quartzJob.getEmail());
              map.put("?????????ID", quartzJob.getSubTask());
              map.put("???????????????????????????", quartzJob.getPauseAfterFailure());
              map.put("?????????", quartzJob.getCreateBy());
              map.put("?????????", quartzJob.getUpdateBy());
              map.put("????????????", quartzJob.getCreateTime());
              map.put("????????????", quartzJob.getUpdateTime());
        list.add(map);
      }
      FileUtil.downloadExcel(list, response);
    }

    @Override
    public void downloadLog(List<QuartzLog> queryAllLog, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (QuartzLog quartzLog : queryAllLog) {
            Map<String,Object> map = new LinkedHashMap<>();
            map.put("????????????", quartzLog.getJobName());
            map.put("Bean??????", quartzLog.getBeanName());
            map.put("????????????", quartzLog.getMethodName());
            map.put("??????", quartzLog.getParams());
            map.put("?????????", quartzLog.getCronExpression());
            map.put("????????????", quartzLog.getExceptionDetail());
            map.put("??????/??????", quartzLog.getTime());
            map.put("??????", quartzLog.getIsSuccess() ? "??????" : "??????");
            map.put("????????????", quartzLog.getCreateTime());
            list.add(map);
        }
        FileUtil.downloadExcel(list, response);
    }
}
