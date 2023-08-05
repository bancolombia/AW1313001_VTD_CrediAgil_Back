package com.bcol.vtd.api.preaprobadoca.web.rest;

import com.bcol.vtd.api.preaprobadoca.service.DocumentoService;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.List;


@Path("servicio")
public class ServicioPreAprobadoCrediAgil{

	private static final String enconding = "UTF-8";

	@Inject
	private Instance<Pagina0CFPCADelegado> pagina0c;
	@Inject
	private Instance<Pagina1FPCADelegado> pagina1;
	@Inject
	private Instance<Pagina2FPCADelegado> pagina2;
	@Inject
	private Instance<Pagina3AFPCADelegado> pagina3a;
	@Inject
	private Instance<Pagina3BFPCADelegado> pagina3B;
	@Inject
	private Instance<ProcesosPreaprobadoCADelegado> procesosPreaprobadoCADelegado;
	@Inject
	private Instance<DocumentoService> documentoService;


	/**
	 * Método encargado de la calificación de la experiencia
	 * @param requestContext
	 * @param idSesion
	 * @param parametros
	 * @author Gustavo Chavarro Ortiz
	 * @return
	 */
	@POST
	@Path("inquiry")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON + ";charset=" + enconding)
	public Response calificarEncuesta(@Context HttpServletRequest requestContext,
					@CookieParam(ConstantesCA.SESSIONID_COOKIE_KEY) String idSesion, List<Parametro> parametros) {
		
		return procesosPreaprobadoCADelegado.get().encuesta(requestContext, idSesion, parametros);
	}


	@POST
	@Path("obtenerDocumentos")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON + ";charset=" + enconding)
	public Response obtenerDocumentos(@Context HttpServletRequest requestContext,
					@CookieParam(ConstantesCA.JWT_COOKIE_KEY) String authorization,
					@CookieParam(ConstantesCA.SESSIONID_COOKIE_KEY) String idSesion, String json) {

		if (UtilApi.validateJsonRequest(json)) {

			try {
				ObjectMapper mapper = new ObjectMapper();
				VentaDigitalCrediAgil ventaDigitalCA = mapper.readValue(json, VentaDigitalCrediAgil.class);
				
				return documentoService.get()
						.obtenerCartaBienvenida(requestContext, ventaDigitalCA, idSesion, authorization);
				
			} catch (Exception e) {

				RespuestaServicio respuestaServicio = new RespuestaServicio();
				respuestaServicio.setDescripcion(Status.INTERNAL_SERVER_ERROR.name());
				
				return Response.status(Status.INTERNAL_SERVER_ERROR).entity(respuestaServicio).build();
			}
			
		} else {
			RespuestaServicio respuestaServicio = new RespuestaServicio();
			respuestaServicio.setDescripcion(Status.BAD_REQUEST.name());
			
			return Response.status(Status.BAD_REQUEST).entity(respuestaServicio).build();
		}
	}

	@GET
	@Path("status")
	public Response status() { return Response.status(Response.Status.OK).build(); }
}
