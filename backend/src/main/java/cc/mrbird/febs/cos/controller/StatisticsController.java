package cc.mrbird.febs.cos.controller;

import cc.mrbird.febs.common.utils.R;
import cc.mrbird.febs.cos.entity.CommodityInfo;
import cc.mrbird.febs.cos.entity.DailySupplyPlanDetail;
import cc.mrbird.febs.cos.service.*;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author FanK
 */
@RestController
@RequestMapping("/cos/statistics")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class StatisticsController {

    private final IDailySupplyPlanDetailService dailySupplyPlanDetailService;

    private final ICommodityInfoService commodityInfoService;

    private final IMaterialInfoService materialInfoService;

    private final ICommodityMaterialRelationService commodityMaterialRelationService;

    private final IOrderInfoService orderInfoService;

    private final IOrderDetailService orderDetailService;

    /**
     * 统计供应达成率分析
     *
     * @param date 日期（格式：yyyy-MM-dd），为空则查询当天
     * @return 结果
     */
    @GetMapping("/supplyFulfillmentRateAnalysis")
    public R supplyFulfillmentRateAnalysis(String date) {
        if (date == null || date.isEmpty()) {
            date = DateUtil.formatDate(new Date());
        }

        List<DailySupplyPlanDetail> list = dailySupplyPlanDetailService.list(Wrappers.<DailySupplyPlanDetail>lambdaQuery().eq(DailySupplyPlanDetail::getDate, date));

        LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        result.put("date", date);

        if (list == null || list.isEmpty()) {
            result.put("analysisList", new ArrayList<>());
            result.put("summary", new LinkedHashMap<String, Object>() {{
                put("totalCommodities", 0);
                put("averageFulfillmentRate", 0);
                put("insufficientSupplyCount", 0);
                put("overPlannedCount", 0);
                put("normalCount", 0);
            }});
            return R.ok(result);
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

        List<Map<String, Object>> analysisList = new ArrayList<>();
        int insufficientSupplyCount = 0;
        int overPlannedCount = 0;
        int normalCount = 0;
        BigDecimal totalFulfillmentRate = BigDecimal.ZERO;

        for (DailySupplyPlanDetail detail : list) {
            CommodityInfo commodityInfo = commodityInfoMap.get(detail.getCommodityId());
            if (commodityInfo == null) {
                continue;
            }

            Integer plannedQuantity = detail.getPlannedQuantity() != null ? detail.getPlannedQuantity() : 0;
            Integer actualQuantity = detail.getActualQuantity() != null ? detail.getActualQuantity() : 0;

            BigDecimal fulfillmentRate;
            if (plannedQuantity == 0) {
                fulfillmentRate = BigDecimal.ZERO;
            } else {
                fulfillmentRate = new BigDecimal(actualQuantity)
                        .divide(new BigDecimal(plannedQuantity), 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"));
            }

            String status;
            String statusDesc;
            if (fulfillmentRate.compareTo(BigDecimal.ZERO) == 0 && plannedQuantity > 0) {
                status = "INSUFFICIENT";
                statusDesc = "供应不足";
                insufficientSupplyCount++;
            } else if (fulfillmentRate.compareTo(new BigDecimal("100")) > 0) {
                status = "OVER_PLANNED";
                statusDesc = "计划过剩";
                overPlannedCount++;
            } else {
                status = "NORMAL";
                statusDesc = "正常";
                normalCount++;
            }

            BigDecimal shortageQuantity = BigDecimal.ZERO;
            if (actualQuantity < plannedQuantity) {
                shortageQuantity = new BigDecimal(plannedQuantity - actualQuantity);
            }

            BigDecimal surplusQuantity = BigDecimal.ZERO;
            if (actualQuantity > plannedQuantity) {
                surplusQuantity = new BigDecimal(actualQuantity - plannedQuantity);
            }

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("commodityId", commodityInfo.getId());
            item.put("commodityCode", commodityInfo.getCode());
            item.put("commodityName", commodityInfo.getName());
            item.put("plannedQuantity", plannedQuantity);
            item.put("actualQuantity", actualQuantity);
            item.put("fulfillmentRate", fulfillmentRate.setScale(2, RoundingMode.HALF_UP));
            item.put("status", status);
            item.put("statusDesc", statusDesc);
            item.put("shortageQuantity", shortageQuantity.setScale(2, RoundingMode.HALF_UP));
            item.put("surplusQuantity", surplusQuantity.setScale(2, RoundingMode.HALF_UP));
            item.put("unitPrice", detail.getUnitPrice());
            item.put("shopId", commodityInfo.getShopId());

            analysisList.add(item);
            totalFulfillmentRate = totalFulfillmentRate.add(fulfillmentRate);
        }

        int totalCommodities = analysisList.size();
        BigDecimal averageFulfillmentRate = totalCommodities > 0
                ? totalFulfillmentRate.divide(new BigDecimal(totalCommodities), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalCommodities", totalCommodities);
        summary.put("averageFulfillmentRate", averageFulfillmentRate);
        summary.put("insufficientSupplyCount", insufficientSupplyCount);
        summary.put("overPlannedCount", overPlannedCount);
        summary.put("normalCount", normalCount);

        result.put("analysisList", analysisList);
        result.put("summary", summary);

        return R.ok(result);
    }

    /**
     * 统计金额偏差分析
     *
     * @param date 日期（格式：yyyy-MM-dd），为空则查询当天
     * @return 结果
     */
    @GetMapping("/queryAmountDeviationAnalysis")
    public R queryAmountDeviationAnalysis(String date) {
        if (date == null || date.isEmpty()) {
            date = DateUtil.formatDate(new Date());
        }

        List<DailySupplyPlanDetail> list = dailySupplyPlanDetailService.list(
                Wrappers.<DailySupplyPlanDetail>lambdaQuery().eq(DailySupplyPlanDetail::getDate, date)
        );

        LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        result.put("date", date);

        if (list == null || list.isEmpty()) {
            result.put("analysisList", new ArrayList<>());
            result.put("summary", new LinkedHashMap<String, Object>() {{
                put("totalCommodities", 0);
                put("totalPlannedAmount", BigDecimal.ZERO);
                put("totalActualAmount", BigDecimal.ZERO);
                put("totalDeviation", BigDecimal.ZERO);
                put("averageDeviationRate", BigDecimal.ZERO);
                put("lowSalesCount", 0);
                put("highSalesCount", 0);
                put("normalCount", 0);
            }});
            return R.ok(result);
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

        List<Map<String, Object>> analysisList = new ArrayList<>();
        int lowSalesCount = 0;
        int highSalesCount = 0;
        int normalCount = 0;
        BigDecimal totalPlannedAmount = BigDecimal.ZERO;
        BigDecimal totalActualAmount = BigDecimal.ZERO;
        BigDecimal totalDeviationRate = BigDecimal.ZERO;

        for (DailySupplyPlanDetail detail : list) {
            CommodityInfo commodityInfo = commodityInfoMap.get(detail.getCommodityId());
            if (commodityInfo == null) {
                continue;
            }

            BigDecimal plannedAmount = detail.getPlannedAmount() != null ? detail.getPlannedAmount() : BigDecimal.ZERO;
            BigDecimal actualAmount = detail.getActualAmount() != null ? detail.getActualAmount() : BigDecimal.ZERO;

            BigDecimal amountDeviation = actualAmount.subtract(plannedAmount);

            BigDecimal deviationRate;
            if (plannedAmount.compareTo(BigDecimal.ZERO) == 0) {
                deviationRate = BigDecimal.ZERO;
            } else {
                deviationRate = amountDeviation
                        .divide(plannedAmount, 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"));
            }

            String status;
            String statusDesc;
            String possibleReason;

            if (deviationRate.compareTo(new BigDecimal("-50")) < 0) {
                status = "LOW_SALES";
                statusDesc = "销售额偏低";
                possibleReason = "可能存在缺货、滞销或退单情况";
                lowSalesCount++;
            } else if (deviationRate.compareTo(new BigDecimal("50")) > 0) {
                status = "HIGH_SALES";
                statusDesc = "销售额偏高";
                possibleReason = "可能存在溢价销售或超量供应";
                highSalesCount++;
            } else {
                status = "NORMAL";
                statusDesc = "正常";
                possibleReason = "销售情况符合预期";
                normalCount++;
            }

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("commodityId", commodityInfo.getId());
            item.put("commodityCode", commodityInfo.getCode());
            item.put("commodityName", commodityInfo.getName());
            item.put("plannedAmount", plannedAmount.setScale(2, RoundingMode.HALF_UP));
            item.put("actualAmount", actualAmount.setScale(2, RoundingMode.HALF_UP));
            item.put("amountDeviation", amountDeviation.setScale(2, RoundingMode.HALF_UP));
            item.put("deviationRate", deviationRate.setScale(2, RoundingMode.HALF_UP));
            item.put("status", status);
            item.put("statusDesc", statusDesc);
            item.put("possibleReason", possibleReason);
            item.put("unitPrice", detail.getUnitPrice());
            item.put("shopId", commodityInfo.getShopId());
            item.put("plannedQuantity", detail.getPlannedQuantity());
            item.put("actualQuantity", detail.getActualQuantity());

            analysisList.add(item);

            totalPlannedAmount = totalPlannedAmount.add(plannedAmount);
            totalActualAmount = totalActualAmount.add(actualAmount);
            totalDeviationRate = totalDeviationRate.add(deviationRate.abs());
        }

        int totalCommodities = analysisList.size();
        BigDecimal totalDeviation = totalActualAmount.subtract(totalPlannedAmount);
        BigDecimal averageDeviationRate = totalCommodities > 0
                ? totalDeviationRate.divide(new BigDecimal(totalCommodities), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal overallDeviationRate;
        if (totalPlannedAmount.compareTo(BigDecimal.ZERO) == 0) {
            overallDeviationRate = BigDecimal.ZERO;
        } else {
            overallDeviationRate = totalDeviation
                    .divide(totalPlannedAmount, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
        }

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalCommodities", totalCommodities);
        summary.put("totalPlannedAmount", totalPlannedAmount.setScale(2, RoundingMode.HALF_UP));
        summary.put("totalActualAmount", totalActualAmount.setScale(2, RoundingMode.HALF_UP));
        summary.put("totalDeviation", totalDeviation.setScale(2, RoundingMode.HALF_UP));
        summary.put("overallDeviationRate", overallDeviationRate.setScale(2, RoundingMode.HALF_UP));
        summary.put("averageDeviationRate", averageDeviationRate);
        summary.put("lowSalesCount", lowSalesCount);
        summary.put("highSalesCount", highSalesCount);
        summary.put("normalCount", normalCount);

        result.put("analysisList", analysisList);
        result.put("summary", summary);

        return R.ok(result);
    }

    /**
     * 毛利与毛利率统计
     *
     * @param date 日期（格式：yyyy-MM-dd），为空则查询当天
     * @return 结果
     */
    @GetMapping("/queryGrossProfitMarginStatistics")
    public R queryGrossProfitMarginStatistics(String date) {
        if (date == null || date.isEmpty()) {
            date = DateUtil.formatDate(new Date());
        }

        List<DailySupplyPlanDetail> list = dailySupplyPlanDetailService.list(
                Wrappers.<DailySupplyPlanDetail>lambdaQuery().eq(DailySupplyPlanDetail::getDate, date)
        );

        LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        result.put("date", date);

        if (list == null || list.isEmpty()) {
            result.put("analysisList", new ArrayList<>());
            result.put("summary", new LinkedHashMap<String, Object>() {{
                put("totalCommodities", 0);
                put("totalActualAmount", BigDecimal.ZERO);
                put("totalMaterialCost", BigDecimal.ZERO);
                put("totalGrossProfit", BigDecimal.ZERO);
                put("averageGrossProfitMargin", BigDecimal.ZERO);
                put("highProfitCount", 0);
                put("mediumProfitCount", 0);
                put("lowProfitCount", 0);
            }});
            return R.ok(result);
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

        List<Map<String, Object>> analysisList = new ArrayList<>();
        int highProfitCount = 0;
        int mediumProfitCount = 0;
        int lowProfitCount = 0;
        BigDecimal totalActualAmount = BigDecimal.ZERO;
        BigDecimal totalMaterialCost = BigDecimal.ZERO;
        BigDecimal totalGrossProfit = BigDecimal.ZERO;
        BigDecimal totalGrossProfitMargin = BigDecimal.ZERO;

        for (DailySupplyPlanDetail detail : list) {
            CommodityInfo commodityInfo = commodityInfoMap.get(detail.getCommodityId());
            if (commodityInfo == null) {
                continue;
            }

            BigDecimal actualAmount = detail.getActualAmount() != null ? detail.getActualAmount() : BigDecimal.ZERO;
            BigDecimal materialCost = detail.getEstimatedMaterialCost() != null ? detail.getEstimatedMaterialCost() : BigDecimal.ZERO;

            BigDecimal grossProfit = actualAmount.subtract(materialCost);

            BigDecimal grossProfitMargin;
            if (actualAmount.compareTo(BigDecimal.ZERO) == 0) {
                grossProfitMargin = BigDecimal.ZERO;
            } else {
                grossProfitMargin = grossProfit
                        .divide(actualAmount, 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"));
            }

            String profitLevel;
            String profitLevelDesc;
            String productType;

            if (grossProfitMargin.compareTo(new BigDecimal("60")) >= 0) {
                profitLevel = "HIGH";
                profitLevelDesc = "高毛利";
                productType = "明星产品";
                highProfitCount++;
            } else if (grossProfitMargin.compareTo(new BigDecimal("30")) >= 0) {
                profitLevel = "MEDIUM";
                profitLevelDesc = "中等毛利";
                productType = "常规产品";
                mediumProfitCount++;
            } else {
                profitLevel = "LOW";
                profitLevelDesc = "低毛利";
                productType = "引流产品";
                lowProfitCount++;
            }

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("commodityId", commodityInfo.getId());
            item.put("commodityCode", commodityInfo.getCode());
            item.put("commodityName", commodityInfo.getName());
            item.put("actualAmount", actualAmount.setScale(2, RoundingMode.HALF_UP));
            item.put("materialCost", materialCost.setScale(2, RoundingMode.HALF_UP));
            item.put("grossProfit", grossProfit.setScale(2, RoundingMode.HALF_UP));
            item.put("grossProfitMargin", grossProfitMargin.setScale(2, RoundingMode.HALF_UP));
            item.put("profitLevel", profitLevel);
            item.put("profitLevelDesc", profitLevelDesc);
            item.put("productType", productType);
            item.put("unitPrice", detail.getUnitPrice());
            item.put("shopId", commodityInfo.getShopId());
            item.put("actualQuantity", detail.getActualQuantity());
            item.put("plannedQuantity", detail.getPlannedQuantity());

            analysisList.add(item);

            totalActualAmount = totalActualAmount.add(actualAmount);
            totalMaterialCost = totalMaterialCost.add(materialCost);
            totalGrossProfit = totalGrossProfit.add(grossProfit);
            totalGrossProfitMargin = totalGrossProfitMargin.add(grossProfitMargin);
        }

        int totalCommodities = analysisList.size();
        BigDecimal averageGrossProfitMargin = totalCommodities > 0
                ? totalGrossProfitMargin.divide(new BigDecimal(totalCommodities), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal overallGrossProfitMargin;
        if (totalActualAmount.compareTo(BigDecimal.ZERO) == 0) {
            overallGrossProfitMargin = BigDecimal.ZERO;
        } else {
            overallGrossProfitMargin = totalGrossProfit
                    .divide(totalActualAmount, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
        }

        Map<String, Object> topProfitProducts = new LinkedHashMap<>();
        List<Map<String, Object>> sortedByProfit = new ArrayList<>(analysisList);
        sortedByProfit.sort((a, b) -> {
            BigDecimal marginA = (BigDecimal) a.get("grossProfitMargin");
            BigDecimal marginB = (BigDecimal) b.get("grossProfitMargin");
            return marginB.compareTo(marginA);
        });

        if (!sortedByProfit.isEmpty()) {
            int topCount = Math.min(5, sortedByProfit.size());
            topProfitProducts.put("topProducts", sortedByProfit.subList(0, topCount));
        } else {
            topProfitProducts.put("topProducts", new ArrayList<>());
        }

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalCommodities", totalCommodities);
        summary.put("totalActualAmount", totalActualAmount.setScale(2, RoundingMode.HALF_UP));
        summary.put("totalMaterialCost", totalMaterialCost.setScale(2, RoundingMode.HALF_UP));
        summary.put("totalGrossProfit", totalGrossProfit.setScale(2, RoundingMode.HALF_UP));
        summary.put("overallGrossProfitMargin", overallGrossProfitMargin.setScale(2, RoundingMode.HALF_UP));
        summary.put("averageGrossProfitMargin", averageGrossProfitMargin);
        summary.put("highProfitCount", highProfitCount);
        summary.put("mediumProfitCount", mediumProfitCount);
        summary.put("lowProfitCount", lowProfitCount);

        result.put("analysisList", analysisList);
        result.put("summary", summary);
        result.put("topProfitProducts", topProfitProducts);

        return R.ok(result);
    }

    /**
     * 成本占比分析
     *
     * @param date 日期（格式：yyyy-MM-dd），为空则查询当天
     * @return 结果
     */
    @GetMapping("/queryCostPercentageAnalysis")
    public R queryCostPercentageAnalysis(String date) {
        if (date == null || date.isEmpty()) {
            date = DateUtil.formatDate(new Date());
        }

        List<DailySupplyPlanDetail> list = dailySupplyPlanDetailService.list(
                Wrappers.<DailySupplyPlanDetail>lambdaQuery().eq(DailySupplyPlanDetail::getDate, date)
        );

        LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        result.put("date", date);

        if (list == null || list.isEmpty()) {
            result.put("analysisList", new ArrayList<>());
            result.put("summary", new LinkedHashMap<String, Object>() {{
                put("totalCommodities", 0);
                put("totalActualAmount", BigDecimal.ZERO);
                put("totalMaterialCost", BigDecimal.ZERO);
                put("overallCostPercentage", BigDecimal.ZERO);
                put("averageCostPercentage", BigDecimal.ZERO);
                put("highCostCount", 0);
                put("mediumCostCount", 0);
                put("lowCostCount", 0);
                put("abnormalCostCount", 0);
            }});
            return R.ok(result);
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

        List<Map<String, Object>> analysisList = new ArrayList<>();
        int highCostCount = 0;
        int mediumCostCount = 0;
        int lowCostCount = 0;
        int abnormalCostCount = 0;
        BigDecimal totalActualAmount = BigDecimal.ZERO;
        BigDecimal totalMaterialCost = BigDecimal.ZERO;
        BigDecimal totalCostPercentage = BigDecimal.ZERO;

        for (DailySupplyPlanDetail detail : list) {
            CommodityInfo commodityInfo = commodityInfoMap.get(detail.getCommodityId());
            if (commodityInfo == null) {
                continue;
            }

            BigDecimal actualAmount = detail.getActualAmount() != null ? detail.getActualAmount() : BigDecimal.ZERO;
            BigDecimal materialCost = detail.getEstimatedMaterialCost() != null ? detail.getEstimatedMaterialCost() : BigDecimal.ZERO;

            BigDecimal costPercentage;
            if (actualAmount.compareTo(BigDecimal.ZERO) == 0) {
                costPercentage = BigDecimal.ZERO;
            } else {
                costPercentage = materialCost
                        .divide(actualAmount, 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"));
            }

            String costLevel;
            String costLevelDesc;
            String riskLevel;
            String suggestion;

            if (costPercentage.compareTo(new BigDecimal("70")) >= 0) {
                costLevel = "HIGH";
                costLevelDesc = "高成本占比";
                riskLevel = "高风险";
                suggestion = "建议优化原材料采购或调整售价，成本占比过高影响利润";
                highCostCount++;
                abnormalCostCount++;
            } else if (costPercentage.compareTo(new BigDecimal("50")) >= 0) {
                costLevel = "MEDIUM_HIGH";
                costLevelDesc = "中高成本占比";
                riskLevel = "中风险";
                suggestion = "关注成本控制，寻找降低成本的方案";
                mediumCostCount++;
            } else if (costPercentage.compareTo(new BigDecimal("30")) >= 0) {
                costLevel = "MEDIUM";
                costLevelDesc = "中等成本占比";
                riskLevel = "低风险";
                suggestion = "成本控制在合理范围内";
                mediumCostCount++;
            } else {
                costLevel = "LOW";
                costLevelDesc = "低成本占比";
                riskLevel = "正常";
                suggestion = "成本控制良好，有较高利润空间";
                lowCostCount++;
            }

            if (actualAmount.compareTo(BigDecimal.ZERO) == 0 && materialCost.compareTo(BigDecimal.ZERO) > 0) {
                riskLevel = "异常";
                suggestion = "存在成本但无销售额，可能存在损耗或浪费问题";
                abnormalCostCount++;
            }

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("commodityId", commodityInfo.getId());
            item.put("commodityCode", commodityInfo.getCode());
            item.put("commodityName", commodityInfo.getName());
            item.put("actualAmount", actualAmount.setScale(2, RoundingMode.HALF_UP));
            item.put("materialCost", materialCost.setScale(2, RoundingMode.HALF_UP));
            item.put("costPercentage", costPercentage.setScale(2, RoundingMode.HALF_UP));
            item.put("costLevel", costLevel);
            item.put("costLevelDesc", costLevelDesc);
            item.put("riskLevel", riskLevel);
            item.put("suggestion", suggestion);
            item.put("unitPrice", detail.getUnitPrice());
            item.put("shopId", commodityInfo.getShopId());
            item.put("actualQuantity", detail.getActualQuantity());
            item.put("plannedQuantity", detail.getPlannedQuantity());

            analysisList.add(item);

            totalActualAmount = totalActualAmount.add(actualAmount);
            totalMaterialCost = totalMaterialCost.add(materialCost);
            totalCostPercentage = totalCostPercentage.add(costPercentage);
        }

        int totalCommodities = analysisList.size();
        BigDecimal overallCostPercentage;
        if (totalActualAmount.compareTo(BigDecimal.ZERO) == 0) {
            overallCostPercentage = BigDecimal.ZERO;
        } else {
            overallCostPercentage = totalMaterialCost
                    .divide(totalActualAmount, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
        }

        BigDecimal averageCostPercentage = totalCommodities > 0
                ? totalCostPercentage.divide(new BigDecimal(totalCommodities), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        Map<String, Object> highCostWarning = new LinkedHashMap<>();
        List<Map<String, Object>> sortedByCost = new ArrayList<>(analysisList);
        sortedByCost.sort((a, b) -> {
            BigDecimal percentageA = (BigDecimal) a.get("costPercentage");
            BigDecimal percentageB = (BigDecimal) b.get("costPercentage");
            return percentageB.compareTo(percentageA);
        });

        if (!sortedByCost.isEmpty()) {
            List<Map<String, Object>> highCostItems = sortedByCost.stream()
                    .filter(item -> ((String) item.get("costLevel")).equals("HIGH"))
                    .collect(Collectors.toList());
            highCostWarning.put("highCostItems", highCostItems.isEmpty() ? new ArrayList<>() : highCostItems);

            List<Map<String, Object>> abnormalItems = sortedByCost.stream()
                    .filter(item -> {
                        BigDecimal amount = (BigDecimal) item.get("actualAmount");
                        BigDecimal cost = (BigDecimal) item.get("materialCost");
                        return amount.compareTo(BigDecimal.ZERO) == 0 && cost.compareTo(BigDecimal.ZERO) > 0;
                    })
                    .collect(Collectors.toList());
            highCostWarning.put("abnormalItems", abnormalItems);
        } else {
            highCostWarning.put("highCostItems", new ArrayList<>());
            highCostWarning.put("abnormalItems", new ArrayList<>());
        }

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalCommodities", totalCommodities);
        summary.put("totalActualAmount", totalActualAmount.setScale(2, RoundingMode.HALF_UP));
        summary.put("totalMaterialCost", totalMaterialCost.setScale(2, RoundingMode.HALF_UP));
        summary.put("overallCostPercentage", overallCostPercentage.setScale(2, RoundingMode.HALF_UP));
        summary.put("averageCostPercentage", averageCostPercentage);
        summary.put("highCostCount", highCostCount);
        summary.put("mediumCostCount", mediumCostCount);
        summary.put("lowCostCount", lowCostCount);
        summary.put("abnormalCostCount", abnormalCostCount);

        result.put("analysisList", analysisList);
        result.put("summary", summary);
        result.put("highCostWarning", highCostWarning);

        return R.ok(result);
    }


    /**
     * 销售排行榜
     *
     * @param date       日期（格式：yyyy-MM-dd 或 yyyy-MM），为空则查询当月
     * @param sortBy     排序方式（quantity: 按实际供应量, amount: 按实际金额），默认为amount
     * @param periodType 统计周期类型（day: 按天, month: 按月），默认为month
     * @param topN       返回前N名，默认为10
     * @return 结果
     */
    @GetMapping("/querySalesRanking")
    public R querySalesRanking(String date, String sortBy, String periodType, Integer topN) {
        if (sortBy == null || sortBy.isEmpty()) {
            sortBy = "amount";
        }

        if (periodType == null || periodType.isEmpty()) {
            periodType = "month";
        }

        if (topN == null || topN <= 0) {
            topN = 10;
        }

        if (date == null || date.isEmpty()) {
            if ("month".equals(periodType)) {
                date = DateUtil.format(new Date(), "yyyy-MM");
            } else {
                date = DateUtil.formatDate(new Date());
            }
        }

        List<DailySupplyPlanDetail> list;
        String periodLabel;

        if ("month".equals(periodType)) {
            String monthPrefix = date.length() >= 7 ? date.substring(0, 7) : date;
            list = dailySupplyPlanDetailService.list(
                    Wrappers.<DailySupplyPlanDetail>lambdaQuery()
                            .likeRight(DailySupplyPlanDetail::getDate, monthPrefix)
            );
            periodLabel = monthPrefix;
        } else {
            list = dailySupplyPlanDetailService.list(
                    Wrappers.<DailySupplyPlanDetail>lambdaQuery()
                            .eq(DailySupplyPlanDetail::getDate, date)
            );
            periodLabel = date;
        }

        LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        result.put("period", periodLabel);
        result.put("periodType", periodType);
        result.put("sortBy", sortBy);

        if (list == null || list.isEmpty()) {
            result.put("rankingList", new ArrayList<>());
            result.put("summary", new LinkedHashMap<String, Object>() {{
                put("totalCommodities", 0);
                put("totalActualQuantity", 0);
                put("totalActualAmount", BigDecimal.ZERO);
            }});
            return R.ok(result);
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

        Map<Integer, DailySupplyPlanDetail> aggregatedMap = new LinkedHashMap<>();
        for (DailySupplyPlanDetail detail : list) {
            Integer commodityId = detail.getCommodityId();
            if (aggregatedMap.containsKey(commodityId)) {
                DailySupplyPlanDetail existing = aggregatedMap.get(commodityId);
                Integer existingActualQty = existing.getActualQuantity() != null ? existing.getActualQuantity() : 0;
                Integer newActualQty = detail.getActualQuantity() != null ? detail.getActualQuantity() : 0;
                existing.setActualQuantity(existingActualQty + newActualQty);

                BigDecimal existingActualAmt = existing.getActualAmount() != null ? existing.getActualAmount() : BigDecimal.ZERO;
                BigDecimal newActualAmt = detail.getActualAmount() != null ? detail.getActualAmount() : BigDecimal.ZERO;
                existing.setActualAmount(existingActualAmt.add(newActualAmt));

                BigDecimal existingPlannedAmt = existing.getPlannedAmount() != null ? existing.getPlannedAmount() : BigDecimal.ZERO;
                BigDecimal newPlannedAmt = detail.getPlannedAmount() != null ? detail.getPlannedAmount() : BigDecimal.ZERO;
                existing.setPlannedAmount(existingPlannedAmt.add(newPlannedAmt));

                BigDecimal existingMaterialCost = existing.getEstimatedMaterialCost() != null ? existing.getEstimatedMaterialCost() : BigDecimal.ZERO;
                BigDecimal newMaterialCost = detail.getEstimatedMaterialCost() != null ? detail.getEstimatedMaterialCost() : BigDecimal.ZERO;
                existing.setEstimatedMaterialCost(existingMaterialCost.add(newMaterialCost));
            } else {
                aggregatedMap.put(commodityId, detail);
            }
        }

        List<Map<String, Object>> rankingList = new ArrayList<>();
        int totalActualQuantity = 0;
        BigDecimal totalActualAmount = BigDecimal.ZERO;

        for (Map.Entry<Integer, DailySupplyPlanDetail> entry : aggregatedMap.entrySet()) {
            DailySupplyPlanDetail detail = entry.getValue();
            CommodityInfo commodityInfo = commodityInfoMap.get(detail.getCommodityId());
            if (commodityInfo == null) {
                continue;
            }

            Integer actualQuantity = detail.getActualQuantity() != null ? detail.getActualQuantity() : 0;
            BigDecimal actualAmount = detail.getActualAmount() != null ? detail.getActualAmount() : BigDecimal.ZERO;
            BigDecimal plannedAmount = detail.getPlannedAmount() != null ? detail.getPlannedAmount() : BigDecimal.ZERO;
            BigDecimal materialCost = detail.getEstimatedMaterialCost() != null ? detail.getEstimatedMaterialCost() : BigDecimal.ZERO;

            BigDecimal grossProfit = actualAmount.subtract(materialCost);
            BigDecimal grossProfitMargin;
            if (actualAmount.compareTo(BigDecimal.ZERO) == 0) {
                grossProfitMargin = BigDecimal.ZERO;
            } else {
                grossProfitMargin = grossProfit
                        .divide(actualAmount, 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"));
            }

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("rank", 0);
            item.put("commodityId", commodityInfo.getId());
            item.put("commodityCode", commodityInfo.getCode());
            item.put("commodityName", commodityInfo.getName());
            item.put("shopId", commodityInfo.getShopId());
            item.put("actualQuantity", actualQuantity);
            item.put("actualAmount", actualAmount.setScale(2, RoundingMode.HALF_UP));
            item.put("plannedAmount", plannedAmount.setScale(2, RoundingMode.HALF_UP));
            item.put("materialCost", materialCost.setScale(2, RoundingMode.HALF_UP));
            item.put("grossProfit", grossProfit.setScale(2, RoundingMode.HALF_UP));
            item.put("grossProfitMargin", grossProfitMargin.setScale(2, RoundingMode.HALF_UP));
            item.put("unitPrice", detail.getUnitPrice());

            rankingList.add(item);

            totalActualQuantity += actualQuantity;
            totalActualAmount = totalActualAmount.add(actualAmount);
        }

        if ("quantity".equals(sortBy)) {
            rankingList.sort((a, b) -> {
                Integer qtyA = (Integer) a.get("actualQuantity");
                Integer qtyB = (Integer) b.get("actualQuantity");
                return qtyB.compareTo(qtyA);
            });
        } else {
            rankingList.sort((a, b) -> {
                BigDecimal amtA = (BigDecimal) a.get("actualAmount");
                BigDecimal amtB = (BigDecimal) b.get("actualAmount");
                return amtB.compareTo(amtA);
            });
        }

        int finalTopN = Math.min(topN, rankingList.size());
        List<Map<String, Object>> topRankingList = rankingList.subList(0, finalTopN);

        for (int i = 0; i < topRankingList.size(); i++) {
            topRankingList.get(i).put("rank", i + 1);
        }

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalCommodities", rankingList.size());
        summary.put("totalActualQuantity", totalActualQuantity);
        summary.put("totalActualAmount", totalActualAmount.setScale(2, RoundingMode.HALF_UP));

        if (!topRankingList.isEmpty()) {
            Map<String, Object> topItem = topRankingList.get(0);
            String finalSortBy = sortBy;
            summary.put("topCommodity", new LinkedHashMap<String, Object>() {{
                put("commodityId", topItem.get("commodityId"));
                put("commodityName", topItem.get("commodityName"));
                put("value", "quantity".equals(finalSortBy) ? topItem.get("actualQuantity") : topItem.get("actualAmount"));
            }});
        }

        result.put("rankingList", topRankingList);
        result.put("summary", summary);

        return R.ok(result);
    }

    /**
     * 同环比分析
     *
     * @param analysisType 分析类型（week: 周对比, month: 月对比），默认为month
     * @return 结果
     */
    @GetMapping("/queryAnalysis")
    public R queryAnalysis(String analysisType) {
        if (analysisType == null || analysisType.isEmpty()) {
            analysisType = "month";
        }

        LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        result.put("analysisType", analysisType);

        if ("week".equals(analysisType)) {
            return R.ok(queryWeekAnalysis(result));
        } else {
            return R.ok(queryMonthAnalysis(result));
        }
    }

    /**
     * 周对比分析
     */
    private LinkedHashMap<String, Object> queryWeekAnalysis(LinkedHashMap<String, Object> result) {
        Date now = new Date();

        String currentWeekStart = DateUtil.formatDate(DateUtil.beginOfWeek(now));
        String currentWeekEnd = DateUtil.formatDate(DateUtil.endOfWeek(now));

        String lastWeekStart = DateUtil.formatDate(DateUtil.beginOfWeek(DateUtil.lastWeek()));
        String lastWeekEnd = DateUtil.formatDate(DateUtil.endOfWeek(DateUtil.lastWeek()));

        List<DailySupplyPlanDetail> currentWeekList = dailySupplyPlanDetailService.list(
                Wrappers.<DailySupplyPlanDetail>lambdaQuery()
                        .ge(DailySupplyPlanDetail::getDate, currentWeekStart)
                        .le(DailySupplyPlanDetail::getDate, currentWeekEnd)
        );

        List<DailySupplyPlanDetail> lastWeekList = dailySupplyPlanDetailService.list(
                Wrappers.<DailySupplyPlanDetail>lambdaQuery()
                        .ge(DailySupplyPlanDetail::getDate, lastWeekStart)
                        .le(DailySupplyPlanDetail::getDate, lastWeekEnd)
        );

        Map<String, Object> currentWeekData = aggregatePeriodData(currentWeekList);
        Map<String, Object> lastWeekData = aggregatePeriodData(lastWeekList);

        Map<String, Object> quantityComparison = calculateGrowthRate(
                (Integer) currentWeekData.get("totalActualQuantity"),
                (Integer) lastWeekData.get("totalActualQuantity")
        );

        Map<String, Object> amountComparison = calculateGrowthRate(
                (BigDecimal) currentWeekData.get("totalActualAmount"),
                (BigDecimal) lastWeekData.get("totalActualAmount")
        );

        Map<String, Object> plannedAmountComparison = calculateGrowthRate(
                (BigDecimal) currentWeekData.get("totalPlannedAmount"),
                (BigDecimal) lastWeekData.get("totalPlannedAmount")
        );

        Map<String, Object> materialCostComparison = calculateGrowthRate(
                (BigDecimal) currentWeekData.get("totalMaterialCost"),
                (BigDecimal) lastWeekData.get("totalMaterialCost")
        );

        result.put("currentWeek", new LinkedHashMap<String, Object>() {{
            put("startDate", currentWeekStart);
            put("endDate", currentWeekEnd);
            put("data", currentWeekData);
        }});

        result.put("lastWeek", new LinkedHashMap<String, Object>() {{
            put("startDate", lastWeekStart);
            put("endDate", lastWeekEnd);
            put("data", lastWeekData);
        }});

        result.put("comparison", new LinkedHashMap<String, Object>() {{
            put("actualQuantity", quantityComparison);
            put("actualAmount", amountComparison);
            put("plannedAmount", plannedAmountComparison);
            put("materialCost", materialCostComparison);
        }});

        return result;
    }

    /**
     * 月对比分析
     */
    private LinkedHashMap<String, Object> queryMonthAnalysis(LinkedHashMap<String, Object> result) {
        Date now = new Date();

        String currentMonth = DateUtil.format(now, "yyyy-MM");
        String lastMonth = DateUtil.format(DateUtil.lastMonth(), "yyyy-MM");

        List<DailySupplyPlanDetail> currentMonthList = dailySupplyPlanDetailService.list(
                Wrappers.<DailySupplyPlanDetail>lambdaQuery()
                        .likeRight(DailySupplyPlanDetail::getDate, currentMonth)
        );

        List<DailySupplyPlanDetail> lastMonthList = dailySupplyPlanDetailService.list(
                Wrappers.<DailySupplyPlanDetail>lambdaQuery()
                        .likeRight(DailySupplyPlanDetail::getDate, lastMonth)
        );

        Map<String, Object> currentMonthData = aggregatePeriodData(currentMonthList);
        Map<String, Object> lastMonthData = aggregatePeriodData(lastMonthList);

        Map<String, Object> quantityComparison = calculateGrowthRate(
                (Integer) currentMonthData.get("totalActualQuantity"),
                (Integer) lastMonthData.get("totalActualQuantity")
        );

        Map<String, Object> amountComparison = calculateGrowthRate(
                (BigDecimal) currentMonthData.get("totalActualAmount"),
                (BigDecimal) lastMonthData.get("totalActualAmount")
        );

        Map<String, Object> plannedAmountComparison = calculateGrowthRate(
                (BigDecimal) currentMonthData.get("totalPlannedAmount"),
                (BigDecimal) lastMonthData.get("totalPlannedAmount")
        );

        Map<String, Object> materialCostComparison = calculateGrowthRate(
                (BigDecimal) currentMonthData.get("totalMaterialCost"),
                (BigDecimal) lastMonthData.get("totalMaterialCost")
        );

        result.put("currentMonth", new LinkedHashMap<String, Object>() {{
            put("month", currentMonth);
            put("data", currentMonthData);
        }});

        result.put("lastMonth", new LinkedHashMap<String, Object>() {{
            put("month", lastMonth);
            put("data", lastMonthData);
        }});

        result.put("comparison", new LinkedHashMap<String, Object>() {{
            put("actualQuantity", quantityComparison);
            put("actualAmount", amountComparison);
            put("plannedAmount", plannedAmountComparison);
            put("materialCost", materialCostComparison);
        }});

        return result;
    }

    /**
     * 聚合周期数据
     */
    private Map<String, Object> aggregatePeriodData(List<DailySupplyPlanDetail> dataList) {
        int totalActualQuantity = 0;
        BigDecimal totalActualAmount = BigDecimal.ZERO;
        BigDecimal totalPlannedAmount = BigDecimal.ZERO;
        BigDecimal totalMaterialCost = BigDecimal.ZERO;
        int commodityCount = 0;

        if (dataList != null && !dataList.isEmpty()) {
            Set<Integer> commodityIds = new HashSet<>();
            for (DailySupplyPlanDetail detail : dataList) {
                Integer actualQty = detail.getActualQuantity() != null ? detail.getActualQuantity() : 0;
                BigDecimal actualAmt = detail.getActualAmount() != null ? detail.getActualAmount() : BigDecimal.ZERO;
                BigDecimal plannedAmt = detail.getPlannedAmount() != null ? detail.getPlannedAmount() : BigDecimal.ZERO;
                BigDecimal materialCost = detail.getEstimatedMaterialCost() != null ? detail.getEstimatedMaterialCost() : BigDecimal.ZERO;

                totalActualQuantity += actualQty;
                totalActualAmount = totalActualAmount.add(actualAmt);
                totalPlannedAmount = totalPlannedAmount.add(plannedAmt);
                totalMaterialCost = totalMaterialCost.add(materialCost);

                commodityIds.add(detail.getCommodityId());
            }
            commodityCount = commodityIds.size();
        }

        BigDecimal grossProfit = totalActualAmount.subtract(totalMaterialCost);
        BigDecimal grossProfitMargin;
        if (totalActualAmount.compareTo(BigDecimal.ZERO) == 0) {
            grossProfitMargin = BigDecimal.ZERO;
        } else {
            grossProfitMargin = grossProfit
                    .divide(totalActualAmount, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("totalActualQuantity", totalActualQuantity);
        data.put("totalActualAmount", totalActualAmount.setScale(2, RoundingMode.HALF_UP));
        data.put("totalPlannedAmount", totalPlannedAmount.setScale(2, RoundingMode.HALF_UP));
        data.put("totalMaterialCost", totalMaterialCost.setScale(2, RoundingMode.HALF_UP));
        data.put("grossProfit", grossProfit.setScale(2, RoundingMode.HALF_UP));
        data.put("grossProfitMargin", grossProfitMargin.setScale(2, RoundingMode.HALF_UP));
        data.put("commodityCount", commodityCount);
        data.put("recordCount", dataList != null ? dataList.size() : 0);

        return data;
    }

    /**
     * 计算增长率
     */
    private Map<String, Object> calculateGrowthRate(Number currentValue, Number lastValue) {
        Map<String, Object> comparison = new LinkedHashMap<>();

        comparison.put("currentValue", currentValue);
        comparison.put("lastValue", lastValue);

        BigDecimal current = currentValue instanceof BigDecimal ? (BigDecimal) currentValue : new BigDecimal(currentValue.toString());
        BigDecimal last = lastValue instanceof BigDecimal ? (BigDecimal) lastValue : new BigDecimal(lastValue.toString());

        BigDecimal change = current.subtract(last);
        comparison.put("change", change.setScale(2, RoundingMode.HALF_UP));

        BigDecimal growthRate;
        if (last.compareTo(BigDecimal.ZERO) == 0) {
            growthRate = BigDecimal.ZERO;
        } else {
            growthRate = change
                    .divide(last, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
        }
        comparison.put("growthRate", growthRate.setScale(2, RoundingMode.HALF_UP));

        String trend;
        String trendDesc;
        if (growthRate.compareTo(BigDecimal.ZERO) > 0) {
            trend = "UP";
            trendDesc = "增长";
        } else if (growthRate.compareTo(BigDecimal.ZERO) < 0) {
            trend = "DOWN";
            trendDesc = "下降";
        } else {
            trend = "FLAT";
            trendDesc = "持平";
        }
        comparison.put("trend", trend);
        comparison.put("trendDesc", trendDesc);

        return comparison;
    }
}
