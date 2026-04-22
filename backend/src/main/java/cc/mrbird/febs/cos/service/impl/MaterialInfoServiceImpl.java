package cc.mrbird.febs.cos.service.impl;

import cc.mrbird.febs.cos.entity.MaterialInfo;
import cc.mrbird.febs.cos.dao.MaterialInfoMapper;
import cc.mrbird.febs.cos.service.IMaterialInfoService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;

/**
 * @author FanK
 */
@Service
public class MaterialInfoServiceImpl extends ServiceImpl<MaterialInfoMapper, MaterialInfo> implements IMaterialInfoService {

    /**
     * 分页获取原材料
     *
     * @param page         分页对象
     * @param materialInfo 原材料
     * @return 结果
     */
    @Override
    public IPage<LinkedHashMap<String, Object>> queryPage(Page<MaterialInfo> page, MaterialInfo materialInfo) {
        return baseMapper.queryPage(page, materialInfo);
    }
}
