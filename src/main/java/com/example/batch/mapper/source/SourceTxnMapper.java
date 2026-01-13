package com.example.batch.mapper.source;

import com.example.batch.domain.TxnRecord;

import java.util.List;
import java.util.Map;

public interface SourceTxnMapper {
    List<TxnRecord> selectPaged(Map<String, Object> params);
}
