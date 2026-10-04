package com.feelingpilates.transicion.programacion.r4.adapter.jpa.testinfra;

import com.feelingpilates.transicion.programacion.r4.adapter.jpa.*;
import com.feelingpilates.transicion.programacion.r4.adapter.jpa.mapper.*;
import com.feelingpilates.transicion.programacion.r4.adapter.jpa.projection.*;
import com.feelingpilates.transicion.programacion.r4.read.*;
import com.feelingpilates.transicion.programacion.adapter.jpa.testinfra.F2eSelectOnlyRole;
import jakarta.persistence.*;
import org.flywaydb.core.Flyway;
import org.postgresql.ds.PGSimpleDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.*;
import org.springframework.context.annotation.*;
import org.springframework.orm.jpa.*;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import javax.sql.DataSource;
import java.sql.*;
import java.time.*;
import java.util.*;

@TestConfiguration(proxyBeanMethods=false)
@EnableTransactionManagement
@EnableAspectJAutoProxy(exposeProxy=true)
public class AdjustmentPostgresTestConfiguration {
    public static final LocalDate FECHA=LocalDate.of(2026,10,3);
    public static final UUID SERIES=UUID.fromString("10000000-0000-0000-0000-000000000001");
    public static final UUID CANCEL=UUID.fromString("10000000-0000-0000-0000-000000000002");
    public static final UUID REPLACE=UUID.fromString("70000000-0000-0000-0000-000000000003");
    public static final UUID ADD=UUID.fromString("80000000-0000-0000-0000-000000000004");
    private F2eSelectOnlyRole role;
    private List<List<List<String>>> canonical;
    private UUID salon,instructor,activity;
    @Bean(destroyMethod="stop") PostgreSQLContainer<?> r4Container() {
        var c=new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"));c.start();return c;
    }
    public static PGSimpleDataSource ds(String url,String username,String password) {
        var ds=new PGSimpleDataSource();ds.setURL(url);ds.setUser(username);ds.setPassword(password);return ds;
    }
    public static String url(PostgreSQLContainer<?> c) { return "jdbc:postgresql://"+c.getHost()+":"+c.getMappedPort(5432)+"/"+c.getDatabaseName(); }
    @Bean("r4Admin") DataSource admin(PostgreSQLContainer<?> c,org.springframework.core.env.Environment environment) {
        var admin=ds(url(c),c.getUsername(),c.getPassword());
        try(var conn=admin.getConnection();var q=conn.createStatement()) { q.execute("CREATE DATABASE r4_canonical"); }
        catch(SQLException e) { throw new IllegalStateException(e); }
        var expected=ds(url(c).replace("/"+c.getDatabaseName(),"/r4_canonical"),c.getUsername(),c.getPassword());
        migrate(expected,"47");canonical=readSchema(expected);validateCanonical(canonical);
        System.out.println("R4_RAW_CANONICAL_SCHEMA "+canonical);
        String target=environment.getProperty("r4.test.target","47");migrate(admin,target);
        role=F2eSelectOnlyRole.crear(admin,"f2e_r4_reader_","adjustments-a",target.equals("47")?
                Set.of("public.programacion_ajuste_fecha","public.flyway_schema_history"):Set.of("public.flyway_schema_history"));
        if(target.equals("47")) reset(admin);return admin;
    }
    private static void verifyMigrationBytes() {
        Map<String,String> expected=Map.ofEntries(
            Map.entry("V10__salones_semilla.sql","0806a7d947f5266a2a86ee4494885ca4413ffcb46a0d5f88c4044a07f6780f76"),
            Map.entry("V11__salones_gestion.sql","686184cebdda9003c51f45ab0cc6c7a486c4511026dea1e71892fbe75985e59b"),
            Map.entry("V12__salon_direccion_completa.sql","baaf4378963894eb972917767172297130ee59e204f84fc9962d10e8129f5c3b"),
            Map.entry("V13__usuario_foto_binaria.sql","01a04f3919be365a770b273a00168616a0cfb33412ab711ec493edd4c217b4ef"),
            Map.entry("V14__salon_inventario_maquinas.sql","7ef8be8cfd0426ba643b3c43bc848812c92c063d2ffe3681024c34eb8dd843e8"),
            Map.entry("V15__calendario_instructores.sql","956e8530816d830756bbc8c477b61105b26f5e2e3bc0d96d468d748a87796310"),
            Map.entry("V16__permisos_calendario_granular.sql","161edc552853d6c83464b5787f7f5065ab8577d7454f27bdec5e7fa2f5c006da"),
            Map.entry("V17__turno_instructor_actividad.sql","43b1ab7f41feaddc9bce6961581cd2f99f8a9c90e482ce7f8ba5fd29c544190f"),
            Map.entry("V18__salon_horario_excepcion.sql","453b8e5afff4a20c3d2234ea311485a7fee77efeb7a6333dfe6417d8126f7627"),
            Map.entry("V19__turno_instructor_multiples.sql","256199bce25ef692f444753307c2723149cee449f8ee4082a18fa57389d0d0b1"),
            Map.entry("V1__esquema_usuarios_rbac.sql","99481b564267ac0c5e5e2f4aa1175140ce24a6213e853bceb6bf115f3924e9f8"),
            Map.entry("V20__turno_instructor_asignacion.sql","9302feeace2fa0547e850f7e053bc670c8a6734951fe38f57ee0de6a5065b7f1"),
            Map.entry("V21__limpiar_asignaciones_sin_especialidad.sql","dd5318b5b3060756eeb440daed26236416916ff0b0353e68619bbb80b53998ad"),
            Map.entry("V22_1__paquetes_y_compras.sql","cf979d2f8e8346359835d7f3bd23ada8c4a2370715a216f17e7060f7b8c19c32"),
            Map.entry("V22_2__compra_idempotencia.sql","12aa65639797ab47f625b58c4fe5dcf8c3f57b142a31c1409f28ebcfbed50523"),
            Map.entry("V22_3__permiso_reembolsar_pagos.sql","fb62666ab153404e7f912c8d845489c69945f06ad59ee3946b1a43206687c201"),
            Map.entry("V22__asignacion_rango_horario.sql","2651e52a312b11c1e7daaf19bf152b5df2d569b40a62c9890d8be7a721bd182b"),
            Map.entry("V23__caja_paquete_actividades.sql","8a7dcbda84afada3a0d43ec55047f15c7e4ff969c522e2cb013b7c6bc81bca01"),
            Map.entry("V24__eliminar_paquetes_semilla.sql","3439e511642a81e03b00a8fbd2d82c20df383fcdde6e3972133b6733130031df"),
            Map.entry("V25__compra_salon.sql","151d97d78d46cf5047145cdad3f82212ed5c020b4e70f304d03312c927707ff7"),
            Map.entry("V26__compra_grupo.sql","32a29edf4df2173cbf090f70cddcec94106411bc3501043e1b2858109f08acf6"),
            Map.entry("V27__compra_motivo_estado.sql","ff29c4097edf05ba3c1a5e36e592f51f4398b64b38f2b8f1b9a384723ad41e7e"),
            Map.entry("V28__permisos_caja_granulares.sql","39caeddc01248976ce069dc3d4f131a55b1192d41753d5fbdef906c93f1e329a"),
            Map.entry("V29__permiso_vista_caja.sql","e703ffe6cd3c6cf6083e7b9799ecf3226cee8690f9b6fc821c94ff967b933c23"),
            Map.entry("V2__datos_iniciales_rbac.sql","0dd3f21ad92449ae2f0833d6ab15a4870aced4a44c942dd2007ec2845809bd02"),
            Map.entry("V30__simplificar_descripcion_permisos_caja.sql","9721121a15e9361d1de0875080101c592647a091406b812561d2018e4aaf1f33"),
            Map.entry("V31__reestructurar_permisos_caja.sql","0f9b7d0c08c4fe0a4ac179ede49166a1aaeffbdf02887e448e230862bba198d9"),
            Map.entry("V32__renombrar_permisos_caja_a_venta.sql","19ef5766baf60dd5adc285412a111a70ab5c9f871e9af3a985ba3827fbe25e50"),
            Map.entry("V33__granularizar_permisos_catalogo_venta.sql","7894280b0a26b47d0f5d72782ed5623ddc3181f1512b48f6add6271e25bb0f3e"),
            Map.entry("V34__renombrar_permisos_catalogo_a_servicios.sql","84b311b98f2add720a4b2b746dd8458941afda7f299c42d1f3f2c51948ea9b7f"),
            Map.entry("V35__eliminar_permiso_venta_reembolsar.sql","052d173e9d123276a8e74484b31baf51cf0530ebc49daad6f9acd12daf4b046a"),
            Map.entry("V36__recursos_y_actividad_recurso.sql","247bd2e963c8b340de5e9370d237386b2f488dfeb4a738fbf0b9ab4c10bb31df"),
            Map.entry("V37__permisos_actividades.sql","f9150e47cae63f3672e423356028cdf854a896dacd77fc976fedcc55bfd5c323"),
            Map.entry("V38__participantes_por_reserva.sql","3ab7d119c919693a87958c481218ce0642c9333ec3fda60744f2d54ea2eaccb8"),
            Map.entry("V39__cantidad_actividad_recurso.sql","09673b33102ac04e2493ee74046b792d8685c8a2e0cb026b0330bbeb947fdd0f"),
            Map.entry("V3__invitaciones_usuario.sql","a1f899eef559a66201bc529af54e1d30427c5b88dc97adea25809d9b12ba0506"),
            Map.entry("V40__etiquetas_actividad.sql","df0fe140f9b0a0d7fa956e421589c2a336cd5280dd58dd39e193596794145e33"),
            Map.entry("V41__programacion_bloque_asignacion.sql","dc2e2ab0eaeb1070e803210bdaa8093899810f7251272229de07f000507001f4"),
            Map.entry("V42__salon_politicas_programacion.sql","292e6526485433ababa8c0e3d04eaf3cb59aadefe4554df62fb36d41ef70afc9"),
            Map.entry("V43__horario_operacion_vigencia.sql","f66940c681c74a765e25029a7fe75fe9d64cf0ed05e6c0faa657f81ff32a3761"),
            Map.entry("V44__btree_gist_extension.sql","a290425e1d173f7132fa66e1360411c0897b326e79744b07a95fc74af08c6722"),
            Map.entry("V45__horario_operacion_exclude_vigencia.sql","270d646845ab485b8b5fdb787134e4204c9c120da406d269d6d820a8a71875bf"),
            Map.entry("V46__horario_operacion_drop_unique_dia.sql","f0de75f89423779dcc22e1a90aecd5e09cb084c11b32546463b54c45ecc4e755"),
            Map.entry("V47__programacion_ajuste_fecha.sql","afcd7316ec922e1afd10fa719cd3d26815b62e8b0a46a3641a266df8ee390e06"),
            Map.entry("V4__usuario_admin_inicial.sql","28398c642f80dc50668634cdfbfc7c699bddeb5511d57d4ba9f7653f7fc96655"),
            Map.entry("V5__permiso_activar_usuario.sql","3aa1f0e16b57cb3ba3f49f8e1f3f7f9309636f0e6bb8857182f357c7ed680eef"),
            Map.entry("V6__rol_super_admin.sql","86b0e88db30e280ce87c5a22d7e6440a4812129aa04f8e014f2fa2ff754c8c21"),
            Map.entry("V7__categoria_permiso.sql","8bc8c9d2067b57b783f615787cb92000e19e8a5ef53ecd52be77fca8afc41e32"),
            Map.entry("V8__permiso_gestionar_roles.sql","4481cded81b9264fc2fc1b74a735e32d238543a1130bf66ee3732920545062b5"),
            Map.entry("V9__catalogo_ubicaciones.sql","177fb922724e544e99c009383d8946228c6ef3aa3e88350acb67dd72a1c2f230"));
        try {
            Set<String> actual=new HashSet<>();
            try(var paths=java.nio.file.Files.list(java.nio.file.Path.of("src/main/resources/db/migration"))) {
                paths.filter(p->p.toString().endsWith(".sql")).forEach(p->actual.add(p.getFileName().toString()));
            }
            if(!actual.equals(expected.keySet())) throw new IllegalStateException("frozen migration inventory");
            for(var entry:expected.entrySet()) {
                byte[] original=java.nio.file.Files.readAllBytes(java.nio.file.Path.of("src/main/resources/db/migration",entry.getKey()));
                try(var input=AdjustmentPostgresTestConfiguration.class.getResourceAsStream("/db/migration/"+entry.getKey())) {
                    if(input==null||!Arrays.equals(original,input.readAllBytes())||!entry.getValue().equals(
                            HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(original))))
                        throw new IllegalStateException("frozen migration drift: "+entry.getKey());
                }
            }
        } catch(java.io.IOException|java.security.NoSuchAlgorithmException e) { throw new IllegalStateException(e); }
    }
    public static void migrate(DataSource ds,String target) {
        verifyMigrationBytes();var f=Flyway.configure().dataSource(ds).locations("classpath:db/migration").target(target).load();f.migrate();f.validate();
        if(target.equals("47") && (f.info().applied().length!=50||!f.info().current().getVersion().getVersion().equals("47")))
            throw new IllegalStateException("canonical Flyway50/V47 required");
    }
    public static List<List<List<String>>> readSchema(DataSource ds) {
        List<List<List<String>>> sets=new ArrayList<>();
        try(var c=ds.getConnection()) {
            for(String id:AdjustmentTransactionTestOwner.schemaIds()) {
                List<Object[]> rows=new ArrayList<>();
                try(var q=c.prepareStatement(AdjustmentProjectionCatalog.R4_ADJUSTMENT_V1.statement(id).sql());var r=q.executeQuery()) {
                    while(r.next()) { Object[] row=new Object[r.getMetaData().getColumnCount()];for(int i=0;i<row.length;i++) row[i]=r.getObject(i+1);rows.add(row); }
                }
                sets.add(AdjustmentTransactionTestOwner.canonicalRows(rows));
            }
        } catch(SQLException e) { throw new IllegalStateException(e); }
        return List.copyOf(sets);
    }
    private static void validateCanonical(List<List<List<String>>> s) {
        if(s.size()!=4||s.get(0).size()!=50||s.get(1).size()!=12||s.get(2).size()!=7||s.get(3).size()!=3)
            throw new IllegalStateException("canonical adjustment structure");
        var v47=s.get(0).getLast();
        if(!v47.get(1).equals("TEXT/47")||!v47.get(5).equals("INTEGER/-1624594375")||!v47.get(6).equals("BOOLEAN/true"))
            throw new IllegalStateException("installed V47 checksum");
    }
    @Bean AdjustmentJdbcCapture adjustmentJdbcCapture() { return new AdjustmentJdbcCapture(); }
    @Bean("r4DS") DataSource readerDs(@Qualifier("r4Admin") DataSource admin,PostgreSQLContainer<?> c,AdjustmentJdbcCapture capture) {
        return capture.wrap(role.crearDataSource(url(c)));
    }
    @Bean AdjustmentStatementPolicyInspector adjustmentStatementPolicyInspector() { return new AdjustmentStatementPolicyInspector(); }
    @Bean(name="r4EMF",destroyMethod="close") EntityManagerFactory factory(@Qualifier("r4DS") DataSource ds,AdjustmentStatementPolicyInspector inspector) {
        var f=new LocalContainerEntityManagerFactoryBean();f.setDataSource(ds);f.setPersistenceUnitName("r4-test-only");
        f.setPackagesToScan("com.feelingpilates.transicion.programacion.r4");f.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        f.setJpaPropertyMap(Map.of("hibernate.hbm2ddl.auto","none","hibernate.dialect","org.hibernate.dialect.PostgreSQLDialect",
                "hibernate.default_schema","public","hibernate.session_factory.statement_inspector",inspector,
                "hibernate.type.java_time_use_direct_jdbc",true));
        f.afterPropertiesSet();return Objects.requireNonNull(f.getObject());
    }
    @Bean("r4EM") EntityManager shared(@Qualifier("r4EMF") EntityManagerFactory factory,AdjustmentJdbcCapture jdbc) {
        return jdbc.wrapEntityManager(SharedEntityManagerCreator.createSharedEntityManager(factory));
    }
    @Bean("f2eReaderTransactionManager") JpaTransactionManager manager(@Qualifier("r4EMF") EntityManagerFactory f,@Qualifier("r4DS") DataSource ds) {
        var m=new JpaTransactionManager(f);m.setDataSource(ds);return m;
    }
    @Bean AdjustmentProjectionQueryExecutor adjustmentExecutor(@Qualifier("r4EM") EntityManager em) { return new AdjustmentProjectionQueryExecutor(em,AdjustmentProjectionCatalog.R4_ADJUSTMENT_V1); }
    @Bean AdjustmentTransactionTestOwner.Registry adjustmentRegistry() { return new AdjustmentTransactionTestOwner.Registry(); }
    @Bean("adjustmentReader") AdjustmentJpaReader reader(AdjustmentProjectionQueryExecutor e,AdjustmentTransactionTestOwner.Registry r) {
        return new AdjustmentJpaReader(e,new AdjustmentProjectionMapper(),AdjustmentProjectionCatalog.R4_ADJUSTMENT_V1,r);
    }
    @Bean public static org.springframework.beans.factory.config.BeanPostProcessor adjustmentFailureBoundary() {
        return new org.springframework.beans.factory.config.BeanPostProcessor() {
            @Override public Object postProcessAfterInitialization(Object bean,String name) {
                if(!name.equals("adjustmentReader")) return bean;
                var p=new org.springframework.aop.framework.ProxyFactory(bean);p.setInterfaces(AdjustmentReadPort.class);p.setExposeProxy(true);
                p.addAdvice((org.aopalliance.intercept.MethodInterceptor) invocation->{
                    try { return invocation.proceed(); }
                    catch(org.springframework.transaction.IllegalTransactionStateException|org.springframework.beans.factory.NoSuchBeanDefinitionException e) {
                        Object[] args=invocation.getArguments();LocalDate date=args.length==2&&args[1] instanceof LocalDate d?d:null;
                        throw new AdjustmentReadFailure(AdjustmentReadFailure.Category.TRANSACTION_CONTEXT_INVALID,date,List.of(),e);
                    }
                });return p.getProxy();
            }
        };
    }
    @Bean AdjustmentTransactionTestOwner.Descriptor descriptor(PostgreSQLContainer<?> c,@Qualifier("r4Admin") DataSource admin,
            @Qualifier("r4DS") DataSource ds,@Qualifier("r4EMF") EntityManagerFactory f,@Qualifier("r4EM") EntityManager em,
            @Qualifier("f2eReaderTransactionManager") JpaTransactionManager m) {
        return new AdjustmentTransactionTestOwner.Descriptor("fixture:postgres16:r4:adjustments-a",url(c),c.getDatabaseName(),role.principal(),ds,f,em,m,canonical);
    }
    @Bean("adjustmentInnerOwner") AdjustmentTransactionTestOwner.Inner inner(AdjustmentReadPort r,AdjustmentProjectionQueryExecutor e,
            AdjustmentTransactionTestOwner.Descriptor d,AdjustmentTransactionTestOwner.Registry registry,AdjustmentJdbcCapture jdbc) {
        return new AdjustmentTransactionTestOwner.Inner(r,e,d,registry,jdbc);
    }
    @Bean AdjustmentTransactionTestOwner adjustmentOwner(ApplicationContext c,AdjustmentJdbcCapture j) { return new AdjustmentTransactionTestOwner(c,j); }
    public String principal() { return role.principal(); }
    public UUID salon() { return salon; } public UUID instructor() { return instructor; } public UUID activity() { return activity; }
    public void reset(DataSource admin) {
        try(var c=admin.getConnection();var q=c.createStatement()) {
            q.executeUpdate("DELETE FROM programacion_ajuste_fecha");
            salon=first(c,"salon");instructor=first(c,"usuario");activity=first(c,"tipo_actividad");
        } catch(SQLException e) { throw new IllegalStateException(e); }
        insert(admin,CANCEL,"CANCELACION",FECHA,SERIES,true);
        insert(admin,REPLACE,"REEMPLAZO",FECHA,UUID.fromString("10000000-0000-0000-0000-000000000099"),true);
        insert(admin,ADD,"ADICION",FECHA,null,true);
    }
    public void insert(DataSource admin,UUID id,String tipo,LocalDate fecha,UUID series,boolean active) {
        boolean cancel=tipo.equals("CANCELACION");
        try(var c=admin.getConnection();var q=c.prepareStatement("INSERT INTO programacion_ajuste_fecha "
                +"(id,tipo,fecha,asignacion_serie_id,salon_resultado_id,instructor_resultado_id,tipo_actividad_resultado_id,hora_inicio_resultado,hora_fin_resultado,activo,creado_en,actualizado_en) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)")) {
            q.setObject(1,id);q.setString(2,tipo);q.setObject(3,fecha);q.setObject(4,series);
            q.setObject(5,cancel?null:salon);q.setObject(6,cancel?null:instructor);q.setObject(7,cancel?null:activity);
            q.setObject(8,cancel?null:LocalTime.of(9,0,0,123456000));q.setObject(9,cancel?null:LocalTime.of(10,0,0,654321000));
            q.setBoolean(10,active);q.setObject(11,OffsetDateTime.parse("2026-10-03T06:00:00.123456Z"));
            q.setObject(12,OffsetDateTime.parse("2026-10-02T06:00:00.654321Z"));q.executeUpdate();
        } catch(SQLException e) { throw new IllegalStateException(e); }
    }
    private UUID first(Connection c,String table) throws SQLException {
        try(var q=c.createStatement();var r=q.executeQuery("SELECT id FROM "+table+" ORDER BY id LIMIT 1")) {
            if(!r.next()) throw new IllegalStateException("empty master fixture "+table);return r.getObject(1,UUID.class);
        }
    }
}
