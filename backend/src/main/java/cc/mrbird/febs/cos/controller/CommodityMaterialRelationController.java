package cc.mrbird.febs.cos.controller;


import cc.mrbird.febs.common.utils.R;
import cc.mrbird.febs.cos.entity.CommodityMaterialRelation;
import cc.mrbird.febs.cos.service.ICommodityMaterialRelationService;
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
@RequestMapping("/cos/commodity-material-relation")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CommodityMaterialRelationController {

    private final ICommodityMaterialRelationService commodityMaterialRelationService;

    /**
     * 分页获取菜品原材料关联信息
     *
     * @param page                      分页对象
     * @param commodityMaterialRelation 菜品原材料关联信息
     * @return 结果
     */
    @GetMapping("/page")
    public R page(Page<CommodityMaterialRelation> page, CommodityMaterialRelation commodityMaterialRelation) {
        return R.ok();
    }

    /**
     * 获取ID获取菜品原材料关联详情
     *
     * @param id 主键
     * @return 结果
     */
    @GetMapping("/{id}")
    public R detail(@PathVariable("id") Integer id) {
        return R.ok(commodityMaterialRelationService.getById(id));
    }

    /**
     * 获取菜品原材料关联信息列表
     *
     * @return 结果
     */
    @GetMapping("/list")
    public R list() {
        return R.ok(commodityMaterialRelationService.list());
    }

    /**
     * 新增菜品原材料关联信息
     *
     * @param commodityMaterialRelation 菜品原材料关联信息
     * @return 结果
     */
    @PostMapping
    public R save(CommodityMaterialRelation commodityMaterialRelation) {
        commodityMaterialRelation.setCreateDate(DateUtil.formatDateTime(new Date()));
        return R.ok(commodityMaterialRelationService.save(commodityMaterialRelation));
    }

    /**
     * 修改菜品原材料关联信息
     *
     * @param commodityMaterialRelation 菜品原材料关联信息
     * @return 结果
     */
    @PutMapping
    public R edit(CommodityMaterialRelation commodityMaterialRelation) {
        return R.ok(commodityMaterialRelationService.updateById(commodityMaterialRelation));
    }

    /**
     * 删除菜品原材料关联信息
     *
     * @param ids ids
     * @return 菜品原材料关联信息
     */
    @DeleteMapping("/{ids}")
    public R deleteByIds(@PathVariable("ids") List<Integer> ids) {
        return R.ok(commodityMaterialRelationService.removeByIds(ids));
    }
}
