package cc.mrbird.febs.cos.controller;


import cc.mrbird.febs.common.exception.FebsException;
import cc.mrbird.febs.common.utils.R;
import cc.mrbird.febs.cos.entity.DailySupplyPlanDetail;
import cc.mrbird.febs.cos.service.IDailySupplyPlanDetailService;
import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * @author FanK
 */
@RestController
@RequestMapping("/cos/daily-supply-plan-detail")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DailySupplyPlanDetailController {

    private final IDailySupplyPlanDetailService dailySupplyPlanDetailService;

    /**
     * 分页获取每日供应信息
     *
     * @param page                  分页对象
     * @param dailySupplyPlanDetail 每日供应信息
     * @return 结果
     */
    @GetMapping("/page")
    public R page(Page<DailySupplyPlanDetail> page, DailySupplyPlanDetail dailySupplyPlanDetail) {
        return R.ok(dailySupplyPlanDetailService.queryPage(page, dailySupplyPlanDetail));
    }

    /**
     * 获取ID获取每日供应详情
     *
     * @param id 主键
     * @return 结果
     */
    @GetMapping("/{id}")
    public R detail(@PathVariable("id") Integer id) {
        return R.ok(dailySupplyPlanDetailService.getById(id));
    }

    /**
     * 获取每日供应信息列表
     *
     * @return 结果
     */
    @GetMapping("/list")
    public R list() {
        return R.ok(dailySupplyPlanDetailService.list());
    }

    /**
     * 查询今日供应菜品信息
     *
     * @return 列表
     */
    @GetMapping("/queryCommodityByDate")
    public R queryCommodityByDate() {
        return R.ok(dailySupplyPlanDetailService.queryCommodityByDate());
    }

    /**
     * 新增每日供应信息
     *
     * @param dailySupplyPlanDetail 每日供应信息
     * @return 结果
     */
    @PostMapping
    public R save(DailySupplyPlanDetail dailySupplyPlanDetail) throws FebsException {
        if (dailySupplyPlanDetail.getDate() == null || dailySupplyPlanDetail.getDate().trim().isEmpty()) {
            throw new FebsException("供应日期不能为空");
        }

        if (dailySupplyPlanDetail.getDailyPlanStr() == null || dailySupplyPlanDetail.getDailyPlanStr().trim().isEmpty()) {
            throw new FebsException("供应计划数据不能为空");
        }

        boolean exists = dailySupplyPlanDetailService.existsByDate(dailySupplyPlanDetail.getDate());
        if (exists) {
            throw new FebsException("该日期的供应计划已存在，请勿重复添加");
        }

        List<DailySupplyPlanDetail> list = JSONUtil.toList(dailySupplyPlanDetail.getDailyPlanStr(), DailySupplyPlanDetail.class);

        if (list == null || list.isEmpty()) {
            throw new FebsException("供应计划列表不能为空");
        }

        boolean result = dailySupplyPlanDetailService.batchSave(list, dailySupplyPlanDetail.getDate());
        return R.ok(result);
    }

    /**
     * 查询每日供应采购计划
     *
     * @param date 日期
     * @return 结果
     */
    @GetMapping("/querySupplyPlanMaterial")
    public R querySupplyPlanMaterial(String date) {
        return R.ok(dailySupplyPlanDetailService.querySupplyPlanMaterial(date));
    }

    /**
     * 修改每日供应信息
     *
     * @param dailySupplyPlanDetail 每日供应信息
     * @return 结果
     */
    @PutMapping
    public R edit(DailySupplyPlanDetail dailySupplyPlanDetail) {
        return R.ok(dailySupplyPlanDetailService.updateById(dailySupplyPlanDetail));
    }

    /**
     * 删除每日供应信息
     *
     * @param ids ids
     * @return 每日供应信息
     */
    @DeleteMapping("/{ids}")
    public R deleteByIds(@PathVariable("ids") List<Integer> ids) {
        return R.ok(dailySupplyPlanDetailService.removeByIds(ids));
    }
}
