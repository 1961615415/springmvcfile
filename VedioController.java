package cn.knet.domain.web;

import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.knet.domain.domain.KnetShowNews;
import cn.knet.domain.domain.KnetShowNewsExample;
import cn.knet.domain.domain.KnetShowVideoExample;
import cn.knet.domain.page.Page;
import cn.knet.domain.repository.KnetShowNewsMapper;
import cn.knet.domain.repository.KnetShowVideoMapper;
import cn.knet.domain.vo.LigerBean;

/**
 * Handles requests for the application home page.
 */
@Controller
@RequestMapping(value = "/video")
public class VedioController {
	@Resource
	private KnetShowVideoMapper knetShowVideoMapper;
	@Resource
	private KnetShowNewsMapper knetShowNewsMapper;
	/**
	 * 查询更多新闻和视频
	 */
	@RequestMapping(value = { "/toList" }, method = {RequestMethod.POST,RequestMethod.GET})
	public String toList(Model model) {
		//新闻列表
		KnetShowNewsExample newsExample = new KnetShowNewsExample();
		newsExample.setOrderByClause("num desc");
		List<KnetShowNews> newsList = knetShowNewsMapper.selectByExample(newsExample);
		model.addAttribute("newsList", newsList);
		return "vedio/list";
	}
	
	@RequestMapping(value = { "/queryVideo" }, method = {RequestMethod.POST,RequestMethod.GET})
	@ResponseBody
	public LigerBean queryVideo(int page, int pagesize){
		//视频列表
		KnetShowVideoExample videoExample = new KnetShowVideoExample();
		videoExample.setOrderByClause("num desc");
		videoExample.setPage(new Page(page, pagesize));
		return new LigerBean(knetShowVideoMapper.countByExample(videoExample), 
				knetShowVideoMapper.selectByExample(videoExample));
	}
	
	@RequestMapping(value = { "/toNews" }, method = {RequestMethod.POST,RequestMethod.GET})
	public String toNews(Model model, String id) {
		if(StringUtils.isNotBlank(id)){
			KnetShowNews news = knetShowNewsMapper.selectByPrimaryKey(id);
			model.addAttribute("news", news);
		}
		return "vedio/news";
	}
	
	@ModelAttribute(value = "path")
	public String getPath(HttpServletRequest request) {
		String path=request.getRequestURI();
		return path.replace(request.getContextPath(), "");
	}
}
