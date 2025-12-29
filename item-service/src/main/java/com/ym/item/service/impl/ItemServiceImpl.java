package com.ym.item.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmall.common.exception.BizIllegalException;
import com.hmall.common.utils.BeanUtils;


import com.ym.api.dto.ItemDTO;
import com.ym.api.dto.OrderDetailDTO;
import com.ym.item.domain.po.Item;
import com.ym.item.mapper.ItemMapper;
import com.ym.item.service.IItemService;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

/**
 * <p>
 * 商品表 服务实现类
 * </p>
 *
 * @author 虎哥
 */
@Service

public class ItemServiceImpl extends ServiceImpl<ItemMapper, Item> implements IItemService {

    @Override
    public void deductStock(List<OrderDetailDTO> items) {
        String sqlStatement = "com.ym.item.mapper.ItemMapper.updateStock";
        boolean r = false;
        try {
            r = executeBatch(items, (sqlSession, entity) -> sqlSession.update(sqlStatement, entity));
        } catch (Exception e) {
            log.error("更新库存异常", e);
            return;
        }
        if (!r) {
            throw new BizIllegalException("库存不足！");
        }
    }

    @Override
    public List<ItemDTO> queryItemByIds(Collection<Long> ids) {
        return BeanUtils.copyList(listByIds(ids), ItemDTO.class);
    }
}
