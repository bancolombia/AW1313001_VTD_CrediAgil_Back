package com.bcol.vtd.api.preaprobadoca.factory;

import javax.inject.Inject;


public class ProxyFactory {
	
	@Inject
	ParameterService parameterService;
	
	public GestionSolicitudCreditoConsumoImpl cargarProxySolicitudCredito(String idSesion,String ipcliente, String tokenUsuario) throws Exception {
		
		return new GestionSolicitudCreditoConsumoImpl(parameterService.getParametersDatosSolicitudCredito(),
				idSesion, ipcliente, tokenUsuario);
	}

	public SessionService sessionServiceImpl(SessionRequest rqdatosCliente) throws ConsumerSessionException {
		
		return new SessionService(parameterService.getParametersGestionSesion(), rqdatosCliente);
	}
}
