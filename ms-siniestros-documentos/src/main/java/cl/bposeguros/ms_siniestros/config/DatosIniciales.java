package cl.bposeguros.ms_siniestros.config;

import cl.bposeguros.ms_siniestros.model.Asignacion;
import cl.bposeguros.ms_siniestros.model.Aviso;
import cl.bposeguros.ms_siniestros.model.DocumentoAdjunto;
import cl.bposeguros.ms_siniestros.model.Gestion;
import cl.bposeguros.ms_siniestros.model.Siniestro;
import cl.bposeguros.ms_siniestros.model.SolicitudDocumento;
import cl.bposeguros.ms_siniestros.repository.AdjuntoRepository;
import cl.bposeguros.ms_siniestros.repository.AsignacionRepository;
import cl.bposeguros.ms_siniestros.repository.AvisoRepository;
import cl.bposeguros.ms_siniestros.repository.GestionRepository;
import cl.bposeguros.ms_siniestros.repository.SiniestroRepository;
import cl.bposeguros.ms_siniestros.repository.SolicitudRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
@RequiredArgsConstructor
public class DatosIniciales implements CommandLineRunner {

    private final SiniestroRepository siniestroRepository;
    private final AsignacionRepository asignacionRepository;
    private final GestionRepository gestionRepository;
    private final SolicitudRepository solicitudRepository;
    private final AdjuntoRepository adjuntoRepository;
    private final AvisoRepository avisoRepository;

    @Override
    public void run(String... args) {
        if (siniestroRepository.count() > 0) {
            return;
        }

        
        siniestroRepository.saveAll(List.of(
                sin("SIN-26-004801", 1, 1, 1, "2026-09-02", "2026-09-02", "Maipú",
                        "Choque leve en un estacionamiento; se dañó el parachoques trasero.",
                        3, "2026-09-18", 9L, "2026-09-03 09:15", false, null, null),
                sin("SIN-26-004815", 2, 2, 5, "2026-08-27", "2026-08-29", "Puente Alto",
                        "Entraron a mi casa mientras no estaba y se llevaron varias especies.",
                        2, "2026-08-30", 10L, "2026-08-30 09:00", false, null, null),
                sin("SIN-26-004822", 3, 4, 3, "2026-09-10", "2026-09-10", "Providencia",
                        "Se trizó el parabrisas mientras conducía.",
                        4, "2026-09-21", 11L, "2026-09-12 08:30", false, "APROBADO",
                        "Tu siniestro fue aprobado por Seguros Valle Central. La compañía se contactará contigo para coordinar la reposición del parabrisas."),
                sin("SIN-26-004830", 4, 3, 4, "2026-09-14", "2026-09-16", "Viña del Mar",
                        "Apareció una mancha de humedad en el cielo del living por una filtración.",
                        2, "2026-09-17", 12L, "2026-09-17 09:00", false, null, null),
                sin("SIN-26-004838", 5, 1, 2, "2026-09-20", "2026-09-21", "La Florida",
                        "Me robaron el vehículo que estaba estacionado en la calle.",
                        2, "2026-09-22", 9L, "2026-09-22 10:00", false, null, null),
                sin("SIN-26-004851", 6, 3, 6, "2026-09-25", "2026-09-26", "Concepción",
                        "Se produjo un principio de incendio en la cocina de mi casa.",
                        1, "2026-09-26", null, null, false, null, null),
                sin("SIN-26-004857", 7, 4, 1, "2026-09-29", "2026-09-30", "Quilicura",
                        "Choque con otro vehículo en una intersección.",
                        1, "2026-09-30", null, null, false, null, null),
                sin("SIN-26-004860", 8, 2, 4, "2026-08-10", "2026-09-28", "Rancagua",
                        "Una filtración de agua en el baño dañó el piso y una muralla.",
                        1, "2026-09-28", null, null, true, null, null),
                sin("SIN-26-004712", 1, 1, 3, "2026-07-15", "2026-07-15", "Santiago",
                        "Se quebró un vidrio lateral del auto.",
                        4, "2026-07-20", 11L, "2026-07-16 09:00", false, "DESISTIDO",
                        "Cerramos tu caso porque nos indicaste que no continuarás con el reclamo. Gracias por avisarnos."),
                sin("SIN-26-004745", 2, 4, 1, "2026-07-30", "2026-08-01", "La Reina",
                        "Choque en una rotonda con daños en la parte delantera.",
                        4, "2026-08-19", 13L, "2026-08-02 09:00", false, "RECHAZADO",
                        "Lamentamos informarte que el daño reportado no está cubierto por tu póliza. Si tienes dudas, puedes comunicarte con tu compañía de seguros.")
        ));

        
        asignacionRepository.saveAll(List.of(
                asig(1, 9, "2026-09-03 09:15", null, null),
                asig(2, 10, "2026-08-30 09:00", null, null),
                asig(3, 11, "2026-09-12 08:30", "2026-09-21 12:30", "CIERRE"),
                asig(4, 12, "2026-09-17 09:00", null, null),
                asig(5, 9, "2026-09-22 10:00", null, null),
                asig(9, 11, "2026-07-16 09:00", "2026-07-20 10:00", "CIERRE"),
                asig(10, 13, "2026-08-02 09:00", "2026-08-19 11:00", "CIERRE")
        ));

        gestionRepository.saveAll(List.of(
                ges(1, "2026-09-02 18:40", "Reporte", "Recibimos el reporte de tu siniestro.", "Asegurado", true),
                ges(1, "2026-09-03 09:15", "Asignación", "Tu caso fue asignado a Paulina F. – Equipo Vehículos.", "N. Reyes (ADM-01)", true),
                ges(1, "2026-09-03 09:15", "Cambio de etapa", "Tu caso pasó a \"En evaluación\".", "N. Reyes (ADM-01)", true),
                ges(1, "2026-09-05 11:02", "Solicitud de documento", "Necesitamos el presupuesto de reparación del taller.", "P. Fernández (LIQ-01)", true),
                ges(1, "2026-09-08 16:30", "Documento recibido", "Recibimos tu presupuesto de reparación.", "Asegurado", true),
                ges(1, "2026-09-10 10:20", Gestion.TIPO_NOTA_LIQUIDADOR, "Se coordina inspección con taller; contacto telefónico con jefe de taller.", "P. Fernández (LIQ-01)", false),
                ges(1, "2026-09-12 10:00", "Inspección", "Revisamos tu vehículo en el taller.", "P. Fernández (LIQ-01)", true),
                ges(1, "2026-09-18 15:45", "Cambio de etapa", "Tu caso pasó a \"En liquidación\". Estamos definiendo la resolución con tu compañía.", "P. Fernández (LIQ-01)", true),
                ges(2, "2026-08-29 10:05", "Reporte", "Recibimos el reporte de tu siniestro.", "Asegurado", true),
                ges(2, "2026-08-30 09:00", "Asignación", "Tu caso fue asignado a Matías G. – Equipo Hogar.", "F. Cortés (ADM-02)", true),
                ges(2, "2026-09-02 12:10", "Solicitud de documento", "Necesitamos la constancia de denuncia policial y las boletas de lo robado.", "M. Gallardo (LIQ-02)", true),
                ges(2, "2026-09-17 09:00", "Recordatorio", "Aún tienes documentos pendientes para continuar con tu caso.", "Sistema", true)
        ));

        solicitudRepository.saveAll(List.of(
                sol(1, "Presupuesto de reparación", "2026-09-05", true, "2026-09-08"),
                sol(2, "Constancia de denuncia policial", "2026-09-02", true, "2026-09-04"),
                sol(2, "Boletas o facturas de lo robado", "2026-09-02", false, null),
                sol(4, "Informe o boleta del gasfíter", "2026-09-18", false, null),
                sol(5, "Constancia de denuncia policial", "2026-09-23", false, null),
                sol(5, "Padrón del vehículo", "2026-09-23", false, null)
        ));

        // Registros de ejemplo: no tienen archivo real en disco (ruta vacia)
        adjuntoRepository.saveAll(List.of(
                adj(1, "foto_parachoques_1.jpg", "Fotografía del daño", "2026-09-02", "Asegurado", null),
                adj(1, "foto_parachoques_2.jpg", "Fotografía del daño", "2026-09-02", "Asegurado", null),
                adj(1, "presupuesto_taller.pdf", "Presupuesto de reparación", "2026-09-08", "Asegurado", 1L),
                adj(2, "constancia_denuncia.pdf", "Constancia de denuncia policial", "2026-09-04", "Asegurado", 2L),
                adj(3, "parabrisas_trizado.jpg", "Fotografía del daño", "2026-09-10", "Asegurado", null),
                adj(3, "boleta_reposicion.pdf", "Presupuesto o boleta", "2026-09-15", "Asegurado", null),
                adj(4, "mancha_cielo_living.jpg", "Fotografía del daño", "2026-09-16", "Asegurado", null),
                adj(4, "informe_inspeccion.pdf", "Informe de inspección", "2026-09-22", "C. Ibarra (LIQ-04)", null)
        ));

        avisoRepository.saveAll(List.of(
                avi(1, "2026-09-18 15:45", "Cambio de etapa", "Tu siniestro SIN-26-004801 pasó a \"En liquidación\".", true),
                avi(2, "2026-09-17 09:00", "Recordatorio", "Aún falta un documento para tu siniestro SIN-26-004815.", false),
                avi(3, "2026-09-21 12:30", "Cierre", "Tu siniestro SIN-26-004822 fue aprobado. Revisa el mensaje de cierre.", true),
                avi(4, "2026-09-18 10:05", "Solicitud de documento", "Necesitamos el informe o boleta del gasfíter para tu siniestro SIN-26-004830.", true),
                avi(5, "2026-09-23 14:10", "Solicitud de documento", "Necesitamos 2 documentos para tu siniestro SIN-26-004838.", false),
                avi(10, "2026-08-19 11:00", "Cierre", "Tu siniestro SIN-26-004745 fue cerrado. Revisa el detalle de la resolución.", true)
        ));
    }

    private Siniestro sin(String numero, long asegurado, long compania, long tipo, String ocurrencia, String reporte,
                          String comuna, String descripcion, int etapa, String fechaEtapa, Long liquidador,
                          String fechaAsignacion, boolean fueraDePlazo, String resultado, String mensajeCierre) {
        return Siniestro.builder()
                .numero(numero).aseguradoId(asegurado).companiaId(compania).tipoId(tipo)
                .fechaOcurrencia(ocurrencia).fechaReporte(reporte).comuna(comuna).descripcion(descripcion)
                .etapa(etapa).fechaCambioEtapa(fechaEtapa)
                .liquidadorId(liquidador).fechaAsignacion(fechaAsignacion)
                .fueraDePlazo(fueraDePlazo).resultado(resultado).mensajeCierre(mensajeCierre)
                .build();
    }

    private Asignacion asig(long siniestro, long liquidador, String inicio, String fin, String motivoFin) {
        return Asignacion.builder()
                .siniestroId(siniestro).liquidadorId(liquidador)
                .fechaInicio(inicio).fechaFin(fin).motivoFin(motivoFin)
                .build();
    }

    private Gestion ges(long siniestro, String fecha, String tipo, String descripcion, String responsable, boolean visible) {
        return Gestion.builder()
                .siniestroId(siniestro).fechaHora(fecha).tipo(tipo).descripcion(descripcion)
                .responsable(responsable).visibleAsegurado(visible)
                .build();
    }

    private SolicitudDocumento sol(long siniestro, String documento, String fecha, boolean entregado, String fechaEntrega) {
        return SolicitudDocumento.builder()
                .siniestroId(siniestro).documentoSolicitado(documento).comentario("")
                .fechaSolicitud(fecha).entregado(entregado).fechaEntrega(fechaEntrega)
                .build();
    }

    private DocumentoAdjunto adj(long siniestro, String archivo, String tipo, String fecha, String por, Long solicitudId) {
        return DocumentoAdjunto.builder()
                .siniestroId(siniestro).nombreArchivo(archivo).rutaArchivo("").tipoDocumento(tipo)
                .fechaCarga(fecha).cargadoPor(por).comentario("").solicitudId(solicitudId)
                .build();
    }

    private Aviso avi(long siniestro, String fecha, String motivo, String mensaje, boolean leido) {
        return Aviso.builder()
                .siniestroId(siniestro).fechaHora(fecha).motivo(motivo).mensaje(mensaje).leido(leido)
                .build();
    }
}