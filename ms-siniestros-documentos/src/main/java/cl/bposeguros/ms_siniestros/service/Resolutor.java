package cl.bposeguros.ms_siniestros.service;

import cl.bposeguros.ms_siniestros.client.CatalogoClient;
import cl.bposeguros.ms_siniestros.client.UsuarioClient;
import cl.bposeguros.ms_siniestros.dto.CompaniaDTO;
import cl.bposeguros.ms_siniestros.dto.TipoSiniestroDTO;
import cl.bposeguros.ms_siniestros.dto.UsuarioDTO;

import java.util.HashMap;
import java.util.Map;

// Pide a los otros microservicios los datos que hacen falta (nombre del asegurado, de la compañía...)
public class Resolutor {

    private final UsuarioClient usuarioClient;
    private final CatalogoClient catalogoClient;
    private final Map<Long, UsuarioDTO> usuarios = new HashMap<>();
    private final Map<Long, CompaniaDTO> companias = new HashMap<>();
    private final Map<Long, TipoSiniestroDTO> tipos = new HashMap<>();

    public Resolutor(UsuarioClient usuarioClient, CatalogoClient catalogoClient) {
        this.usuarioClient = usuarioClient;
        this.catalogoClient = catalogoClient;
    }

    public UsuarioDTO usuario(Long id) {
        if (id == null) {
            return null;
        }
        if (!usuarios.containsKey(id)) {
            usuarios.put(id, usuarioClient.obtener(id));
        }
        return usuarios.get(id);
    }

    public CompaniaDTO compania(Long id) {
        if (id == null) {
            return null;
        }
        if (!companias.containsKey(id)) {
            companias.put(id, catalogoClient.obtenerCompania(id));
        }
        return companias.get(id);
    }

    public TipoSiniestroDTO tipo(Long id) {
        if (id == null) {
            return null;
        }
        if (!tipos.containsKey(id)) {
            tipos.put(id, catalogoClient.obtenerTipo(id));
        }
        return tipos.get(id);
    }
}