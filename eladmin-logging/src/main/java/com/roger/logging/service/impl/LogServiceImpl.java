package com.roger.logging.service.impl;

import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.roger.common.core.domain.Log;
import com.roger.common.core.domain.base.PageInfo;
import com.roger.common.core.domain.base.impl.BaseServiceImpl;
import com.roger.common.core.domain.dto.SaveLogDTO;
import com.roger.common.core.utils.*;
import com.roger.common.security.utils.QueryHelpMybatisPlus;
import com.roger.logging.domain.dto.LogErrorDTO;
import com.roger.logging.domain.dto.LogSmallDTO;
import com.roger.logging.domain.query.LogQueryParam;
import com.roger.logging.mapper.LogMapper;
import com.roger.logging.service.LogService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;

// 默认不使用缓存
//import org.springframework.cache.annotation.CacheConfig;
//import org.springframework.cache.annotation.CacheEvict;
//import org.springframework.cache.annotation.Cacheable;

/**
* @author jinjin
* @date 2020-09-27
*/
@Slf4j
@Service
@AllArgsConstructor
// @CacheConfig(cacheNames = LogService.CACHE_KEY)
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class LogServiceImpl extends BaseServiceImpl<Log> implements LogService {

    // private final RedisUtils redisUtils;
    private final LogMapper logMapper;

    @Override
    public Object queryAll(LogQueryParam query, Pageable pageable) {
        IPage<Log> page = PageUtil.toMybatisPage(pageable);
        IPage<Log> pageList = logMapper.selectPage(page, QueryHelpMybatisPlus.getPredicate(query));
        String status = "ERROR";
        if (status.equals(query.getLogType())) {
            return ConvertUtil.convertPage(pageList, LogErrorDTO.class);
        }
        return ConvertUtil.convertPage(pageList, Log.class);
    }

    @Override
    public List<Log> queryAll(LogQueryParam query){
        return logMapper.selectList(QueryHelpMybatisPlus.getPredicate(query));
    }

    @Override
    public PageInfo<LogSmallDTO> queryAllByUser(LogQueryParam query, Pageable pageable) {
        IPage<Log> page = PageUtil.toMybatisPage(pageable);
        IPage<Log> pageList = logMapper.selectPage(page, QueryHelpMybatisPlus.getPredicate(query));
        return ConvertUtil.convertPage(pageList, LogSmallDTO.class);
    }

    @Override
    // @Cacheable(key = "'id:' + #p0")
    public Log findById(Long id) {
        return logMapper.selectById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeByLogType(String logType) {
        UpdateWrapper<Log> wrapper = new UpdateWrapper<>();
        wrapper.lambda().eq(Log::getLogType, logType);
        return logMapper.delete(wrapper) > 0;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(SaveLogDTO saveLogDTO) {
        Log log = saveLogDTO.getLog();

        StringBuilder params = new StringBuilder("{");
        //参数值
        List<String> argValues = saveLogDTO.getArgValues();
        //参数名称
        for (String argValue : argValues) {
            params.append(argValue).append(" ");
        }
        // 描述
        if (log != null) {
            log.setDescription(saveLogDTO.getLogDescription());
        }
        assert log != null;
        log.setRequestIp(saveLogDTO.getIp());
        //log.setAddress(StringUtils.getCityInfo(log.getRequestIp()));
        log.setMethod(saveLogDTO.getMethodName());
        log.setUsername(saveLogDTO.getUsername());
        log.setParams(params + " }");
        log.setBrowser(saveLogDTO.getBrowser());
        if (log.getId() == null) {
            logMapper.insert(log);
        } else {
            logMapper.updateById(log);
        }
    }

    @Override
    public Object findByErrDetail(Long id) {
        Log log = findById(id);
        ValidationUtil.isNull(log.getId(), "Log", "id", id);
        byte[] details = log.getExceptionDetail();
        return Dict.create().set("exception", new String(ObjectUtil.isNotNull(details) ? details : "".getBytes()));
    }

    @Override
    public void download(List<Log> logs, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Log log : logs) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("用户名", log.getUsername());
            map.put("IP", log.getRequestIp());
            map.put("IP来源", log.getAddress());
            map.put("描述", log.getDescription());
            map.put("浏览器", log.getBrowser());
            map.put("请求耗时/毫秒", log.getTime());
            map.put("异常详情", new String(ObjectUtil.isNotNull(log.getExceptionDetail()) ? log.getExceptionDetail() : "".getBytes()));
            map.put("创建日期", log.getCreateTime());
            list.add(map);
        }
        FileUtil.downloadExcel(list, response);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delAllByError() {
        this.removeByLogType("ERROR");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delAllByInfo() {
        this.removeByLogType("INFO");
    }
}
