package com.roger.generator.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.roger.generator.domain.ColumnInfo;
import com.roger.generator.domain.vo.TableInfo;
import com.roger.common.core.domain.base.CommonMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
* @author jinjin
* @date 2020-09-25
*/
@Repository
public interface ColumnInfoMapper extends CommonMapper<ColumnInfo> {

    List<TableInfo> getTables();

    int getTablesTotal();

    IPage<TableInfo> selectPageOfTables(IPage<?> page, @Param("name") String tableName);

    List<ColumnInfo> queryColumnInfo(String tableName);
}
