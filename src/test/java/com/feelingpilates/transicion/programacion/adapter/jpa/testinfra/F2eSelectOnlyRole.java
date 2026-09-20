package com.feelingpilates.transicion.programacion.adapter.jpa.testinfra;

import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.sql.Connection;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.Base64;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class F2eSelectOnlyRole {

    private final String principal;
    private final String contrasena;

    private F2eSelectOnlyRole(String principal, String contrasena) {
        this.principal = principal;
        this.contrasena = contrasena;
    }

    public static F2eSelectOnlyRole crear(DataSource privilegiada, String claveCatalogo) {
        String principal = "f2e_r1_reader_" + claveCatalogo.replace('-', '_');
        byte[] secreto = new byte[24];
        new SecureRandom().nextBytes(secreto);
        String contrasena = Base64.getUrlEncoder().withoutPadding().encodeToString(secreto);
        try (var conexion = privilegiada.getConnection(); var sentencia = conexion.createStatement()) {
            sentencia.execute("DROP ROLE IF EXISTS " + principal);
            sentencia.execute("CREATE ROLE " + principal + " LOGIN PASSWORD '" + contrasena + "'");
            sentencia.execute("REVOKE CONNECT, TEMPORARY ON DATABASE " + conexion.getCatalog() + " FROM PUBLIC");
            sentencia.execute("REVOKE CREATE ON SCHEMA public FROM PUBLIC");
            sentencia.execute("REVOKE EXECUTE ON ALL FUNCTIONS IN SCHEMA public FROM PUBLIC");
            sentencia.execute("GRANT CONNECT ON DATABASE " + conexion.getCatalog() + " TO " + principal);
            sentencia.execute("GRANT USAGE ON SCHEMA public TO " + principal);
            sentencia.execute("GRANT SELECT ON TABLE public.reserva TO " + principal);
        } catch (SQLException excepcion) {
            throw new IllegalStateException("Unable to create F2E SELECT-only role", excepcion);
        }
        return new F2eSelectOnlyRole(principal, contrasena);
    }

    public DataSource crearDataSource(String jdbcUrl) {
        DataSourceLectorContabilizado dataSource = new DataSourceLectorContabilizado();
        dataSource.configurarUrlCanonica(jdbcUrl);
        dataSource.setUser(principal);
        dataSource.setPassword(contrasena);
        return dataSource;
    }

    public void comprobarEscrituraDenegada(DataSource readerDataSource) {
        try (var conexion = readerDataSource.getConnection();
             var sentencia = conexion.prepareStatement(
                     "INSERT INTO public.reserva "
                             + "(id,salon_id,instructor_id,cliente_id,tipo_actividad_id,fecha,hora_inicio,hora_fin,estado) "
                             + "VALUES (?,?,?,?,?,?,?,?,?)")) {
            sentencia.executeUpdate();
            throw new AssertionError("SELECT-only principal unexpectedly inserted a reservation");
        } catch (SQLException esperada) {
            if (!"42501".equals(esperada.getSQLState())) {
                throw new IllegalStateException("Negative write control failed for an unexpected reason", esperada);
            }
        }
    }

    public String principal() {
        return principal;
    }

    static final class DataSourceLectorContabilizado extends PGSimpleDataSource {
        private static final Pattern URL_CANONICA = Pattern.compile(
                "jdbc:postgresql://([a-z0-9.-]+):([0-9]+)/([A-Za-z0-9_]+)");
        private static final Set<String> METODOS_ESTADO_CONEXION = Set.of(
                "setAutoCommit", "setReadOnly", "setTransactionIsolation", "setCatalog", "setSchema",
                "setHoldability", "setTypeMap", "setClientInfo", "setNetworkTimeout");
        private final AtomicLong preparacionesJdbc = new AtomicLong();
        private final AtomicLong conexionesEntregadas = new AtomicLong();
        private final AtomicReference<String> mutacionMetadatos = new AtomicReference<>();
        private final AtomicReference<String> ultimaUrlMetadataOriginal = new AtomicReference<>();
        private final AtomicReference<String> ultimaUrlMetadataEntregada = new AtomicReference<>();
        private final AtomicLong observacionesMetadataDesdeMutacion = new AtomicLong();
        private final AtomicBoolean conmutarFisicaEnSiguienteConexion = new AtomicBoolean();
        private final AtomicBoolean conmutacionFisicaRealizada = new AtomicBoolean();
        private final AtomicBoolean recursosFisicosDistintos = new AtomicBoolean();
        private volatile String urlConfigurada;

        void configurarUrlCanonica(String url) {
            Matcher partes = URL_CANONICA.matcher(url);
            if (!partes.matches()) {
                throw new IllegalArgumentException("Invalid canonical R1 JDBC URL");
            }
            int puerto;
            try {
                puerto = Integer.parseInt(partes.group(2));
            } catch (NumberFormatException excepcion) {
                throw new IllegalArgumentException("Invalid canonical R1 JDBC URL", excepcion);
            }
            if (puerto < 1 || puerto > 65_535 || !Integer.toString(puerto).equals(partes.group(2))) {
                throw new IllegalArgumentException("Invalid canonical R1 JDBC URL");
            }
            setApplicationName(null);
            urlConfigurada = url;
            setServerNames(new String[]{partes.group(1)});
            setPortNumbers(new int[]{puerto});
            setDatabaseName(partes.group(3));
        }

        String urlConfigurada() {
            return urlConfigurada;
        }

        @Override
        public Connection getConnection() throws SQLException {
            return crearConexionParticipante(null, getUser(), getPassword());
        }

        @Override
        public Connection getConnection(String usuario, String contrasena) throws SQLException {
            return crearConexionParticipante(null, usuario, contrasena);
        }

        long preparacionesJdbc() {
            return preparacionesJdbc.get();
        }

        long conexionesEntregadas() {
            return conexionesEntregadas.get();
        }

        String ultimaUrlMetadataOriginal() {
            return ultimaUrlMetadataOriginal.get();
        }

        String ultimaUrlMetadataEntregada() {
            return ultimaUrlMetadataEntregada.get();
        }

        long observacionesMetadataDesdeMutacion() {
            return observacionesMetadataDesdeMutacion.get();
        }

        void conmutarFisicaEnSiguienteConexion() {
            conmutarFisicaEnSiguienteConexion.set(true);
        }

        boolean conmutacionFisicaRealizada() {
            return conmutacionFisicaRealizada.get();
        }

        boolean recursosFisicosDistintos() {
            return recursosFisicosDistintos.get();
        }

        void configurarMutacionMetadatos(String mutacion) {
            ultimaUrlMetadataOriginal.set(null);
            ultimaUrlMetadataEntregada.set(null);
            observacionesMetadataDesdeMutacion.set(0);
            mutacionMetadatos.set(mutacion);
        }

        void limpiarMutacionMetadatos() {
            mutacionMetadatos.set(null);
        }

        DataSourceLectorContabilizado copiaReal() {
            DataSourceLectorContabilizado copia = new DataSourceLectorContabilizado();
            copia.configurarUrlCanonica(urlConfigurada);
            copia.setUser(getUser());
            copia.setPassword(getPassword());
            return copia;
        }

        private Connection crearConexionParticipante(
                Connection conexionExistente,
                String usuario,
                String contrasena) throws SQLException {
            Connection primera = envolver(conexionExistente != null
                    ? conexionExistente : super.getConnection(usuario, contrasena));
            conexionesEntregadas.incrementAndGet();
            if (!conmutarFisicaEnSiguienteConexion.getAndSet(false)) {
                return primera;
            }
            Connection segunda = envolver(usuario == null
                    ? super.getConnection() : super.getConnection(usuario, contrasena));
            return envolverConmutacionFisica(primera, segunda);
        }

        private Connection envolver(Connection delegada) {
            return (Connection) Proxy.newProxyInstance(
                    Connection.class.getClassLoader(),
                    new Class<?>[]{Connection.class},
                    (proxy, metodo, argumentos) -> {
                        String mutacion = mutacionMetadatos.get();
                        if ("DATABASE".equals(mutacion) && "getCatalog".equals(metodo.getName())) {
                            return delegada.getCatalog() + "_hostile";
                        }
                        if ("SCHEMA".equals(mutacion) && "getSchema".equals(metodo.getName())) {
                            return delegada.getSchema() + "_hostile";
                        }
                        if ("getMetaData".equals(metodo.getName())) {
                            var metadata = delegada.getMetaData();
                            return Proxy.newProxyInstance(
                                    java.sql.DatabaseMetaData.class.getClassLoader(),
                                    new Class<?>[]{java.sql.DatabaseMetaData.class},
                                    (proxyMetadata, metodoMetadata, argumentosMetadata) -> {
                                        if (esMutacionUrl(mutacion)
                                                && "getURL".equals(metodoMetadata.getName())) {
                                            String original = metadata.getURL();
                                            String entregada = urlMetadataHostil(mutacion, original);
                                            observacionesMetadataDesdeMutacion.incrementAndGet();
                                            ultimaUrlMetadataOriginal.set(original);
                                            ultimaUrlMetadataEntregada.set(entregada);
                                            return entregada;
                                        }
                                        if ("getURL".equals(metodoMetadata.getName())) {
                                            String original = metadata.getURL();
                                            observacionesMetadataDesdeMutacion.incrementAndGet();
                                            ultimaUrlMetadataOriginal.set(original);
                                            ultimaUrlMetadataEntregada.set(original);
                                            return original;
                                        }
                                        if ("PRINCIPAL".equals(mutacion)
                                                && "getUserName".equals(metodoMetadata.getName())) {
                                            return metadata.getUserName() + "_hostile";
                                        }
                                        try {
                                            return metodoMetadata.invoke(metadata, argumentosMetadata);
                                        } catch (InvocationTargetException excepcion) {
                                            throw excepcion.getCause();
                                        }
                                    });
                        }
                        if (metodo.getName().startsWith("prepareStatement")
                                || metodo.getName().startsWith("prepareCall")) {
                            preparacionesJdbc.incrementAndGet();
                        }
                        try {
                            return metodo.invoke(delegada, argumentos);
                        } catch (InvocationTargetException excepcion) {
                            throw excepcion.getCause();
                        }
                    });
        }

        private Connection envolverConmutacionFisica(Connection primera, Connection segunda) throws SQLException {
            AtomicReference<Connection> actual = new AtomicReference<>(primera);
            Object recursoInicial = primera.unwrap(org.postgresql.PGConnection.class);
            return (Connection) Proxy.newProxyInstance(
                    Connection.class.getClassLoader(),
                    new Class<?>[]{Connection.class},
                    (proxy, metodo, argumentos) -> {
                        String nombre = metodo.getName();
                        if ("equals".equals(nombre)) return proxy == argumentos[0];
                        if ("hashCode".equals(nombre)) return System.identityHashCode(proxy);
                        if ("toString".equals(nombre)) return "F2E transaction-bound switching Connection";
                        if ("close".equals(nombre)) {
                            cerrarAmbas(primera, segunda);
                            return null;
                        }
                        if ("commit".equals(nombre) || "rollback".equals(nombre)
                                || METODOS_ESTADO_CONEXION.contains(nombre)) {
                            Object resultado = invocarConexion(primera, metodo, argumentos);
                            invocarConexion(segunda, metodo, argumentos);
                            return resultado;
                        }
                        if (nombre.startsWith("prepareStatement") || nombre.startsWith("prepareCall")
                                || "createStatement".equals(nombre)) {
                            if (actual.compareAndSet(primera, segunda)) {
                                Object recursoFinal = segunda.unwrap(org.postgresql.PGConnection.class);
                                recursosFisicosDistintos.set(recursoInicial != recursoFinal);
                                conmutacionFisicaRealizada.set(true);
                            }
                        }
                        return invocarConexion(actual.get(), metodo, argumentos);
                    });
        }

        private void cerrarAmbas(Connection primera, Connection segunda) throws SQLException {
            SQLException fallo = null;
            try {
                primera.close();
            } catch (SQLException excepcion) {
                fallo = excepcion;
            }
            try {
                segunda.close();
            } catch (SQLException excepcion) {
                if (fallo == null) fallo = excepcion;
                else fallo.addSuppressed(excepcion);
            }
            if (fallo != null) throw fallo;
        }

        private Object invocarConexion(
                Connection conexion,
                java.lang.reflect.Method metodo,
                Object[] argumentos) throws Throwable {
            try {
                return metodo.invoke(conexion, argumentos);
            } catch (InvocationTargetException excepcion) {
                throw excepcion.getCause();
            }
        }

        private boolean esMutacionUrl(String mutacion) {
            return mutacion != null && mutacion.startsWith("JDBC_URL");
        }

        private String urlMetadataHostil(String mutacion, String original) {
            return switch (mutacion) {
                case "JDBC_URL", "JDBC_URL_QUERY" -> original + "?ApplicationName=hostile";
                case "JDBC_URL_QUERY_DUPLICADA" -> original + "?ApplicationName=a&ApplicationName=b";
                case "JDBC_URL_FRAGMENTO" -> original + "#hostile";
                case "JDBC_URL_USER_INFO" -> insertarTrasAutoridad(original, "reader@");
                case "JDBC_URL_PORCENTAJE" -> original + "%2Fhostile";
                case "JDBC_URL_DATABASE" -> original + "_hostile";
                case "JDBC_URL_HOST" -> insertarTrasAutoridad(original, "hostile.");
                case "JDBC_URL_PUERTO" -> cambiarPuerto(original);
                default -> throw new IllegalStateException("Unknown hostile JDBC URL mutation");
            };
        }

        private String insertarTrasAutoridad(String original, String insercion) {
            int indice = original.indexOf("//");
            return indice < 0 ? original + insercion
                    : original.substring(0, indice + 2) + insercion + original.substring(indice + 2);
        }

        private String cambiarPuerto(String original) {
            Matcher partes = URL_CANONICA.matcher(original);
            if (!partes.matches()) return original + ":1";
            int puerto = Integer.parseInt(partes.group(2));
            int alterno = puerto == 65_535 ? 65_534 : puerto + 1;
            return "jdbc:postgresql://" + partes.group(1) + ':' + alterno + '/' + partes.group(3);
        }
    }
}
