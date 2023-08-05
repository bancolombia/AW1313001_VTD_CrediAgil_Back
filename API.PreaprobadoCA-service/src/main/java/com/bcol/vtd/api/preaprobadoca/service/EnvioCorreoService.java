package com.bcol.vtd.api.preaprobadoca.service;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import java.util.Map;


public class EnvioCorreoService {
	
	@Inject
	ParameterService parameterService;
	
	public Response enviarCorreo(Map<String, Object> datosCorreo,Map<String, String> propiedades,String idSesion) {
		 EnviadorCorreoProxy correoProxy = new EnviadorCorreoProxy(propiedades,idSesion);
		 
		 return correoProxy.prepararEnvioCorreo(datosCorreo);
		 
	}

}
