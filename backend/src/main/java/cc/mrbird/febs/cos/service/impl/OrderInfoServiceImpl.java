package cc.mrbird.febs.cos.service.impl;

import cc.mrbird.febs.cos.dao.ShopInfoMapper;
import cc.mrbird.febs.cos.entity.*;
import cc.mrbird.febs.cos.dao.OrderInfoMapper;
import cc.mrbird.febs.cos.service.IBulletinInfoService;
import cc.mrbird.febs.cos.service.ICommodityInfoService;
import cc.mrbird.febs.cos.service.IOrderInfoService;
import cc.mrbird.febs.cos.service.IUserInfoService;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author FanK
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OrderInfoServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo> implements IOrderInfoService {

    private final IUserInfoService userInfoService;

    private final ICommodityInfoService commodityInfoService;

    private final ShopInfoMapper shopInfoMapper;

    private final IBulletinInfoService bulletinInfoService;

    /**
     * 分页查询订单信息
     *
     * @param page      分页对象
     * @param orderInfo 订单信息
     * @return 结果
     */
    @Override
    public IPage<LinkedHashMap<String, Object>> getOrderByPage(Page<OrderInfo> page, OrderInfo orderInfo) {
        return baseMapper.getOrderByPage(page, orderInfo);
    }

    /**
     * 根据用户ID获取订单
     *
     * @param userId 用户ID
     * @return 结果
     */
    @Override
    public List<LinkedHashMap<String, Object>> orderListByUserId(Integer userId) {
        return baseMapper.orderListByUserId(userId);
    }

    /**
     * 获取主页数据
     *
     * @return 结果
     */
    @Override
    public LinkedHashMap<String, Object> home() {
        LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        result.put("userNum", userInfoService.count());
        result.put("userAuditNum", userInfoService.count(Wrappers.<UserInfo>lambdaQuery().eq(UserInfo::getType, 2)));
        result.put("orderNum", this.count(Wrappers.<OrderInfo>lambdaQuery().ne(OrderInfo::getOrderStatus, 0)));
        result.put("commodityNum", this.commodityInfoService.count());
        // 订单统计
        result.put("orderRevenueData", baseMapper.orderRevenueData());
        // 本月订单量 本月收益
        result.put("orderMonthRevenue", baseMapper.orderMonthRevenue());
        // 本月收益占比
        result.put("orderPriceRate", baseMapper.orderPriceRate());
        // 商铺收益订单统计
        result.put("shopOrderRate", baseMapper.shopOrderRate());
        // 所有店铺信息
        List<LinkedHashMap<String, Object>> shopList = baseMapper.shopList();
        result.put("shopList", shopList);
        return result;
    }

    /**
     * 根据商铺获取订单统计
     *
     * @param shopId 商铺ID
     * @return 结果
     */
    @Override
    public List<LinkedHashMap<String, Object>> shopOrderRateByComm(Integer shopId) {
        return baseMapper.shopOrderRateByComm(shopId);
    }

    /**
     * 商家首页统计
     *
     * @param shopId 商家ID
     * @return 结果
     */
    @Override
    public LinkedHashMap<String, Object> selectHomeDataByShop(Integer shopId) {
        // 返回数据
        LinkedHashMap<String, Object> result = new LinkedHashMap<>();

        // 商家信息
        ShopInfo shopInfo = shopInfoMapper.selectOne(Wrappers.<ShopInfo>lambdaQuery().eq(ShopInfo::getSysUserId, shopId));

        List<OrderInfo> orderInfoList = this.list(Wrappers.<OrderInfo>lambdaQuery().eq(OrderInfo::getShopId, shopInfo.getId()).eq(OrderInfo::getOrderStatus, 1).or().eq(OrderInfo::getOrderStatus, 3));
        // 总订单数量
        result.put("orderCode", orderInfoList.size());
        // 总收益
        result.put("orderPrice", orderInfoList.stream().map(e -> NumberUtil.mul(e.getOrderPrice(), e.getUserNum())).reduce(BigDecimal.ZERO, BigDecimal::add));
        // 餐品数量
        result.put("pharmacyNum", commodityInfoService.count(Wrappers.<CommodityInfo>lambdaQuery().eq(CommodityInfo::getShopId, shopInfo.getId())));

        // 本月订单数量
        List<OrderInfo> orderList = baseMapper.selectOrderByMonth(shopInfo.getId());
        result.put("monthOrderNum", CollectionUtil.isEmpty(orderList) ? 0 : orderList.size());
        BigDecimal orderPrice = orderList.stream().map(e -> NumberUtil.mul(e.getOrderPrice(), e.getUserNum())).reduce(BigDecimal.ZERO, BigDecimal::add);
        // 获取本月收益
        result.put("monthOrderPrice", orderPrice);

        // 本年订单数量
        List<OrderInfo> orderYearList = baseMapper.selectOrderByYear(shopInfo.getId());
        result.put("yearOrderNum", CollectionUtil.isEmpty(orderYearList) ? 0 : orderYearList.size());
        // 本年总收益
        BigDecimal orderYearPrice = orderYearList.stream().map(e -> NumberUtil.mul(e.getOrderPrice(), e.getUserNum())).reduce(BigDecimal.ZERO, BigDecimal::add);
        result.put("yearOrderPrice", orderYearPrice);

        // 公告信息
        result.put("bulletin", bulletinInfoService.list(Wrappers.<BulletinInfo>lambdaQuery().eq(BulletinInfo::getRackUp, 1)));

        // 近十天内订单统计
        result.put("orderNumWithinDays", baseMapper.selectOrderNumWithinDays(shopInfo.getId()));
        // 近十天内收益统计
        result.put("orderPriceWithinDays", baseMapper.selectOrderPriceWithinDays(shopInfo.getId()));
        return result;
    }

    /**
     * 获取订单详情
     *
     * @param orderCode 订单编号
     * @return 结果
     */
    @Override
    public LinkedHashMap<String, Object> selectOrderDetail(String orderCode) {
        // 返回数据
        LinkedHashMap<String, Object> result = new LinkedHashMap<String, Object>() {
            {
                put("order", null);
                put("detail", Collections.emptyList());
                put("shop", null);
                put("user", null);
                put("commodity", null);
            }
        };

        // 订单信息
        OrderInfo orderInfo = this.getOne(Wrappers.<OrderInfo>lambdaQuery().eq(OrderInfo::getCode, orderCode));
        result.put("order", orderInfo);

        // 发起人
        UserInfo userInfo = new UserInfo();
        result.put("user", userInfo);

        // 餐品信息
        CommodityInfo commodityInfo = commodityInfoService.getById(orderInfo.getCommodityId());
        result.put("commodity", commodityInfo);

        // 商铺信息
        ShopInfo shopInfo = shopInfoMapper.selectById(commodityInfo.getShopId());
        result.put("shop", shopInfo);

        // 订单详情
        result.put("detail", baseMapper.selectOrderDetail(orderCode));

        return result;
    }

    /**
     * 查询用户购物车
     *
     * @param userId 用户ID
     * @return 结果
     */
    @Override
    public List<LinkedHashMap<String, Object>> selGoodsCart(Integer userId) {
        return baseMapper.selGoodsCart(userId);
    }

    /**
     * 根据订单ID获取订单信息
     *
     * @param ids 订单IDs
     * @return 结果
     */
    @Override
    public List<LinkedHashMap<String, Object>> selOrderListByOrderIds(List<String> ids) {
        return baseMapper.selOrderListByOrderIds(ids);
    }

    /**
     * 获取用户所有订单
     *
     * @param userId 用户ID
     * @return 结果
     */
    @Override
    public List<LinkedHashMap<String, Object>> getOrderListByUserId(Integer userId) {
        return baseMapper.getOrderListByUserId(userId);
    }

    /**
     * 根据用户获取卖出的订单
     *
     * @param userId 用户ID
     * @return 结果
     */
    @Override
    public List<LinkedHashMap<String, Object>> getOrderByUserId(Integer userId) {
        return baseMapper.getOrderByUserId(userId);
    }

    /**
     * 获取订单结算推荐
     *
     * @param orderInfo 订单信息
     * @return 列表
     */
    @Override
    public LinkedHashMap<String, Object> orderOverRecommend(OrderInfo orderInfo) {
        List<OrderDetail> orderDetailList = JSONUtil.toList(orderInfo.getOrderDetailStr(), OrderDetail.class);
        List<Integer> commodityIdList = orderDetailList.stream().map(OrderDetail::getCommodityId).collect(Collectors.toList());
        List<CommodityInfo> commodityInfoList = commodityInfoService.list(
                Wrappers.<CommodityInfo>lambdaQuery().in(CommodityInfo::getId, commodityIdList)
        );
        Map<Integer, CommodityInfo> commodityInfoMap = commodityInfoList.stream().collect(Collectors.toMap(CommodityInfo::getId, e -> e));

        BigDecimal totalPrice = BigDecimal.ZERO;
        BigDecimal totalCalories = BigDecimal.ZERO;
        BigDecimal totalProtein = BigDecimal.ZERO;
        BigDecimal totalFat = BigDecimal.ZERO;
        BigDecimal totalCarbohydrate = BigDecimal.ZERO;
        BigDecimal totalSodium = BigDecimal.ZERO;
        BigDecimal totalFiber = BigDecimal.ZERO;
        BigDecimal totalSugar = BigDecimal.ZERO;

        List<Map<String, Object>> commodityDetails = new java.util.ArrayList<>();

        for (OrderDetail detail : orderDetailList) {
            CommodityInfo commodity = commodityInfoMap.get(detail.getCommodityId());
            if (commodity == null) {
                continue;
            }

            BigDecimal quantityInGrams = detail.getQuantity() != null ? detail.getQuantity() : BigDecimal.ZERO;
            BigDecimal pricePer100g = detail.getItemPrice() != null ? detail.getItemPrice() : BigDecimal.ZERO;

            BigDecimal itemTotalPrice = pricePer100g.multiply(quantityInGrams).divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
            totalPrice = totalPrice.add(itemTotalPrice);

            BigDecimal caloriesPer100g = commodity.getCalories() != null ? commodity.getCalories() : BigDecimal.ZERO;
            BigDecimal proteinPer100g = commodity.getProtein() != null ? commodity.getProtein() : BigDecimal.ZERO;
            BigDecimal fatPer100g = commodity.getFat() != null ? commodity.getFat() : BigDecimal.ZERO;
            BigDecimal carbohydratePer100g = commodity.getCarbohydrate() != null ? commodity.getCarbohydrate() : BigDecimal.ZERO;
            BigDecimal sodiumPer100g = commodity.getSodium() != null ? commodity.getSodium() : BigDecimal.ZERO;
            BigDecimal fiberPer100g = commodity.getFiber() != null ? commodity.getFiber() : BigDecimal.ZERO;
            BigDecimal sugarPer100g = commodity.getSugar() != null ? commodity.getSugar() : BigDecimal.ZERO;

            BigDecimal itemCalories = caloriesPer100g.multiply(quantityInGrams).divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
            BigDecimal itemProtein = proteinPer100g.multiply(quantityInGrams).divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
            BigDecimal itemFat = fatPer100g.multiply(quantityInGrams).divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
            BigDecimal itemCarbohydrate = carbohydratePer100g.multiply(quantityInGrams).divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
            BigDecimal itemSodium = sodiumPer100g.multiply(quantityInGrams).divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
            BigDecimal itemFiber = fiberPer100g.multiply(quantityInGrams).divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
            BigDecimal itemSugar = sugarPer100g.multiply(quantityInGrams).divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);

            totalCalories = totalCalories.add(itemCalories);
            totalProtein = totalProtein.add(itemProtein);
            totalFat = totalFat.add(itemFat);
            totalCarbohydrate = totalCarbohydrate.add(itemCarbohydrate);
            totalSodium = totalSodium.add(itemSodium);
            totalFiber = totalFiber.add(itemFiber);
            totalSugar = totalSugar.add(itemSugar);

            Map<String, Object> itemDetail = new LinkedHashMap<>();
            itemDetail.put("commodityId", commodity.getId());
            itemDetail.put("commodityName", commodity.getName());
            itemDetail.put("quantityInGrams", quantityInGrams);
            itemDetail.put("pricePer100g", pricePer100g);
            itemDetail.put("totalPrice", itemTotalPrice);
            itemDetail.put("calories", itemCalories);
            itemDetail.put("protein", itemProtein);
            itemDetail.put("fat", itemFat);
            itemDetail.put("carbohydrate", itemCarbohydrate);
            itemDetail.put("sodium", itemSodium);
            itemDetail.put("fiber", itemFiber);
            itemDetail.put("sugar", itemSugar);
            commodityDetails.add(itemDetail);
        }

        LinkedHashMap<String, Object> nutritionSummary = new LinkedHashMap<>();
        nutritionSummary.put("totalCalories", totalCalories.setScale(2, java.math.RoundingMode.HALF_UP));
        nutritionSummary.put("totalProtein", totalProtein.setScale(2, java.math.RoundingMode.HALF_UP));
        nutritionSummary.put("totalFat", totalFat.setScale(2, java.math.RoundingMode.HALF_UP));
        nutritionSummary.put("totalCarbohydrate", totalCarbohydrate.setScale(2, java.math.RoundingMode.HALF_UP));
        nutritionSummary.put("totalSodium", totalSodium.setScale(2, java.math.RoundingMode.HALF_UP));
        nutritionSummary.put("totalFiber", totalFiber.setScale(2, java.math.RoundingMode.HALF_UP));
        nutritionSummary.put("totalSugar", totalSugar.setScale(2, java.math.RoundingMode.HALF_UP));

        List<String> recommendations = new java.util.ArrayList<>();
        List<String> warnings = new java.util.ArrayList<>();

        if (totalCalories.compareTo(new BigDecimal("600")) < 0) {
            recommendations.add("热量摄入偏低（当前：" + totalCalories.setScale(0, java.math.RoundingMode.HALF_UP) + " kcal），建议适当增加主食或蛋白质食物，维持精力充沛需要 600-800 kcal。");
        } else if (totalCalories.compareTo(new BigDecimal("800")) > 0) {
            warnings.add("热量摄入偏高（当前：" + totalCalories.setScale(0, java.math.RoundingMode.HALF_UP) + " kcal），建议控制在 600-800 kcal 范围内。");
        } else {
            recommendations.add("热量摄入合理（" + totalCalories.setScale(0, java.math.RoundingMode.HALF_UP) + " kcal），符合 600-800 kcal 的标准。");
        }

        if (totalProtein.compareTo(new BigDecimal("25")) < 0) {
            recommendations.add("蛋白质摄入不足（当前：" + totalProtein.setScale(1, java.math.RoundingMode.HALF_UP) + " g），建议补充瘦肉、蛋类或豆制品，单顿需达到 25-35 g 以支持肌肉修复。");
        } else if (totalProtein.compareTo(new BigDecimal("35")) > 0) {
            warnings.add("蛋白质摄入偏高（当前：" + totalProtein.setScale(1, java.math.RoundingMode.HALF_UP) + " g），建议控制在 25-35 g 范围内。");
        } else {
            recommendations.add("蛋白质摄入充足（" + totalProtein.setScale(1, java.math.RoundingMode.HALF_UP) + " g），符合 25-35 g 的标准。");
        }

        if (totalFat.compareTo(new BigDecimal("20")) < 0) {
            recommendations.add("脂肪摄入偏低（当前：" + totalFat.setScale(1, java.math.RoundingMode.HALF_UP) + " g），建议适量增加健康脂肪来源，正常范围为 20-25 g。");
        } else if (totalFat.compareTo(new BigDecimal("25")) > 0) {
            warnings.add("脂肪摄入偏高（当前：" + totalFat.setScale(1, java.math.RoundingMode.HALF_UP) + " g），建议减少油腻食物，控制在 20-25 g 范围内。");
        } else {
            recommendations.add("脂肪摄入合理（" + totalFat.setScale(1, java.math.RoundingMode.HALF_UP) + " g），符合 20-25 g 的标准。");
        }

        if (totalCarbohydrate.compareTo(new BigDecimal("80")) < 0) {
            recommendations.add("碳水化合物摄入不足（当前：" + totalCarbohydrate.setScale(1, java.math.RoundingMode.HALF_UP) + " g），建议增加米饭、面食等主食，正常范围为 80-100 g（约 1.5-2 碗熟米饭）。");
        } else if (totalCarbohydrate.compareTo(new BigDecimal("100")) > 0) {
            warnings.add("碳水化合物摄入偏高（当前：" + totalCarbohydrate.setScale(1, java.math.RoundingMode.HALF_UP) + " g），建议控制在 80-100 g 范围内。");
        } else {
            recommendations.add("碳水化合物摄入合理（" + totalCarbohydrate.setScale(1, java.math.RoundingMode.HALF_UP) + " g），符合 80-100 g 的标准。");
        }

        if (totalSodium.compareTo(new BigDecimal("800")) > 0) {
            warnings.add("钠含量超标（当前：" + totalSodium.setScale(0, java.math.RoundingMode.HALF_UP) + " mg），中餐炒菜容易超标，建议清淡饮食，每日应低于 2000 mg。");
        } else {
            recommendations.add("钠含量控制良好（" + totalSodium.setScale(0, java.math.RoundingMode.HALF_UP) + " mg），低于 800 mg 的建议值。");
        }

        if (totalFiber.compareTo(new BigDecimal("8")) < 0) {
            recommendations.add("膳食纤维摄入不足（当前：" + totalFiber.setScale(1, java.math.RoundingMode.HALF_UP) + " g），建议多吃蔬菜、水果和粗粮，单顿需达到 8-10 g。");
        } else {
            recommendations.add("膳食纤维摄入充足（" + totalFiber.setScale(1, java.math.RoundingMode.HALF_UP) + " g），符合 8-10 g 的标准。");
        }

        if (totalSugar.compareTo(new BigDecimal("15")) > 0) {
            warnings.add("添加糖摄入偏高（当前：" + totalSugar.setScale(1, java.math.RoundingMode.HALF_UP) + " g），建议减少含糖饮料和甜味酱汁，应低于 15 g。");
        } else {
            recommendations.add("添加糖控制良好（" + totalSugar.setScale(1, java.math.RoundingMode.HALF_UP) + " g），低于 15 g 的建议值。");
        }

        LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        result.put("totalPrice", totalPrice.setScale(2, java.math.RoundingMode.HALF_UP));
        result.put("nutritionSummary", nutritionSummary);
        result.put("commodityDetails", commodityDetails);
        result.put("recommendations", recommendations);
        result.put("warnings", warnings);

        return result;
    }
}
