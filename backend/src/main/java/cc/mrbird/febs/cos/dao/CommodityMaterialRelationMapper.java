package cc.mrbird.febs.cos.dao;

import cc.mrbird.febs.cos.entity.CommodityMaterialRelation;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import java.util.LinkedHashMap;

/**
 * @author FanK
 */
public interface CommodityMaterialRelationMapper extends BaseMapper<CommodityMaterialRelation> {

    /**
     * 分页获取菜品原材料关联信息
     *
     * @param page                      分页对象
     * @param commodityMaterialRelation 菜品原材料关联信息
     * @return 结果
     */
    IPage<LinkedHashMap<String, Object>> queryPage(Page<CommodityMaterialRelation> page, @Param("querForm") CommodityMaterialRelation commodityMaterialRelation);
}
