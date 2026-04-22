package cc.mrbird.febs.cos.dao;

import cc.mrbird.febs.cos.entity.DailySupplyPlanDetail;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import java.util.LinkedHashMap;

/**
 * @author FanK
 */
public interface DailySupplyPlanDetailMapper extends BaseMapper<DailySupplyPlanDetail> {

    /**
     * 分页获取每日供应信息
     *
     * @param page                  分页对象
     * @param dailySupplyPlanDetail 每日供应信息
     * @return 结果
     */
    IPage<LinkedHashMap<String, Object>> queryPage(Page<DailySupplyPlanDetail> page, @Param("queryForm") DailySupplyPlanDetail dailySupplyPlanDetail);
}
