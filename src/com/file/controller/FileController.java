package com.file.controller;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.swing.ImageIcon;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import com.file.utils.MarkImageUtils;
import com.sun.mail.handlers.message_rfc822;

@Controller
public class FileController {
	@Resource
	MarkImageUtils markImageUtils;
	@RequestMapping(value = { "/index.shtml", "/" }, method = {
			RequestMethod.POST, RequestMethod.GET })
	public String index() {
		return "/file";
	}

	/*
	 * 采用spring提供的上传文件的方法
	 */
	@RequestMapping(value = { "springUpload.shtml" }, method = { RequestMethod.POST })
	public String springUpload(HttpServletRequest request, Model model)
			throws IllegalStateException, IOException {
		long startTime = System.currentTimeMillis();
		// 得到上传文件的保存目录，将上传的文件存放于WEB-INF目录下，不允许外界直接访问，保证上传文件的安全
		String savePath = request.getSession().getServletContext()
				.getRealPath("/upload");
		File filesave = new File(savePath);
		// 判断上传文件的保存目录是否存在
		if (!filesave.exists() && !filesave.isDirectory()) {
			System.out.println(savePath + "目录不存在，需要创建");
			// 创建目录
			filesave.mkdir();
		}
		// 将当前上下文初始化给 CommonsMutipartResolver （多部分解析器）
		CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(
				request.getSession().getServletContext());
		List<String> filename = new ArrayList<String>();
		List<String> filenamesy = new ArrayList<String>();
		List<String> filenamesyt = new ArrayList<String>();
		// 检查form中是否有enctype="multipart/form-data"
		if (multipartResolver.isMultipart(request)) {
			// 将request变成多部分request
			MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
			// 获取multiRequest 中所有的文件名
			Iterator iter = multiRequest.getFileNames();
			while (iter.hasNext()) {
				// 一次遍历所有文件
				MultipartFile file = multiRequest.getFile(iter.next()
						.toString());
				if (file != null) {
					String path = savePath + "\\" + file.getOriginalFilename();
					// 上传
					file.transferTo(new File(path));
					filename.add(path.substring(path.lastIndexOf("upload"),
							path.length()));
					String sypath = savePath + "\\sy_"
							+ file.getOriginalFilename();
					//添加水印
					markImageUtils.markImageByText("实体店", path, sypath, -30,true);
					String sytpath = savePath + "\\syt_"
							+ file.getOriginalFilename();
					//添加水印图片
					markImageUtils.markImageByIcon(savePath + "\\1.png", path, sytpath, -45, true);
					filenamesy.add(sypath.substring(
							sypath.lastIndexOf("upload"), sypath.length()));
					filenamesyt.add(sytpath.substring(
							sytpath.lastIndexOf("upload"), sytpath.length()));
				}
			}
		}
		model.addAttribute("filename", filename);
		model.addAttribute("filenamesy", filenamesy);
		model.addAttribute("filenamesyt", filenamesyt);
		long endTime = System.currentTimeMillis();
		System.out.println("方法三的运行时间：" + String.valueOf(endTime - startTime)
				+ "ms");
		return "/success";
	}
	 
}
