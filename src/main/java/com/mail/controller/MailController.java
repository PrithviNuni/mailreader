package com.mail.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mail.service.MailReaderService;

@Controller
public class MailController {
	
	@Autowired
	MailReaderService mailReaderService;
	
	
	@RequestMapping(value = "/copyMail", method = RequestMethod.GET)
	@ResponseBody
	public Boolean readMail() {
		
		return mailReaderService.readMailsFromExchangeServer();
	}

}
