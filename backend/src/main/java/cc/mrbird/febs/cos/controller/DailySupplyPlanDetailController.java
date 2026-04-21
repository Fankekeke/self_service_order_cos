package cc.mrbird.febs.cos.controller;


import cc.mrbird.febs.common.utils.R;
import cc.mrbird.febs.cos.entity.DailySupplyPlanDetail;
import cc.mrbird.febs.cos.service.IDailySupplyPlanDetailService;
import cn.hutool.core.date.DateUtil;
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
        return R.ok();
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
     * 新增每日供应信息
     *
     * @param dailySupplyPlanDetail 每日供应信息
     * @return 结果
     */
    @PostMapping
    public R save(DailySupplyPlanDetail dailySupplyPlanDetail) {
        dailySupplyPlanDetail.setCreateDate(DateUtil.formatDateTime(new Date()));
        return R.ok(dailySupplyPlanDetailService.save(dailySupplyPlanDetail));
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
