package com.roger.tools.domain.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
* @author jinjin
* @date 2020-09-27
*/
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class AlipayConfigDto implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 防止精度丢失 */
    @JsonSerialize(using= ToStringSerializer.class)
    private Long id;

    private String appId;

    private String charset;

    private String format;

    private String gatewayUrl;

    private String notifyUrl;

    private String privateKey;

    private String publicKey;

    private String returnUrl;

    private String signType;

    private String sysServiceProviderId;
}
