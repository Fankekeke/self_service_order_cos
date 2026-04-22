package cc.mrbird.febs.cos.service.impl;

import cc.mrbird.febs.cos.entity.DailySupplyPlanDetail;
import cc.mrbird.febs.cos.dao.DailySupplyPlanDetailMapper;
import cc.mrbird.febs.cos.service.IDailySupplyPlanDetailService;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author FanK
 */
@Service
public class DailySupplyPlanDetailServiceImpl extends ServiceImpl<DailySupplyPlanDetailMapper, DailySupplyPlanDetail> implements IDailySupplyPlanDetailService {

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

        for (DailySupplyPlanDetail detail : list) {
            detail.setDate(date);
            detail.setCreateDate(now);
        }

        return this.saveBatch(list);
    }
}
