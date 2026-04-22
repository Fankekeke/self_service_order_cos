package cc.mrbird.febs.cos.service.impl;

import cc.mrbird.febs.cos.entity.DailySupplyPlanDetail;
import cc.mrbird.febs.cos.dao.DailySupplyPlanDetailMapper;
import cc.mrbird.febs.cos.service.IDailySupplyPlanDetailService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;

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
}
