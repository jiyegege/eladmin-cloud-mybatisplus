package com.roger.mnt.domain.dto;

import com.roger.common.core.domain.base.DataDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
* @author jinjin
* @date 2020-09-27
*/
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class AppDto extends DataDto implements Serializable {
    private static final long serialVersionUID = 1L;


    private Long id;

    private String name;

    private String uploadPath;

    private String deployPath;

    private String backupPath;

    private Integer port;

    private String startScript;

    private String deployScript;

}
