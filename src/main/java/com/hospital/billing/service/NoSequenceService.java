package com.hospital.billing.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.StoredProcedureQuery;
import org.springframework.stereotype.Service;

@Service
public class NoSequenceService {

    @PersistenceContext
    private EntityManager entityManager;

    public String getNextNo(String seqType) {
        StoredProcedureQuery query =
                entityManager.createStoredProcedureQuery("HOSPITAL.SP_NEXT_NO");

        query.registerStoredProcedureParameter("P_SEQ_TYPE", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("P_NO", String.class, ParameterMode.OUT);

        query.setParameter("P_SEQ_TYPE", seqType);
        query.execute();

        Object result = query.getOutputParameterValue("P_NO");

        if (result == null) {
            throw new IllegalStateException("자동 번호 생성에 실패했습니다. seqType=" + seqType);
        }

        return result.toString();
    }
}