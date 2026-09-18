-- Encoder y guards exclusivamente de la foundation PN14 Slice2.
CREATE FUNCTION public.pn14_s2_hash(c text) RETURNS text LANGUAGE sql IMMUTABLE STRICT
AS $$ SELECT encode(sha256(convert_to(c,'UTF8')),'hex') $$;

CREATE FUNCTION public.pn14_s2_t(tipo text, valor text) RETURNS text LANGUAGE sql IMMUTABLE
AS $$ SELECT CASE WHEN valor IS NULL THEN 'NULL:~NULL' ELSE tipo||':'||valor END $$;

-- Los valores JSONB ya incluyen tipo; jsonb no serializa el contenido contractual.
CREATE FUNCTION public.pn14_s2_obj(m jsonb) RETURNS text LANGUAGE sql IMMUTABLE STRICT AS $$
 SELECT coalesce(string_agg(octet_length(convert_to(key,'UTF8'))::text||':'||key||
     octet_length(convert_to(value,'UTF8'))::text||':'||value||E'\n',''
     ORDER BY convert_to(key,'UTF8')),'') FROM jsonb_each_text(m)
$$;

CREATE FUNCTION public.pn14_s2_instant(s timestamptz) RETURNS text LANGUAGE sql IMMUTABLE STRICT AS $$
 SELECT to_char(s AT TIME ZONE 'UTC','YYYY-MM-DD"T"HH24:MI:SS.US"Z"')
$$;
CREATE FUNCTION public.pn14_s2_iso_exponente(moneda text) RETURNS integer LANGUAGE sql IMMUTABLE STRICT AS $$
 SELECT exponente FROM (VALUES ('ADP',0),('AED',2),('AFA',2),('AFN',2),('ALL',2),('AMD',2),('ANG',2),('AOA',2),('ARS',2),('ATS',2),('AUD',2),('AWG',2),('AYM',2),('AZM',2),('AZN',2),('BAM',2),('BBD',2),('BDT',2),('BEF',0),('BGL',2),('BGN',2),('BHD',3),('BIF',0),('BMD',2),('BND',2),('BOB',2),('BOV',2),('BRL',2),('BSD',2),('BTN',2),('BWP',2),('BYB',0),('BYN',2),('BYR',0),('BZD',2),('CAD',2),('CDF',2),('CHE',2),('CHF',2),('CHW',2),('CLF',4),('CLP',0),('CNY',2),('COP',2),('COU',2),('CRC',2),('CSD',2),('CUC',2),('CUP',2),('CVE',2),('CYP',2),('CZK',2),('DEM',2),('DJF',0),('DKK',2),('DOP',2),('DZD',2),('EEK',2),('EGP',2),('ERN',2),('ESP',0),('ETB',2),('EUR',2),('FIM',2),('FJD',2),('FKP',2),('FRF',2),('GBP',2),('GEL',2),('GHC',2),('GHS',2),('GIP',2),('GMD',2),('GNF',0),('GRD',0),('GTQ',2),('GWP',2),('GYD',2),('HKD',2),('HNL',2),('HRK',2),('HTG',2),('HUF',2),('IDR',2),('IEP',2),('ILS',2),('INR',2),('IQD',3),('IRR',2),('ISK',0),('ITL',0),('JMD',2),('JOD',3),('JPY',0),('KES',2),('KGS',2),('KHR',2),('KMF',0),('KPW',2),('KRW',0),('KWD',3),('KYD',2),('KZT',2),('LAK',2),('LBP',2),('LKR',2),('LRD',2),('LSL',2),('LTL',2),('LUF',0),('LVL',2),('LYD',3),('MAD',2),('MDL',2),('MGA',2),('MGF',0),('MKD',2),('MMK',2),('MNT',2),('MOP',2),('MRO',2),('MRU',2),('MTL',2),('MUR',2),('MVR',2),('MWK',2),('MXN',2),('MXV',2),('MYR',2),('MZM',2),('MZN',2),('NAD',2),('NGN',2),('NIO',2),('NLG',2),('NOK',2),('NPR',2),('NZD',2),('OMR',3),('PAB',2),('PEN',2),('PGK',2),('PHP',2),('PKR',2),('PLN',2),('PTE',0),('PYG',0),('QAR',2),('ROL',0),('RON',2),('RSD',2),('RUB',2),('RUR',2),('RWF',0),('SAR',2),('SBD',2),('SCR',2),('SDD',2),('SDG',2),('SEK',2),('SGD',2),('SHP',2),('SIT',2),('SKK',2),('SLE',2),('SLL',2),('SOS',2),('SRD',2),('SRG',2),('SSP',2),('STD',2),('STN',2),('SVC',2),('SYP',2),('SZL',2),('THB',2),('TJS',2),('TMM',2),('TMT',2),('TND',3),('TOP',2),('TPE',0),('TRL',0),('TRY',2),('TTD',2),('TWD',2),('TZS',2),('UAH',2),('UGX',0),('USD',2),('USN',2),('USS',2),('UYI',0),('UYU',2),('UZS',2),('VEB',2),('VED',2),('VEF',2),('VES',2),('VND',0),('VUV',0),('WST',2),('XAD',2),('XAF',0),('XCD',2),('XCG',2),('XOF',0),('XPF',0),('YER',2),('YUM',2),('ZAR',2),('ZMK',2),('ZMW',2),('ZWD',2),('ZWG',2),('ZWL',2),('ZWN',2),('ZWR',2))
 AS iso(codigo,exponente) WHERE codigo=moneda
$$;
CREATE FUNCTION public.pn14_s2_uuid(nombre text) RETURNS uuid LANGUAGE plpgsql IMMUTABLE STRICT AS $$
DECLARE h text := md5(convert_to(nombre,'UTF8'));
BEGIN
 h := overlay(h placing '3' from 13 for 1);
 h := overlay(h placing to_hex((get_byte(decode(h,'hex'),8) & 63) | 128) from 17 for 2);
 RETURN h::uuid;
END $$;

-- Parsear longitudes en bytes evita reinterpretación Unicode, LF, tipos o NULL.
CREATE FUNCTION public.pn14_s2_parse(canon text) RETURNS jsonb LANGUAGE plpgsql IMMUTABLE STRICT AS $$
DECLARE b bytea:=convert_to(canon,'UTF8'); p integer:=0; inicio integer; n integer;
 k text; v text; anterior bytea; m jsonb:='{}'; j integer; tipo text; contenido text; nested jsonb;
BEGIN
 WHILE p < octet_length(b) LOOP
  FOR j IN 1..2 LOOP
   inicio:=p;
   WHILE p<octet_length(b) AND get_byte(b,p) BETWEEN 48 AND 57 LOOP p:=p+1; END LOOP;
   IF p=inicio OR p>=octet_length(b) OR get_byte(b,p)<>58 OR (p-inicio>1 AND get_byte(b,inicio)=48)
   THEN RAISE EXCEPTION 'pn14_s2 canon longitud'; END IF;
   n:=convert_from(substring(b FROM inicio+1 FOR p-inicio),'UTF8')::integer; p:=p+1;
   IF n>octet_length(b)-p THEN RAISE EXCEPTION 'pn14_s2 canon longitud'; END IF;
   IF j=1 THEN k:=convert_from(substring(b FROM p+1 FOR n),'UTF8');
   ELSE v:=convert_from(substring(b FROM p+1 FOR n),'UTF8'); END IF;
   p:=p+n;
  END LOOP;
  IF p>=octet_length(b) OR get_byte(b,p)<>10 OR anterior IS NOT NULL AND anterior>=convert_to(k,'UTF8')
  THEN RAISE EXCEPTION 'pn14_s2 canon orden/LF'; END IF;
  p:=p+1; anterior:=convert_to(k,'UTF8');
  IF position(':' IN v)=0 THEN RAISE EXCEPTION 'pn14_s2 tipo'; END IF;
  tipo:=split_part(v,':',1); contenido:=substring(v FROM length(tipo)+2);
  CASE tipo
   WHEN 'NULL' THEN IF contenido<>'~NULL' THEN RAISE EXCEPTION 'pn14_s2 NULL'; END IF;
   WHEN 'STRING' THEN NULL;
   WHEN 'ENUM' THEN IF contenido !~ '^[A-Z][A-Z0-9_]*$' THEN RAISE EXCEPTION 'pn14_s2 ENUM'; END IF;
   WHEN 'UUID' THEN IF contenido<>(contenido::uuid)::text THEN RAISE EXCEPTION 'pn14_s2 UUID'; END IF;
   WHEN 'INT' THEN IF contenido<>(contenido::integer)::text THEN RAISE EXCEPTION 'pn14_s2 INT'; END IF;
   WHEN 'LONG' THEN IF contenido<>(contenido::bigint)::text THEN RAISE EXCEPTION 'pn14_s2 LONG'; END IF;
   WHEN 'BOOL' THEN IF contenido NOT IN ('true','false') THEN RAISE EXCEPTION 'pn14_s2 BOOL'; END IF;
   WHEN 'INSTANT' THEN
    IF contenido !~ '^[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}\.[0-9]{6}Z$'
       OR contenido<>public.pn14_s2_instant(contenido::timestamptz)
    THEN RAISE EXCEPTION 'pn14_s2 INSTANT'; END IF;
   WHEN 'OBJECT','LIST' THEN
    nested:=public.pn14_s2_parse(contenido);
    IF tipo='LIST' AND EXISTS (SELECT 1 FROM generate_series(1,(SELECT count(*)::integer FROM jsonb_object_keys(nested))) g
       WHERE NOT nested ? g::text) THEN RAISE EXCEPTION 'pn14_s2 LIST'; END IF;
   ELSE RAISE EXCEPTION 'pn14_s2 tipo desconocido';
  END CASE;
  m:=m||jsonb_build_object(k,v);
 END LOOP;
 IF public.pn14_s2_obj(m)<>canon THEN RAISE EXCEPTION 'pn14_s2 canon representación'; END IF;
 RETURN m;
END $$;

CREATE FUNCTION public.pn14_s2_bundle(c public.compra) RETURNS jsonb LANGUAGE sql IMMUTABLE STRICT AS $$
 SELECT jsonb_build_object('orden_venta_id',c.orden_venta_id,'cliente_snapshot_id',c.cliente_snapshot_id,'numero_linea',c.numero_linea,'producto_fuente_id',c.producto_fuente_id,'nombre_producto_snapshot',c.nombre_producto_snapshot,'tipo_producto_snapshot',c.tipo_producto_snapshot,'precio_venta_unidades_minimas',c.precio_venta_unidades_minimas,'moneda_snapshot_iso',c.moneda_snapshot_iso,'congelado_en',c.congelado_en,'contrato_canonico',c.contrato_canonico,'contrato_hash',c.contrato_hash,'politica_canonica',c.politica_canonica,'politica_hash',c.politica_hash,'procedencia_canonica',c.procedencia_canonica,'politica_esquema',c.politica_esquema,'politica_id',c.politica_id,'politica_version',c.politica_version,'zona_negocio',c.zona_negocio,'vigencia_unidad',c.vigencia_unidad,'vigencia_cantidad',c.vigencia_cantidad,'extension_alcance',c.extension_alcance,'reserva_limite_post_vencimiento_dias',c.reserva_limite_post_vencimiento_dias,'cancelacion_anticipacion_segundos',c.cancelacion_anticipacion_segundos,'cancelacion_cuota_mensual',c.cancelacion_cuota_mensual,'recuperacion_unidad',c.recuperacion_unidad,'recuperacion_cantidad',c.recuperacion_cantidad,'recuperacion_politica_id',c.recuperacion_politica_id,'recuperacion_politica_version',c.recuperacion_politica_version,'reembolso_alcance',c.reembolso_alcance,'reembolso_ventana_adicional',c.reembolso_ventana_adicional,'reembolso_politica_id',c.reembolso_politica_id,'reembolso_politica_version',c.reembolso_politica_version,'unidad_monetaria_exponente',c.unidad_monetaria_exponente)
$$;
ALTER TABLE public.compra ADD CONSTRAINT pn14_s2_bundle_completo CHECK (
 (orden_venta_id IS NULL AND num_nonnulls(orden_venta_id,cliente_snapshot_id,numero_linea,producto_fuente_id,nombre_producto_snapshot,tipo_producto_snapshot,precio_venta_unidades_minimas,moneda_snapshot_iso,congelado_en,contrato_canonico,contrato_hash,politica_canonica,politica_hash,procedencia_canonica,politica_esquema,politica_id,politica_version,zona_negocio,vigencia_unidad,vigencia_cantidad,extension_alcance,reserva_limite_post_vencimiento_dias,cancelacion_anticipacion_segundos,cancelacion_cuota_mensual,recuperacion_unidad,recuperacion_cantidad,recuperacion_politica_id,recuperacion_politica_version,reembolso_alcance,reembolso_ventana_adicional,reembolso_politica_id,reembolso_politica_version,unidad_monetaria_exponente)=0) OR
 (orden_venta_id IS NOT NULL AND num_nonnulls(orden_venta_id,cliente_snapshot_id,numero_linea,nombre_producto_snapshot,tipo_producto_snapshot,precio_venta_unidades_minimas,moneda_snapshot_iso,congelado_en,contrato_canonico,contrato_hash,politica_canonica,politica_hash,procedencia_canonica,politica_esquema,politica_id,politica_version,zona_negocio,vigencia_unidad,vigencia_cantidad,extension_alcance,reserva_limite_post_vencimiento_dias,cancelacion_anticipacion_segundos,cancelacion_cuota_mensual,recuperacion_unidad,recuperacion_cantidad,recuperacion_politica_id,recuperacion_politica_version,reembolso_alcance,reembolso_ventana_adicional,reembolso_politica_id,reembolso_politica_version,unidad_monetaria_exponente)=32
  AND cliente_snapshot_id=usuario_id AND numero_linea>0 AND precio_venta_unidades_minimas>=0
  AND nombre_producto_snapshot !~ '^\s*$' AND tipo_producto_snapshot IN ('CLASE_INDIVIDUAL','PAQUETE')
  AND moneda_snapshot_iso ~ '^[A-Z]{3}$' AND public.pn14_s2_iso_exponente(moneda_snapshot_iso) IS NOT NULL
  AND unidad_monetaria_exponente=public.pn14_s2_iso_exponente(moneda_snapshot_iso)
  AND politica_esquema='PN14_S2_1' AND zona_negocio='America/Mexico_City'
  AND politica_id !~ '^\s*$' AND politica_version !~ '^\s*$'
  AND vigencia_unidad IN ('DIAS','MESES') AND vigencia_cantidad>0
  AND extension_alcance IN ('MISMA_ACTIVIDAD','TODAS_LAS_ACTIVIDADES_ACTIVAS')
  AND reserva_limite_post_vencimiento_dias>=0 AND cancelacion_anticipacion_segundos>=0 AND cancelacion_cuota_mensual>=0
  AND recuperacion_unidad IN ('DIAS','MESES') AND recuperacion_cantidad>0
  AND recuperacion_politica_id !~ '^\s*$' AND recuperacion_politica_version !~ '^\s*$'
  AND reembolso_alcance='PRODUCTO_ENTERO' AND NOT reembolso_ventana_adicional
  AND reembolso_politica_id !~ '^\s*$' AND reembolso_politica_version !~ '^\s*$'
  AND unidad_monetaria_exponente BETWEEN 0 AND 9
  AND contrato_hash ~ '^[0-9a-f]{64}$' AND politica_hash ~ '^[0-9a-f]{64}$'
  AND public.pn14_s2_hash(contrato_canonico)=contrato_hash
  AND public.pn14_s2_hash(politica_canonica)=politica_hash)
);
ALTER TABLE public.orden_venta ADD CONSTRAINT pn14_s2_orden_hash CHECK (
 public.pn14_s2_hash(payload_canonico)=payload_hash
 AND public.pn14_s2_iso_exponente(moneda_iso) IS NOT NULL
 AND scope_key ~ '^LEGACY_(GRUPO|COMPRA):[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$'
 AND id=public.pn14_s2_uuid('PN14_S2_1:ORDEN:'||scope_key)
 AND congelado_en BETWEEN '0001-01-01T00:00:00Z'::timestamptz AND '9999-12-31T23:59:59.999999Z'::timestamptz
);
ALTER TABLE public.informe_backfill_snapshot ADD CONSTRAINT pn14_s2_informe_hash CHECK (
 public.pn14_s2_hash(fuente_raw)=fuente_raw_hash
 AND public.pn14_s2_hash(fuentes_canonicas||fuente_raw)=fuente_payload_hash
 AND scope_key ~ '^LEGACY_(GRUPO|COMPRA):[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$'
 AND id=public.pn14_s2_uuid('PN14_S2_1:INFORME:'||scope_key||':'||fuente_payload_hash)
);

CREATE FUNCTION public.pn14_s2_terminos() RETURNS text LANGUAGE sql IMMUTABLE AS $$
 SELECT public.pn14_s2_obj(jsonb_build_object(
 'saldoCeroExtiende','BOOL:false','acortaVigencia','BOOL:false','reviveExpirado','BOOL:false',
 'ancla','ENUM:ACREDITACION_COMPLETADA','limitesExclusivos','BOOL:true','igualdadCutoffValida','BOOL:true',
 'cancelacionLiberaCapacidad','BOOL:true','tardiaConsume','BOOL:true','cuotaAgotadaConsume','BOOL:true',
 'noAsistidaConsume','BOOL:true','estudioRestauraSinCuota','BOOL:true','asistenciaPendienteTimer','BOOL:false',
 'asistenciaResolucion','ENUM:ADMIN','reintegroEsDinero','BOOL:false','recuperacionMismaActividad','BOOL:true',
 'recuperacionDerechoNuevo','BOOL:true','refundExigeIntegridad','BOOL:true','refundExigeKBCERcero','BOOL:true',
 'refundPermiteContencion','BOOL:false','refundPermiteExpiracion','BOOL:false',
 'claveCuota','STRING:clienteId+YearMonth(inicioSesion,zonaNegocio)+policyVersion'))
$$;
CREATE FUNCTION public.pn14_s2_politica(c public.compra) RETURNS text LANGUAGE sql STABLE STRICT AS $$
 SELECT public.pn14_s2_obj(jsonb_build_object(
 'esquema',public.pn14_s2_t('STRING',c.politica_esquema),
 'id',public.pn14_s2_t('STRING',c.politica_id),'version',public.pn14_s2_t('STRING',c.politica_version),
 'zonaNegocio',public.pn14_s2_t('STRING',c.zona_negocio),
 'unidadMonetariaExponente',public.pn14_s2_t('INT',c.unidad_monetaria_exponente::text),
 'vigencia','OBJECT:'||public.pn14_s2_obj(jsonb_build_object('unidad',public.pn14_s2_t('ENUM',c.vigencia_unidad),
 'cantidad',public.pn14_s2_t('INT',c.vigencia_cantidad::text),'extensionAlcance',public.pn14_s2_t('ENUM',c.extension_alcance))),
 'reservaCancelacion','OBJECT:'||public.pn14_s2_obj(jsonb_build_object(
 'limitePostVencimientoDias',public.pn14_s2_t('INT',c.reserva_limite_post_vencimiento_dias::text),
 'anticipacionSegundos',public.pn14_s2_t('LONG',c.cancelacion_anticipacion_segundos::text),
 'cuotaMensual',public.pn14_s2_t('INT',c.cancelacion_cuota_mensual::text))),
 'recuperacion','OBJECT:'||public.pn14_s2_obj(jsonb_build_object('unidad',public.pn14_s2_t('ENUM',c.recuperacion_unidad),
 'cantidad',public.pn14_s2_t('INT',c.recuperacion_cantidad::text),'politicaId',public.pn14_s2_t('STRING',c.recuperacion_politica_id),
 'politicaVersion',public.pn14_s2_t('STRING',c.recuperacion_politica_version))),
 'reembolso','OBJECT:'||public.pn14_s2_obj(jsonb_build_object('alcance',public.pn14_s2_t('ENUM',c.reembolso_alcance),
 'ventanaAdicional',public.pn14_s2_t('BOOL',c.reembolso_ventana_adicional::text),
 'politicaId',public.pn14_s2_t('STRING',c.reembolso_politica_id),'politicaVersion',public.pn14_s2_t('STRING',c.reembolso_politica_version))),
 'terminos','OBJECT:'||public.pn14_s2_terminos()))
$$;
CREATE FUNCTION public.pn14_s2_componente(c public.compra_componente_snapshot) RETURNS text LANGUAGE sql STABLE STRICT AS $$
 SELECT public.pn14_s2_obj(jsonb_build_object('id',public.pn14_s2_t('UUID',c.id::text),
 'compraId',public.pn14_s2_t('UUID',c.compra_id::text),'numero',public.pn14_s2_t('INT',c.numero::text),
 'actividadId',public.pn14_s2_t('UUID',c.actividad_id::text),'nombreActividad',public.pn14_s2_t('STRING',c.nombre_actividad),
 'cantidad',public.pn14_s2_t('INT',c.cantidad::text),'politicaVersion',public.pn14_s2_t('STRING',c.politica_version)))
$$;
CREATE FUNCTION public.pn14_s2_componentes(compra uuid) RETURNS text LANGUAGE sql STABLE STRICT AS $$
 SELECT public.pn14_s2_obj(coalesce(jsonb_object_agg(numero::text,'OBJECT:'||public.pn14_s2_componente(c)),'{}'))
 FROM public.compra_componente_snapshot c WHERE compra_id=compra
$$;
CREATE FUNCTION public.pn14_s2_contrato(c public.compra) RETURNS text LANGUAGE sql STABLE STRICT AS $$
 SELECT public.pn14_s2_obj(jsonb_build_object(
 'id',public.pn14_s2_t('UUID',c.id::text),'ordenId',public.pn14_s2_t('UUID',c.orden_venta_id::text),
 'clienteId',public.pn14_s2_t('UUID',c.cliente_snapshot_id::text),'numeroLinea',public.pn14_s2_t('INT',c.numero_linea::text),
 'productoFuenteId',public.pn14_s2_t('UUID',c.producto_fuente_id::text),'nombreProducto',public.pn14_s2_t('STRING',c.nombre_producto_snapshot),
 'tipoProducto',public.pn14_s2_t('ENUM',c.tipo_producto_snapshot),
 'precioVenta','OBJECT:'||public.pn14_s2_obj(jsonb_build_object('unidadesMinimas',public.pn14_s2_t('LONG',c.precio_venta_unidades_minimas::text),
 'monedaIso',public.pn14_s2_t('STRING',c.moneda_snapshot_iso))),
 'politica','OBJECT:'||public.pn14_s2_politica(c),'componentes','LIST:'||public.pn14_s2_componentes(c.id),
 'proveniencia','OBJECT:'||c.procedencia_canonica,
 'congeladoEn',public.pn14_s2_t('INSTANT',public.pn14_s2_instant(c.congelado_en))))
$$;
CREATE FUNCTION public.pn14_s2_orden(o public.orden_venta) RETURNS text LANGUAGE sql STABLE STRICT AS $$
 SELECT public.pn14_s2_obj(jsonb_build_object(
 'id',public.pn14_s2_t('UUID',o.id::text),'clienteId',public.pn14_s2_t('UUID',o.cliente_id::text),
 'scopeKey',public.pn14_s2_t('STRING',o.scope_key),
 'total','OBJECT:'||public.pn14_s2_obj(jsonb_build_object('unidadesMinimas',public.pn14_s2_t('LONG',o.total_unidades_minimas::text),
 'monedaIso',public.pn14_s2_t('STRING',o.moneda_iso))),
 'congeladoEn',public.pn14_s2_t('INSTANT',public.pn14_s2_instant(o.congelado_en)),
 'compras','LIST:'||(SELECT public.pn14_s2_obj(coalesce(jsonb_object_agg(numero_linea::text,
 'OBJECT:'||public.pn14_s2_obj(public.pn14_s2_parse(c.contrato_canonico)||jsonb_build_object('contratoHash','STRING:'||c.contrato_hash))),'{}'))
 FROM public.compra c WHERE orden_venta_id=o.id)))
$$;

CREATE FUNCTION public.pn14_s2_paths(canon text, prefijo text) RETURNS text[] LANGUAGE plpgsql IMMUTABLE STRICT AS $$
DECLARE m jsonb:=public.pn14_s2_parse(canon); e record; tipo text; valor text; p text; resultado text[]:=ARRAY[]::text[];
BEGIN
 FOR e IN SELECT * FROM jsonb_each_text(m) LOOP
  tipo:=split_part(e.value,':',1); valor:=substring(e.value FROM length(tipo)+2);
  p:=CASE WHEN prefijo='' THEN e.key ELSE prefijo||'.'||e.key END;
  IF tipo='OBJECT' THEN resultado:=resultado||public.pn14_s2_paths(valor,p);
  ELSIF tipo='LIST' THEN
   FOR e IN SELECT * FROM jsonb_each_text(public.pn14_s2_parse(valor)) LOOP
    tipo:=split_part(e.value,':',1);
    IF tipo='OBJECT' THEN resultado:=resultado||public.pn14_s2_paths(substring(e.value FROM 8),p||'['||e.key||']');
    ELSE resultado:=array_append(resultado,p||'['||e.key||']'); END IF;
   END LOOP;
  ELSE resultado:=array_append(resultado,p); END IF;
 END LOOP;
 RETURN resultado;
END $$;
CREATE FUNCTION public.pn14_s2_proveniencia(c public.compra) RETURNS void LANGUAGE plpgsql STABLE STRICT AS $$
DECLARE m jsonb:=public.pn14_s2_parse(c.procedencia_canonica); campos jsonb; x record; campo jsonb;
 paths text[]:=ARRAY[]::text[]; requeridos text[]; raw text; h text;
BEGIN
 IF NOT m ?& ARRAY['origen','reglaVersion','actorId','evidenciaEn','referencia','fuenteRaw','fuenteRawHash','campos']
    OR (SELECT count(*) FROM jsonb_object_keys(m))<>8
    OR m->>'origen' NOT IN ('ENUM:CONTRATO_CONTEMPORANEO_VERIFICADO','ENUM:ARCHIVO_VERSIONADO_VERIFICADO','ENUM:EVIDENCIA_COMERCIAL_VERIFICADA')
    OR m->>'reglaVersion'<>'STRING:PN14_S2_1' OR m->>'actorId' NOT LIKE 'UUID:%'
    OR m->>'evidenciaEn' NOT LIKE 'INSTANT:%' OR m->>'referencia' NOT LIKE 'STRING:%'
    OR substring(m->>'referencia' FROM 8) ~ '^\s*$'
    OR m->>'fuenteRaw' NOT LIKE 'STRING:%' OR m->>'fuenteRawHash' NOT LIKE 'STRING:%'
    OR m->>'campos' NOT LIKE 'LIST:%'
 THEN RAISE EXCEPTION 'pn14_s2 proveniencia incompleta'; END IF;
 raw:=substring(m->>'fuenteRaw' FROM 8); h:=substring(m->>'fuenteRawHash' FROM 8);
 IF raw ~ '^\s*$' OR h<>public.pn14_s2_hash(raw) THEN RAISE EXCEPTION 'pn14_s2 proveniencia hash'; END IF;
 campos:=public.pn14_s2_parse(substring(m->>'campos' FROM 6));
 FOR x IN SELECT * FROM jsonb_each_text(campos) ORDER BY key::integer LOOP
  IF x.value NOT LIKE 'OBJECT:%' THEN RAISE EXCEPTION 'pn14_s2 campo fuente tipo'; END IF;
  campo:=public.pn14_s2_parse(substring(x.value FROM 8));
  IF NOT campo ?& ARRAY['fieldPath','referencia','fuenteHash']
    OR (SELECT count(*) FROM jsonb_object_keys(campo))<>3 OR campo->>'fieldPath' NOT LIKE 'STRING:%'
    OR substring(campo->>'fieldPath' FROM 8) ~ '^\s*$' OR campo->>'referencia' NOT LIKE 'STRING:%'
    OR substring(campo->>'referencia' FROM 8) ~ '^\s*$'
    OR campo->>'fuenteHash' !~ '^STRING:[0-9a-f]{64}$'
    OR substring(campo->>'fieldPath' FROM 8)=ANY(paths)
    OR cardinality(paths)>0 AND convert_to(paths[cardinality(paths)],'UTF8')>=convert_to(substring(campo->>'fieldPath' FROM 8),'UTF8')
  THEN RAISE EXCEPTION 'pn14_s2 campo fuente inválido/duplicado/orden'; END IF;
  paths:=array_append(paths,substring(campo->>'fieldPath' FROM 8));
 END LOOP;
 requeridos:=public.pn14_s2_paths(public.pn14_s2_obj(public.pn14_s2_parse(c.contrato_canonico)-'proveniencia'-'congeladoEn'),'');
 requeridos:=requeridos||ARRAY['scopeKey','membership','numeroLineas','total.unidadesMinimas','total.monedaIso'];
 IF c.producto_fuente_id IS NULL THEN requeridos:=array_append(requeridos,'productoIdentidadAlternativa'); END IF;
 IF NOT paths @> requeridos THEN RAISE EXCEPTION 'pn14_s2 fuente por campo ausente'; END IF;
END $$;

CREATE FUNCTION public.pn14_s2_orden_guard() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
 IF TG_OP='INSERT' THEN
  IF NEW.estado_fundacion<>'PREPARANDO' THEN RAISE EXCEPTION 'pn14_s2 orden debe preparar'; END IF;
  RETURN NEW;
 END IF;
 IF TG_OP='DELETE' OR OLD.estado_fundacion='CONGELADA'
   OR NEW.estado_fundacion<>'CONGELADA' OR to_jsonb(NEW)-'estado_fundacion'<>to_jsonb(OLD)-'estado_fundacion'
 THEN RAISE EXCEPTION 'pn14_s2 orden inmutable'; END IF;
 RETURN NEW;
END $$;
CREATE TRIGGER pn14_s2_orden_guard BEFORE INSERT OR UPDATE OR DELETE ON public.orden_venta
 FOR EACH ROW EXECUTE FUNCTION public.pn14_s2_orden_guard();

CREATE FUNCTION public.pn14_s2_raiz_guard() RETURNS trigger LANGUAGE plpgsql AS $$
DECLARE estado text;
BEGIN
 IF TG_OP='INSERT' THEN
  IF NEW.orden_venta_id IS NOT NULL THEN RAISE EXCEPTION 'pn14_s2 sólo attach compra existente'; END IF; RETURN NEW;
 END IF;
 IF OLD.orden_venta_id IS NOT NULL THEN
  IF TG_OP='DELETE' OR OLD.id<>NEW.id OR OLD.usuario_id<>NEW.usuario_id
     OR public.pn14_s2_bundle(OLD)<>public.pn14_s2_bundle(NEW)
  THEN RAISE EXCEPTION 'pn14_s2 raíz inmutable'; END IF;
 ELSIF TG_OP='UPDATE' AND NEW.orden_venta_id IS NOT NULL THEN
  SELECT estado_fundacion INTO estado FROM public.orden_venta WHERE id=NEW.orden_venta_id FOR UPDATE;
  IF estado IS DISTINCT FROM 'PREPARANDO' OR OLD.id<>NEW.id OR OLD.usuario_id<>NEW.usuario_id
     OR NEW.precio_venta_unidades_minimas<>OLD.monto_centavos
     OR NEW.moneda_snapshot_iso<>upper(OLD.moneda)
  THEN RAISE EXCEPTION 'pn14_s2 attach inválido'; END IF;
 END IF;
 IF TG_OP='DELETE' THEN RETURN OLD; END IF; RETURN NEW;
END $$;
CREATE TRIGGER pn14_s2_raiz_guard BEFORE INSERT OR UPDATE OR DELETE ON public.compra
 FOR EACH ROW EXECUTE FUNCTION public.pn14_s2_raiz_guard();

CREATE FUNCTION public.pn14_s2_componente_guard() RETURNS trigger LANGUAGE plpgsql AS $$
DECLARE estado text; version text;
BEGIN
 IF TG_OP<>'INSERT' THEN RAISE EXCEPTION 'pn14_s2 componente inmutable'; END IF;
 SELECT o.estado_fundacion,c.politica_version INTO estado,version FROM public.compra c
 JOIN public.orden_venta o ON o.id=c.orden_venta_id WHERE c.id=NEW.compra_id FOR UPDATE OF o;
 IF estado IS DISTINCT FROM 'PREPARANDO' OR version<>NEW.politica_version
    OR NEW.id<>public.pn14_s2_uuid('PN14_S2_1:COMPONENTE:'||NEW.compra_id||':'||NEW.numero)
 THEN RAISE EXCEPTION 'pn14_s2 componente postseal/inválido'; END IF;
 RETURN NEW;
END $$;
CREATE TRIGGER pn14_s2_componente_guard BEFORE INSERT OR UPDATE OR DELETE ON public.compra_componente_snapshot
 FOR EACH ROW EXECUTE FUNCTION public.pn14_s2_componente_guard();

CREATE FUNCTION public.pn14_s2_informe_guard() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN RAISE EXCEPTION 'pn14_s2 informe append-only'; END $$;
CREATE TRIGGER pn14_s2_informe_guard BEFORE UPDATE OR DELETE ON public.informe_backfill_snapshot
 FOR EACH ROW EXECUTE FUNCTION public.pn14_s2_informe_guard();

CREATE FUNCTION public.pn14_s2_truncate_guard() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
 IF TG_TABLE_NAME<>'compra' OR EXISTS(SELECT 1 FROM public.compra WHERE orden_venta_id IS NOT NULL)
 THEN RAISE EXCEPTION 'pn14_s2 TRUNCATE protegido'; END IF; RETURN NULL;
END $$;
CREATE TRIGGER pn14_s2_orden_truncate BEFORE TRUNCATE ON public.orden_venta
 FOR EACH STATEMENT EXECUTE FUNCTION public.pn14_s2_truncate_guard();
CREATE TRIGGER pn14_s2_raiz_truncate BEFORE TRUNCATE ON public.compra
 FOR EACH STATEMENT EXECUTE FUNCTION public.pn14_s2_truncate_guard();
CREATE TRIGGER pn14_s2_componente_truncate BEFORE TRUNCATE ON public.compra_componente_snapshot
 FOR EACH STATEMENT EXECUTE FUNCTION public.pn14_s2_truncate_guard();
CREATE TRIGGER pn14_s2_informe_truncate BEFORE TRUNCATE ON public.informe_backfill_snapshot
 FOR EACH STATEMENT EXECUTE FUNCTION public.pn14_s2_truncate_guard();

CREATE FUNCTION public.pn14_s2_validar_sello(orden uuid) RETURNS void LANGUAGE plpgsql AS $$
DECLARE o public.orden_venta; c public.compra; n integer; minimo integer; maximo integer; suma numeric; proc text;
BEGIN
 SELECT * INTO o FROM public.orden_venta WHERE id=orden;
 IF NOT FOUND OR o.estado_fundacion<>'CONGELADA' THEN RAISE EXCEPTION 'pn14_s2 PREPARANDO no puede commit'; END IF;
 SELECT count(*),min(numero_linea),max(numero_linea),sum(precio_venta_unidades_minimas::numeric)
 INTO n,minimo,maximo,suma FROM public.compra WHERE orden_venta_id=orden;
 IF n<>o.numero_lineas OR minimo<>1 OR maximo<>n OR suma<0 OR suma>9223372036854775807
   OR suma<>o.total_unidades_minimas THEN RAISE EXCEPTION 'pn14_s2 grupo parcial/suma'; END IF;
 IF o.scope_key LIKE 'LEGACY_GRUPO:%' THEN
  IF (SELECT count(*) FROM public.compra WHERE grupo_compra_id=substring(o.scope_key FROM 14)::uuid)<>o.numero_lineas
    OR EXISTS(SELECT 1 FROM public.compra WHERE orden_venta_id=orden
       AND grupo_compra_id IS DISTINCT FROM substring(o.scope_key FROM 14)::uuid)
  THEN RAISE EXCEPTION 'pn14_s2 membership scope completo requerido'; END IF;
 ELSE
  IF o.numero_lineas<>1 OR NOT EXISTS(SELECT 1 FROM public.compra WHERE orden_venta_id=orden
     AND id=substring(o.scope_key FROM 15)::uuid AND grupo_compra_id IS NULL)
  THEN RAISE EXCEPTION 'pn14_s2 scope individual incoherente'; END IF;
 END IF;
 FOR c IN SELECT * FROM public.compra WHERE orden_venta_id=orden LOOP
  SELECT count(*),min(numero),max(numero) INTO n,minimo,maximo FROM public.compra_componente_snapshot WHERE compra_id=c.id;
  IF n=0 OR minimo<>1 OR maximo<>n OR c.congelado_en<>o.congelado_en
    OR c.politica_canonica<>public.pn14_s2_politica(c) OR c.contrato_canonico<>public.pn14_s2_contrato(c)
    OR EXISTS(SELECT 1 FROM public.compra_componente_snapshot WHERE compra_id=c.id AND politica_version<>c.politica_version)
  THEN RAISE EXCEPTION 'pn14_s2 policy/contrato/componentes incoherentes'; END IF;
  PERFORM public.pn14_s2_proveniencia(c);
 END LOOP;
 SELECT public.pn14_s2_obj(jsonb_build_object('compras','LIST:'||
 public.pn14_s2_obj(jsonb_object_agg(numero_linea::text,'OBJECT:'||procedencia_canonica)))) INTO proc
 FROM public.compra WHERE orden_venta_id=orden;
 IF o.procedencia_canonica<>proc OR o.payload_canonico<>public.pn14_s2_orden(o)
 THEN RAISE EXCEPTION 'pn14_s2 orden canon/proveniencia incoherente'; END IF;
END $$;
CREATE FUNCTION public.pn14_s2_sello_deferred() RETURNS trigger LANGUAGE plpgsql AS $$
DECLARE orden uuid;
BEGIN
 IF TG_TABLE_NAME='orden_venta' THEN orden:=NEW.id;
 ELSIF TG_TABLE_NAME='compra' THEN orden:=NEW.orden_venta_id;
 ELSE SELECT orden_venta_id INTO orden FROM public.compra WHERE id=NEW.compra_id; END IF;
 IF orden IS NOT NULL THEN PERFORM public.pn14_s2_validar_sello(orden); END IF; RETURN NULL;
END $$;
CREATE CONSTRAINT TRIGGER pn14_s2_orden_sello AFTER INSERT OR UPDATE ON public.orden_venta
 DEFERRABLE INITIALLY DEFERRED FOR EACH ROW EXECUTE FUNCTION public.pn14_s2_sello_deferred();
CREATE CONSTRAINT TRIGGER pn14_s2_raiz_sello AFTER UPDATE ON public.compra
 DEFERRABLE INITIALLY DEFERRED FOR EACH ROW WHEN (OLD.orden_venta_id IS NULL AND NEW.orden_venta_id IS NOT NULL)
 EXECUTE FUNCTION public.pn14_s2_sello_deferred();
CREATE CONSTRAINT TRIGGER pn14_s2_componente_sello AFTER INSERT ON public.compra_componente_snapshot
 DEFERRABLE INITIALLY DEFERRED FOR EACH ROW EXECUTE FUNCTION public.pn14_s2_sello_deferred();
