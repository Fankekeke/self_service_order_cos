package cc.mrbird.febs.cos.service;

import cc.mrbird.febs.cos.entity.DailySupplyPlanDetail;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.apache.ibatis.annotations.Param;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author FanK
 */
public interface IDailySupplyPlanDetailService extends IService<DailySupplyPlanDetail> {

    /**
     * 分页获取每日供应信息
     *
     * @param page                  分页对象
     * @param dailySupplyPlanDetail 每日供应信息
     * @return 结果
     */
    IPage<LinkedHashMap<String, Object>> queryPage(Page<DailySupplyPlanDetail> page, DailySupplyPlanDetail dailySupplyPlanDetail);

    /**
     * 查询今日供应菜品信息
     *
     * @return 列表
     */
    LinkedHashMap<String, Object> queryCommodityByDate();

    /**
     * 检查指定日期是否已存在供应计划
     *
     * @param date 日期
     * @return 是否存在
     */
    boolean existsByDate(String date);

    /**
     * 批量保存每日供应计划明细
     *
     * @param list 供应计划明细列表
     * @param date 供应日期
     * @return 是否成功
     */
    boolean batchSave(List<DailySupplyPlanDetail> list, String date);

    /**
     * 查询每日供应计划明细
     *
     * @param date 日期
     * @return 结果
     */
    LinkedHashMap<String, Object> querySupplyPlanMaterial(String date);
}
