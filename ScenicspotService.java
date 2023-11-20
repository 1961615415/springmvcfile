package cn.knet.domain.service;

import cn.knet.domain.entity.KnetDnameFamousCategory;
import cn.knet.domain.entity.KnetDnameSceneType;
import cn.knet.domain.entity.KnetDnameScenicspot;
import cn.knet.domain.mapper.KnetDnameFamousCategoryMapper;
import cn.knet.domain.mapper.KnetDnameSceneTypeMapper;
import cn.knet.domain.mapper.KnetDnameScenicspotMapper;
import cn.knet.domain.vo.ScenicspotVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author dcx
 * @create 2020-01-13 15:45
 */
@Service
public class ScenicspotService {
    @Resource
    private KnetDnameFamousCategoryMapper categoryMapper;
    @Resource
    private KnetDnameSceneTypeMapper typeMapper;
    @Resource
    private KnetDnameScenicspotMapper scenicspotMapper;

    /***
     * 景区的所有的类别
     * @return
     */
    public List<KnetDnameFamousCategory> getAllClassify() {
        List<KnetDnameSceneType> list = typeMapper.selectList(new QueryWrapper<KnetDnameSceneType>().eq("SCENE", "景区场景"));
        if (list != null && list.size() > 0) {
            KnetDnameSceneType sceneType = list.get(0);
            if (StringUtils.isNotBlank(sceneType.getId())) {
                return categoryMapper.selectList(new QueryWrapper<KnetDnameFamousCategory>().eq("SCENE", sceneType.getId()).orderByAsc("CREATE_DATE"));
            }
        }
        return null;
    }

    /***
     * 获取所有的景点
     * @return
     */
    public List<ScenicspotVo> getAllScenicspot() {
        List<ScenicspotVo> voList = new ArrayList<>();
        List<KnetDnameFamousCategory> categories = getAllClassify();
        if (categories != null && categories.size() > 0) {
            for (KnetDnameFamousCategory category : categories) {
                ScenicspotVo vo = new ScenicspotVo();
                vo.setCategoryId(category.getId());
                vo.setCategoryName(category.getCategory());
                Page p = new Page(1, 100);
                vo.setiPage(scenicspotMapper.selectPage(p, new QueryWrapper<KnetDnameScenicspot>().eq("CLASSIFY", category.getId()).ne("NAME","中外著名景点介绍")));
                voList.add(vo);
            }
        }
        return voList;
    }

    /****
     * 获取指定类别的景点
     * @param page
     * @param pageSize
     * @param classifyId
     * @return
     */
    public List<ScenicspotVo> getScenicspotByClass(int page, int pageSize, String classifyId) {
        List<ScenicspotVo> voList = new ArrayList<>();
        List<KnetDnameFamousCategory> categories = getAllClassify();
        if (categories != null && categories.size() > 0) {
            for (KnetDnameFamousCategory category : categories) {
                if (StringUtils.isNotBlank(classifyId) && classifyId.equals(category.getId())) {
                    ScenicspotVo vo = new ScenicspotVo();
                    vo.setCategoryId(category.getId());
                    vo.setCategoryName(category.getCategory());
                    Page p = new Page(page, pageSize);
                    vo.setiPage(scenicspotMapper.selectPage(p, new QueryWrapper<KnetDnameScenicspot>().eq("CLASSIFY", classifyId).ne("NAME","中外著名景点介绍")));
                    voList.add(vo);
                    return voList;
                }
            }
        }
        return null;
    }

    /***
     * 获取网址ID
     * @param domain
     * @return
     */
    public String getDomainId(String domain) {
        KnetDnameScenicspot scenicspot = scenicspotMapper.selectOne(new QueryWrapper<KnetDnameScenicspot>().eq("NAME", domain));
        System.out.println(scenicspot);
        if (scenicspot != null) {
            return scenicspot.getDomainId();
        }
        return "";
    }
}
