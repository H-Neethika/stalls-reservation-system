package com.exhibition.exhibition_service.service.impl;

import com.exhibition.exhibition_service.model.StallType;
import com.exhibition.exhibition_service.repository.StallTypeRepository;
import com.exhibition.exhibition_service.service.StallTypeService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StallTypeServiceImpl implements StallTypeService {

    private final StallTypeRepository stallTypeRepository;

    @Override
    public List<StallType> getAll() {
        return stallTypeRepository.findAll();
    }
}
