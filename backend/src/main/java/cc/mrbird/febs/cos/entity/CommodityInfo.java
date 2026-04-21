package cc.mrbird.febs.cos.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 餐品管理
 *
 * @author FanK
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class CommodityInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "ID", type = IdType.AUTO)
    private Integer id;

    /**
     * 餐品编号
     */
    private String code;

    /**
     * 餐品名称
     */
    private String name;

    /**
     * 价格
     */
    private BigDecimal price;

    /**
     * 库存数量
     */
    private Integer stockNum;

    /**
     * 餐品型号
     */
    private String model;

    /**
     * 餐品描述
     */
    private String content;

    /**
     * 创建时间
     */
    private String createDate;

    /**
     * 图册
     */
    private String images;

    /**
     * 删除标识
     */
    private String delFlag;

    /**
     * 所属商家
     */
    private Integer shopId;

    /**
     * 餐品类型
     */
    private Integer type;

    /**
     * 餐品状态（0.下架 1.上架）
     */
    private String onPut;

    /**
     * 热量（千卡）
     */
    private BigDecimal calories;

    /**
     * 蛋白质含量（克/100g）
     */
    private BigDecimal protein;

    /**
     * 脂肪含量（克/100g）
     */
    private BigDecimal fat;

    /**
     * 碳水化合物含量（克/100g）
     */
    private BigDecimal carbohydrate;

    /**
     * 钠含量（毫克/100g）
     */
    private BigDecimal sodium;

    /**
     * 膳食纤维含量（克/100g）
     */
    private BigDecimal fiber;

    /**
     * 糖含量（克/100g）
     */
    private BigDecimal sugar;

    /**
     * 份量说明
     */
    private String servingSize;

    /**
     * 过敏原信息
     */
    private String allergenInfo;

    /**
     * 营养备注
     */
    private String nutritionRemark;
}
