package cc.mrbird.febs.cos.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 菜品原材料关联表
 *
 * @author FanK
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class CommodityMaterialRelation implements Serializable {

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
     * 原材料ID
     */
    private Integer materialId;

    /**
     * 所需数量
     */
    private BigDecimal quantity;

    /**
     * 单位
     */
    private String unit;

    /**
     * 备注说明
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createDate;


}
