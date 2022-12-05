package cn.knet.boss.web;


import cn.knet.boss.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 代理商申请资格、登录
 * 
 * @author 徐先念
 * 
 */
@Controller
public class UploadController extends SuperController {
	@Autowired
	FileService fileService;
	@RequestMapping(value = "/upload", method = { RequestMethod.POST, RequestMethod.HEAD })
	@ResponseBody
	public Map<String, String> upload(MultipartFile file) throws Exception {
		Map<String, String> map = new HashMap<String, String>();
		map.put("url", fileService.doUploadFile(file));
		map.put("name", file.getOriginalFilename());
		return map;
	}
}
