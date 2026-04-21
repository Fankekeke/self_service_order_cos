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
 * 原材料信息表
 *
 * @author FanK
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class MaterialInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "ID", type = IdType.AUTO)
    private Integer id;

    /**
     * 原材料编号
     */
    private String code;

    /**
     * 原材料名称
     */
    private String name;

    /**
     * 原材料分类
     */
    private String category;

    /**
     * 单位（如：kg、g、L、ml等）
     */
    private String unit;

    /**
     * 规格型号
     */
    private String specification;

    /**
     * 供应商
     */
    private String supplier;

    /**
     * 单价
     */
    private BigDecimal unitPrice;

    /**
     * 保质期（天）
     */
    private Integer shelfLife;

    /**
     * 储存条件
     */
    private String storageCondition;

    /**
     * 详细描述
     */
    private String description;

    /**
     * 图片
     */
    private String images;

    /**
     * 创建时间
     */
    private String createDate;


}
