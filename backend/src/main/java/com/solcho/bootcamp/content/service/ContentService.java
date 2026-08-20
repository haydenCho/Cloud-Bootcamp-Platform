package com.solcho.bootcamp.content.service;

import com.solcho.bootcamp.common.exception.ApiException;
import com.solcho.bootcamp.content.dto.ContentResponse;
import com.solcho.bootcamp.content.entity.Content;
import com.solcho.bootcamp.content.repository.ContentRepository;
import com.solcho.bootcamp.unit.entity.Unit;
import com.solcho.bootcamp.unit.repository.UnitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ContentService {

    private final ContentRepository contentRepository;
    private final UnitRepository unitRepository;

    public ContentService(ContentRepository contentRepository, UnitRepository unitRepository) {
        this.contentRepository = contentRepository;
        this.unitRepository = unitRepository;
    }

    @Transactional(readOnly = true)
    public ContentResponse getByUnitCode(String code) {
        Unit unit = unitRepository.findByCode(code)
                .orElseThrow(() -> ApiException.notFound("존재하지 않는 단원입니다."));
        Content content = contentRepository.findFirstByUnitIdOrderByIdAsc(unit.getId())
                .orElseThrow(() -> ApiException.notFound("등록된 학습 콘텐츠가 없습니다."));
        return ContentResponse.of(unit.getCode(), content);
    }
}
