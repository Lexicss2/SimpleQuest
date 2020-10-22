package com.lex.simplequest.data.location.repository

import com.lex.simplequest.domain.repository.LocationRepository

interface LocationQuerySpecificationImpl : LocationRepository.LocationQuerySpecification {
    fun getWhereClause(): String
}