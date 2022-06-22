package com.thebizio.biziosupport.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RespMsgWithBodyDto extends ResponseMessageDto {
	private Object resObj;

	public RespMsgWithBodyDto(String msg, Object res) {
		super(msg);
		this.resObj = res;
	}

	public RespMsgWithBodyDto(String msg, int statusCode, Object res) {
		super(msg, statusCode);
		resObj = res;
	}
}
