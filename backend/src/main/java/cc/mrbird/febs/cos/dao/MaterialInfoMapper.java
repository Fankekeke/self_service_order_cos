package cc.mrbird.febs.cos.dao;

import cc.mrbird.febs.cos.entity.MaterialInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import java.util.LinkedHashMap;

/**
 * @author FanK
 */
public interface MaterialInfoMapper extends BaseMapper<MaterialInfo> {

    /**
     * 分页获取原材料
     *
     * @param page         分页对象
     * @param materialInfo 原材料
     * @return 结果
     */
    IPage<LinkedHashMap<String, Object>> queryPage(Page<MaterialInfo> page, @Param("materialInfo") MaterialInfo materialInfo);
}
