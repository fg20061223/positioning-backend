package com.positioning.business.service;

import com.positioning.business.entity.ParkingRecord;
import com.positioning.business.mapper.ParkingRecordMapper;
import com.positioning.business.mapper.ParkingSpaceMapper;
import com.positioning.common.exception.BizException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 停车记录服务: 入场/离场并联动车位状态
 */
@Service
@RequiredArgsConstructor
public class ParkingRecordService {

    private final ParkingRecordMapper parkingRecordMapper;
    private final ParkingSpaceMapper parkingSpaceMapper;

    /** 入场停车: 记录状态置为 PARKING, 车位置为 OCCUPIED */
    @Transactional(rollbackFor = Exception.class)
    public ParkingRecord park(ParkingRecord record) {
        record.setEntryTime(record.getEntryTime() == null ? LocalDateTime.now() : record.getEntryTime());
        record.setStatus("PARKING");
        if (record.getSource() == null || record.getSource().isBlank()) {
            record.setSource("APP");
        }
        parkingRecordMapper.insert(record);
        if (record.getSpaceId() != null) {
            parkingSpaceMapper.updateStatus(record.getSpaceId(), "OCCUPIED", "APP");
        }
        return record;
    }

    /** 离场: 记录状态置为 ENDED, 车位释放为 FREE */
    @Transactional(rollbackFor = Exception.class)
    public ParkingRecord end(Long id) {
        ParkingRecord record = parkingRecordMapper.selectById(id);
        if (record == null) {
            throw new BizException(404, "停车记录不存在");
        }
        record.setExitTime(LocalDateTime.now());
        record.setStatus("ENDED");
        parkingRecordMapper.updateById(record);
        if (record.getSpaceId() != null) {
            parkingSpaceMapper.updateStatus(record.getSpaceId(), "FREE", null);
        }
        return record;
    }
}
