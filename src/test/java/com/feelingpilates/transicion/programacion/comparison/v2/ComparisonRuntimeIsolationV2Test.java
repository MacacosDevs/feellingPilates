package com.feelingpilates.transicion.programacion.comparison.v2;
import org.junit.jupiter.api.Test;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.regex.*;
import javax.sql.DataSource;
import jakarta.persistence.EntityManagerFactory;
import static org.junit.jupiter.api.Assertions.*;
import static com.feelingpilates.transicion.programacion.comparison.v2.ComparisonTestFixturesV2.*;
class ComparisonRuntimeIsolationV2Test {
    static List<Class<?>> productionTypes() {
        return ComparisonArchitectureV2Test.MAIN.stream().<Class<?>>map(name->{try {return Class.forName(ComparisonArchitectureV2Test.PACKAGE+"."+name);}catch(ClassNotFoundException e){throw new AssertionError(e);}}).toList();
    }
    static boolean r5Type(Class<?> type) {
        if(type==null)return false;
        return productionTypes().stream().anyMatch(t->type.getName().equals(t.getName())||type.getName().startsWith(t.getName()+"$"));
    }
    static void noRegistration(ConfigurableApplicationContext context) {
        var factory=context.getBeanFactory();
        try {
            var field=org.springframework.beans.factory.support.DefaultListableBeanFactory.class.getDeclaredField("resolvableDependencies");field.setAccessible(true);
            var dependencies=(Map<?,?>)field.get(factory);
            for(var entry:dependencies.entrySet()) {
                assertFalse(entry.getKey() instanceof Class<?> type&&r5Type(type),"resolvable R5 type");
                assertFalse(r5Type(entry.getValue().getClass()),"resolvable R5 object");
            }
        } catch(ReflectiveOperationException e){throw new AssertionError(e);}
        for(var type:productionTypes())assertEquals(0,context.getBeanNamesForType(type,true,true).length,type.getName());
        var registered=new TreeSet<String>(Arrays.asList(factory.getBeanDefinitionNames()));
        registered.addAll(Arrays.asList(factory.getSingletonNames()));
        for(String name:registered) {
            if(factory.containsBeanDefinition(name)) {
                var definition=factory.getBeanDefinition(name);inspectDefinition(definition,context);String beanClass=definition.getBeanClassName();
                assertFalse(beanClass!=null&&productionTypes().stream().anyMatch(t->beanClass.equals(t.getName())||beanClass.startsWith(t.getName()+"$")),name);
            }
            assertFalse(r5Type(factory.getType(name,false)),name);
            for(String dependency:factory.getDependenciesForBean(name)) {
                assertFalse(dependency.contains(ComparisonArchitectureV2Test.PACKAGE),name+"->"+dependency);
                if(context.containsBean(dependency))assertFalse(r5Type(factory.getType(dependency,false)),name+"->"+dependency);
                else assertTrue(dependency.startsWith("org.springframework.")||dependency.matches("\\(inner bean\\)#[0-9a-f]+"),"unclassified resolvable dependency "+dependency);
            }
            assertFalse(r5Type(org.springframework.aop.support.AopUtils.getTargetClass(context.getBean(name))),"runtime instance "+name);
            if(factory.isFactoryBean(name)){var f=(FactoryBean<?>)context.getBean("&"+name);assertFalse(r5Type(f.getObjectType()),"factory "+name);}
        }
    }
    static void inspectDefinition(org.springframework.beans.factory.config.BeanDefinition d,ConfigurableApplicationContext context) {
        String name=d.getBeanClassName();assertFalse(name!=null&&productionTypes().stream().anyMatch(t->name.equals(t.getName())||name.startsWith(t.getName()+"$")),"nested definition "+name);
        assertFalse(r5Type(d.getResolvableType().resolve()),"nested resolvable definition");
        for(var p:d.getPropertyValues().getPropertyValueList())inspectValue(p.getValue(),context);
        for(var v:d.getConstructorArgumentValues().getGenericArgumentValues())inspectValue(v.getValue(),context);
        for(var v:d.getConstructorArgumentValues().getIndexedArgumentValues().values())inspectValue(v.getValue(),context);
    }
    static void inspectValue(Object value,ConfigurableApplicationContext context) {
        if(value==null)return;
        if(value instanceof org.springframework.beans.factory.config.BeanDefinition d)inspectDefinition(d,context);
        else if(value instanceof org.springframework.beans.factory.config.BeanDefinitionHolder h)inspectDefinition(h.getBeanDefinition(),context);
        else if(value instanceof org.springframework.beans.factory.config.RuntimeBeanReference r) {
            assertFalse(r.getBeanName().contains(ComparisonArchitectureV2Test.PACKAGE));
            if(context.containsBean(r.getBeanName()))assertFalse(r5Type(context.getType(r.getBeanName())));
        } else if(value instanceof Iterable<?> xs)xs.forEach(x->inspectValue(x,context));
        else if(value instanceof Map<?,?> m)m.forEach((k,v)->{inspectValue(k,context);inspectValue(v,context);});
        else if(value.getClass().isArray())for(int n=0;n<java.lang.reflect.Array.getLength(value);n++)inspectValue(java.lang.reflect.Array.get(value,n),context);
        else if(value instanceof Class<?> type)assertFalse(r5Type(type));
        else assertFalse(r5Type(value.getClass()));
    }
    @Test void fullExistingProductionContextsRetainRealGraphWithoutDatabaseOrReaderCalls() throws Exception {
        String previous=System.getProperty("spring.devtools.restart.enabled");System.setProperty("spring.devtools.restart.enabled","false");
        try {
            for(String profile:List.of("inherited","prod","default","dev","shadow-disabled","shadow-enabled","default-enabled")) {
                var attempts=new AtomicInteger();var contained=new AtomicInteger();
                DataSource ds=org.mockito.Mockito.mock(DataSource.class, invocation->{
                    if(invocation.getMethod().getName().equals("getConnection")){attempts.incrementAndGet();throw new AssertionError("comparison suite may not obtain a connection");}
                    return org.mockito.Answers.RETURNS_DEFAULTS.answer(invocation);
                });
                var scheduler=new ThreadPoolTaskScheduler();scheduler.setPoolSize(1);scheduler.setTaskDecorator(task->{contained.incrementAndGet();return ()->{};});
                var args=new ArrayList<>(List.of("--server.port=0","--spring.flyway.enabled=false","--spring.sql.init.mode=never",
                        "--spring.jpa.hibernate.ddl-auto=none","--spring.jpa.properties.hibernate.hbm2ddl.auto=none",
                        "--spring.jpa.properties.hibernate.boot.allow_jdbc_metadata_access=false","--spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect",
                        "--logging.level.root=OFF","--spring.main.banner-mode=off"));
                if(profile.startsWith("shadow")) {
                    args.add("--spring.profiles.active=f2e-detector-shadow");args.add("--app.f2e.detector.adapters.enabled="+profile.equals("shadow-enabled"));
                } else if(profile.equals("default-enabled")) {args.add("--spring.profiles.active=default");args.add("--app.f2e.detector.adapters.enabled=true");}
                else if(!profile.equals("inherited"))args.add("--spring.profiles.active="+profile);
                try(var c=new SpringApplicationBuilder(com.feelingpilates.FeelingpilatesApplication.class).registerShutdownHook(false)
                        .initializers(context->{var f=(org.springframework.beans.factory.support.BeanDefinitionRegistry)context.getBeanFactory();
                            var data=new RootBeanDefinition(DataSource.class,()->ds);f.registerBeanDefinition("dataSource",data);
                            var tasks=new RootBeanDefinition(ThreadPoolTaskScheduler.class,()->scheduler);tasks.setDestroyMethodName("shutdown");f.registerBeanDefinition("taskScheduler",tasks);})
                        .run(args.toArray(String[]::new))) {
                    assertTrue(c.isActive());assertEquals(0,attempts.get());
                    assertTrue(org.springframework.aop.support.AopUtils.isAopProxy(c.getBean("turnoInstructorService")));
                    assertTrue(org.springframework.aop.support.AopUtils.isAopProxy(c.getBean("reservaService")));
                    assertFalse(org.mockito.Mockito.mockingDetails(c.getBean("turnoInstructorService")).isMock());
                    assertFalse(org.mockito.Mockito.mockingDetails(c.getBean("reservaService")).isMock());
                    assertTrue(c.getBean(EntityManagerFactory.class).isOpen());assertNotNull(c.getBean(org.springframework.orm.jpa.JpaTransactionManager.class));
                    assertTrue(c.getBeanNamesForType(com.feelingpilates.calendario.repositorio.ReservaRepository.class).length>0);
                    assertFalse(org.mockito.Mockito.mockingDetails(c.getBean(com.feelingpilates.calendario.repositorio.ReservaRepository.class)).isMock());
                    assertFalse(c.getBean(ScheduledAnnotationBeanPostProcessor.class).getScheduledTasks().isEmpty());assertTrue(contained.get()>0,"real scheduled registrations are guarded");
                    noRegistration(c);productiveInventory(c);classpathEdges();assertEquals(0,attempts.get());
                    c.getBeanFactory().registerSingleton("negativeR5Registration",new ComparisonCompatibilityV2());
                    assertThrows(AssertionError.class,()->noRegistration(c));((org.springframework.beans.factory.support.DefaultListableBeanFactory)c.getBeanFactory()).destroySingleton("negativeR5Registration");
                    System.out.println("COMPARISON_V2_RUNTIME profile="+profile+" active="+Arrays.toString(c.getEnvironment().getActiveProfiles())+" default="+Arrays.toString(c.getEnvironment().getDefaultProfiles())+" dbAttempts="+attempts.get()+" guardedRegistrations="+contained.get());
                } finally {scheduler.shutdown();assertEquals(0,attempts.get(),"startup, inspection and shutdown remain DB-free");}
            }
        } finally {if(previous==null)System.clearProperty("spring.devtools.restart.enabled");else System.setProperty("spring.devtools.restart.enabled",previous);}
    }
    static void productiveInventory(ConfigurableApplicationContext c) throws Exception {
        int checked=0;try(var files=Files.walk(Path.of("src/main/java"))) {
            for(var p:files.filter(x->x.toString().endsWith(".java")).toList()) {
                String source=Files.readString(p);
                if(!Pattern.compile("@(Service|Component|RestController|Controller|Configuration)\\b").matcher(source).find())continue;
                if(source.contains("@Profile")||source.contains("@Conditional"))continue;
                var pkg=Pattern.compile("package\\s+([\\w.]+);").matcher(source);if(!pkg.find())continue;
                Class<?> type=Class.forName(pkg.group(1)+"."+p.getFileName().toString().replace(".java",""));
                assertTrue(c.getBeanNamesForType(type,true,true).length>0,"existing productive component "+type);checked++;
            }
        }
        assertTrue(checked>30);System.out.println("COMPARISON_V2_RUNTIME productiveSourceComponents="+checked);
    }
    static void classpathEdges() throws Exception {
        for(var type:productionTypes()) {
            try(var in=type.getResourceAsStream("/"+type.getName().replace('.','/')+".class")) {
                assertNotNull(in);String pool=new String(in.readAllBytes(),StandardCharsets.ISO_8859_1);
                for(String edge:List.of("org/springframework","jakarta/persistence","org/hibernate","java/sql/","javax/sql/","java/net/","java/nio/file/","java/lang/reflect/",
                        "NominalJpaReader","AdjustmentJpaReader","NominalReadSnapshotContext","AdjustmentReadSnapshotContext","ReadPort","/repositorio/","/servicio/","/adapter/"))assertFalse(pool.contains(edge),type+":"+edge);
            }
        }
    }
    static final class ComposerFactory implements FactoryBean<ComparisonCompatibilityV2> {
        public ComparisonCompatibilityV2 getObject(){return new ComparisonCompatibilityV2();}public Class<?> getObjectType(){return ComparisonCompatibilityV2.class;}
    }
    static final class CallerFixture {final ComparisonCompatibilityV2 composer;CallerFixture(ComparisonCompatibilityV2 composer){this.composer=composer;}}
    @Test void independentBeanFactoryDependencyAndCallerNegativesUseSameDetector() {
        try(var c=new org.springframework.context.annotation.AnnotationConfigApplicationContext()) {
            c.registerBean("badFactory",ComposerFactory.class);c.refresh();assertThrows(AssertionError.class,()->noRegistration(c));
        }
        try(var c=new org.springframework.context.annotation.AnnotationConfigApplicationContext()) {
            c.registerBean("composer",ComparisonCompatibilityV2.class);c.registerBean("productiveCaller",CallerFixture.class);c.refresh();
            assertTrue(Arrays.asList(c.getBeanFactory().getDependenciesForBean("productiveCaller")).contains("composer"));assertThrows(AssertionError.class,()->noRegistration(c));
        }
        try(var c=new org.springframework.context.annotation.AnnotationConfigApplicationContext()) {
            c.getBeanFactory().registerResolvableDependency(ComparisonCompatibilityV2.class,new ComparisonCompatibilityV2());c.refresh();
            assertThrows(AssertionError.class,()->noRegistration(c));
        }
        try(var c=new org.springframework.context.annotation.AnnotationConfigApplicationContext()) {
            c.refresh();c.getBeanFactory().registerSingleton("nestedR5Registration",new ComparisonCandidateEvidenceV2.Observation("a","b",ComparisonCandidateEvidenceV2.Match.MISMATCH));
            assertArrayEquals(new String[]{"nestedR5Registration"},c.getBeanNamesForType(ComparisonCandidateEvidenceV2.Observation.class));
            assertFalse(c.getBeanFactory().containsBeanDefinition("nestedR5Registration"));
            assertThrows(AssertionError.class,()->noRegistration(c));
        }
        assertThrows(AssertionError.class,()->ComparisonArchitectureV2Test.caller("class Productive { ComparisonCompatibilityV2 composer; }"));
    }
    @Test void syntheticOutputHasNoRealDetectorAdmissionOrProductiveRoute() throws Exception {
        var i=comparison(input(1,new int[]{0},0));var r=new ComparisonCompatibilityV2().compare(i);assertEquals(ComparisonInputV2.Mode.SYNTHETIC_DESIGN_FIXTURE,i.mode());
        assertTrue(productionTypes().stream().noneMatch(t->com.feelingpilates.transicion.programacion.detector.SourceSnapshot.class.isAssignableFrom(t)));
        ComparisonArchitectureV2Test.caller(Files.readString(Path.of("src/main/java/com/feelingpilates/FeelingpilatesApplication.java")));
        for(var method:ComparisonResultV2.class.getDeclaredMethods())assertFalse(method.getReturnType().getSimpleName().contains("DetectorEvaluationRequest"));
    }
}
