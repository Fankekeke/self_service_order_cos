package cc.mrbird.febs.cos.service.impl;

import cc.mrbird.febs.cos.entity.CommodityInfo;
import cc.mrbird.febs.cos.entity.CommodityMaterialRelation;
import cc.mrbird.febs.cos.entity.DailySupplyPlanDetail;
import cc.mrbird.febs.cos.dao.DailySupplyPlanDetailMapper;
import cc.mrbird.febs.cos.entity.MaterialInfo;
import cc.mrbird.febs.cos.service.ICommodityInfoService;
import cc.mrbird.febs.cos.service.ICommodityMaterialRelationService;
import cc.mrbird.febs.cos.service.IDailySupplyPlanDetailService;
import cc.mrbird.febs.cos.service.IMaterialInfoService;
import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author FanK
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DailySupplyPlanDetailServiceImpl extends ServiceImpl<DailySupplyPlanDetailMapper, DailySupplyPlanDetail> implements IDailySupplyPlanDetailService {

    private final ICommodityInfoService commodityInfoService;

    private final IMaterialInfoService materialInfoService;

    private final ICommodityMaterialRelationService commodityMaterialRelationService;

    /**
     * 分页获取每日供应信息
     *
     * @param page                  分页对象
     * @param dailySupplyPlanDetail 每日供应信息
     * @return 结果
     */
    @Override
    public IPage<LinkedHashMap<String, Object>> queryPage(Page<DailySupplyPlanDetail> page, DailySupplyPlanDetail dailySupplyPlanDetail) {
        return baseMapper.queryPage(page, dailySupplyPlanDetail);
    }

    /**
     * 查询今日供应菜品信息
     *
     * @return 列表
     */
    @Override
    public LinkedHashMap<String, Object> queryCommodityByDate() {
        String today = DateUtil.formatDate(new Date());
        List<DailySupplyPlanDetail> list = list(Wrappers.<DailySupplyPlanDetail>lambdaQuery().eq(DailySupplyPlanDetail::getDate, today));

        LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        result.put("date", today);

        if (list == null || list.isEmpty()) {
            result.put("commodityList", new ArrayList<>());
            result.put("totalCount", 0);
            return result;
        }

        List<Integer> commodityIdList = list.stream()
                .map(DailySupplyPlanDetail::getCommodityId)
                .distinct()
                .collect(Collectors.toList());

        List<CommodityInfo> commodityInfoList = commodityInfoService.list(
                Wrappers.<CommodityInfo>lambdaQuery().in(CommodityInfo::getId, commodityIdList)
        );
        Map<Integer, CommodityInfo> commodityInfoMap = commodityInfoList.stream()
                .collect(Collectors.toMap(CommodityInfo::getId, commodity -> commodity));

        List<Map<String, Object>> commodityList = new ArrayList<>();
        int totalAvailableCount = 0;

        for (DailySupplyPlanDetail detail : list) {
            CommodityInfo commodityInfo = commodityInfoMap.get(detail.getCommodityId());
            if (commodityInfo == null) {
                continue;
            }

            Integer plannedQuantity = detail.getPlannedQuantity() != null ? detail.getPlannedQuantity() : 0;
            Integer actualQuantity = detail.getActualQuantity() != null ? detail.getActualQuantity() : 0;

            int availableQuantity = plannedQuantity - actualQuantity;

            if (availableQuantity > 0) {
                totalAvailableCount += availableQuantity;

                Map<String, Object> commodityItem = new LinkedHashMap<>();
                commodityItem.put("commodity", commodityInfo);
                commodityItem.put("plannedQuantity", plannedQuantity);
                commodityItem.put("actualQuantity", actualQuantity);
                commodityItem.put("availableQuantity", availableQuantity);
                commodityItem.put("unitPrice", detail.getUnitPrice());

                commodityList.add(commodityItem);
            }
        }

        result.put("commodityList", commodityList);
        result.put("totalCount", totalAvailableCount);

        return result;
    }

    /**
     * 检查指定日期是否已存在供应计划
     *
     * @param date 日期
     * @return 是否存在
     */
    @Override
    public boolean existsByDate(String date) {
        LambdaQueryWrapper<DailySupplyPlanDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DailySupplyPlanDetail::getDate, date);
        return this.count(wrapper) > 0;
    }

    /**
     * 批量保存每日供应计划明细
     *
     * @param list 供应计划明细列表
     * @param date 供应日期
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchSave(List<DailySupplyPlanDetail> list, String date) {
        String now = DateUtil.formatDateTime(new Date());

        List<Integer> commodityIdList = list.stream().map(DailySupplyPlanDetail::getCommodityId).collect(Collectors.toList());
        List<CommodityInfo> commodityInfoList = commodityInfoService.list(Wrappers.<CommodityInfo>lambdaQuery().in(CommodityInfo::getId, commodityIdList));
        Map<Integer, CommodityInfo> commodityInfoMap = commodityInfoList.stream().collect(Collectors.toMap(CommodityInfo::getId, commodityInfo -> commodityInfo));

        List<CommodityMaterialRelation> commodityMaterialRelationList = commodityMaterialRelationService.list(Wrappers.<CommodityMaterialRelation>lambdaQuery().in(CommodityMaterialRelation::getCommodityId, commodityIdList));
        Map<Integer, List<CommodityMaterialRelation>> commodityMaterialRelationMap = commodityMaterialRelationList.stream().collect(Collectors.groupingBy(CommodityMaterialRelation::getCommodityId));

        List<Integer> materialIdList = commodityMaterialRelationList.stream()
                .map(CommodityMaterialRelation::getMaterialId)
                .distinct()
                .collect(Collectors.toList());
        Map<Integer, MaterialInfo> materialInfoMap = new HashMap<>();
        if (!materialIdList.isEmpty()) {
            List<MaterialInfo> materialInfoList = materialInfoService.list(Wrappers.<MaterialInfo>lambdaQuery().in(MaterialInfo::getId, materialIdList));
            materialInfoMap = materialInfoList.stream().collect(Collectors.toMap(MaterialInfo::getId, material -> material));
        }

        for (DailySupplyPlanDetail detail : list) {
            detail.setActualQuantity(0);
            detail.setActualAmount(BigDecimal.ZERO);
            detail.setDate(date);
            detail.setCreateDate(now);

            CommodityInfo commodityInfo = commodityInfoMap.get(detail.getCommodityId());
            if (commodityInfo != null) {
                if (detail.getUnitPrice() == null) {
                    detail.setUnitPrice(commodityInfo.getPrice());
                }
            }

            if (detail.getPlannedQuantity() != null && detail.getUnitPrice() != null) {
                BigDecimal plannedAmount = detail.getUnitPrice().multiply(new BigDecimal(detail.getPlannedQuantity())).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                detail.setPlannedAmount(plannedAmount);
            } else {
                detail.setPlannedAmount(BigDecimal.ZERO);
            }

            List<CommodityMaterialRelation> materialRelations = commodityMaterialRelationMap.get(detail.getCommodityId());
            if (materialRelations != null && !materialRelations.isEmpty() && detail.getPlannedQuantity() != null) {
                BigDecimal totalMaterialCost = BigDecimal.ZERO;
                List<Map<String, Object>> procurementList = new ArrayList<>();

                for (CommodityMaterialRelation relation : materialRelations) {
                    MaterialInfo materialInfo = materialInfoMap.get(relation.getMaterialId());
                    if (materialInfo != null) {
                        BigDecimal requiredQuantity = relation.getQuantity().multiply(new BigDecimal(detail.getPlannedQuantity())).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                        BigDecimal materialCost = requiredQuantity.multiply(materialInfo.getUnitPrice() != null ? materialInfo.getUnitPrice() : BigDecimal.ZERO);
                        totalMaterialCost = totalMaterialCost.add(materialCost);

                        Map<String, Object> procurementItem = new LinkedHashMap<>();
                        procurementItem.put("materialId", materialInfo.getId());
                        procurementItem.put("materialCode", materialInfo.getCode());
                        procurementItem.put("materialName", materialInfo.getName());
                        procurementItem.put("category", materialInfo.getCategory());
                        procurementItem.put("requiredQuantity", requiredQuantity.setScale(2, RoundingMode.HALF_UP));
                        procurementItem.put("unit", relation.getUnit() != null ? relation.getUnit() : materialInfo.getUnit());
                        procurementItem.put("unitPrice", materialInfo.getUnitPrice());
                        procurementItem.put("totalCost", materialCost.setScale(2, RoundingMode.HALF_UP));
                        procurementItem.put("supplier", materialInfo.getSupplier());
                        procurementList.add(procurementItem);
                    }
                }

                detail.setEstimatedMaterialCost(totalMaterialCost.setScale(2, RoundingMode.HALF_UP));

                if (!procurementList.isEmpty()) {
                    detail.setRemark(JSONUtil.toJsonStr(procurementList));
                }
            } else {
                detail.setEstimatedMaterialCost(BigDecimal.ZERO);
            }
        }


        return this.saveBatch(list);
    }

    /**
     * 查询每日供应计划明细
     *
     * @param date 日期
     * @return 结果
     */
    @Override
    public LinkedHashMap<String, Object> querySupplyPlanMaterial(String date) {
        List<DailySupplyPlanDetail> list = list(Wrappers.<DailySupplyPlanDetail>lambdaQuery().eq(DailySupplyPlanDetail::getDate, date));

        LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        if (list == null || list.isEmpty()) {
            result.put("procurementList", new ArrayList<>());
            result.put("totalAmount", BigDecimal.ZERO);
            return result;
        }

        Map<String, Map<String, Object>> materialAggregateMap = new LinkedHashMap<>();

        for (DailySupplyPlanDetail detail : list) {
            String remark = detail.getRemark();
            if (remark != null && !remark.isEmpty()) {
                try {
                    cn.hutool.json.JSONArray procurementArray = JSONUtil.parseArray(remark);
                    for (int i = 0; i < procurementArray.size(); i++) {
                        cn.hutool.json.JSONObject itemJson = procurementArray.getJSONObject(i);

                        String materialName = itemJson.getStr("materialName");
                        String unit = itemJson.getStr("unit");
                        String aggregateKey = materialName + "_" + unit;

                        BigDecimal requiredQuantity = itemJson.getBigDecimal("requiredQuantity");
                        BigDecimal totalCost = itemJson.getBigDecimal("totalCost");

                        if (materialAggregateMap.containsKey(aggregateKey)) {
                            Map<String, Object> existingItem = materialAggregateMap.get(aggregateKey);
                            BigDecimal existingQuantity = (BigDecimal) existingItem.get("requiredQuantity");
                            BigDecimal existingCost = (BigDecimal) existingItem.get("totalCost");

                            existingItem.put("requiredQuantity", existingQuantity.add(requiredQuantity));
                            existingItem.put("totalCost", existingCost.add(totalCost));
                        } else {
                            Map<String, Object> newItem = new LinkedHashMap<>();
                            newItem.put("materialId", itemJson.getInt("materialId"));
                            newItem.put("materialCode", itemJson.getStr("materialCode"));
                            newItem.put("materialName", materialName);
                            newItem.put("category", itemJson.getStr("category"));
                            newItem.put("requiredQuantity", requiredQuantity);
                            newItem.put("unit", unit);
                            newItem.put("unitPrice", itemJson.getBigDecimal("unitPrice"));
                            newItem.put("totalCost", totalCost);
                            newItem.put("supplier", itemJson.getStr("supplier"));
                            materialAggregateMap.put(aggregateKey, newItem);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        for (Map.Entry<String, Map<String, Object>> entry : materialAggregateMap.entrySet()) {
            Map<String, Object> item = entry.getValue();
            BigDecimal quantity = (BigDecimal) item.get("requiredQuantity");
            BigDecimal cost = (BigDecimal) item.get("totalCost");
            item.put("requiredQuantity", quantity.setScale(2, RoundingMode.HALF_UP));
            item.put("totalCost", cost.setScale(2, RoundingMode.HALF_UP));
        }

        List<Map<String, Object>> procurementList = new ArrayList<>(materialAggregateMap.values());

        BigDecimal totalAmount = procurementList.stream()
                .map(item -> (BigDecimal) item.get("totalCost"))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        result.put("procurementList", procurementList);
        result.put("totalAmount", totalAmount.setScale(2, RoundingMode.HALF_UP));
        result.put("date", date);

        return result;
    }
}
