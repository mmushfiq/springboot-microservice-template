package com.company.project.template.mapper;

import com.company.project.common.config.CommonMapperConfig;
import com.company.project.template.dao.entity.AccountEntity;
import com.company.project.template.model.dto.AccountDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", config = CommonMapperConfig.class)
public interface AccountMapper extends EntityMapper<AccountDto, AccountEntity> {

    AccountMapper INSTANCE = Mappers.getMapper(AccountMapper.class);

    void updateAccountEntity(AccountDto accountDto, @MappingTarget AccountEntity accountEntity);

}
