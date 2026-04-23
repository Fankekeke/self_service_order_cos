package cc.mrbird.febs.cos.entity;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;

import java.time.LocalDateTime;
import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 每日供应计划菜品明细表
 *
 * @author FanK
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class DailySupplyPlanDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "ID", type = IdType.AUTO)
    private Integer id;

    /**
     * 菜品ID
     */
    private Integer commodityId;

    /**
     * 计划供应量
     */
    private Integer plannedQuantity;

    /**
     * 实际供应量
     */
    private Integer actualQuantity;

    /**
     * 单价
     */
    private BigDecimal unitPrice;

    /**
     * 计划金额
     */
    private BigDecimal plannedAmount;

    /**
     * 实际金额
     */
    private BigDecimal actualAmount;

    /**
     * 预估原材料成本
     */
    private BigDecimal estimatedMaterialCost;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private String createDate;

    /**
     * 更新时间
     */
    private String updateDate;

    @TableField(exist = false)
    private String dailyPlanStr;

    private String date;
}
