package cc.mrbird.febs.cos.service.impl;

import cc.mrbird.febs.cos.entity.CommodityInfo;
import cc.mrbird.febs.cos.dao.CommodityInfoMapper;
import cc.mrbird.febs.cos.entity.CommodityMaterialRelation;
import cc.mrbird.febs.cos.entity.MaterialInfo;
import cc.mrbird.febs.cos.service.ICommodityInfoService;
import cc.mrbird.febs.cos.service.ICommodityMaterialRelationService;
import cc.mrbird.febs.cos.service.IMaterialInfoService;
import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author FanK
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CommodityInfoServiceImpl extends ServiceImpl<CommodityInfoMapper, CommodityInfo> implements ICommodityInfoService {

    private final ICommodityMaterialRelationService commodityMaterialRelationService;

    private final IMaterialInfoService materialInfoService;

    /**
     * 分页查询餐品信息
     *
     * @param page          分页对象
     * @param commodityInfo 餐品信息
     * @return 结果
     */
    @Override
    public IPage<LinkedHashMap<String, Object>> getCommodityByPage(Page<CommodityInfo> page, CommodityInfo commodityInfo) {

        IPage<LinkedHashMap<String, Object>> pageData = baseMapper.getCommodityByPage(page, commodityInfo);
        List<LinkedHashMap<String, Object>> data = pageData.getRecords();
        if (CollectionUtil.isEmpty(data)) {
            return pageData;
        }

        for (LinkedHashMap<String, Object> item : data) {
            Integer commodityId = (Integer) item.get("id");
            if (commodityId != null) {
                List<CommodityMaterialRelation> relations = commodityMaterialRelationService.list(
                        Wrappers.<CommodityMaterialRelation>lambdaQuery()
                                .eq(CommodityMaterialRelation::getCommodityId, commodityId)
                );

                if (CollectionUtil.isNotEmpty(relations)) {
                    List<Integer> materialIds = relations.stream()
                            .map(CommodityMaterialRelation::getMaterialId)
                            .collect(Collectors.toList());

                    List<MaterialInfo> materials = materialInfoService.list(
                            Wrappers.<MaterialInfo>lambdaQuery()
                                    .in(MaterialInfo::getId, materialIds)
                    );

                    Map<Integer, MaterialInfo> materialMap = materials.stream()
                            .collect(Collectors.toMap(MaterialInfo::getId, m -> m));

                    List<LinkedHashMap<String, Object>> materialList = relations.stream().map(relation -> {
                        LinkedHashMap<String, Object> materialDetail = new LinkedHashMap<>();
                        MaterialInfo material = materialMap.get(relation.getMaterialId());
                        if (material != null) {
                            materialDetail.put("materialId", material.getId());
                            materialDetail.put("materialName", material.getName());
                            materialDetail.put("materialCode", material.getCode());
                            materialDetail.put("category", material.getCategory());
                            materialDetail.put("unit", material.getUnit());
                            materialDetail.put("specification", material.getSpecification());
                            materialDetail.put("supplier", material.getSupplier());
                            materialDetail.put("unitPrice", material.getUnitPrice());
                            materialDetail.put("quantity", relation.getQuantity());
                            materialDetail.put("relationUnit", relation.getUnit());
                            materialDetail.put("remark", relation.getRemark());
                        }
                        return materialDetail;
                    }).collect(Collectors.toList());

                    item.put("materials", materialList);
                } else {
                    item.put("materials", new java.util.ArrayList<>());
                }
            }
        }

        return pageData;
    }

    /**
     * 小程序热销产品
     *
     * @return 结果
     */
    @Override
    public List<LinkedHashMap<String, Object>> getCommodityHot() {
        return baseMapper.getCommodityHot();
    }

    /**
     * 餐品详情
     *
     * @param commodityId 餐品ID
     * @return 结果
     */
    @Override
    public LinkedHashMap<String, Object> goodsDetail(Integer commodityId) {
        return baseMapper.goodsDetail(commodityId);
    }

    /**
     * 查询店铺及餐品信息
     *
     * @return 结果
     */
    @Override
    public List<LinkedHashMap<String, Object>> selShopDetailList() {
        List<LinkedHashMap<String, Object>> shopList = baseMapper.selShopList();
        shopList.forEach(item -> item.put("goods", this.list(Wrappers.<CommodityInfo>lambdaQuery().eq(CommodityInfo::getShopId, item.get("id")))));
        return shopList;
    }

    @Override
    public LinkedHashMap<String, Object> getShopDetail(Integer shopId) {
        LinkedHashMap<String, Object> result = baseMapper.shopInfoById(shopId);
        result.put("goods", baseMapper.shopInfoDetail(shopId));
        return result;
    }

    /**
     * 店铺餐品排序方式
     *
     * @param shopId 商铺ID
     * @param type   类型
     * @return 结果
     */
    @Override
    public List<LinkedHashMap<String, Object>> shopCommoditySort(Integer shopId, Integer type) {
        return baseMapper.shopCommoditySort(shopId, type);
    }

    /**
     * 模糊查询店内餐品
     *
     * @param shopId 商铺ID
     * @param key    餐品
     * @return 结果
     */
    @Override
    public List<LinkedHashMap<String, Object>> commodityLikeByShop(Integer shopId, String key) {
        return baseMapper.commodityLikeByShop(shopId, key);
    }

    /**
     * 查找餐品或店铺
     *
     * @param key 餐品
     * @return 结果
     */
    @Override
    public LinkedHashMap<String, Object> getGoodsFuzzy(String key) {
        LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        result.put("commodity", baseMapper.commodityLikeByShop(null, key));
        result.put("shop", baseMapper.shopInfoLike(key));
        return result;
    }

    /**
     * 根据用户获取餐品信息
     *
     * @param userId 用户ID
     * @return 结果
     */
    @Override
    public List<LinkedHashMap<String, Object>> getGoodsByUserId(Integer userId) {
        return baseMapper.getGoodsByUserId(userId);
    }
}
