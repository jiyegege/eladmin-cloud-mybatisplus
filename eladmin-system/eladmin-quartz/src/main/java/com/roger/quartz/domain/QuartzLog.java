package com.roger.quartz.domain;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModelProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
* @author jinjin
* @date 2020-09-27
*/
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@TableName("sys_quartz_log")
public class QuartzLog implements Serializable {
    private static final long serialVersionUID = 1L;


    @ApiModelProperty(value = "ID")
    @TableId(value = "log_id", type= IdType.AUTO)
    private Long id;

    private String beanName;

    @TableField(fill= FieldFill.INSERT)
    private Date createTime;

    private String cronExpression;

    private String exceptionDetail;

    private Boolean isSuccess;

    private String jobName;

    private String methodName;

    private String params;

    private Long time;

    public void copyFrom(QuartzLog source){
        BeanUtil.copyProperties(source,this, CopyOptions.create().setIgnoreNullValue(true));
    }
}
