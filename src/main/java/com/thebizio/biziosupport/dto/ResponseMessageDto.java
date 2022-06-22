package com.thebizio.biziosupport.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseMessageDto {
	private String message;

	private int statusCode;

	public ResponseMessageDto(String msg) {
		message = msg;
		this.statusCode = HttpStatus.OK.value();
	}
}
