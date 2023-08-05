package com.bcol.vtd.api.preaprobadoca.service;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import java.util.List;

public interface DocumentoService {
 
	Response obtenerCartaBienvenida(HttpServletRequest requestContext, VentaDigitalCrediAgil ventaDigitalLibreInversion, String idSesion, String authorization);
	
	RespuestaServicioEnvioCorreo getDocumento(List<Parametro> listaParametros);
}
