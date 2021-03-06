/*
 *  Copyright 2019-2020 Zheng Jie
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.roger.tools.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.roger.common.core.config.FileProperties;
import com.roger.common.security.utils.QueryHelpMybatisPlus;
import com.roger.common.core.domain.base.impl.BaseServiceImpl;
import com.roger.common.core.exception.BadRequestException;
import com.roger.common.core.utils.ConvertUtil;
import com.roger.common.core.utils.FileUtil;
import com.roger.common.core.utils.PageUtil;
import com.roger.common.core.utils.StringUtils;
import com.roger.tools.domain.LocalStorage;
import com.roger.tools.domain.dto.LocalStorageDto;
import com.roger.tools.domain.query.LocalStorageQueryParam;
import com.roger.tools.mapper.LocalStorageMapper;
import com.roger.tools.service.LocalStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
* @author Zheng Jie
* @date 2019-09-05
*/
@Service
@RequiredArgsConstructor
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class LocalStorageServiceImpl extends BaseServiceImpl<LocalStorage> implements LocalStorageService {

    private final FileProperties properties;
    private final LocalStorageMapper localStorageMapper;

    @Override
    public Object queryAll(LocalStorageQueryParam query, Pageable pageable){
        IPage<LocalStorage> page = PageUtil.toMybatisPage(pageable);
        IPage<LocalStorage> pageList = localStorageMapper.selectPage(page, QueryHelpMybatisPlus.getPredicate(query));
        return ConvertUtil.convertPage(pageList, LocalStorageDto.class);
    }

    @Override
    public List<LocalStorageDto> queryAll(LocalStorageQueryParam query){
        return ConvertUtil.convertList(localStorageMapper.selectList(QueryHelpMybatisPlus.getPredicate(query)), LocalStorageDto.class);
    }

    @Override
    public LocalStorageDto findById(Long id){
        return ConvertUtil.convert(localStorageMapper.selectById(id), LocalStorageDto.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LocalStorage create(String name, MultipartFile multipartFile) {
        FileUtil.checkSize(properties.getMaxSize(), multipartFile.getSize());
        String suffix = FileUtil.getExtensionName(multipartFile.getOriginalFilename());
        String type = FileUtil.getFileType(suffix);
        File file = FileUtil.upload(multipartFile, properties.getPath().getPath() + type +  File.separator);
        if(ObjectUtil.isNull(file)){
            throw new BadRequestException("????????????");
        }
        try {
            name = StringUtils.isBlank(name) ? FileUtil.getFileNameNoEx(multipartFile.getOriginalFilename()) : name;
            LocalStorage localStorage = new LocalStorage(
                    file.getName(),
                    name,
                    suffix,
                    file.getPath(),
                    type,
                    FileUtil.getSize(multipartFile.getSize())
            );
            localStorageMapper.insert(localStorage);
            return localStorage;
        }catch (Exception e){
            FileUtil.del(file);
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(LocalStorage resources) {
        localStorageMapper.updateById(resources);
        // delCaches(resources.id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAll(Long[] ids) {
        for (Long id : ids) {
            LocalStorage storage = localStorageMapper.selectById(id);
            FileUtil.del(storage.getPath());
            localStorageMapper.deleteById(id);
        }
    }

    @Override
    public void download(List<LocalStorageDto> queryAll, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (LocalStorageDto localStorageDTO : queryAll) {
            Map<String,Object> map = new LinkedHashMap<>();
            map.put("?????????", localStorageDTO.getRealName());
            map.put("?????????", localStorageDTO.getName());
            map.put("????????????", localStorageDTO.getType());
            map.put("????????????", localStorageDTO.getSize());
            map.put("?????????", localStorageDTO.getCreateBy());
            map.put("????????????", localStorageDTO.getCreateTime());
            list.add(map);
        }
        FileUtil.downloadExcel(list, response);
    }
}
