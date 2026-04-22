package cc.mrbird.febs.cos.service.impl;

import cc.mrbird.febs.cos.entity.CommodityMaterialRelation;
import cc.mrbird.febs.cos.dao.CommodityMaterialRelationMapper;
import cc.mrbird.febs.cos.service.ICommodityMaterialRelationService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;

/**
 * @author FanK
 */
@Service
public class CommodityMaterialRelationServiceImpl extends ServiceImpl<CommodityMaterialRelationMapper, CommodityMaterialRelation> implements ICommodityMaterialRelationService {

    /**
     * 分页获取菜品原材料关联信息
     *
     * @param page                      分页对象
     * @param commodityMaterialRelation 菜品原材料关联信息
     * @return 结果
     */
    @Override
    public IPage<LinkedHashMap<String, Object>> queryPage(Page<CommodityMaterialRelation> page, CommodityMaterialRelation commodityMaterialRelation) {
        return baseMapper.queryPage(page, commodityMaterialRelation);
    }
}
