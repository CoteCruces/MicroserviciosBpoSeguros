package cl.bposeguros.ms_siniestros.service;

import cl.bposeguros.ms_siniestros.client.CatalogoClient;
import cl.bposeguros.ms_siniestros.client.UsuarioClient;
import cl.bposeguros.ms_siniestros.dto.AsignarSiniestro;
import cl.bposeguros.ms_siniestros.dto.AvanzarEtapaSiniestro;
import cl.bposeguros.ms_siniestros.dto.LiquidadorPropuesto;
import cl.bposeguros.ms_siniestros.dto.RespuestaCerrarSiniestro;
import cl.bposeguros.ms_siniestros.dto.CompaniaDTO;
import cl.bposeguros.ms_siniestros.dto.ReportarSiniestroRequest;
import cl.bposeguros.ms_siniestros.dto.SiniestroRespuesta;
import cl.bposeguros.ms_siniestros.dto.TipoSiniestroDTO;
import cl.bposeguros.ms_siniestros.dto.UsuarioDTO;
import cl.bposeguros.ms_siniestros.model.Asignacion;
import cl.bposeguros.ms_siniestros.model.Etapa;
import cl.bposeguros.ms_siniestros.model.Siniestro;
import cl.bposeguros.ms_siniestros.repository.AsignacionRepository;
import cl.bposeguros.ms_siniestros.repository.SiniestroRepository;
import cl.bposeguros.ms_siniestros.repository.SolicitudRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SiniestroService {

    private static final long ROL_ASEGURADO = 1L;
    private static final long ROL_LIQUIDADOR = 2L;

    private final SiniestroRepository siniestroRepository;
    private final AsignacionRepository asignacionRepository;
    private final SolicitudRepository solicitudRepository;
    private final SeguimientoService seguimientoService;
    private final UsuarioClient usuarioClient;
    private final CatalogoClient catalogoClient;

    
    public List<SiniestroRespuesta> listar(Long aseguradoId, Long liquidadorId, boolean sinAsignar,
                                          boolean porReasignar, boolean soloAbiertos, String desde, String hasta,
                                          String buscar, String orden, Integer limite) {
        List<Siniestro> base;
        if (aseguradoId != null) {
            base = siniestroRepository.findByAseguradoId(aseguradoId);
        } else if (liquidadorId != null) {
            base = siniestroRepository.findByLiquidadorId(liquidadorId);
        } else {
            base = siniestroRepository.findAll();
        }

        LocalDate desdeFecha = (desde == null || desde.isBlank()) ? null : Fechas.parsear(desde, "fecha desde");
        LocalDate hastaFecha = (hasta == null || hasta.isBlank()) ? null : Fechas.parsear(hasta, "fecha hasta");
        if (desdeFecha != null && hastaFecha != null && desdeFecha.isAfter(hastaFecha)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La fecha desde no puede ser posterior a la fecha hasta");
        }

        Comparator<Siniestro> orden1;
        if ("reporte".equalsIgnoreCase(orden)) {
            orden1 = Comparator.comparing(Siniestro::getFechaReporte).thenComparing(Siniestro::getId).reversed();
        } else if (liquidadorId != null || sinAsignar || porReasignar) {
            orden1 = Comparator.comparing((Siniestro s) -> estaCerrado(s))
                    .thenComparing(Siniestro::getFechaCambioEtapa)
                    .thenComparing(Siniestro::getId);
        } else {
            orden1 = Comparator.comparing((Siniestro s) -> estaCerrado(s))
                    .thenComparing(Siniestro::getFechaCambioEtapa, Comparator.reverseOrder())
                    .thenComparing(Siniestro::getId, Comparator.reverseOrder());
        }

        Resolutor resolutor = nuevoResolutor();
        String texto = buscar == null ? "" : buscar.trim().toLowerCase();
        String rutBuscado = texto.replace(".", "").replace("-", "");

        List<SiniestroRespuesta> resultado = base.stream()
                .filter(s -> !sinAsignar || (s.getLiquidadorId() == null && !estaCerrado(s)))
                .filter(s -> !porReasignar || esPorReasignar(s))
                .filter(s -> !soloAbiertos || !estaCerrado(s))
                .filter(s -> desdeFecha == null || !LocalDate.parse(s.getFechaOcurrencia()).isBefore(desdeFecha))
                .filter(s -> hastaFecha == null || !LocalDate.parse(s.getFechaOcurrencia()).isAfter(hastaFecha))
                .sorted(orden1)
                .map(s -> aRespuesta(s, resolutor))
                .filter(r -> texto.isEmpty()
                        || r.numero().toLowerCase().contains(texto)
                        || r.aseguradoNombre().toLowerCase().contains(texto)
                        || r.aseguradoRut().toLowerCase().contains(rutBuscado))
                .toList();

        if (limite != null && limite > 0 && resultado.size() > limite) {
            return resultado.subList(0, limite);
        }
        return resultado;
    }


    public SiniestroRespuesta obtener(Long id, Long aseguradoId, Long liquidadorId) {
        Siniestro s = seguimientoService.buscarSiniestro(id);
        if (aseguradoId != null && !aseguradoId.equals(s.getAseguradoId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Siniestro no encontrado");
        }
        if (liquidadorId != null && !liquidadorId.equals(s.getLiquidadorId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Siniestro no encontrado");
        }
        return aRespuesta(s, nuevoResolutor());
    }

    
    public List<LiquidadorPropuesto> listarCandidatos(Long siniestroId) {
        Siniestro s = seguimientoService.buscarSiniestro(siniestroId);
        TipoSiniestroDTO tipo = catalogoClient.obtenerTipo(s.getTipoId());
        if (tipo == null) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "No se encontró el tipo de siniestro en el catálogo");
        }
        String equipo = equipoDe(tipo);
        return usuarioClient.listarPorRol(ROL_LIQUIDADOR).stream()
                .filter(UsuarioDTO::activo)
                .filter(u -> equipo.equalsIgnoreCase(u.equipo()))
                .map(u -> new LiquidadorPropuesto(u.id(), u.nombre(), u.codigo(), u.equipo(),
                        siniestroRepository.countByLiquidadorIdAndEtapaNot(u.id(), Etapa.CERRADO.getId())))
                .sorted(Comparator.comparingLong(LiquidadorPropuesto::casosAbiertos).thenComparing(LiquidadorPropuesto::id))
                .toList();
    }

    

    @Transactional
    public SiniestroRespuesta reportar(ReportarSiniestroRequest d) {
        if (d.aseguradoId() == null || d.companiaId() == null || d.tipoId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Faltan datos del siniestro");
        }
        UsuarioDTO asegurado = usuarioClient.obtener(d.aseguradoId());
        if (asegurado == null || asegurado.rolId() == null || asegurado.rolId() != ROL_ASEGURADO) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo un asegurado puede reportar un siniestro");
        }
        if (!asegurado.activo()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Tu cuenta no está activa");
        }

        CompaniaDTO compania = catalogoClient.obtenerCompania(d.companiaId());
        if (compania == null || !compania.activa()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La compañía elegida no está disponible");
        }
        TipoSiniestroDTO tipo = catalogoClient.obtenerTipo(d.tipoId());
        if (tipo == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El tipo de siniestro no existe");
        }
        boolean esVehiculo = "VEHICULO".equals(tipo.linea());
        if (esVehiculo && !compania.gestionaVehiculos()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Esa compañía no gestiona siniestros de vehículos");
        }
        if (!esVehiculo && !compania.gestionaHogar()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Esa compañía no gestiona siniestros de hogar");
        }

        LocalDate ocurrencia = Fechas.parsear(d.fechaOcurrencia(), "fecha de ocurrencia");
        LocalDate hoy = Fechas.hoy();
        if (ocurrencia.isAfter(hoy)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La fecha de ocurrencia no puede ser futura");
        }
        String comuna = d.comuna() == null ? "" : d.comuna().trim();
        if (comuna.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Indica la comuna donde ocurrió");
        }
        String descripcion = d.descripcion() == null ? "" : d.descripcion().trim();
        if (descripcion.length() < 10) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Describe lo ocurrido (mínimo 10 caracteres)");
        }
        if (descripcion.length() > 1000) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La descripción es muy larga (máximo 1000 caracteres)");
        }

        Optional<Siniestro> duplicado = siniestroRepository
                .findFirstByAseguradoIdAndCompaniaIdAndTipoIdAndFechaOcurrenciaAndEtapaNot(
                        d.aseguradoId(), d.companiaId(), d.tipoId(), ocurrencia.toString(), Etapa.CERRADO.getId());
        if (duplicado.isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ya reportaste un siniestro igual que sigue abierto: " + duplicado.get().getNumero());
        }

        boolean fueraDePlazo = Fechas.diasHabilesEntre(ocurrencia, hoy) > compania.plazoDias();
        Siniestro nuevo = siniestroRepository.save(Siniestro.builder()
                .numero(siguienteNumero())
                .aseguradoId(d.aseguradoId())
                .companiaId(d.companiaId())
                .tipoId(d.tipoId())
                .fechaOcurrencia(ocurrencia.toString())
                .fechaReporte(hoy.toString())
                .comuna(comuna)
                .descripcion(descripcion)
                .etapa(Etapa.RECIBIDO.getId())
                .fechaCambioEtapa(hoy.toString())
                .fueraDePlazo(fueraDePlazo)
                .build());

        seguimientoService.registrarGestion(nuevo.getId(), "Reporte",
                "Recibimos el reporte de tu siniestro.", "Asegurado", true);
        return aRespuesta(nuevo, nuevoResolutor());
    }

    
    @Transactional
    public SiniestroRespuesta asignar(Long siniestroId, AsignarSiniestro d) {
        UsuarioDTO analista = seguimientoService.exigirAnalista(d.solicitanteId());
        Siniestro s = seguimientoService.buscarSiniestro(siniestroId);
        if (estaCerrado(s)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El caso ya está cerrado");
        }
        if (s.getLiquidadorId() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El caso ya tiene un liquidador asignado");
        }
        if (d.liquidadorId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Elige a quién asignar el caso");
        }
        UsuarioDTO liquidador = usuarioClient.obtener(d.liquidadorId());
        if (liquidador == null || liquidador.rolId() == null || liquidador.rolId() != ROL_LIQUIDADOR) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El liquidador elegido no existe");
        }
        if (!liquidador.activo()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El liquidador está bloqueado");
        }
        TipoSiniestroDTO tipo = catalogoClient.obtenerTipo(s.getTipoId());
        if (tipo != null && !equipoDe(tipo).equalsIgnoreCase(liquidador.equipo())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El liquidador debe ser del equipo " + equipoDe(tipo));
        }

        boolean esReasignacion = s.getLiquidadorAnteriorId() != null;
        boolean cambiaEtapa = s.getEtapa() == Etapa.RECIBIDO.getId();
        String ahora = Fechas.ahora();
        String responsable = Nombres.responsable(analista.nombre(), analista.codigo());
        String visible = Nombres.visible(liquidador.nombre(), liquidador.equipo());

        s.setLiquidadorId(liquidador.id());
        s.setFechaAsignacion(ahora);
        s.setLiquidadorAnteriorId(null);
        if (cambiaEtapa) {
            s.setEtapa(Etapa.EN_EVALUACION.getId());
            s.setFechaCambioEtapa(ahora.substring(0, 10));
        }
        siniestroRepository.save(s);

        asignacionRepository.save(Asignacion.builder()
                .siniestroId(s.getId())
                .liquidadorId(liquidador.id())
                .fechaInicio(ahora)
                .build());

        seguimientoService.registrarGestion(s.getId(), "Asignación",
                (esReasignacion ? "Tu caso fue reasignado a " : "Tu caso fue asignado a ") + visible + ".",
                responsable, true);
        if (cambiaEtapa) {
            seguimientoService.registrarGestion(s.getId(), "Cambio de etapa",
                    "Tu caso pasó a \"En evaluación\".", responsable, true);
        }
        seguimientoService.registrarAviso(s.getId(), "Liquidador asignado",
                "Tu caso ya tiene liquidador: " + visible + "."
                        + (cambiaEtapa ? " Pasó a \"En evaluación\"." : ""));
        return aRespuesta(s, nuevoResolutor());
    }

    
    @Transactional
    public SiniestroRespuesta avanzarEtapa(Long siniestroId, AvanzarEtapaSiniestro d) {
        Siniestro s = seguimientoService.buscarSiniestro(siniestroId);
        UsuarioDTO liquidador = seguimientoService.exigirLiquidadorDelCaso(s, d.liquidadorId());

        if (s.getEtapa() != Etapa.EN_EVALUACION.getId()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Este caso no se puede avanzar desde su etapa actual");
        }
        if (solicitudRepository.countBySiniestroIdAndEntregadoFalse(s.getId()) > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Primero deben estar entregados todos los documentos pendientes");
        }
        String ahora = Fechas.ahora();
        s.setEtapa(Etapa.EN_LIQUIDACION.getId());
        s.setFechaCambioEtapa(ahora.substring(0, 10));
        siniestroRepository.save(s);

        seguimientoService.registrarGestion(s.getId(), "Cambio de etapa",
                "Tu caso pasó a \"En liquidación\". Estamos definiendo la resolución con tu compañía.",
                Nombres.responsable(liquidador.nombre(), liquidador.codigo()), true);
        seguimientoService.registrarAviso(s.getId(), "Cambio de etapa",
                "Tu siniestro " + s.getNumero() + " pasó a \"En liquidación\".");
        return aRespuesta(s, nuevoResolutor());
    }

    @Transactional
    public SiniestroRespuesta cerrar(Long siniestroId, RespuestaCerrarSiniestro d) {
        Siniestro s = seguimientoService.buscarSiniestro(siniestroId);
        UsuarioDTO liquidador = seguimientoService.exigirLiquidadorDelCaso(s, d.liquidadorId());

        Etapa etapaActual = Etapa.desdeId(s.getEtapa());
        String resultado = d.resultado() == null ? "" : d.resultado().trim().toUpperCase();
        if (resultado.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Elige el resultado");
        }
        if (!etapaActual.resultadosPermitidos().contains(resultado)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Ese resultado no es válido para un caso en etapa \"" + etapaActual.getEtiqueta() + "\"");
        }
        String mensaje = d.mensaje() == null ? "" : d.mensaje().trim();
        if (mensaje.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Escribe un mensaje para el asegurado");
        }
        if (mensaje.length() < 15) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Explica un poco más (mínimo 15 caracteres)");
        }

        String ahora = Fechas.ahora();
        s.setEtapa(Etapa.CERRADO.getId());
        s.setFechaCambioEtapa(ahora.substring(0, 10));
        s.setResultado(resultado);
        s.setMensajeCierre(mensaje);
        siniestroRepository.save(s);

        asignacionRepository.findFirstBySiniestroIdAndFechaFinIsNull(s.getId()).ifPresent(a -> {
            a.setFechaFin(ahora);
            a.setMotivoFin("CIERRE");
            asignacionRepository.save(a);
        });

        String nombreResultado = resultado.charAt(0) + resultado.substring(1).toLowerCase();
        seguimientoService.registrarGestion(s.getId(), "Cierre",
                "Tu caso fue cerrado. Resultado: " + nombreResultado + ".",
                Nombres.responsable(liquidador.nombre(), liquidador.codigo()), true);
        seguimientoService.registrarAviso(s.getId(), "Cierre",
                "Tu siniestro " + s.getNumero() + " fue cerrado (" + nombreResultado + "). Revisa el mensaje de cierre.");
        return aRespuesta(s, nuevoResolutor());
    }

    

    public Resolutor nuevoResolutor() {
        return new Resolutor(usuarioClient, catalogoClient);
    }

    public boolean estaCerrado(Siniestro s) {
        return s.getEtapa() == Etapa.CERRADO.getId();
    }

    
    public boolean esPorReasignar(Siniestro s) {
        return s.getLiquidadorId() == null && s.getLiquidadorAnteriorId() != null && !estaCerrado(s);
    }

    
    public SiniestroRespuesta aRespuesta(Siniestro s, Resolutor r) {
        UsuarioDTO asegurado = r.usuario(s.getAseguradoId());
        UsuarioDTO liquidador = r.usuario(s.getLiquidadorId());
        UsuarioDTO anterior = r.usuario(s.getLiquidadorAnteriorId());
        CompaniaDTO compania = r.compania(s.getCompaniaId());
        TipoSiniestroDTO tipo = r.tipo(s.getTipoId());
        Etapa etapa = Etapa.desdeId(s.getEtapa());

        Long dias = null;
        if (s.getFechaAsignacion() != null) {
            dias = Fechas.diasEntre(s.getFechaAsignacion(), estaCerrado(s) ? s.getFechaCambioEtapa() : null);
        }

        return new SiniestroRespuesta(
                s.getId(), s.getNumero(),
                s.getAseguradoId(),
                asegurado == null ? "" : asegurado.nombre(),
                asegurado == null ? "" : asegurado.rut(),
                s.getCompaniaId(), compania == null ? "" : compania.nombre(),
                s.getTipoId(), tipo == null ? "" : tipo.nombre(),
                s.getFechaOcurrencia(), s.getFechaReporte(), s.getComuna(), s.getDescripcion(),
                etapa.getId(), etapa.name(), etapa.getEtiqueta(), s.getFechaCambioEtapa(),
                s.getLiquidadorId(),
                liquidador == null ? null : liquidador.nombre(),
                liquidador == null ? null : liquidador.codigo(),
                liquidador == null ? null : liquidador.equipo(),
                s.getFechaAsignacion(), dias,
                s.getLiquidadorAnteriorId(),
                anterior == null ? null : anterior.nombre(),
                esPorReasignar(s),
                s.isFueraDePlazo(), s.getResultado(), s.getMensajeCierre(),
                seguimientoService.ultimaActualizacion(s.getId()));
    }

    private String equipoDe(TipoSiniestroDTO tipo) {
        return "VEHICULO".equals(tipo.linea()) ? "Vehículos" : "Hogar";
    }

    
    private String siguienteNumero() {
        String prefijo = "SIN-" + String.format("%02d", Fechas.hoy().getYear() % 100) + "-";
        int ultimo = siniestroRepository.findTopByNumeroStartingWithOrderByNumeroDesc(prefijo)
                .map(x -> Integer.parseInt(x.getNumero().substring(prefijo.length())))
                .orElse(0);
        return prefijo + String.format("%06d", ultimo + 1);
    }
}