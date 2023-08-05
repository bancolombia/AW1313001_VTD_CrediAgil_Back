package com.bcol.vtd.api.preaprobadoca.facade;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

public interface SecurityCallService {

	Response mantenerSesion(HttpServletRequest requestContext,String idSesion,String authorization);
	
	Response cerrarSesion(HttpServletRequest requestContext, String idSesion, String authorization);

}
