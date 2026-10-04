package com.feelingpilates.transicion.programacion.r4.adapter.jpa;
import com.feelingpilates.transicion.programacion.r4.adapter.jpa.projection.*;
import com.feelingpilates.transicion.programacion.r4.read.*;
import jakarta.persistence.*;
import org.hibernate.query.NativeQuery;
import org.junit.jupiter.api.Test;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class AdjustmentProjectionQueryExecutorTest {
    @Test void typedNativeBindingScalarQueryOnlyOnceAndTwelveColumns() {
        EntityManager em=mock(EntityManager.class);Query q=mock(Query.class);NativeQuery nq=mock(NativeQuery.class,RETURNS_SELF);
        String sql=AdjustmentProjectionCatalog.R4_ADJUSTMENT_V1.statement("R4_ADJUSTMENTS_ON_DATE_V1").sql();
        when(em.createNativeQuery(sql)).thenReturn(q);when(q.unwrap(NativeQuery.class)).thenReturn(nq);
        var values=AdjustmentProjectionMapperTest.values("ADICION",new UUID(0,1));
        when(nq.getResultList()).thenReturn(Collections.singletonList(values));
        var result=new AdjustmentProjectionQueryExecutor(em,AdjustmentProjectionCatalog.R4_ADJUSTMENT_V1).read(AdjustmentProjectionMapperTest.DATE);
        assertEquals(1,result.size());assertEquals(LocalTime.parse("09:00:00.123456"),result.getFirst().horaInicioResultado());
        verify(nq).setParameter("fecha",AdjustmentProjectionMapperTest.DATE,LocalDate.class);verify(nq).setParameter("active",true,Boolean.class);
        verify(nq).getResultList();verify(em).createNativeQuery(sql);verify(nq,times(10)).addScalar(anyString(),any(Class.class));verify(nq,times(2)).addScalar(anyString(),any(org.hibernate.metamodel.model.domain.BasicDomainType.class));
    }
    @Test void scalarTypeNullListAndArityAreFailClosedAndCausePreserved() {
        var e=new AdjustmentProjectionQueryExecutor(mock(EntityManager.class),AdjustmentProjectionCatalog.R4_ADJUSTMENT_V1);
        for(int i=0;i<12;i++) {
            Object[] v=AdjustmentProjectionMapperTest.values("ADICION",new UUID(0,1));v[i]=new Object();
            assertEquals(AdjustmentReadFailure.Category.MALFORMED_PROJECTION,assertThrows(AdjustmentReadFailure.class,
                    ()->e.materialize(Collections.singletonList(v),AdjustmentProjectionMapperTest.DATE)).category());
        }
        for(List<?> rows:Arrays.asList(null,List.of("not-row"),Collections.singletonList(new Object[11]),Collections.singletonList(null)))
            assertThrows(AdjustmentReadFailure.class,()->e.materialize(rows,AdjustmentProjectionMapperTest.DATE));
        EntityManager em=mock(EntityManager.class);var cause=new PersistenceException("42501",new java.sql.SQLException("denied","42501"));
        when(em.createNativeQuery(anyString())).thenThrow(cause);var failure=assertThrows(AdjustmentReadFailure.class,
                ()->new AdjustmentProjectionQueryExecutor(em,AdjustmentProjectionCatalog.R4_ADJUSTMENT_V1).read(AdjustmentProjectionMapperTest.DATE));
        assertEquals(AdjustmentReadFailure.Category.DATABASE_READ_FAILURE,failure.category());assertSame(cause,failure.getCause());
    }
    @Test void exactNineCatalogIdsPlaceholdersAndNoAdditionalDataSemantics() {
        var c=AdjustmentProjectionCatalog.R4_ADJUSTMENT_V1;assertEquals(9,c.statements().size());
        for(var s:c.statements()) assertEquals(s.catalogId(),AdjustmentProjectionCatalog.id(s.positionalSql()));
        var data=c.statement("R4_ADJUSTMENTS_ON_DATE_V1");assertEquals("df80dc6e8de6a448b4b1432e9451a5bc659272275ab933e28b644a352a7b56ca",data.catalogId());
        assertTrue(data.sql().endsWith("WHERE fecha = :fecha AND activo = :active ORDER BY fecha, id"));
        for(String token:List.of("JOIN","DISTINCT","LIMIT","FOR UPDATE","SELECT *","BETWEEN")) assertFalse(data.sql().contains(token));
        assertEquals(17,AdjustmentProjectionCatalog.manifest().size());assertEquals(1,AdjustmentProjectionCatalog.manifest().stream().filter(s->s.equals(data.logicalId())).count());
        assertThrows(IllegalArgumentException.class,()->c.statement("unknown"));
    }
}
