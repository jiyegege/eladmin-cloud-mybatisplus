package com.roger.system.core.domain;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.roger.common.core.domain.base.DataEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
* @author jinjin
* @date 2020-09-25
*/
@Data
@TableName("sys_role")
public class Role extends DataEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "ID")
    @TableId(value="role_id", type= IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "名称")
    @NotBlank
    private String name;

    @ApiModelProperty(value = "角色级别")
    private Integer level;

    @ApiModelProperty(value = "描述")
    private String description;

    @ApiModelProperty(value = "数据权限")
    private String dataScope;

    public void copyFrom(Role source){
        BeanUtil.copyProperties(source,this, CopyOptions.create().setIgnoreNullValue(true));
    }
}
