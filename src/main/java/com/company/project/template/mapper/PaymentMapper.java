package com.company.project.template.mapper;

import com.company.project.common.config.CommonMapperConfig;
import com.company.project.template.dao.document.PaymentLogDocument;
import com.company.project.template.model.dto.PaymentDto;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", config = CommonMapperConfig.class)
public interface PaymentMapper {

    PaymentMapper INSTANCE = Mappers.getMapper(PaymentMapper.class);

    PaymentLogDocument toDocument(PaymentDto dto, String status);

    PaymentDto toDto(PaymentLogDocument document);

}
