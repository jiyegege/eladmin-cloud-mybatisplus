package com.roger.system.core.domain.dto;

import com.roger.common.core.domain.base.DataDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

/**
* @author jinjin
* @date 2020-09-25
*/
@Getter
@Setter
@NoArgsConstructor
public class JobDto extends DataDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    private String name;

    private Boolean enabled;

    private Integer jobSort;

    public JobDto(String name, Boolean enabled) {
        this.name = name;
        this.enabled = enabled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        JobDto dto = (JobDto) o;
        return Objects.equals(id, dto.id) &&
                Objects.equals(name, dto.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}
