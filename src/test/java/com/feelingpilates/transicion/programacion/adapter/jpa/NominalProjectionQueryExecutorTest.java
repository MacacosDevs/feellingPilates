package com.feelingpilates.transicion.programacion.adapter.jpa;

import com.feelingpilates.transicion.programacion.adapter.jpa.mapper.NominalProjectionMapper;
import com.feelingpilates.transicion.programacion.adapter.jpa.projection.*;
import com.feelingpilates.transicion.programacion.adapter.jpa.testinfra.*;
import com.feelingpilates.transicion.programacion.read.*;
import com.feelingpilates.programacion.dominio.ReferenciaOcurrencia;
import org.junit.jupiter.api.*;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.hibernate.query.NativeQuery;
import static org.mockito.Mockito.*;

class NominalProjectionQueryExecutorTest {
    static EntityManager precisionEntityManager() {
        EntityManager em=mock(EntityManager.class);
        var emf=mock(jakarta.persistence.EntityManagerFactory.class);
        var factory=mock(org.hibernate.engine.spi.SessionFactoryImplementor.class);
        var services=mock(org.hibernate.engine.jdbc.spi.JdbcServices.class);
        when(em.getEntityManagerFactory()).thenReturn(emf);
        when(emf.unwrap(org.hibernate.engine.spi.SessionFactoryImplementor.class)).thenReturn(factory);
        when(factory.getJdbcServices()).thenReturn(services);
        when(services.getDialect()).thenReturn(new NominalProjectionQueryExecutor.ScalarPostgresDialect());
        return em;
    }
    @Test void lossyDialectIsRejectedBeforePreparingAnyDataQuery() {
        EntityManager em=precisionEntityManager();
        when(em.getEntityManagerFactory().unwrap(org.hibernate.engine.spi.SessionFactoryImplementor.class)
                .getJdbcServices().getDialect()).thenReturn(new org.hibernate.dialect.PostgreSQLDialect());
        var executor=new NominalProjectionQueryExecutor(em,NominalProjectionCatalog.R3_NOMINAL_V1);
        assertEquals(NominalReadFailure.Category.TRANSACTION_CONTEXT_INVALID,
                assertThrows(NominalReadFailure.class,()->executor.consultarNominal(NominalProjectionMapperTest.DATE)).category());
        verify(em,never()).createNativeQuery(anyString());
    }
    @Test void exactNativeSqlNamedTypesDateAndAllPhysicalVersions() {
        EntityManager em=precisionEntityManager(); Query query=mock(Query.class); NativeQuery nativeQuery=mock(NativeQuery.class);
        when(em.createNativeQuery(NominalProjectionCatalog.DATA_SQL)).thenReturn(query);
        when(query.unwrap(NativeQuery.class)).thenReturn(nativeQuery);
        Object[] first=NominalProjectionMapperTest.values(),second=NominalProjectionMapperTest.values();second[1]=UUID.randomUUID();
        when(nativeQuery.getResultList()).thenReturn(List.of(first,second));
        var executor=new NominalProjectionQueryExecutor(em,NominalProjectionCatalog.R3_NOMINAL_V1);
        var date=LocalDate.of(2026,9,27);var rows=executor.consultarNominal(date);assertEquals(2,rows.size());
        verify(nativeQuery).setParameter("assignmentActive",Boolean.TRUE,Boolean.class);
        verify(nativeQuery).setParameter("blockActive",Boolean.TRUE,Boolean.class);
        verify(nativeQuery).setParameter("fecha",date,LocalDate.class);
        verify(nativeQuery).setParameter("dayOfWeek",(short)0,Short.class);
        assertEquals(first[1],rows.get(0).assignmentId());assertEquals(second[1],rows.get(1).assignmentId());
        verify(em).createNativeQuery(NominalProjectionCatalog.DATA_SQL);
    }
    @Test void operationalQueryFailureRetainsCauseNeverEmptySuccess() {
        EntityManager em=precisionEntityManager();var cause=new jakarta.persistence.PersistenceException("probe");
        when(em.createNativeQuery(anyString())).thenThrow(cause);
        var executor=new NominalProjectionQueryExecutor(em,NominalProjectionCatalog.R3_NOMINAL_V1);
        var failure=assertThrows(NominalReadFailure.class,()->executor.consultarNominal(NominalProjectionMapperTest.DATE));
        assertEquals(NominalReadFailure.Category.DATABASE_READ_FAILURE,failure.category());assertSame(cause,failure.getCause());
        assertThrows(NominalReadFailure.class,()->executor.consultarNominal(null));
    }
    @Test void frozenFiveStatementCatalogIdsAndIndependentOccurrenceMap() {
        var catalog=NominalProjectionCatalog.R3_NOMINAL_V1;assertEquals(5,catalog.statements().size());
        assertEquals(Set.of("R3_NOMINAL_ON_DATE_V1","R3_TX_ISOLATION_V1","R3_TX_READ_ONLY_V1",
                "R3_TX_RESOURCE_IDENTITY_V1","R3_TX_SNAPSHOT_V1"),catalog.statements().keySet());
        for(var s:catalog.statements().values()) {
            String executed=s.sql().replaceAll(":(assignmentActive|blockActive|fecha|dayOfWeek)","?");
            assertEquals(s.catalogId(),F2eStatementPolicyInspector.identificar(F2eStatementPolicyInspector.normalizar(executed)));
        }
        var matcher=java.util.regex.Pattern.compile(":(assignmentActive|blockActive|fecha|dayOfWeek)").matcher(NominalProjectionCatalog.DATA_SQL);
        List<String> occurrences=new ArrayList<>();while(matcher.find()) occurrences.add(matcher.group(1));
        assertEquals(List.of("assignmentActive","blockActive","fecha","fecha","fecha","fecha","dayOfWeek"),occurrences);
        assertEquals(occurrences,NominalProjectionCatalog.JDBC_OCCURRENCES);
        assertTrue(NominalProjectionCatalog.DATA_SQL.endsWith("ORDER BY a.serie_id, a.id"));
        for(String forbidden:List.of("DISTINCT","LIMIT","ROW_NUMBER","GROUP BY","MAX(","MIN(","FOR UPDATE"))
            assertFalse(NominalProjectionCatalog.DATA_SQL.toUpperCase().contains(forbidden));
        assertThrows(IllegalArgumentException.class,()->catalog.statement("UNKNOWN"));
    }
}
