package com.feelingpilates.transicion.programacion.adapter.jpa.testinfra;

import com.feelingpilates.transicion.programacion.adapter.jpa.policy.F2eSqlPolicyViolationException;
import com.feelingpilates.transicion.programacion.read.ReadSnapshotContext;
import org.hibernate.resource.jdbc.spi.StatementInspector;

import java.nio.charset.StandardCharsets;
import java.nio.charset.CodingErrorAction;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

public final class F2eStatementPolicyInspector implements StatementInspector {

    static final String ID_AISLAMIENTO = "4a669a2f628e12468e0d532889e0bcafd38c09a5aadfb27f2f20f56159c1671e";
    static final String ID_SOLO_LECTURA = "9963ea856cdf9bfb3e9c440b1c3a6c062fd375a906f465cbe7a7ac88878716c7";
    static final String ID_POR_IDENTIDADES = ReadSnapshotContext.ProjectionCatalogVersion.R1_RESERVA_V1
            .statementIdByIds();
    static final String ID_POR_SCOPE = ReadSnapshotContext.ProjectionCatalogVersion.R1_RESERVA_V1
            .statementIdByScope();

    private static final Map<String, String> CATALOGO = Map.of(
            ID_AISLAMIENTO, "R1_TX_ISOLATION_V1",
            ID_SOLO_LECTURA, "R1_TX_READ_ONLY_V1",
            ID_POR_IDENTIDADES, ReadSnapshotContext.ProjectionCatalogVersion.R1_RESERVA_V1.statementByIds(),
            ID_POR_SCOPE, ReadSnapshotContext.ProjectionCatalogVersion.R1_RESERVA_V1.statementByScope());
    private static final Set<String> PALABRAS_DENEGADAS = Set.of(
            " insert ", " update ", " delete ", " merge ", " alter ", " create ",
            " drop ", " truncate ", " grant ", " revoke ", " for update", " for share",
            " nextval", " setval", " currval", "pg_current_snapshot(");

    private final ThreadLocal<Captura> capturaActual = new ThreadLocal<>();
    private final AtomicLong statementsAceptados = new AtomicLong();

    public Captura abrirCaptura(String identidadInvocacion) {
        if (identidadInvocacion == null || identidadInvocacion.isBlank() || capturaActual.get() != null) {
            throw new IllegalStateException("F2E statement capture cannot be nested or anonymous");
        }
        Captura captura = new Captura(identidadInvocacion, Thread.currentThread());
        capturaActual.set(captura);
        return captura;
    }

    public List<String> cerrarCaptura(Captura captura) {
        Captura actual = capturaActual.get();
        if (actual != captura || captura.hilo != Thread.currentThread() || captura.cerrada) {
            throw new IllegalStateException("F2E statement capture ownership not proven");
        }
        captura.cerrada = true;
        capturaActual.remove();
        return List.copyOf(captura.identificadores);
    }

    public void descartarCaptura(Captura captura) {
        if (capturaActual.get() == captura) {
            captura.cerrada = true;
            capturaActual.remove();
        }
    }

    @Override
    public String inspect(String sql) {
        Captura captura = capturaActual.get();
        if (captura == null || captura.hilo != Thread.currentThread() || captura.cerrada) {
            throw new IllegalStateException("F2E SQL observed outside invocation capture");
        }
        String canonica;
        try {
            canonica = normalizar(sql);
        } catch (IllegalArgumentException excepcion) {
            throw new F2eSqlPolicyViolationException(
                    F2eSqlPolicyViolationException.Reason.NORMALIZATION_REJECTED, null);
        }
        String identificador = identificar(canonica);
        String rodeada = " " + canonica.toLowerCase(java.util.Locale.ROOT) + " ";
        if (!canonica.regionMatches(true, 0, "SELECT ", 0, "SELECT ".length())) {
            throw new F2eSqlPolicyViolationException(
                    F2eSqlPolicyViolationException.Reason.STATEMENT_CLASS_DENIED, identificador);
        }
        if (PALABRAS_DENEGADAS.stream().anyMatch(rodeada::contains)) {
            throw new F2eSqlPolicyViolationException(
                    F2eSqlPolicyViolationException.Reason.DENYLIST_VIOLATION, identificador);
        }
        if (!CATALOGO.containsKey(identificador)) {
            throw new F2eSqlPolicyViolationException(
                    F2eSqlPolicyViolationException.Reason.CATALOG_MISS, identificador);
        }
        captura.identificadores.add(identificador);
        statementsAceptados.incrementAndGet();
        return sql;
    }

    private long statementsAceptadosPrueba() {
        return statementsAceptados.get();
    }

    public static String normalizar(String sql) {
        if (sql == null || sql.isEmpty() || sql.indexOf('\0') >= 0) {
            throw new IllegalArgumentException("SQL normalization rejected");
        }
        validarUtf8(sql);
        StringBuilder salida = new StringBuilder();
        boolean espacioPendiente = false;
        int indice = 0;
        while (indice < sql.length()) {
            char caracter = sql.charAt(indice);
            if (caracter == ';' || (caracter == '-' && siguiente(sql, indice, '-'))
                    || (caracter == '/' && siguiente(sql, indice, '*'))) {
                throw new IllegalArgumentException("SQL normalization rejected");
            }
            if (caracter == '\'' || caracter == '"') {
                if (espacioPendiente && !salida.isEmpty()) salida.append(' ');
                espacioPendiente = false;
                indice = copiarCadena(sql, indice, caracter, salida);
                continue;
            }
            if (caracter == '$' && indice + 1 < sql.length()
                    && (sql.charAt(indice + 1) == '$' || esInicioEtiqueta(sql.charAt(indice + 1)))) {
                int finEtiqueta = sql.indexOf('$', indice + 1);
                if (finEtiqueta > indice) {
                    String delimitador = sql.substring(indice, finEtiqueta + 1);
                    if (delimitador.equals("$$") || delimitador.substring(1, delimitador.length() - 1)
                            .matches("[A-Za-z_][A-Za-z0-9_]*")) {
                        if (espacioPendiente && !salida.isEmpty()) salida.append(' ');
                        espacioPendiente = false;
                        int cierre = sql.indexOf(delimitador, finEtiqueta + 1);
                        if (cierre < 0) throw new IllegalArgumentException("unclosed dollar quote");
                        salida.append(sql, indice, cierre + delimitador.length());
                        indice = cierre + delimitador.length();
                        continue;
                    }
                }
            }
            if (esEspacioAscii(caracter)) {
                espacioPendiente = !salida.isEmpty();
                indice++;
                continue;
            }
            if (espacioPendiente && !salida.isEmpty()) salida.append(' ');
            espacioPendiente = false;
            if (caracter == '?' || (caracter == '$' && indice + 1 < sql.length()
                    && esDigitoAscii(sql.charAt(indice + 1)))) {
                salida.append('?');
                indice++;
                while (indice < sql.length() && esDigitoAscii(sql.charAt(indice))) indice++;
                continue;
            }
            salida.append(caracter);
            indice++;
        }
        String normalizada = salida.toString();
        if (normalizada.isEmpty()) throw new IllegalArgumentException("empty normalized SQL");
        return colapsarListaMarcadores(normalizada);
    }

    public static String identificar(String canonica) {
        byte[] sql = Objects.requireNonNull(canonica, "canonica").getBytes(StandardCharsets.UTF_8);
        byte[] prefijo = ("F2E_SQL_CATALOG_ID_V1\n" + sql.length + ":").getBytes(StandardCharsets.UTF_8);
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(prefijo);
            return java.util.HexFormat.of().formatHex(digest.digest(sql));
        } catch (NoSuchAlgorithmException excepcion) {
            throw new IllegalStateException(excepcion);
        }
    }

    private static int copiarCadena(String sql, int inicio, char delimitador, StringBuilder salida) {
        salida.append(delimitador);
        int indice = inicio + 1;
        while (indice < sql.length()) {
            char actual = sql.charAt(indice);
            salida.append(actual);
            indice++;
            if (actual == delimitador) {
                if (indice < sql.length() && sql.charAt(indice) == delimitador) {
                    salida.append(delimitador);
                    indice++;
                } else {
                    return indice;
                }
            }
        }
        throw new IllegalArgumentException("unclosed SQL quote");
    }

    private static String colapsarListaMarcadores(String sql) {
        StringBuilder salida = new StringBuilder();
        int indice = 0;
        while (indice < sql.length()) {
            if (sql.charAt(indice) == '(') {
                int cierre = sql.indexOf(')', indice + 1);
                if (cierre > indice && sql.substring(indice + 1, cierre).matches("\\s*\\?\\s*(,\\s*\\?\\s*)*")) {
                    salida.append("(?*)");
                    indice = cierre + 1;
                    continue;
                }
            }
            salida.append(sql.charAt(indice++));
        }
        return salida.toString();
    }

    private static boolean siguiente(String valor, int indice, char esperado) {
        return indice + 1 < valor.length() && valor.charAt(indice + 1) == esperado;
    }

    private static boolean esInicioEtiqueta(char valor) {
        return (valor >= 'A' && valor <= 'Z') || (valor >= 'a' && valor <= 'z') || valor == '_';
    }

    private static boolean esDigitoAscii(char valor) {
        return valor >= '0' && valor <= '9';
    }

    private static void validarUtf8(String valor) {
        try {
            StandardCharsets.UTF_8.newEncoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .encode(java.nio.CharBuffer.wrap(valor));
        } catch (java.nio.charset.CharacterCodingException excepcion) {
            throw new IllegalArgumentException("SQL normalization rejected", excepcion);
        }
    }

    private static boolean esEspacioAscii(char valor) {
        return valor == ' ' || (valor >= '\t' && valor <= '\r');
    }

    public static final class Captura {
        private final String identidadInvocacion;
        private final Thread hilo;
        private final List<String> identificadores = new ArrayList<>();
        private boolean cerrada;

        private Captura(String identidadInvocacion, Thread hilo) {
            this.identidadInvocacion = identidadInvocacion;
            this.hilo = hilo;
        }

        public String identidadInvocacion() { return identidadInvocacion; }
    }
}
